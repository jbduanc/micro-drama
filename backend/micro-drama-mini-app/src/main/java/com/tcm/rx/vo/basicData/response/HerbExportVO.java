package com.tcm.rx.vo.basicData.response;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class HerbExportVO {
    @ExcelProperty(value = "饮片编码")
    private String herbCode;

    @ExcelProperty(value = "饮片名称")
    private String herbName;

    @ExcelProperty(value = "助记码")
    private String helpCode;

    @ExcelProperty(value = "饮片规格")
    private String spec;

    @ExcelProperty(value = "饮片单位")
    private String unit;

    @ExcelProperty(value = "成本价")
    private BigDecimal costPrice;

    @ExcelProperty(value = "毛利率")
    private BigDecimal grossMargin;

    @ExcelProperty(value = "零售价")
    private BigDecimal retailPrice;

    @ExcelProperty(value = "所属医疗机构")
    private String hspNames;

    @ExcelProperty(value = "状态")
    private String status;

    @ExcelProperty(value = "是否支持医保")
    private String medicalInsurance;

    @ExcelProperty(value = "更新时间")
    private String updateTime;


}