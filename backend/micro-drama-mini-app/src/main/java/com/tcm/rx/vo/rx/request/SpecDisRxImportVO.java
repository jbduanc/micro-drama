package com.tcm.rx.vo.rx.request;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 专病专方导入VO
 */
@Data
public class SpecDisRxImportVO {

    /**
     * 方剂名称
     */
    @ExcelProperty(value = "方剂名称*")
    private String name;

    /**
     * 助记码
     */
    @ExcelProperty(value = "助记码*")
    private String helpCode;

    /**
     * 治疗疾病
     */
    @ExcelProperty(value = "治疗疾病*")
    private String treatmentDisease;

    /**
     * 主治功效
     */
    @ExcelProperty(value = "主治功效*")
    private String mainEfficacy;

    /**
     * 中医疾病，多个逗号分割
     */
    @ExcelProperty(value = "适应症*")
    private String tcmDisease;

    /**
     * 中医疾病助记码，多个逗号分割
     */
    @ExcelProperty(value = "适应症助记码*")
    private String tcmDiseaseHelpCode;

    /**
     * 饮片名称
     */
    @ExcelProperty(value = "饮片名称*")
    private String herbName;

    /**
     * 饮片用量
     */
    @ExcelProperty(value = "饮片用量*")
    private BigDecimal herbDosage;

    /**
     * 饮片单位
     */
    @ExcelProperty(value = "饮片单位*")
    private String unit;

    /**
     * 用法
     */
    @ExcelProperty(value = "用法")
    private String usage;

    /**
     * 方剂来源
     */
    @ExcelProperty(value = "方剂来源")
    private String source;

    /**
     * 按语
     */
    @ExcelProperty(value = "按语")
    private String remark;

    /**
     * 校验数据错误的提示
     */
    @ExcelIgnore
    private String errMsg;
}
