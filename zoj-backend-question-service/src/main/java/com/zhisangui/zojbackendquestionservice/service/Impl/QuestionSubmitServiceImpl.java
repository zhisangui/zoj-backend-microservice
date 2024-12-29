package com.zhisangui.zojbackendquestionservice.service.Impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhisangui.zojbackendcommon.common.ErrorCode;
import com.zhisangui.zojbackendcommon.constant.CommonConstant;
import com.zhisangui.zojbackendcommon.constant.RabbitMQConstant;
import com.zhisangui.zojbackendcommon.exception.BusinessException;
import com.zhisangui.zojbackendcommon.utils.SqlUtils;
import com.zhisangui.zojbackendquestionservice.mapper.QuestionSubmitMapper;
import com.zhisangui.zojbackendquestionservice.mq.MyMessageProducer;
import com.zhisangui.zojbackendquestionservice.service.QuestionService;
import com.zhisangui.zojbackendquestionservice.service.QuestionSubmitService;
import com.zhisangui.zojbackendserviceclient.service.JudgeFeignClient;
import com.zhisangui.zojbackendserviceclient.service.UserFeignClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import zojbackendmodel.model.dto.questionsubmit.QuestionSubmitAddRequest;
import zojbackendmodel.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import zojbackendmodel.model.entity.Question;
import zojbackendmodel.model.entity.QuestionSubmit;
import zojbackendmodel.model.entity.User;
import zojbackendmodel.model.enums.JudgeStatusEnum;
import zojbackendmodel.model.enums.SubmitLanguageEunm;
import zojbackendmodel.model.vo.QuestionSubmitVO;
import zojbackendmodel.model.vo.QuestionVO;
import zojbackendmodel.model.vo.UserVO;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zsg
 * @description 针对表【question_submit(题目提交)】的数据库操作Service实现
 * @createDate 2024-09-19 16:53:48
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private MyMessageProducer myMessageProducer;

    @Resource
    @Lazy
    private JudgeFeignClient judgeFeignClient;
    /**
     * 提交解答
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public boolean doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        // 判断实体是否存在，根据类别获取实体
        if (questionSubmitAddRequest == null)
            return false;
        long questionId = questionSubmitAddRequest.getQuestionId();
        if (questionId <= 0)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 锁必须要包裹住事务方法
//        QuestionSubmitService = (QuestionSubmitService) AopContext.currentProxy();
//        synchronized (String.valueOf(userId).intern()) {
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setUserId(loginUser.getId());

        String language = questionSubmitAddRequest.getLanguage();
        if (StrUtil.isBlank(language)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择代码语言");
        }
        SubmitLanguageEunm submitLanguageEunm = SubmitLanguageEunm.getEnumByValue(language);
        if (submitLanguageEunm == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持该语言");
        }
        questionSubmit.setLanguage(language);
        String code = questionSubmitAddRequest.getCode();
        if (StrUtil.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请勿提交空代码");
        }
        questionSubmit.setCode(code);
        questionSubmit.setStatus(JudgeStatusEnum.WAITING.getValue());
        boolean res = this.save(questionSubmit);

        // 修改题目提交数
        Question newQuestion = new Question();
        newQuestion.setId(questionId);
        newQuestion.setSubmitNum(question.getSubmitNum() + 1);
        boolean update = questionService.updateById(newQuestion);

        if (res && update) {
            // 原先使用线程池另开线程进行
//            CompletableFuture.runAsync(() -> {
//                judgeFeignClient.doJudge(questionSubmit.getId());
//            });
            // 优化为消息队列异步消费
            myMessageProducer.sendMessage(RabbitMQConstant.EXCHANGE_NAME,
                    RabbitMQConstant.ROUTING_KEY, String.valueOf(questionSubmit.getId()));
        }
        return res;
    }

    /**
     * 获得查询的query
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {

        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        // 1. 判空
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }

        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        String title = questionSubmitQueryRequest.getTitle();
        Integer status = questionSubmitQueryRequest.getStatus();
        String language = questionSubmitQueryRequest.getLanguage();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();
        // 2. 拼接查询条件
        if (questionId != null) {
            queryWrapper.eq("questionId", questionId);
        }
        if (userId != null) {
            queryWrapper.eq("userId", userId);
        }
        if (title != null) {
            queryWrapper.like("title", title);
        }
        if (status != null) {
            JudgeStatusEnum judgeStatusEnum = JudgeStatusEnum.getEnumByValue(status);
            if (judgeStatusEnum == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "查找题目状态异常");
            }
            queryWrapper.eq("status", status);
        }
        if (language != null) {
            SubmitLanguageEunm submitLanguageEunm = SubmitLanguageEunm.getEnumByValue(language);
            if (submitLanguageEunm == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "暂不支持该语言");
            }
            queryWrapper.like("language", language);
        }

        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 进行脱敏，返回vo
     *
     * @param questionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionPage, HttpServletRequest request) {
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        List<QuestionSubmit> questionSubmits = questionPage.getRecords();
        if (CollUtil.isEmpty(questionSubmits))
            return questionSubmitVOPage;
//        QuestionSubmitVO questionSubmitVO = new QuestionSubmitVO();
//        questionSubmitVO.setId();
//        questionSubmitVO.setQuestionId();
//        questionSubmitVO.setUserId();
//        questionSubmitVO.setLanguage();
//        questionSubmitVO.setCode();
//        questionSubmitVO.setStatus();
//        questionSubmitVO.setJudgeInfo();
//        questionSubmitVO.setUser();
//        questionSubmitVO.setQuestion();

        // 获得所有的提交用户id
        Set<Long> userIdSet = questionSubmits.stream().map(QuestionSubmit::getUserId).collect(Collectors.toSet());

        // 获得所有的题目id
        Set<Long> questionIdSet = questionSubmits.stream().map(QuestionSubmit::getQuestionId).collect(Collectors.toSet());

        // 获得所有的提交用户，以 提交用户id 为划分
        Map<Long, List<User>> userIdUserMap =
                userFeignClient.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        // 获得所有的题目，以 题目id 为划分
        Map<Long, List<Question>> questionIdQuestionMap =
                questionService.listByIds(questionIdSet).stream().collect(Collectors.groupingBy(Question::getId));
        User loginUser = userFeignClient.getLoginUser(request);
        // 使用questionSubmits流，将信息补进去，注意，若不为本人或管理员，则不展示代码
        List<QuestionSubmitVO> questionSubmitVOS = questionSubmits.stream().map(questionSubmit -> {
            QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);

            Long userId = questionSubmitVO.getUserId();
            if (!Objects.equals(userId, loginUser.getId()) && !userFeignClient.isAdmin(loginUser)) {
                questionSubmitVO.setCode("");
            }

            Long questionId = questionSubmitVO.getQuestionId();

            User user = userIdUserMap.get(userId).get(0);
            Question question = questionIdQuestionMap.get(questionId).get(0);

            UserVO userVO = userFeignClient.getUserVO(user);
            QuestionVO questionVO = questionService.getQuestionVO(question, request);

            questionSubmitVO.setUser(userVO);
            questionSubmitVO.setQuestion(questionVO);
            return questionSubmitVO;
        }).collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOS);
        return questionSubmitVOPage;
    }

//    todo: 加锁，防止用户同一时间多次提交，占用资源
//    /**
//     * 封装了事务的方法
//     *
//     * @param userId
//     * @param questionId
//     * @return
//     */
//    @Transactional(rollbackFor = Exception.class)
//    public int doQuestionThumbInner(long userId, long questionId) {
//        QuestionThumb questionThumb = new QuestionThumb();
//        questionThumb.setUserId(userId);
//        questionThumb.setQuestionId(questionId);
//        QueryWrapper<QuestionThumb> thumbQueryWrapper = new QueryWrapper<>(questionThumb);
//        QuestionThumb oldQuestionThumb = this.getOne(thumbQueryWrapper);
//        boolean result;
//        // 已点赞
//        if (oldQuestionThumb != null) {
//            result = this.remove(thumbQueryWrapper);
//            if (result) {
//                // 点赞数 - 1
//                result = questionService.update()
//                        .eq("id", questionId)
//                        .gt("thumbNum", 0)
//                        .setSql("thumbNum = thumbNum - 1")
//                        .update();
//                return result ? -1 : 0;
//            } else {
//                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
//            }
//        } else {
//            // 未点赞
//            result = this.save(questionThumb);
//            if (result) {
//                // 点赞数 + 1
//                result = questionService.update()
//                        .eq("id", questionId)
//                        .setSql("thumbNum = thumbNum + 1")
//                        .update();
//                return result ? 1 : 0;
//            } else {
//                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
//            }
//        }
//    }
}




