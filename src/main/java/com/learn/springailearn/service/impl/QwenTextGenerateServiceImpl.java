package com.learn.springailearn.service.impl;

import com.alibaba.fastjson2.JSON;
import com.learn.springailearn.service.QwenTextGenerateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Map;

@Slf4j
@Service
public class QwenTextGenerateServiceImpl implements QwenTextGenerateService {


    private final ChatClient chatClient;

    public QwenTextGenerateServiceImpl(ChatClient chatClient) {
        this.chatClient = chatClient;
    }


    public String generateText(String userPrompt) {
        Assert.hasText(userPrompt, "prompt must not be empty");
        log.debug("user prompt : {}", userPrompt);
        // 使用ChatClient的正确方式
        String response = chatClient
                .prompt()
                .user(userPrompt)
                .call()
                .content();
        log.debug("result : {}", response);
        return response;
    }


    /**
     * 带系统指令的文本生成（标准化提示词）
     * @param systemPrompt 系统指令（定义AI的行为）
     * @param userPrompt 用户提示词
     * @param params 提示词中的动态参数
     * @return 生成的文本内容
     * @throws IllegalArgumentException 当系统指令或用户提示词为空时抛出
     */
    public String generateTextWithSystemPrompt(String systemPrompt, String userPrompt, Map<String, Object> params) {
        // 参数非空校验
        Assert.hasText(systemPrompt, "系统指令不能为空");
        Assert.hasText(userPrompt, "用户提示词不能为空");
        log.debug("开始调用通义千问生成文本，系统指令：{}，用户提示词：{}，参数：{}", systemPrompt, userPrompt, JSON.toJSONString(params));
        // 构建系统提示词模板
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemPrompt);
        // 渲染系统提示词（替换动态参数）
        UserMessage userMessage = new UserMessage(userPrompt);
        Prompt prompt = new Prompt(systemPromptTemplate.createMessage(params), userMessage);
        // 调用ChatClient
        String response = chatClient
                .prompt(prompt)
                .call()
                .content();
        log.debug("带系统指令的文本生成完成，结果：{}", response);
        return response;
    }
}