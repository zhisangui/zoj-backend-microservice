package com.zhisangui.zojbackendserviceclient.service;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import zojbackendmodel.model.entity.QuestionSubmit;

/**
 * 判题服务
 */
@FeignClient(name = "zoj-backend-judge-service", path = "/api/judge/inner")
public interface JudgeFeignClient {
    /**
     * 判题模块进行判题
     *
     * @param questionSubmitId 题目提交id
     * @return
     */
    @PostMapping("/do")
    QuestionSubmit doJudge(@RequestParam("questionSubmitId") Long questionSubmitId);
}