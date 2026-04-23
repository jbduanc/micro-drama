package com.tcm.rx.vo.stat.response;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 适宜技术统计导出VO
 */
@Data
public class ChargeExportVO implements Serializable {

    private static final long serialVersionUID = -4026001812129319630L;

    /**
     * 医疗机构
     */
    @ExcelProperty(value = "医疗机构")
    @ColumnWidth(20)
    private String hspName;

    /**
     * 适宜技术编码
     */
    @ExcelProperty(value = "适宜技术编码")
    @ColumnWidth(20)
    private String itemCode;

    /**
     * 适宜技术名称
     */
    @ExcelProperty(value = "适宜技术名称")
    @ColumnWidth(20)
    private String itemName;

    /**
     * 次数
     */
    @ExcelProperty(value = "次数")
    @ColumnWidth(15)
    private Double quantity;

    /**
     * 单位
     */
    @ExcelProperty(value = "单位")
    @ColumnWidth(15)
    private String priceUnit;

    /**
     * 总金额（元）
     */
    @ExcelProperty(value = "总金额（元）")
    @ColumnWidth(20)
    private Double amount;
}
