package com.series.common.entity;

import com.series.common.exception.ResponseCode;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    // 响应码（0:成功，非0:失败）
    private int code;
    // 响应消息
    private String msg;
    // 响应数据
    private T data;

    /**
     * 服务器时间
     */
    private Date serverTime;
    /**
     * 堆栈信息, 开发阶段返回
     */
    private Object stack;

    private String path;

    public Result(Integer code, T data) {
        this.code = code;
        this.data = data;
    }

    public Result(ResponseCode responseCode, T data) {
        this.code = responseCode.getCode();
        this.msg = responseCode.getMsg();
        this.data = data;
    }

    public Result() {
    }

    public static <T> Result<T> ok() {
        Result<T> result = new Result<>();
        result.setCode(ResponseCode.OK.getCode());
        result.setMsg("成功");
        return result;
    }

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResponseCode.OK.getCode());
        result.setMsg("成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }

    /**
     * 错误返回（带自定义状态码和消息）
     */
    public static <T> Result<T> error(int code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    public Result<T> put(String key, Object value) {
        // 若需要扩展 map 结构，可改为 Map 存储，这里简化演示
        throw new UnsupportedOperationException("如需扩展，可修改 Result 为 Map 存储");
    }
}
