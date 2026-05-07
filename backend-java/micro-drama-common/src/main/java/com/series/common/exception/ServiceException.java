package com.series.common.exception;

import lombok.Data;

/**
 * @author shouhan
 * @version 1.0
 * @date 2022/5/25
 * @desc 异常抛出实体类
 * @see
 * @since 1.0
 */
@Data
public class ServiceException extends RuntimeException {

    /**
     * 返回码
     */
    private Integer code;
    /**
     * 开发阶段错误信息
     */
    private String msg;

    /**
     * 数据
     */
    private Object data;

    public ServiceException() {

    }

    public ServiceException(Integer code) {
        this.code = code;
    }

    public ServiceException(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public ServiceException(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ServiceException(String msg) {
        this.code = 500;
        this.msg = msg;
    }

    public ServiceException(String msg, Integer code) {
        this.msg = msg;
        this.code = code;
    }

    public ServiceException(ResponseCode responseCode) {
        this.code = responseCode.getCode();
        this.msg = responseCode.getMsg();
    }
}
