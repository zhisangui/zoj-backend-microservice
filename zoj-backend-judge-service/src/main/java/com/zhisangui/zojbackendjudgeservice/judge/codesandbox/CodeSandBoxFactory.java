package com.zhisangui.zojbackendjudgeservice.judge.codesandbox;

import com.zhisangui.zojbackendjudgeservice.judge.codesandbox.impl.ExampleCodeSandBox;
import com.zhisangui.zojbackendjudgeservice.judge.codesandbox.impl.RemoteCodeSandBox;
import com.zhisangui.zojbackendjudgeservice.judge.codesandbox.impl.ThirdPartyCodeSandBox;
import lombok.Data;

@Data
public class CodeSandBoxFactory {

    public static CodeSandBox getCodeSandBox(String type) {
        CodeSandBox codeSandBox = new ExampleCodeSandBox();
        if ("remote".equals(type)) {
            codeSandBox = new RemoteCodeSandBox();
        }
        if ("thirdParty".equals(type)) {
            codeSandBox = new ThirdPartyCodeSandBox();
        }
        return codeSandBox;
    }
}