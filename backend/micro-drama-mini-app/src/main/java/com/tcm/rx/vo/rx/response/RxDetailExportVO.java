package com.tcm.rx.vo.rx.response;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 处方导出VO
 */
@Data
public class RxDetailExportVO {
    /**
     * 患者姓名
     */
    @ExcelProperty(index = 0, value = "患者姓名")
    @ColumnWidth(15)
    private String patientName;

    /**
     * 接诊医疗机构
     */
    @ExcelProperty(index = 1, value = "接诊医疗机构")
    @ColumnWidth(20)
    private String hspName;

    /**
     * 处方来源
     */
    @ExcelProperty(index = 2, value = "处方来源")
    @ColumnWidth(15)
    private String sourceName;

    /**
     * 接诊医生
     */
    @ExcelProperty(index = 3, value = "接诊医生")
    @ColumnWidth(15)
    private String prescriberName;

    /**
     * 处方状态
     */
    @ExcelProperty(index = 4, value = "处方状态")
    @ColumnWidth(15)
    private String statusName;

    /**
     * 药品费用(元)
     */
    @ExcelProperty(index = 5, value = "药品费用(元)")
    @ColumnWidth(15)
    private BigDecimal drugCost;

    /**
     * 剂数
     */
    @ExcelProperty(index = 6, value = "剂数")
    @ColumnWidth(10)
    private Integer doseCount;

    /**
     * 处方金额(元)
     */
    @ExcelProperty(index = 7, value = "处方金额(元)")
    @ColumnWidth(15)
    private BigDecimal totalMoney;

    /**
     * 会诊医疗机构
     */
    @ExcelProperty(index = 8, value = "会诊医疗机构")
    @ColumnWidth(20)
    private String consultationOrg;

    /**
     * 会诊医生
     */
    @ExcelProperty(index = 9, value = "会诊医生")
    @ColumnWidth(15)
    private String consultationUserName;

    /**
     * 调配要求
     */
    @ExcelProperty(index = 10, value = "调配要求")
    @ColumnWidth(20)
    private String adjustRequire;
}
