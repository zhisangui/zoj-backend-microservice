package com.zhisangui.zojbackendquestionservice.service;

import zojbackendmodel.model.entity.Question;

/**
 * AI 辅助服务
 */
public interface AiAssistService {

    /**
     * 基于题目生成引导式分析（不直接给答案）
     *
     * @param question 题目
     * @return markdown 分析文本
     */
    String generateAssistMarkdown(Question question);
}