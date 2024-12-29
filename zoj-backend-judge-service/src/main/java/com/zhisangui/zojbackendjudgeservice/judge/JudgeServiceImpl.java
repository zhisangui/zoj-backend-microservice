package com.zhisangui.zojbackendjudgeservice.judge;

import cn.hutool.json.JSONUtil;
import com.zhisangui.zojbackendcommon.common.ErrorCode;
import com.zhisangui.zojbackendcommon.exception.BusinessException;
import com.zhisangui.zojbackendjudgeservice.judge.codesandbox.CodeSandBox;
import com.zhisangui.zojbackendjudgeservice.judge.codesandbox.CodeSandBoxFactory;
import com.zhisangui.zojbackendjudgeservice.judge.codesandbox.impl.ProxyCodeSandBox;
import com.zhisangui.zojbackendjudgeservice.judge.strategy.JudgeContext;
import com.zhisangui.zojbackendjudgeservice.judge.strategy.StrategyManager;
import com.zhisangui.zojbackendserviceclient.service.QuestionFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import zojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import zojbackendmodel.model.codesandbox.ExecuteCodeResponse;
import zojbackendmodel.model.codesandbox.JudgeInfo;
import zojbackendmodel.model.dto.question.JudgeCase;
import zojbackendmodel.model.entity.Question;
import zojbackendmodel.model.entity.QuestionSubmit;
import zojbackendmodel.model.enums.JudgeResEnum;
import zojbackendmodel.model.enums.JudgeStatusEnum;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JudgeServiceImpl implements JudgeService {
    @Value("${code.sandbox:example}")
    private String codeSandboxType;
    @Resource
    private QuestionFeignClient questionFeignClient;

    @Resource
    private StrategyManager strategyManager;

    @Override
    public QuestionSubmit doJudge(Long questionSubmitId) {
        // 1. 判断 id 非空且合法
        if (questionSubmitId == null || questionSubmitId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 获得提交信息和题目
        QuestionSubmit questionSubmit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionFeignClient.getQuestionById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 3. 判断当前提交的状态，为等待中才继续（变相加了个锁）
        Integer status = questionSubmit.getStatus();
        JudgeStatusEnum statusEnum = JudgeStatusEnum.getEnumByValue(status);
        if (!JudgeStatusEnum.WAITING.equals(statusEnum)) {
            return questionSubmit;
        }

        // 3.1 修改当前题目的状态为判断中，防止重复提交
        QuestionSubmit tempQuestionSubmit = new QuestionSubmit();
        tempQuestionSubmit.setId(questionSubmit.getId());
        tempQuestionSubmit.setStatus(JudgeStatusEnum.RUNNING.getValue());
        boolean update = questionFeignClient.updateQuestionSubmitById(tempQuestionSubmit);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }

        // 4. 构造代码沙箱所需的参数
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        String judgeCases = question.getJudgeCase();
        List<String> list = JSONUtil.toList(judgeCases, JudgeCase.class)
                .stream()
                .map(JudgeCase::getInput)
                .collect(Collectors.toList());
        executeCodeRequest.setInputs(list);
        executeCodeRequest.setCode(questionSubmit.getCode());
        executeCodeRequest.setLanguage(questionSubmit.getLanguage());

        // 5. 获得代码沙箱并执行
        CodeSandBox codeSandBox = CodeSandBoxFactory.getCodeSandBox(codeSandboxType);
        codeSandBox = new ProxyCodeSandBox(codeSandBox);
        ExecuteCodeResponse executeCodeResponse = codeSandBox.doJudge(executeCodeRequest);
        // 6. 根据代码沙箱结果进行判断 (不同语言对于结果要求可能不一样，比如java语言对于 timeLimit 不如c++ 严格，使用策略模式)
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);
        judgeContext.setExecuteCodeResponse(executeCodeResponse);
        JudgeInfo judgeInfo = strategyManager.doJudge(judgeContext);

        // 7 根据 judgeInfo 结果对数据库进行修改
        QuestionSubmit newQuestionSubmit = new QuestionSubmit();
        newQuestionSubmit.setId(questionSubmit.getId());
        newQuestionSubmit.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        newQuestionSubmit.setStatus(JudgeStatusEnum.SUCCEED.getValue());
        boolean updated = questionFeignClient.updateQuestionSubmitById(newQuestionSubmit);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        // 8. 修改题目成功数
        if (JudgeResEnum.ACCEPTED.getValue().equals(judgeInfo.getMessage())) {
            Question newQuestion = new Question();
            newQuestion.setId(question.getId());
            newQuestion.setAcceptNum(question.getAcceptNum() + 1);
            boolean updateRes = questionFeignClient.updateQuestionById(newQuestion);
            if (!updateRes) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
        return questionFeignClient.getQuestionSubmitById(questionSubmit.getId());
    }
}