package com.tcm.rx.vo.stat.response;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;

/**
 * 历史适宜技术导出VO
 */
@Data
public class HistoryChargeExportVO implements Serializable {

    private static final long serialVersionUID = -4026001812129319630L;

    /**
     * 适宜技术单ID
     */
    @ExcelProperty(value = "适宜技术单ID")
    @ColumnWidth(20)
    private String rxId;

    /**
     * 适宜技术名称
     */
    @ExcelProperty(value = "适宜技术名称")
    @ColumnWidth(20)
    private String itemName;

    /**
     * 总金额
     */
    @ExcelProperty(value = "总金额")
    @ColumnWidth(20)
    private Double amount;

    /**
     * 患者姓名
     */
    @ExcelProperty(value = "患者姓名")
    @ColumnWidth(20)
    private String patientName;

    /**
     * 状态
     */
    @ExcelIgnore
    private Integer status;

    /**
     * 单据状态
     */
    @ExcelProperty(value = "单据状态")
    @ColumnWidth(20)
    private String statusName;

    /**
     * 更新时间 yyyy-MM-dd HH:mm:ss
     */
    @ExcelProperty(value = "更新时间")
    @ColumnWidth(20)
    private String prescriptionTime;
}
