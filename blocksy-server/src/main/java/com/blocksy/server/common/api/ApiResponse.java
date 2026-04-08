package com.blocksy.server.common.api;

import com.blocksy.server.common.enums.ResponseCodeEnum;

public record ApiResponse<T>(int code, String message, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResponseCodeEnum.SUCCESS.code(), ResponseCodeEnum.SUCCESS.message(), data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(ResponseCodeEnum.SUCCESS.code(), ResponseCodeEnum.SUCCESS.message(), null);
    }

    public static ApiResponse<Void> error(ResponseCodeEnum codeEnum, String message) {
        return new ApiResponse<>(codeEnum.code(), message, null);
    }

    public static ApiResponse<Void> error(String message) {
        return error(ResponseCodeEnum.BAD_REQUEST, message);
    }
}
