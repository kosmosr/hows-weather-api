package app.weather.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum StatusEnum {
    SUCCESS(200, "成功"),
    FAIL(500, "失败"),
    PARAM_ERROR(400, "参数错误"),
    ;

    private final Integer code;

    private final String message;

    StatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
