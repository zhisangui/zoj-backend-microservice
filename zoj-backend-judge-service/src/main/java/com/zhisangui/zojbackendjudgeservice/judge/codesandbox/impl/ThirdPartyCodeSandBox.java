package com.zhisangui.zojbackendjudgeservice.judge.codesandbox.impl;


import com.zhisangui.zojbackendjudgeservice.judge.codesandbox.CodeSandBox;
import zojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import zojbackendmodel.model.codesandbox.ExecuteCodeResponse;

public class ThirdPartyCodeSandBox implements CodeSandBox {

    @Override
    public ExecuteCodeResponse doJudge(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("third party code sandbox");
        return null;
    }
}
