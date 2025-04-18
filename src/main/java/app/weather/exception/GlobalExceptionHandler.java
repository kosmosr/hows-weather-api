package app.weather.exception;

import app.weather.model.response.ResultResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(WebClientResponseException.class)
    public Mono<ResponseEntity<ResultResponse<Object>>> handleWebClientResponseException(WebClientResponseException ex) {
        log.error("调用外部 API 失败: Status={}, Body={}", ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
        int statusCode = ex.getStatusCode().value();
        String message = String.format("外部 API 调用失败: %d - %s", statusCode, ex.getStatusText());
        ResultResponse<Object> errorResponse = ResultResponse.error(statusCode, message);
        return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(errorResponse));
    }

    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<ResultResponse<Object>>> handleRuntimeException(RuntimeException ex) {
        log.error("发生运行时异常: {}", ex.getMessage(), ex);
        ResultResponse<Object> errorResponse = ResultResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务内部错误: " + ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ResultResponse<Object>>> handleGenericException(Exception ex) {
        log.error("发生未捕获异常: {}", ex.getMessage(), ex);
        ResultResponse<Object> errorResponse = ResultResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务发生未知错误");
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }
}
