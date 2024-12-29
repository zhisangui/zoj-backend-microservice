package com.zhisangui.zojbackendjudgeservice.judge;


import zojbackendmodel.model.entity.QuestionSubmit;

/**
 * 判题服务
 */
public interface JudgeService {
    /**
     * 判题模块进行判题
     *
     * @param questionSubmitId 题目提交id
     * @return
     */
    QuestionSubmit doJudge(Long questionSubmitId);
}