package com.tcm.rx.vo.rx.response;

import lombok.Data;

import java.util.List;

@Data
public class CompatTabCheckResVO {

    /**
     * 饮片编码
     */
    private String herbCode;

    /**
     * 禁忌编码
     */
    private String tabooCode;

    /**
     * 禁忌名称
     */
    private String tabooName;

    /**
     * 禁忌类型
     */
    private String tabooType;

    /**
     * 提示
     */
    private String tips;

    /**
     * 十八反、十九畏参照物饮片编码
     */
    private String compHerbCode;
}
