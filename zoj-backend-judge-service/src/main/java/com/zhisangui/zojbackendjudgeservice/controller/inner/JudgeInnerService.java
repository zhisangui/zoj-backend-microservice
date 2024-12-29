package com.zhisangui.zojbackendjudgeservice.controller.inner;

import com.zhisangui.zojbackendjudgeservice.judge.JudgeService;
import com.zhisangui.zojbackendserviceclient.service.JudgeFeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zojbackendmodel.model.entity.QuestionSubmit;

import javax.annotation.Resource;
/**
 * 判题接口
 *
 * @author <a href="https://github.com/zhisangui">zsg</a>
 */
/**
 * @author zsg
 */
@RestController
@RequestMapping("/inner")
public class JudgeInnerService implements JudgeFeignClient {
    @Resource
    private JudgeService judgeService;

    /**
     * 判题
     * @param questionSubmitId 题目提交id
     * @return
     */
    @Override
    @PostMapping("/do")
    public QuestionSubmit doJudge(Long questionSubmitId) {
        return judgeService.doJudge(questionSubmitId);
    }
}
