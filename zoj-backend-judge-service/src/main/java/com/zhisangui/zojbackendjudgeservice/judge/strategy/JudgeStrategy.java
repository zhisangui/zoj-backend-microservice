package com.zhisangui.zojbackendjudgeservice.judge.strategy;


import zojbackendmodel.model.codesandbox.JudgeInfo;

/**
 * 抽象策略接口
 */
public interface JudgeStrategy {
    JudgeInfo doJudge(JudgeContext judgeContext);
}
