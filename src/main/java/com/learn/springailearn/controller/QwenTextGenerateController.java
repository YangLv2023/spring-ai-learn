package com.learn.springailearn.controller;

import com.learn.springailearn.service.QwenTextGenerateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/qwen/text")
@RequiredArgsConstructor
@Tag(name = "通义千问文本生成接口", description = "基于Spring AI Alibaba的文本生成接口")
public class QwenTextGenerateController {

    @Autowired
    private final QwenTextGenerateService qwenTextGenerateService;


    /**
     * 基础文本生成接口
     * @param request 请求参数，包含userPrompt字段
     * @return 生成的文本内容
     */
    @PostMapping("/generate")
    @Operation(summary = "基础文本生成", description = "传入用户提示词，返回通义千问生成的文本")
    public ResponseEntity<String> generateText(@RequestBody Map<String, String> request) {
        String userPrompt = request.get("userPrompt");
        try {
            String result = qwenTextGenerateService.generateText(userPrompt);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.error("文本生成失败：{}", e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("文本生成异常", e);
            return new ResponseEntity<>("服务器内部错误", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 带系统指令的文本生成接口
     * @param request 请求参数，包含systemPrompt、userPrompt、params字段
     * @return 生成的文本内容
     */
    @PostMapping("/generate-with-system")
    @Operation(summary = "带系统指令的文本生成", description = "传入系统指令、用户提示词和动态参数，返回标准化的生成文本")
    public ResponseEntity<String> generateTextWithSystemPrompt(@RequestBody Map<String, Object> request) {
        String systemPrompt = (String) request.get("systemPrompt");
        String userPrompt = (String) request.get("userPrompt");
        Map<String, Object> params = (Map<String, Object>) request.get("params");
        try {
            String result = qwenTextGenerateService.generateTextWithSystemPrompt(systemPrompt, userPrompt, params);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.error("带系统指令的文本生成失败：{}", e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("带系统指令的文本生成异常", e);
            return new ResponseEntity<>("服务器内部错误", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
