package zojbackendmodel.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 题目
 * @TableName question
 */
@TableName(value ="question")
@Data
public class Question implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
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
    private String tags;

    /**
     * 提交数量
     */
    private Integer submitNum;

    /**
     * 通过数量
     */
    private Integer acceptNum;

    /**
     * 判题的配置（json对象）
     */
    private String judgeConfig;

    /**
     * 判题用例（json数组）
     */
    private String judgeCase;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}