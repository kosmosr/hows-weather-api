package app.weather.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultResponse<T> {

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
     * 创建一个带有错误信息的ResultResponse对象。
     *
     * @param code    状态码
     * @param message 错误信息
     * @param <T>     泛型类型，表示响应数据的类型
     * @return 包含给定状态码和错误信息的ResultResponse对象
     */
    public static <T> ResultResponse<T> error(Integer code, String message) {
        ResultResponse<T> response = new ResultResponse<>();
        response.setCode(code);
        response.setMessage(message);
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
