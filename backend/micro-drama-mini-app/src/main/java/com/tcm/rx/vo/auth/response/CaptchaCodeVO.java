package com.tcm.rx.vo.auth.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 验证码 实体VO类
 * @Author xph
 * @Date 2025/7/17 16:55
 */
@Data
public class CaptchaCodeVO implements Serializable {

    private static final long serialVersionUID = -163526564095456770L;

    /**
     * 验证码uuid唯一标识
     */
    private String uuid;

    /**
     * 验证码的值
     */
    private String code;

    /**
     * 图形验证码（base64的图片数据）
     */
    private Object captchaData;

}
