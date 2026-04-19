package com.zhisangui.zojbackendquestionservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 智谱 AI 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.zhipu")
public class AiConfig {

    /**
     * 智谱 API Key
     */
    private String apiKey;

    /**
     * 主模型名称（例如 glm-5.1 / glm-4.5-air）
     */
    private String model = "glm-4.7";

    /**
     * 备用模型名称，主模型返回空内容时自动降级重试
     */
    private String fallbackModel = "glm-4.5-air";
}
