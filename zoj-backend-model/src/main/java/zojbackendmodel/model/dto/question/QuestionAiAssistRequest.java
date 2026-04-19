package zojbackendmodel.model.dto.question;

import lombok.Data;

import java.io.Serializable;

/**
 * AI 辅助分析题目请求
 */
@Data
public class QuestionAiAssistRequest implements Serializable {

    /**
     * 题目 id
     */
    private Long questionId;

    private static final long serialVersionUID = 1L;
}
