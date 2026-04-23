package com.tcm.rx.vo.stat.response;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;

/**
 * 饮片使用明细导出VO
 */
@Data
public class HerbUseExportVO implements Serializable {

    private static final long serialVersionUID = -4026001812129319630L;

    /**
     * 医疗机构
     */
    @ExcelProperty(value = "医疗机构")
    @ColumnWidth(20)
    private String hspName;

    /**
     * 饮片名称
     */
    @ExcelProperty(value = "饮片名称")
    @ColumnWidth(20)
    private String herbName;

    /**
     * 饮片编码
     */
    @ExcelProperty(value = "饮片编码")
    @ColumnWidth(20)
    private String herbCode;

    /**
     * 规格
     */
    @ExcelProperty(value = "规格")
    @ColumnWidth(15)
    private String spec;

    /**
     * 饮片类型
     */
    @ExcelProperty(value = "饮片类型")
    @ColumnWidth(20)
    private String type;

    /**
     * 总用量
     */
    @ExcelProperty(value = "总用量")
    @ColumnWidth(20)
    private Double herbDosage;

    /**
     * 单位
     */
    @ExcelProperty(value = "单位")
    @ColumnWidth(15)
    private String unit;

    /**
     * 成本总金额（元）
     */
    @ExcelProperty(value = "成本总金额（元）")
    @ColumnWidth(20)
    private Double costPrice;

    /**
     * 零售总金额（元）
     */
    @ExcelProperty(value = "零售总金额（元）")
    @ColumnWidth(20)
    private Double retailPrice;

}
