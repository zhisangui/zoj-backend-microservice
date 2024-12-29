package zojbackendmodel.model.dto.question;

import lombok.Data;

/**
 * 判题的配置，时间ms 内存kb
 */
@Data
public class JudgeConfig {
    private Long timeLimit;
    private Long memoryLimit;
    private Long stackLimit;
}
