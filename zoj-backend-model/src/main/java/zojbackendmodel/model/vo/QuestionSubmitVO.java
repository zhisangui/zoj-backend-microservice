package zojbackendmodel.model.vo;

import cn.hutool.json.JSONUtil;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import zojbackendmodel.model.codesandbox.JudgeInfo;
import zojbackendmodel.model.entity.QuestionSubmit;

import java.io.Serializable;
import java.util.Date;

/**
 * 题目视图
 *
 * @author <a href="https://github.com/zhisangui">zsg</a>
 * 
 */
@Data
public class QuestionSubmitVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 提交用户 id
     */
    private Long userId;

    /**
     * 语言
     */
    private String language;

    /**
     * 代码(管理员和自己可见)
     */
    private String code;

    /**
     * 判题状态（0-等待中 1-判题中 2-成功 3-失败)
     */
    private Integer status;

    /**
     * 判题信息（json 对象）
     */
    private JudgeInfo judgeInfo;

    /**
     * 提交用户
     */
    private UserVO user;

    /**
     * 题目信息
     */
    private QuestionVO question;

    /**
     * 提交时间
     */
    private Date createTime;

    /**
     * 包装类转对象
     *
     * @param questionSubmitVO
     * @return
     */
    public static QuestionSubmit voToObj(QuestionSubmitVO questionSubmitVO) {
        if (questionSubmitVO == null) {
            return null;
        }
        QuestionSubmit questionSubmit = new QuestionSubmit();
        BeanUtils.copyProperties(questionSubmitVO, questionSubmit);

        JudgeInfo judgeInfoObj = questionSubmitVO.getJudgeInfo();
        if (judgeInfoObj != null) {
            questionSubmit.setJudgeInfo(JSONUtil.toJsonStr(judgeInfoObj));
        }
        return questionSubmit;
    }

    /**
     * 对象转包装类
     *
     * @param questionSubmit
     * @return
     */
    public static QuestionSubmitVO objToVo(QuestionSubmit questionSubmit) {
        if (questionSubmit == null) {
            return null;
        }
        QuestionSubmitVO questionSubmitVO = new QuestionSubmitVO();
        BeanUtils.copyProperties(questionSubmit, questionSubmitVO);

        String judgeInfo = questionSubmit.getJudgeInfo();
        if (judgeInfo != null) {
            questionSubmitVO.setJudgeInfo(JSONUtil.toBean(judgeInfo, JudgeInfo.class));
        }
        return questionSubmitVO;
    }
}