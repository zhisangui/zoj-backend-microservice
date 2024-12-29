package com.zhisangui.zojbackendjudgeservice.judge.strategy.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.zhisangui.zojbackendjudgeservice.judge.strategy.JudgeContext;
import com.zhisangui.zojbackendjudgeservice.judge.strategy.JudgeStrategy;
import zojbackendmodel.model.codesandbox.ExecuteCodeResponse;
import zojbackendmodel.model.codesandbox.JudgeInfo;
import zojbackendmodel.model.dto.question.JudgeCase;
import zojbackendmodel.model.dto.question.JudgeConfig;
import zojbackendmodel.model.entity.Question;
import zojbackendmodel.model.enums.JudgeResEnum;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 默认的判题具体策略类
 */
public class DefaultJudgeStrategy implements JudgeStrategy {
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        // 1 先初始化返回信息为成功，然后逐一判断
        ExecuteCodeResponse executeCodeResponse = judgeContext.getExecuteCodeResponse();
        JudgeInfo judgeInfo = executeCodeResponse.getJudgeInfo();
        Long memory = judgeInfo.getMemory();
        Long time = judgeInfo.getTime();
        JudgeInfo judgeInfoResponse = new JudgeInfo();
        judgeInfoResponse.setTime(time);
        judgeInfoResponse.setMemory(memory);
        judgeInfoResponse.setMessage(JudgeResEnum.ACCEPTED.getValue());
        // 2 判断答案和沙箱的输出用例是否相同
        Question question = judgeContext.getQuestion();
        List<String> ansOutputs = JSONUtil.toList(question.getJudgeCase(), JudgeCase.class)
                .stream()
                .map(JudgeCase::getOutput)
                .collect(Collectors.toList());
        List<String> outputs = executeCodeResponse.getOutputs();
        if (!CollUtil.isEqualList(ansOutputs, outputs)) {
            judgeInfoResponse.setMessage(JudgeResEnum.WRONG_ANSWER.getValue());
        }
        // 3 判断题目的限制是否符合要求 ,不同策略的不同主要就在这
        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
        Long memoryLimit = judgeConfig.getMemoryLimit();
        Long timeLimit = judgeConfig.getTimeLimit();
        Long stackLimit = judgeConfig.getStackLimit();
        // 3.1 内存超限
        if (memoryLimit < memory) {
            judgeInfoResponse.setMessage(JudgeResEnum.MEMORY_LIMIT_EXCEEDED.getValue());
        }
        // 3.2 时间超限
        if (timeLimit < time) {
            judgeInfoResponse.setMessage(JudgeResEnum.TIME_LIMIT_EXCEEDED.getValue());
        }
        return judgeInfoResponse;
    }
}
