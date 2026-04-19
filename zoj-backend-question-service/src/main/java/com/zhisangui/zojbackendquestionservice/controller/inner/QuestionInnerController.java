package com.zhisangui.zojbackendquestionservice.controller.inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhisangui.zojbackendquestionservice.service.QuestionService;
import com.zhisangui.zojbackendquestionservice.service.QuestionSubmitService;
import com.zhisangui.zojbackendserviceclient.service.QuestionFeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zojbackendmodel.model.entity.Question;
import zojbackendmodel.model.entity.QuestionSubmit;
import zojbackendmodel.model.enums.JudgeStatusEnum;

import javax.annotation.Resource;

@RestController
@RequestMapping("/inner")
public class QuestionInnerController implements QuestionFeignClient {
    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Override
    @GetMapping("get/id")
    public Question getQuestionById(Long questionId) {
        return questionService.getById(questionId);
    }

    @Override
    @GetMapping("question_submit/get/id")
    public QuestionSubmit getQuestionSubmitById(Long questionSubmitId) {
        return questionSubmitService.getById(questionSubmitId);
    }

    @Override
    @PostMapping("/question_submit/update")
    public boolean updateQuestionSubmitById(QuestionSubmit questionSubmit) {
        return questionSubmitService.updateById(questionSubmit);
    }

    @Override
    @PostMapping("/update")
    public boolean updateQuestionById(Question question) {
        return questionService.updateById(question);
    }

    @Override
    @GetMapping("/statistics/submissions/total")
    public Long countTotalSubmissionsByUserId(Long userId) {
        return questionSubmitService.count(buildUserSubmitWrapper(userId));
    }

    @Override
    @GetMapping("/statistics/submissions/accepted")
    public Long countAcceptedSubmissionsByUserId(Long userId) {
        QueryWrapper<QuestionSubmit> queryWrapper = buildUserSubmitWrapper(userId)
                .eq("status", JudgeStatusEnum.SUCCEED.getValue());
        return questionSubmitService.count(queryWrapper);
    }

    @Override
    @GetMapping("/statistics/questions/total")
    public Long countTotalQuestionsByUserId(Long userId) {
        QueryWrapper<QuestionSubmit> queryWrapper = buildUserSubmitWrapper(userId)
                .select("distinct questionId");
        return (long) questionSubmitService.listObjs(queryWrapper).size();
    }

    @Override
    @GetMapping("/statistics/questions/accepted")
    public Long countAcceptedQuestionsByUserId(Long userId) {
        QueryWrapper<QuestionSubmit> queryWrapper = buildUserSubmitWrapper(userId)
                .eq("status", JudgeStatusEnum.SUCCEED.getValue())
                .select("distinct questionId");
        return (long) questionSubmitService.listObjs(queryWrapper).size();
    }

    private QueryWrapper<QuestionSubmit> buildUserSubmitWrapper(Long userId) {
        return new QueryWrapper<QuestionSubmit>().eq("userId", userId);
    }
}
