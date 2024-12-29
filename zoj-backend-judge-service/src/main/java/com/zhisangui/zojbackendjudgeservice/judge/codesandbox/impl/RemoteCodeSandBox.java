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
    @Value("${remote.codesandbox.host:localhost}")
    private String host;

    @Value("${remote.codesandbox.port:8081}")
    private int port;

    @Override
    public ExecuteCodeResponse doJudge(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("remote code sandbox");
        String jsonStr = JSONUtil.toJsonStr(executeCodeRequest);
        // ★ 注意上线的时候ip必须修改
        String body = HttpUtil.createPost("http://" + host + ":" + port + "/executeCode").body(jsonStr).execute().body();
        System.out.println("jsonStr:" + jsonStr);
        System.out.println(body);
        return JSONUtil.toBean(body, ExecuteCodeResponse.class);
    }
}