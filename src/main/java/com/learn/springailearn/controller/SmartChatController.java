package com.learn.springailearn.controller;

import com.learn.springailearn.domain.ChatRecord;
import com.learn.springailearn.service.SmartChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 智能问答接口
 * @author ken
 * @date 2026-01-21
 */
@Slf4j
@RestController
@RequestMapping("/api/smart-chat")
@RequiredArgsConstructor
@Tag(name = "智能问答接口", description = "基于Spring AI Alibaba+MyBatisPlus的智能问答+记录管理接口")
public class SmartChatController {
    private final SmartChatService smartChatService;

    /**
     * 智能问答并保存记录
     * @param request 请求参数：userId（用户ID）、userPrompt（提问内容）
     * @return AI回答内容
     */
    @PostMapping("/chat")
    @Operation(summary = "智能问答", description = "提交用户提问，返回AI回答并保存对话记录")
    public ResponseEntity<String> chat(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String userPrompt = request.get("userPrompt");
        try {
            String result = smartChatService.chatAndSaveRecord(userId, userPrompt);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.error("智能问答参数错误：{}", e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("智能问答处理异常", e);
            return new ResponseEntity<>("智能问答处理失败：" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据用户ID查询对话记录
     * @param userId 用户ID
     * @return 对话记录列表
     */
    @GetMapping("/records/{userId}")
    @Operation(summary = "查询用户对话记录", description = "根据用户ID查询所有有效对话记录")
    public ResponseEntity<List<ChatRecord>> queryChatRecords(
            @Parameter(description = "用户ID", required = true)
            @PathVariable String userId) {
        try {
            List<ChatRecord> records = smartChatService.queryChatRecordsByUserId(userId);
            return new ResponseEntity<>(records, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.error("查询对话记录参数错误：{}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("查询对话记录异常", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除对话记录（逻辑删除）
     * @param recordId 记录ID
     * @return 删除结果
     */
    @DeleteMapping("/records/{recordId}")
    @Operation(summary = "删除对话记录", description = "根据记录ID逻辑删除对话记录")
    public ResponseEntity<Boolean> deleteChatRecord(
            @Parameter(description = "对话记录ID", required = true)
            @PathVariable Long recordId) {
        try {
            boolean result = smartChatService.deleteChatRecordById(recordId);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.error("删除对话记录参数错误：{}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("删除对话记录异常", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}