package zojbackendmodel.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目状态枚举
 *
 * @author <a href="https://github.com/zhisangui">zsg</a>
 * 
 */
public enum JudgeResEnum {

    ACCEPTED("Accepted", "AC"),
    WRONG_ANSWER("Wrong Answer", "WA"),
    COMPILE_ERROR("Compile Error", "CE"),
    MEMORY_LIMIT_EXCEEDED("Memory Limit Exceeded", "MLE"),
    TIME_LIMIT_EXCEEDED("Time Limit Exceeded", "TLE"),
    PRESENTATION_ERROR("Presentation Error", "PE"),
    OUTPUT_LIMIT_EXCEEDED("Output Limit Exceeded", "OLE"),
    DANGEROUS_OPERATION("Dangerous Operation", "Danger Operation"),
    RUNTIME_ERROR("Runtime Error", "RE"),
    SYSTEM_ERROR("System Error", "System Error"),;


    private final String text;

    private final String value;

    JudgeResEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static JudgeResEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (JudgeResEnum anEnum : JudgeResEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
