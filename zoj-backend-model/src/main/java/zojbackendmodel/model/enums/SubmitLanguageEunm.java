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
public enum SubmitLanguageEunm {

    JAVA("java", "java"),
    GOLANG("golang", "golang"),
    CPLUSPLUS("c++", "c++"),
    PYTHON("python", "python");

    private final String text;

    private final String value;

    SubmitLanguageEunm(String text, String value) {
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
    public static SubmitLanguageEunm getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (SubmitLanguageEunm anEnum : SubmitLanguageEunm.values()) {
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
