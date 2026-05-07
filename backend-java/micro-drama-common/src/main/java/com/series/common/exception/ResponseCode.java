package com.series.common.exception;

import lombok.Getter;

/**
 * @author shouhan
 * @version 1.0
 * @date 2022/5/25
 * @desc 返回枚举类
 * @see
 * @since 1.0
 */
@Getter
public enum ResponseCode {
    SUCCESS(0, "操作成功"),
    OK(200, "操作成功"),
    SERVER_NOT_FOUND(404, "服务未找到"),
    AUTH_ERROR(401, "认证失败"),
    WEB_SERVER_PROGRAM_ERROR(500, "服务端异常"),

    ;

    private final int code;
    private final String msg;

    ResponseCode(final int code, final String msg) {
        this.code = code;
        this.msg = msg;
    }
}
