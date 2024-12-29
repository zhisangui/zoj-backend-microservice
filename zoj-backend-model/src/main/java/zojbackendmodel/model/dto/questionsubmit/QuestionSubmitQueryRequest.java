package zojbackendmodel.model.dto.questionsubmit;

import com.zhisangui.zojbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询提交记录请求类
 *
 * @author <a href="https://github.com/zhisangui">zsg</a>
 * 
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionSubmitQueryRequest extends PageRequest implements Serializable {

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 提交用户 id
     */
    private Long userId;

    /**
     * 标题
     */
    private String title;

    /**
     * 判题状态（0-等待中 1-判题中 2-成功 3-失败)
     */
    private Integer status;

    /**
     * 语言
     */
    private String language;
    private static final long serialVersionUID = 1L;
}