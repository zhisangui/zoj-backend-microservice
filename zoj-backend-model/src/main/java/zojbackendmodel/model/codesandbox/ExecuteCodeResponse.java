package zojbackendmodel.model.codesandbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 代码沙箱执行结果
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeResponse {
    /**
     * 程序执行信息(time,memory，messaage为空)
     */
    private JudgeInfo judgeInfo;

    /**
     * 接口状态(0-正常，1-出错）
     */
    private Integer status;

    /**
     * 接口信息（如果程序在某个用例出错了， 该变量为 error 信息，否则置空)
     */
    private String message;

    /**
     * 代码输出
     */
    private List<String> outputs;


}
