package zojbackendmodel.model.vo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import zojbackendmodel.model.dto.question.JudgeConfig;
import zojbackendmodel.model.entity.Question;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 题目视图
 *
 * @author <a href="https://github.com/zhisangui">zsg</a>
 * 
 */
@Data
public class QuestionVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 题目标签（json 数组）
     */
    private List<String> tags;

    /**
     * 判题的配置（json对象）
     */
    private JudgeConfig judgeConfig;

    /**
     * 提交数量
     */
    private Integer submitNum;

    /**
     * 通过数量
     */
    private Integer acceptNum;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建用户
     */
    private UserVO user;

    /**
     * 包装类转对象
     *
     * @param questionVO
     * @return
     */
    public static Question voToObj(QuestionVO questionVO) {
        if (questionVO == null) {
            return null;
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionVO, question);
        List<String> tagList = questionVO.getTags();
        if (CollUtil.isNotEmpty(tagList)) {
            question.setTags(JSONUtil.toJsonStr(tagList));
        }
        JudgeConfig voJudgeConfig = questionVO.getJudgeConfig();
        if (voJudgeConfig != null) {
            question.setJudgeConfig(JSONUtil.toJsonStr(voJudgeConfig));
        }
        return question;
    }

    /**
     * 对象转包装类
     *
     * @param question
     * @return
     */
    public static QuestionVO objToVo(Question question) {
        if (question == null) {
            return null;
        }
        QuestionVO questionVO = new QuestionVO();
        BeanUtils.copyProperties(question, questionVO);
        questionVO.setTags(JSONUtil.toList(question.getTags(), String.class));

        String questionJudgeConfig = question.getJudgeConfig();
        if (questionJudgeConfig != null) {
            questionVO.setJudgeConfig(JSONUtil.toBean(questionJudgeConfig, JudgeConfig.class));
        }
        return questionVO;
    }
}
