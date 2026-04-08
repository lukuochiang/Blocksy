package com.blocksy.server.common.exception;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.common.enums.ResponseCodeEnum;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() == null ? "参数校验失败" : error.getDefaultMessage())
                .orElse("参数校验失败");
        return ResponseEntity.badRequest().body(ApiResponse.error(ResponseCodeEnum.BAD_REQUEST, message));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        ResponseCodeEnum codeEnum = ex.getCode() == ResponseCodeEnum.FORBIDDEN.code()
                ? ResponseCodeEnum.FORBIDDEN
                : ex.getCode() == ResponseCodeEnum.UNAUTHORIZED.code()
                ? ResponseCodeEnum.UNAUTHORIZED
                : ex.getCode() == ResponseCodeEnum.INTERNAL_ERROR.code()
                ? ResponseCodeEnum.INTERNAL_ERROR
                : ResponseCodeEnum.BAD_REQUEST;
        HttpStatus status = codeEnum == ResponseCodeEnum.UNAUTHORIZED
                ? HttpStatus.UNAUTHORIZED
                : codeEnum == ResponseCodeEnum.FORBIDDEN
                ? HttpStatus.FORBIDDEN
                : codeEnum == ResponseCodeEnum.INTERNAL_ERROR
                ? HttpStatus.INTERNAL_SERVER_ERROR
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(ApiResponse.error(codeEnum, ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ResponseCodeEnum.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ResponseCodeEnum.UNAUTHORIZED, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(ResponseCodeEnum.INTERNAL_ERROR, ex.getMessage()));
    }
}
