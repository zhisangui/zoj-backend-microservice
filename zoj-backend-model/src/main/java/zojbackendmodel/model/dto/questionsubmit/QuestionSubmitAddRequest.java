package zojbackendmodel.model.dto.questionsubmit;

import lombok.Data;

import java.io.Serializable;

/**
 * 帖子点赞请求
 *
 * @author <a href="https://github.com/zhisangui">zsg</a>
 * 
 */
@Data
public class QuestionSubmitAddRequest implements Serializable {

    /**
     * 题目 id
     */
    private Long questionId;
    /**
     * 语言
     */
    private String language;

    /**
     * 用户代码
     */
    private String code;

    private static final long serialVersionUID = 1L;
}