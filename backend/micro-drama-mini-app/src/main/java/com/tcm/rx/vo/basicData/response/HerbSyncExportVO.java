package com.tcm.rx.vo.basicData.response;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HerbSyncExportVO {
    /*@ExcelProperty(value = "饮片编码")
    private String herbCode;*/

    /**
     * 客户饮片编码
     */
    @ExcelProperty(value = "饮片编码")
    private String customerHerbCode;

    /**
     * 入库时间
     */
    @ExcelProperty(value = "入库时间")
    private String syncTime;

    /**
     * 客户饮片名称
     */
    @ExcelProperty(value = "饮片名称")
    private String customerHerbName;

    /**
     * 客户饮片规格
     */
    @ExcelProperty(value = "规格")
    private String customerHerbSpec;

    /**
     * 客户饮片单位
     */
    @ExcelProperty(value = "单位")
    private String customerHerbUnit;

    /**
     * 客户饮片入库数量
     */
    @ExcelProperty(value = "数量")
    private BigDecimal customerHerbNumber;

    /**
     * 成本价
     */
    @ExcelProperty(value = "成本价")
    private BigDecimal costPrice;


    /**
     * 合计成本价
     */
    @ExcelProperty(value = "入库金额")
    private BigDecimal customerHerbCostPrice;

    /**
     * 零售价
     */
    @ExcelProperty(value = "零售价")
    private BigDecimal retailPrice;

    /**
     * 合计零售价
     */
    @ExcelProperty(value = "零售金额")
    private BigDecimal customerHerbRetailPrice;

    /**
     * 操作人名称
     */
    @ExcelProperty(value = "入库人")
    private String operatorName;

    /**
     * 采购单号
     */
    @ExcelProperty(value = "批号")
    private String purchaseNo;

    /**
     * 生产厂家
     */
    @ExcelProperty(value = "供货单位")
    private String manufacturer;

    /**
     * 商品产地
     */
    @ExcelProperty(value = "产地名称")
    private String origin;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;
}