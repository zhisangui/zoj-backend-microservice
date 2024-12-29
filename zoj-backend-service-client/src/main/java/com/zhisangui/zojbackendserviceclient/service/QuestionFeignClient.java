package com.zhisangui.zojbackendserviceclient.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import zojbackendmodel.model.entity.Question;
import zojbackendmodel.model.entity.QuestionSubmit;

@FeignClient(name = "zoj-backend-question-service", path = "/api/question/inner")
public interface QuestionFeignClient {
    /**
     * 根据id 获取题目
     * @param questionId
     * @return
     */
    @GetMapping("get/id")
    Question getQuestionById(@RequestParam("questionId") Long questionId);

    /**
     * 根据id 获取题目提交
     * @param questionSubmitId
     * @return
     */
    @GetMapping("question_submit/get/id")
    QuestionSubmit getQuestionSubmitById(@RequestParam("questionSubmitId") Long questionSubmitId);

    /**
     * 根据id 更新题目提交
     * @param questionSubmit
     * @return
     */
    @PostMapping("/question_submit/update")
    boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmit);

    /**
     * 根据id 更新题目
     */
    @PostMapping("/update")
    boolean updateQuestionById(@RequestBody Question question);

}
