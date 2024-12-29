package zojbackendmodel.model.dto.question;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 编辑请求(编辑是给普通用户使用（较少字段），更新是给管理员使用（字段更多））
 *
 * @author <a href="https://github.com/zhisangui">zsg</a>
 * 
 */
@Data
public class QuestionEditRequest implements Serializable {
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
     * 题解
     */
    private String answer;

    /**
     * 题目标签（json 数组）
     */
    private List<String> tags;

    /**
     * 判题的配置（json对象）
     */
    private JudgeConfig judgeConfig;

    /**
     * 判题用例（json数组）
     */
    private List<JudgeCase> judgeCase;

    private static final long serialVersionUID = 1L;
}