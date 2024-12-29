package com.zhisangui.zojbackendjudgeservice.judge.strategy;


import com.zhisangui.zojbackendjudgeservice.judge.strategy.impl.DefaultJudgeStrategy;
import com.zhisangui.zojbackendjudgeservice.judge.strategy.impl.JavaJudgeStrategy;
import org.springframework.stereotype.Component;
import zojbackendmodel.model.codesandbox.JudgeInfo;

/**
 * todo：策略模式修改，改为枚举＋map的形式，更直观
 */
@Component
public class StrategyManager {
    private JudgeStrategy judgeStrategy;

    public JudgeInfo doJudge(JudgeContext judgeContext) {
        String language = judgeContext.getQuestionSubmit().getLanguage();
        if (language.equals("java")) {
            judgeStrategy = new JavaJudgeStrategy();
        } else {
            judgeStrategy = new DefaultJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }

}
