package com.zhisangui.zojbackendjudgeservice.judge.codesandbox.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.zhisangui.zojbackendjudgeservice.judge.codesandbox.CodeSandBox;
import org.springframework.beans.factory.annotation.Value;
import zojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import zojbackendmodel.model.codesandbox.ExecuteCodeResponse;

/**
 * 将代码发送给代码沙箱进行判题，并获取结果
 */

/**
 * @author zsg
 */
public class RemoteCodeSandBox implements CodeSandBox {
    @Override
    public ExecuteCodeResponse doJudge(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("remote code sandbox");
        String jsonStr = JSONUtil.toJsonStr(executeCodeRequest);
        // todo：修改为你的代码沙箱服务，以下是一个示例
        String body = HttpUtil.createPost("http://localhost:8081/executeCode").body(jsonStr).execute().body();
        System.out.println("jsonStr:" + jsonStr);
        System.out.println(body);
        return JSONUtil.toBean(body, ExecuteCodeResponse.class);
    }
}