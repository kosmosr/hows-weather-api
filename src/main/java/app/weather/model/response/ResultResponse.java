package app.weather.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 返回信息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 成功返回
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return ResultResponse
     */
    public static <T> ResultResponse<T> success(T data) {
        ResultResponse<T> response = new ResultResponse<>();
        response.setCode(StatusEnum.SUCCESS.getCode());
        response.setMessage(StatusEnum.SUCCESS.getMessage());
        response.setData(data);
        return response;
    }

    /**
     * 失败返回
     *
     * @param message 错误信息
     * @param <T>
     * @return
     */
    public static <T> ResultResponse<T> error(String message) {
        ResultResponse<T> response = new ResultResponse<>();
        response.setCode(StatusEnum.FAIL.getCode());
        response.setMessage(message);
        return response;
    }

    /**
     * 失败返回
     *
     * @param statusEnum 错误枚举
     * @param <T>
     * @return
     */
    public static <T> ResultResponse<T> error(StatusEnum statusEnum) {
        ResultResponse<T> response = new ResultResponse<>();
        response.setCode(statusEnum.getCode());
        response.setMessage(statusEnum.getMessage());
        return response;
    }

}
