package com.zhisangui.zojbackendjudgeservice.judge.strategy;

import lombok.Data;
import zojbackendmodel.model.codesandbox.ExecuteCodeResponse;
import zojbackendmodel.model.entity.Question;
import zojbackendmodel.model.entity.QuestionSubmit;

/**
 * 使用策略模式进行判题需要的上下文信息
 */
@Data
public class JudgeContext {
    private Question question;

    private QuestionSubmit questionSubmit;

    private ExecuteCodeResponse executeCodeResponse;
}
