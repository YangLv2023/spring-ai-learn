package com.learn.springailearn.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.learn.springailearn.domain.ChatRecord;
import com.learn.springailearn.mapper.ChatRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.util.List;

/**
 * 智能问答+对话记录管理服务
 * @author ken
 * @date 2026-01-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmartChatService {
    private final ChatClient chatClient;
    private final ChatRecordMapper chatRecordMapper;
    private final PlatformTransactionManager transactionManager;

    /**
     * 从配置文件读取当前使用的通义千问模型版本
     */
    @Value("${spring.ai.alibaba.qwen.model:qwen-turbo}")
    private String qwenModel;

    /**
     * 智能问答并保存对话记录
     * @param userId 用户ID
     * @param userPrompt 用户提问内容
     * @return AI生成的回答内容
     * @throws IllegalArgumentException 参数为空时抛出
     */
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 1.5)
    )
    public String chatAndSaveRecord(String userId, String userPrompt) {
        // 1. 参数校验（符合阿里巴巴开发手册：前置参数校验）
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (!StringUtils.hasText(userPrompt)) {
            throw new IllegalArgumentException("用户提问内容不能为空");
        }
        log.debug("开始处理智能问答请求，用户ID：{}，提问内容：{}", userId, userPrompt);

        // 2. 编程式事务定义（隔离级别：读提交，传播行为：REQUIRED）
        DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
        txDefinition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        txDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);

        try {
            // 3. 调用通义千问获取回答
            String aiResponse = chatClient.prompt()
                    .user(userPrompt)
                    .call()
                    .content()
                    .toString();
            if (!StringUtils.hasText(aiResponse)) {
                throw new IllegalArgumentException("AI生成的回答内容不能为空");
            }

            // 4. 构建对话记录实体
            ChatRecord chatRecord = new ChatRecord();
            chatRecord.setUserId(userId);
            chatRecord.setUserPrompt(userPrompt);
            chatRecord.setAiResponse(aiResponse);
            chatRecord.setModel(qwenModel);

            // 5. 保存对话记录
            int insertCount = chatRecordMapper.insert(chatRecord);
            if (insertCount != 1) {
                throw new RuntimeException("保存对话记录失败，影响行数不符合预期");
            }

            // 6. 提交事务
            transactionManager.commit(txStatus);
            log.debug("智能问答并保存记录成功，用户ID：{}，记录ID：{}", userId, chatRecord.getId());
            return aiResponse;
        } catch (Exception e) {
            // 7. 回滚事务
            transactionManager.rollback(txStatus);
            log.error("智能问答并保存记录失败，用户ID：{}，异常信息：{}", userId, e.getMessage(), e);
            throw new RuntimeException("智能问答处理失败：" + e.getMessage(), e);
        }
    }

    /**
     * 智能问答并保存对话记录 - 重试回调方法
     * 当重试达到最大次数后仍失败时执行此方法
     */
    public String recoverChatAndSaveRecord(String userId, String userPrompt, Exception ex) {
        log.error("智能问答重试3次后仍然失败，用户ID：{}，提问内容：{}，最终异常：{}", userId, userPrompt, ex.getMessage(), ex);
        throw new RuntimeException("智能问答处理失败：" + ex.getMessage(), ex);
    }

    /**
     * 根据用户ID查询对话记录
     * @param userId 用户ID
     * @return 该用户的所有有效对话记录
     * @throws IllegalArgumentException 用户ID为空时抛出
     */
    public List<ChatRecord> queryChatRecordsByUserId(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        log.debug("开始查询用户对话记录，用户ID：{}", userId);

        // 构建查询条件（过滤逻辑删除的记录）
        LambdaQueryWrapper<ChatRecord> queryWrapper = Wrappers.lambdaQuery(ChatRecord.class)
                .eq(ChatRecord::getUserId, userId)
                .eq(ChatRecord::getIsDeleted, 0)
                .orderByDesc(ChatRecord::getCreateTime);

        List<ChatRecord> chatRecords = chatRecordMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(chatRecords)) {
            log.debug("用户暂无对话记录，用户ID：{}", userId);
            return Lists.newArrayList();
        }

        log.debug("查询用户对话记录成功，用户ID：{}，记录数量：{}", userId, chatRecords.size());
        return chatRecords;
    }

    /**
     * 根据记录ID删除对话记录（逻辑删除）
     * @param recordId 记录ID
     * @return 是否删除成功
     * @throws IllegalArgumentException 记录ID为空/小于等于0时抛出
     */
    public boolean deleteChatRecordById(Long recordId) {
        if (recordId == null || recordId <= 0) {
            throw new IllegalArgumentException("记录ID不能为空且必须大于0");
        }
        log.debug("开始删除对话记录，记录ID：{}", recordId);

        // 编程式事务保证删除操作的原子性
        DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
        TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);

        try {
            int deleteCount = chatRecordMapper.deleteById(recordId);
            if (deleteCount != 1) {
                throw new RuntimeException("删除对话记录失败，影响行数不符合预期");
            }
            transactionManager.commit(txStatus);
            log.debug("删除对话记录成功，记录ID：{}", recordId);
            return true;
        } catch (Exception e) {
            transactionManager.rollback(txStatus);
            log.error("删除对话记录失败，记录ID：{}，异常信息：{}", recordId, e.getMessage(), e);
            throw new RuntimeException("删除对话记录失败：" + e.getMessage(), e);
        }
    }
}