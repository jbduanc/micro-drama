package com.tcm.rx.vo.rx.request;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 辩证实施导入VO
 */
@Data
public class SynDiffImportVO {

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
     * 简称
     */
    @ExcelProperty(value = "方剂简称*")
    private String alias;

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
     * 中医证候，多个逗号分割
     */
    @ExcelProperty(value = "适应证候*")
    private String tcmPattern;

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
     * 饮片规格
     */
    @ExcelProperty(value = "饮片规格")
    private String spec;

    /**
     * 校验数据错误的提示
     */
    @ExcelIgnore
    private String errMsg;
}
