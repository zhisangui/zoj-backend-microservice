package com.zhisangui.zojbackendjudgeservice.judge.codesandbox.impl;


import com.zhisangui.zojbackendjudgeservice.judge.codesandbox.CodeSandBox;
import zojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import zojbackendmodel.model.codesandbox.ExecuteCodeResponse;
import zojbackendmodel.model.codesandbox.JudgeInfo;
import zojbackendmodel.model.enums.JudgeResEnum;

public class ExampleCodeSandBox implements CodeSandBox {

    @Override
    public ExecuteCodeResponse doJudge(ExecuteCodeRequest executeCodeRequest) {
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeResEnum.ACCEPTED.getValue());
        judgeInfo.setMemory(500L);
        judgeInfo.setTime(500L);
        ExecuteCodeResponse executeCodeResponse = ExecuteCodeResponse.builder()
        		.judgeInfo(judgeInfo)
        		.message("normal")
        		.outputs(executeCodeRequest.getInputs())
        		.build();
        System.out.println("example code sandbox");
        return executeCodeResponse;
    }
}
