package com.blocksy.server.common.exception;

import com.blocksy.server.common.enums.ResponseCodeEnum;

public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = ResponseCodeEnum.BAD_REQUEST.code();
    }

    public BusinessException(ResponseCodeEnum codeEnum, String message) {
        super(message);
        this.code = codeEnum.code();
    }

    public int getCode() {
        return code;
    }
}
