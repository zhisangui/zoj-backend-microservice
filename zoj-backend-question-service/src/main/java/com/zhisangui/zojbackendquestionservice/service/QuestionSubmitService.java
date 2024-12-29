package com.zhisangui.zojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import zojbackendmodel.model.dto.questionsubmit.QuestionSubmitAddRequest;
import zojbackendmodel.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import zojbackendmodel.model.entity.QuestionSubmit;
import zojbackendmodel.model.entity.User;
import zojbackendmodel.model.vo.QuestionSubmitVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author zsg
* @description 针对表【question_submit(题目提交)】的数据库操作Service
* @createDate 2024-09-19 16:53:48
*/
public interface QuestionSubmitService extends IService<QuestionSubmit> {

    boolean doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser);

    QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest);

    Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionPage, HttpServletRequest request);
}
