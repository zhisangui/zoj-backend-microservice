package zojbackendmodel.model.codesandbox;

import lombok.Data;

@Data
public class JudgeInfo {
    /**
     * 程序结果信息（AC、WA..)
     */
    private String message;

    /**
     * 程序耗时
     */
    private Long time;

    /**
     * 占用内存
     */
    private Long memory;
}
