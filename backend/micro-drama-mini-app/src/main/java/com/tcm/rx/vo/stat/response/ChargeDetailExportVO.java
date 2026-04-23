package com.tcm.rx.vo.stat.response;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 适宜技术明细导出VO
 */
@Data
public class ChargeDetailExportVO implements Serializable {

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
     * 接诊医疗机构
     */
    @ExcelProperty(value = "接诊医疗机构")
    @ColumnWidth(20)
    private String hspName;

    /**
     * 接诊医生
     */
    @ExcelProperty(value = "接诊医生")
    @ColumnWidth(20)
    private String prescriberName;

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
     * 总金额
     */
    @ExcelProperty(value = "总金额")
    @ColumnWidth(20)
    private Double amount;

    /**
     * 会诊医疗结构
     */
    @ExcelProperty(value = "会诊医疗结构")
    @ColumnWidth(20)
    private String consultationOrg;

    /**
     * 会诊医生
     */
    @ExcelProperty(value = "会诊医生")
    @ColumnWidth(20)
    private String consultationUserName;

    /**
     * 开单时间 yyyy-MM-dd HH:mm:ss
     */
    @ExcelProperty(value = "开单时间")
    @ColumnWidth(20)
    private String prescriptionTime;

    /**
     * 开单时间 yyyy-MM-dd HH:mm:ss
     */
    @ExcelProperty(value = "缴费时间")
    @ColumnWidth(20)
    private String paymentTime;

    /**
     * 退费时间 yyyy-MM-dd HH:mm:ss
     */
    @ExcelProperty(value = "退费时间")
    @ColumnWidth(20)
    private String refundTime;
}
