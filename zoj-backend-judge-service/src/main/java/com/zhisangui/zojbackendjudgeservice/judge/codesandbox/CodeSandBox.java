package com.zhisangui.zojbackendjudgeservice.judge.codesandbox;


import zojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import zojbackendmodel.model.codesandbox.ExecuteCodeResponse;

public interface CodeSandBox {
    ExecuteCodeResponse doJudge(ExecuteCodeRequest executeCodeRequest);
}
