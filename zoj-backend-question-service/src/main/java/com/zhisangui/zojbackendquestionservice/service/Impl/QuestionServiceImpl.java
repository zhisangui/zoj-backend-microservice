package com.zhisangui.zojbackendquestionservice.service.Impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhisangui.zojbackendcommon.common.ErrorCode;
import com.zhisangui.zojbackendcommon.constant.CommonConstant;
import com.zhisangui.zojbackendcommon.exception.BusinessException;
import com.zhisangui.zojbackendcommon.exception.ThrowUtils;
import com.zhisangui.zojbackendcommon.utils.SqlUtils;
import com.zhisangui.zojbackendquestionservice.mapper.QuestionMapper;
import com.zhisangui.zojbackendquestionservice.service.QuestionService;
import com.zhisangui.zojbackendserviceclient.service.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import zojbackendmodel.model.dto.question.QuestionQueryRequest;
import zojbackendmodel.model.entity.Question;
import zojbackendmodel.model.entity.User;
import zojbackendmodel.model.vo.QuestionVO;
import zojbackendmodel.model.vo.UserVO;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题目服务实现
 *
 * @author <a href="https://github.com/zhisangui">zsg</a>
 *
 */
@Service
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Resource
    private UserFeignClient userFeignClient;


    @Override
    public void validQuestion(Question question, boolean add) {
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String title = question.getTitle();
        String content = question.getContent();
        String tags = question.getTags();
        String judgeConfig = question.getJudgeConfig();
        String judgeCase = question.getJudgeCase();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content, tags, judgeConfig, judgeCase), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
        if (StringUtils.isNotBlank(tags) && tags.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签过长");
        }
        if (StringUtils.isNotBlank(judgeConfig) && judgeConfig.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "判题配置过长");
        }
    }

    /**
     * 获取查询包装类
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();
        Long id = questionQueryRequest.getId();
        String title = questionQueryRequest.getTitle();
        List<String> tagList = questionQueryRequest.getTags();
        Long userId = questionQueryRequest.getUserId();

        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
        QuestionVO questionVO = QuestionVO.objToVo(question);
        // 关联查询用户信息
        Long userId = question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userFeignClient.getUserById(userId);
        }
        UserVO userVO = userFeignClient.getUserVO(user);
        questionVO.setUser(userVO);
        return questionVO;
    }

    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        List<Question> questionList = questionPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        if (CollUtil.isEmpty(questionList)) {
            return questionVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userFeignClient.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 填充信息
        List<QuestionVO> questionVOList = questionList.stream().map(question -> {
            QuestionVO questionVO = QuestionVO.objToVo(question);
            Long userId = question.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionVO.setUser(userFeignClient.getUserVO(user));
            return questionVO;
        }).collect(Collectors.toList());
        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }

}




