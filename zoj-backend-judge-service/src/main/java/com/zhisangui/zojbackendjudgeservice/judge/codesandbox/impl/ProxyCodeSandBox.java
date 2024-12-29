package com.zhisangui.zojbackendjudgeservice.judge.codesandbox.impl;

import cn.hutool.core.date.DateTime;
import com.zhisangui.zojbackendjudgeservice.judge.codesandbox.CodeSandBox;
import lombok.extern.slf4j.Slf4j;
import zojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import zojbackendmodel.model.codesandbox.ExecuteCodeResponse;


@Slf4j
public class ProxyCodeSandBox implements CodeSandBox {

    private final CodeSandBox codeSandBox;

    public ProxyCodeSandBox(CodeSandBox codeSandBox) {
        this.codeSandBox = codeSandBox;
    }

    @Override
    public ExecuteCodeResponse doJudge(ExecuteCodeRequest executeCodeRequest) {
        log.info("沙箱开始判题" + DateTime.now());
        ExecuteCodeResponse executeCodeResponse = codeSandBox.doJudge(executeCodeRequest);
        log.info("沙箱判题结束" + DateTime.now());
        return executeCodeResponse;
    }
}
