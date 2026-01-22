package com.learn.springailearn.service;

import java.util.Map;

public interface QwenTextGenerateService {
    String generateText(String userPrompt);

    String generateTextWithSystemPrompt(String systemPrompt, String userPrompt, Map<String, Object> params);
}