package com.tcm.rx.vo.rx.request;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 配伍禁忌导入VO
 */
@Data
public class CompatTabImportVO {

    /**
     * 禁忌名称
     */
    @ExcelProperty(value = "禁忌名称*")
    private String tabooName;

    /**
     * 禁忌类型
     */
    @ExcelProperty(value = "禁忌类型*")
    private String tabooType;

    /**
     * 是否妊娠
     */
    @ExcelProperty(value = "是否妊娠*")
    private String isPregnant;

    /**
     * 饮片编码
     */
    @ExcelProperty(value = "饮片编码*")
    private String herbCode;

    /**
     * 饮片名称
     */
    @ExcelProperty(value = "饮片名称*")
    private String herbName;

    /**
     * 饮片类型
     */
    @ExcelProperty(value = "饮片类型*")
    private String type;

    /**
     * 饮片规格
     */
    @ExcelProperty(value = "饮片规格*")
    private String spec;

    /**
     * 校验数据错误的提示
     */
    @ExcelIgnore
    private String errMsg;
}
