package com.tcm.rx.vo.basicData.request;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HerbImportVO {
    @ExcelProperty(value = "饮片编码*", index = 0)
    private String herbCode;

    @ExcelProperty(value = "饮片名称*", index = 1)
    private String herbName;

    @ExcelProperty(value = "饮片规格*", index = 2)
    private String spec;

    @ExcelProperty(value = "饮片类型*", index = 3) // 必须是HerbType的值
    private String type;

    @ExcelProperty(value = "饮片单位*", index = 4)    // 必须是HerbUnit的值
    private String unit;

    @ExcelProperty(value = "成本价*", index = 5)
    private String costPrice;

    @ExcelProperty(value = "毛利率", index = 6)   // 选填
    private String grossMargin;

    @ExcelProperty(value = "零售价*", index = 7)
    private String retailPrice;

    @ExcelProperty(value = "助记码", index = 8)
    private String helpCode;

    @ExcelProperty(value = "所属医疗机构名称", index = 9)
    @ContentStyle(dataFormat = 49)
    private String hspNames;

    @ExcelProperty(value = "是否支持医保*", index = 10)// 选填，合法值见MedicalInsuranceEnum
    private String medicalInsurance;

    @ExcelProperty(value = "状态*", index = 11)    // 必须是启用/禁用
    private String status;

    /**
     * 医保编码
     */
    @ExcelProperty(value = "医保编码", index = 12)    // 必须是启用/禁用
    private String medicalCode;

    /**
     * 单位重量
     */
    @ExcelProperty(value = "单位重量(g)", index = 13)
    private String unitWeight;

    /**
     * 性味
     */
    @ExcelProperty(value = "性味", index = 14)
    private String propertyFlavor;

    /**
     * 归经
     */
    @ExcelProperty(value = "归经", index = 15)
    private String meridianTropism;


    /**
     * 功能主治
     */
    @ExcelProperty(value = "功能主治", index = 16)
    private String therapFunction;

    /**
     * 使用禁忌
     */
    @ExcelProperty(value = "使用禁忌", index = 17)
    private String usageForbidden;

    /**
     * 存储
     */
    @ExcelProperty(value = "存储", index = 18)
    private String storageCondition;

    /**
     * HIS饮片ID
     */
    @ExcelProperty(value = "HIS饮片ID", index = 19)
    private String hisHerbId;

    /**
     * 校验数据错误的提示
     */
    @ExcelIgnore
    private String errMsg;
}