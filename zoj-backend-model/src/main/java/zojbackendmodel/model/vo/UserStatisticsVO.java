package zojbackendmodel.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户统计信息
 */
@Data
public class UserStatisticsVO implements Serializable {

    /**
     * 登录天数
     */
    private Integer loginDays;

    /**
     * 总提交次数
     */
    private Long totalSubmissions;

    /**
     * 成功提交次数
     */
    private Long acceptedSubmissions;

    /**
     * 成功率（百分比）
     */
    private Double successRate;

    /**
     * 尝试题目总数
     */
    private Long totalQuestions;

    /**
     * 成功解答题目总数
     */
    private Long acceptedQuestions;

    private static final long serialVersionUID = 1L;
}
