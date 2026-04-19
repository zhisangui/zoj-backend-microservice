package com.zhisangui.zojbackendquestionservice.demo;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.ChatCompletionCreateParams;
import ai.z.openapi.service.model.ChatCompletionResponse;
import ai.z.openapi.service.model.ChatMessage;
import ai.z.openapi.service.model.ChatMessageRole;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 智谱 AI 辅助流程独立调试 Demo
 *
 * 运行方式（PowerShell 示例）：
 * $env:ZHIPU_API_KEY="你的key"; mvn -pl zoj-backend-question-service -DskipTests exec:java -Dexec.mainClass="com.zhisangui.zojbackendquestionservice.demo.ZhipuAiAssistDemo"
 */
public class ZhipuAiAssistDemo {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String SYSTEM_PROMPT = "你是在线判题平台的算法教练。"
            + "你的任务是帮助用户理解题目并给出解题方向，但绝对不能直接给出完整答案代码。"
            + "请严格遵守："
            + "1) 不输出可直接提交通过的完整代码；"
            + "2) 输出必须精简：最多 6 条要点，总字数控制在 180-260 字；"
            + "3) 仅输出 3 个 Markdown 小节：### 思路、### 复杂度、### 易错点；"
            + "4) 语言为中文，句子简短，避免长段落；"
            + "5) 如果信息不足，明确指出缺失信息；"
            + "6) 若超过 260 字请主动压缩。";

    public static void main(String[] args) {
        String apiKey = StringUtils.firstNonBlank(System.getenv("ZHIPU_API_KEY"), System.getProperty("zhipu.apiKey"));
        String model = StringUtils.firstNonBlank(System.getenv("ZHIPU_MODEL"), "glm-4.7");
        String fallbackModel = StringUtils.firstNonBlank(System.getenv("ZHIPU_FALLBACK_MODEL"), "glm-4.5-air");

        if (StringUtils.isBlank(apiKey)) {
            System.err.println("缺少 API Key，请设置环境变量 ZHIPU_API_KEY 或 JVM 参数 -Dzhipu.apiKey=xxx");
            return;
        }

        String title = "两数之和";
        String content = "给定一个整数数组 nums 和一个整数目标值 target，请你在该数组中找出和为目标值 target 的那两个整数，并返回它们的数组下标。你可以假设每种输入只会对应一个答案。";
        String userPrompt = buildUserPrompt(title, content);

        ZhipuAiClient client = ZhipuAiClient.builder().ofZHIPU().apiKey(apiKey).build();
        List<ChatMessage> messages = Arrays.asList(
                ChatMessage.builder().role(ChatMessageRole.SYSTEM.value()).content(SYSTEM_PROMPT).build(),
                ChatMessage.builder().role(ChatMessageRole.USER.value()).content(userPrompt).build()
        );

        String result = callAndPrint(client, messages, model, false);
        if (StringUtils.isBlank(result) && !StringUtils.equals(model, fallbackModel)) {
            System.out.println("主模型返回空内容，开始尝试备用模型: " + fallbackModel);
            result = callAndPrint(client, messages, fallbackModel, true);
        }

        if (StringUtils.isBlank(result)) {
            System.out.println("最终结果为空：请检查模型可用性、账号额度或策略拦截。日志里已打印原始响应摘要。");
        } else {
            System.out.println("\n===== 最终解析内容 =====");
            System.out.println(result);
        }
    }

    private static String callAndPrint(ZhipuAiClient client, List<ChatMessage> messages, String model, boolean fallbackAttempt) {
        ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                .model(model)
                .messages(messages)
                .stream(false)
                .temperature(0.4f)
                .maxTokens(1024)
                .build();

        ChatCompletionResponse response = client.chat().createChatCompletion(request);
        String content = extractContent(response);

        System.out.println("\n===== 调用结果 =====");
        System.out.println("model=" + model + ", fallbackAttempt=" + fallbackAttempt);
        System.out.println("summary=" + buildResponseSummary(response));
        System.out.println("rawResponse=" + safeJson(response));

        return StringUtils.trimToNull(content);
    }

    private static String extractContent(ChatCompletionResponse response) {
        if (response == null || response.getData() == null
                || response.getData().getChoices() == null
                || response.getData().getChoices().isEmpty()
                || response.getData().getChoices().get(0) == null
                || response.getData().getChoices().get(0).getMessage() == null) {
            return null;
        }
        JsonNode messageNode = OBJECT_MAPPER.valueToTree(response.getData().getChoices().get(0).getMessage());
        String contentText = extractTextFromContentNode(messageNode.path("content"));
        if (StringUtils.isNotBlank(contentText)) {
            return normalizeAiText(contentText);
        }

        String reasoningText = extractTextFromContentNode(messageNode.path("reasoning_content"));
        if (StringUtils.isNotBlank(reasoningText)) {
            String safeResult = extractFinalAnswerFromReasoning(reasoningText);
            if (StringUtils.isNotBlank(safeResult)) {
                return safeResult;
            }
        }

        Object content = response.getData().getChoices().get(0).getMessage().getContent();
        if (content == null) {
            return null;
        }
        if (content instanceof String) {
            return ((String) content).trim();
        }

        try {
            JsonNode contentNode = OBJECT_MAPPER.valueToTree(content);
            String parsed = extractTextFromContentNode(contentNode);
            if (StringUtils.isNotBlank(parsed)) {
                return parsed.trim();
            }
            if (contentNode.isValueNode()) {
                return contentNode.asText().trim();
            }
            String fallback = content.toString();
            return StringUtils.isBlank(fallback) ? null : fallback.trim();
        } catch (Exception e) {
            String fallback = content.toString();
            return StringUtils.isBlank(fallback) ? null : fallback.trim();
        }
    }

    private static String extractTextFromContentNode(JsonNode contentNode) {
        if (contentNode == null || contentNode.isNull()) {
            return null;
        }
        if (contentNode.isTextual()) {
            return contentNode.asText();
        }
        if (contentNode.isArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode item : contentNode) {
                String part = extractTextFromContentNode(item);
                if (StringUtils.isNotBlank(part)) {
                    sb.append(part);
                }
            }
            return sb.toString();
        }
        if (contentNode.isObject()) {
            if (contentNode.path("text").isTextual()) {
                return contentNode.path("text").asText();
            }
            if (contentNode.path("content").isTextual()) {
                return contentNode.path("content").asText();
            }
            if (contentNode.path("content").isArray()) {
                return extractTextFromContentNode(contentNode.path("content"));
            }
        }
        return null;
    }

    private static String buildResponseSummary(ChatCompletionResponse response) {
        if (response == null) {
            return "response=null";
        }
        int choiceSize = response.getData() == null || response.getData().getChoices() == null
                ? -1 : response.getData().getChoices().size();
        Object content = null;
        if (choiceSize > 0
                && response.getData().getChoices().get(0) != null
                && response.getData().getChoices().get(0).getMessage() != null) {
            content = response.getData().getChoices().get(0).getMessage().getContent();
        }
        return "choices.size=" + choiceSize
                + ", content.class=" + (content == null ? "null" : content.getClass().getName())
                + ", content.preview=" + preview(content);
    }

    private static String normalizeAiText(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        String normalized = text.replaceAll("\\r\\n", "\n").replaceAll("\n{3,}", "\n\n").trim();
        return StringUtils.isBlank(normalized) ? null : normalized;
    }

    private static final Pattern FINAL_ANSWER_PATTERN = Pattern.compile(
            "(?s)(^|\\n)### 思路\\s*\\n.*?\\n### 复杂度\\s*\\n.*?\\n### 易错点\\s*\\n.*?(?=\\n### |$)");

    private static String extractFinalAnswerFromReasoning(String reasoningText) {
        String normalized = normalizeAiText(reasoningText);
        if (StringUtils.isBlank(normalized)) {
            return null;
        }

        Matcher matcher = FINAL_ANSWER_PATTERN.matcher(normalized);
        if (!matcher.find()) {
            return null;
        }

        String result = normalizeAiText(matcher.group().trim());
        if (StringUtils.isBlank(result)) {
            return null;
        }
        if (StringUtils.countMatches(result, "### 思路") != 1
                || StringUtils.countMatches(result, "### 复杂度") != 1
                || StringUtils.countMatches(result, "### 易错点") != 1) {
            return null;
        }
        return result;
    }

    private static String preview(Object content) {
        if (content == null) {
            return "null";
        }
        String text = String.valueOf(content);
        if (text.length() <= 200) {
            return text;
        }
        return text.substring(0, 200) + "...(truncated)";
    }

    private static String safeJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }

    private static String buildUserPrompt(String title, String content) {
        String normalizedContent = content.length() > 2500 ? content.substring(0, 2500) : content;
        return "请基于以下题目给出精简的引导式分析，不要直接给完整答案代码。\n\n"
                + "【题目标题】\n" + title + "\n\n"
                + "【题目描述】\n" + normalizedContent + "\n\n"
                + "输出要求：总字数 180-260 字，最多 6 条要点。\n"
                + "请仅按以下结构输出：\n"
                + "### 思路\n"
                + "### 复杂度\n"
                + "### 易错点\n";
    }
}
