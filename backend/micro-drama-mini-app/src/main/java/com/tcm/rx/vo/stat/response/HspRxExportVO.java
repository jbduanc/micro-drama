package com.tcm.rx.vo.stat.response;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;

/**
 * 医院数据统计导出VO
 */
@Data
public class HspRxExportVO implements Serializable {

    private static final long serialVersionUID = -4026001812129319630L;

    /**
     * 医疗机构Id
     */
    @ExcelIgnore
    private Long hspId;

    /**
     * 医疗机构名称
     */
    @ExcelProperty(value = "医疗机构名称")
    @ColumnWidth(20)
    private String hspName;

    /**
     * 接诊次数
     */
    @ExcelProperty(value = "接诊次数")
    @ColumnWidth(15)
    private Integer visitNum;

    /**
     * 发起的会诊
     */
    @ExcelProperty(value = "发起的会诊")
    @ColumnWidth(15)
    private Integer consultationNum;

    /**
     * 开方次数
     */
    @ExcelProperty(value = "开方次数")
    @ColumnWidth(15)
    private Integer rxNum;

    /**
     * 执行处方次数
     */
    @ExcelProperty(value = "执行处方次数")
    @ColumnWidth(15)
    private Integer execRxNum;

    /**
     * 执行处方饮片金额（元）
     */
    @ExcelProperty(value = "执行处方饮片金额（元）")
    @ColumnWidth(20)
    private Double execRxDrugCost;

    /**
     * 执行处方加工费（元）
     */
    @ExcelProperty(value = "执行处方加工费（元）")
    @ColumnWidth(20)
    private Double execRxProcessingFee;

    /**
     * 执行处方适宜技术费（元）
     */
    @ExcelProperty(value = "执行处方适宜技术费（元）")
    @ColumnWidth(20)
    private Double execRxShiyiJishuFee;
}
