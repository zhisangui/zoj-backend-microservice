package com.zhisangui.zojbackendquestionservice.service.Impl;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.ChatCompletionCreateParams;
import ai.z.openapi.service.model.ChatCompletionResponse;
import ai.z.openapi.service.model.ChatMessage;
import ai.z.openapi.service.model.ChatMessageRole;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhisangui.zojbackendcommon.common.ErrorCode;
import com.zhisangui.zojbackendcommon.exception.BusinessException;
import com.zhisangui.zojbackendquestionservice.config.AiConfig;
import com.zhisangui.zojbackendquestionservice.service.AiAssistService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import zojbackendmodel.model.entity.Question;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 辅助服务实现
 */
@Service
@Slf4j
public class AiAssistServiceImpl implements AiAssistService {

    @Resource
    private AiConfig aiConfig;

    private static final String SYSTEM_PROMPT = "你是在线判题平台的算法教练。" +
            "你的任务是帮助用户理解题目并给出解题方向，但绝对不能直接给出完整答案代码。" +
            "请严格遵守：" +
            "1) 不输出可直接提交通过的完整代码；" +
            "2) 输出必须精简：最多 6 条要点，总字数控制在 180-260 字；" +
            "3) 仅输出 3 个 Markdown 小节，不要输出多余内容：### 思路、### 复杂度、### 易错点；" +
            "4) 语言为中文，句子简短，避免长段落；" +
            "5) 如果信息不足，明确指出缺失信息；" +
            "6) 若超过 260 字请主动压缩。";

    @Override
    public String generateAssistMarkdown(Question question) {
        if (question == null || question.getId() == null || question.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目信息无效");
        }
        if (StringUtils.isBlank(aiConfig.getApiKey())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 能力未配置：缺少 ai.zhipu.api-key");
        }

        String title = StringUtils.defaultString(question.getTitle(), "未命名题目");
        String content = StringUtils.defaultIfBlank(question.getContent(), "暂无题面描述");
        String userPrompt = buildUserPrompt(title, content);

        try {
            ZhipuAiClient client = ZhipuAiClient.builder()
                    .ofZHIPU()
                    .apiKey(aiConfig.getApiKey())
                    .build();

            List<ChatMessage> messages = Arrays.asList(
                    ChatMessage.builder()
                            .role(ChatMessageRole.SYSTEM.value())
                            .content(SYSTEM_PROMPT)
                            .build(),
                    ChatMessage.builder()
                            .role(ChatMessageRole.USER.value())
                            .content(userPrompt)
                            .build()
            );

            String primaryModel = StringUtils.defaultIfBlank(aiConfig.getModel(), "glm-4.7");
            String aiContent = doChatCompletion(client, messages, primaryModel, question.getId(), false);
            if (StringUtils.isNotBlank(aiContent)) {
                return aiContent.trim();
            }

            String fallbackModel = StringUtils.defaultIfBlank(aiConfig.getFallbackModel(), "glm-4.5-air");
            if (!StringUtils.equals(primaryModel, fallbackModel)) {
                log.warn("主模型返回空内容，开始降级重试, questionId={}, primaryModel={}, fallbackModel={}",
                        question.getId(), primaryModel, fallbackModel);
                aiContent = doChatCompletion(client, messages, fallbackModel, question.getId(), true);
                if (StringUtils.isNotBlank(aiContent)) {
                    return aiContent.trim();
                }
            }

            log.warn("主备模型均未提取到合规结果，返回安全兜底文案, questionId={}", question.getId());
            return buildSafeFallbackMarkdown(title);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用智谱 AI 失败, questionId={}", question.getId(), e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 分析失败，请稍后重试");
        }
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String doChatCompletion(ZhipuAiClient client, List<ChatMessage> messages, String model,
                                    Long questionId, boolean fallbackAttempt) {
        ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                .model(model)
                .messages(messages)
                .stream(false)
                .temperature(0.4f)
                .maxTokens(1024)
                .build();

        ChatCompletionResponse response = client.chat().createChatCompletion(request);
        String aiContent = extractContent(response);
        log.info("智谱 AI 返回结果, questionId={}, model={}, fallbackAttempt={}, response={}",
                questionId, model, fallbackAttempt, buildResponseSummary(response));
        if (StringUtils.isBlank(aiContent)) {
            log.warn("智谱 AI 返回内容为空, questionId={}, model={}, fallbackAttempt={}, summary={}",
                    questionId, model, fallbackAttempt, buildResponseSummary(response));
            return null;
        }
        return aiContent;
    }

    private String extractContent(ChatCompletionResponse response) {
        if (response == null || response.getData() == null
                || response.getData().getChoices() == null
                || response.getData().getChoices().isEmpty()
                || response.getData().getChoices().get(0) == null
                || response.getData().getChoices().get(0).getMessage() == null) {
            return null;
        }

        JsonNode messageNode = OBJECT_MAPPER.valueToTree(response.getData().getChoices().get(0).getMessage());

        String contentText = extractTextFromContentNode(messageNode.path("content"));
        String normalizedContent = normalizeAiText(contentText);
        if (StringUtils.isNotBlank(normalizedContent)) {
            String safeContent = sanitizeAiAnswer(normalizedContent);
            if (StringUtils.isNotBlank(safeContent)) {
                return safeContent;
            }
            if (!containsLeakageMarkers(normalizedContent)) {
                return normalizedContent;
            }
        }

        String reasoningText = extractTextFromContentNode(messageNode.path("reasoning_content"));
        String safeReasoning = sanitizeAiAnswer(reasoningText);
        if (StringUtils.isNotBlank(safeReasoning)) {
            return safeReasoning;
        }

        Object content = response.getData().getChoices().get(0).getMessage().getContent();
        if (content == null) {
            return null;
        }
        if (content instanceof String) {
            String normalized = normalizeAiText((String) content);
            if (StringUtils.isNotBlank(normalized) && !containsLeakageMarkers(normalized)) {
                return normalized;
            }
            return null;
        }

        try {
            JsonNode contentNode = OBJECT_MAPPER.valueToTree(content);
            String parsed = extractTextFromContentNode(contentNode);
            String normalized = normalizeAiText(parsed);
            if (StringUtils.isNotBlank(normalized) && !containsLeakageMarkers(normalized)) {
                return normalized;
            }
            if (contentNode.isValueNode()) {
                String valueText = normalizeAiText(contentNode.asText());
                if (StringUtils.isNotBlank(valueText) && !containsLeakageMarkers(valueText)) {
                    return valueText;
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("解析智谱 AI content 失败, contentType={}", content.getClass().getName(), e);
            String fallback = normalizeAiText(content.toString());
            return StringUtils.isNotBlank(fallback) && !containsLeakageMarkers(fallback) ? fallback : null;
        }
    }

    private String extractTextFromContentNode(JsonNode contentNode) {
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

    private String buildResponseSummary(ChatCompletionResponse response) {
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

    private String normalizeAiText(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        String normalized = text.replaceAll("\\r\\n", "\n").replaceAll("\n{3,}", "\n\n").trim();
        return StringUtils.isBlank(normalized) ? null : normalized;
    }

    private String sanitizeAiAnswer(String rawText) {
        String normalized = normalizeAiText(rawText);
        if (StringUtils.isBlank(normalized)) {
            return null;
        }

        String structured = extractFinalAnswerFromReasoning(normalized);
        if (StringUtils.isBlank(structured)) {
            structured = rebuildMarkdownFromLooseText(normalized);
        }
        if (StringUtils.isBlank(structured)) {
            return null;
        }
        if (containsLeakageMarkers(structured)) {
            return null;
        }
        return structured;
    }

    private static final Pattern FINAL_ANSWER_PATTERN = Pattern.compile(
            "(?s)(^|\\n)### 思路\\s*\\n.*?\\n### 复杂度\\s*\\n.*?\\n### 易错点\\s*\\n.*?(?=\\n### |$)");

    private String extractFinalAnswerFromReasoning(String reasoningText) {
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

    private boolean containsLeakageMarkers(String text) {
        return StringUtils.containsAny(text,
                "分析请求", "起草内容", "草稿", "字数检查", "SYSTEM_PROMPT");
    }

    private String rebuildMarkdownFromLooseText(String text) {
        String normalized = normalizeAiText(text);
        if (StringUtils.isBlank(normalized)) {
            return null;
        }

        int ideaIdx = StringUtils.indexOfAny(normalized, "### 思路", "思路");
        int complexityIdx = StringUtils.indexOfAny(normalized, "### 复杂度", "复杂度");
        int pitfallIdx = StringUtils.indexOfAny(normalized, "### 易错点", "易错点");
        if (ideaIdx < 0 || complexityIdx < 0 || pitfallIdx < 0) {
            return null;
        }

        int[] sorted = new int[]{ideaIdx, complexityIdx, pitfallIdx};
        Arrays.sort(sorted);
        int start = sorted[0];
        if (start < 0 || start >= normalized.length()) {
            return null;
        }

        String fragment = normalized.substring(start);
        fragment = fragment.replaceFirst("(?s)^.*?(### 思路|思路)", "### 思路");
        fragment = fragment.replaceFirst("(?s)(### 思路[\\s\\S]*?)(### 复杂度|复杂度)", "$1\n### 复杂度");
        fragment = fragment.replaceFirst("(?s)(### 复杂度[\\s\\S]*?)(### 易错点|易错点)", "$1\n### 易错点");

        String normalizedFragment = normalizeAiText(fragment);
        if (StringUtils.isBlank(normalizedFragment)) {
            return null;
        }

        Matcher matcher = FINAL_ANSWER_PATTERN.matcher(normalizedFragment);
        if (!matcher.find()) {
            return null;
        }
        String result = normalizeAiText(matcher.group().trim());
        if (StringUtils.isBlank(result)) {
            return null;
        }
        return StringUtils.countMatches(result, "### 思路") == 1
                && StringUtils.countMatches(result, "### 复杂度") == 1
                && StringUtils.countMatches(result, "### 易错点") == 1
                ? result : null;
    }

    private String buildSafeFallbackMarkdown(String title) {
        String safeTitle = StringUtils.defaultIfBlank(title, "该题目");
        return "### 思路\n"
                + "可先从暴力解法入手，明确状态转移或遍历顺序，再逐步优化到更高效方案。"
                + safeTitle + "建议先手写样例推导，确认边界后再编码。\n"
                + "### 复杂度\n"
                + "先给出当前方案的时间与空间复杂度，再评估是否可用哈希、双指针或前缀信息降低复杂度。\n"
                + "### 易错点\n"
                + "重点检查空输入、下标越界、重复元素与结果唯一性条件；提交前用最小样例和极端样例各自验证一次。";
    }

    private String preview(Object content) {

        if (content == null) {
            return "null";
        }
        String text = String.valueOf(content);
        if (text.length() <= 200) {
            return text;
        }
        return text.substring(0, 200) + "...(truncated)";
    }

    private String buildUserPrompt(String title, String content) {
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
