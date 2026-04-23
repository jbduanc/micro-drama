package com.tcm.rx.vo.rx.request;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 诊疗开方系统--协定方excel
 * </p>
 *
 * @author xph
 * @since 2025-07-10
 */
@Data
public class AgreementRxCurrDoctorExcel implements Serializable {

    private static final long serialVersionUID = -4026001812129319630L;


    /**
     * 处方名称
     */
    @ExcelProperty("方剂名称*")
    @ColumnWidth(20)
    private String rxName;

    /**
     * 主要功效
     */
    @ExcelProperty("主要功效")
    @ColumnWidth(50)
    private String mainEfficacy;

    /**
     * 适应症
     */
    @ExcelProperty("适应症")
    @ColumnWidth(50)
    private String adaptation;


    /**
     * 用法
     */
    @ExcelProperty("用法")
    @ColumnWidth(40)
    private String usage;


    /**
     * 饮片编码
     */
    @ExcelProperty("饮片编码*")
    @ColumnWidth(20)
    private String herbCode;

    /**
     * 饮片名称
     */
    @ExcelProperty("饮片名称*")
    @ColumnWidth(20)
    private String herbName;

    /**
     * 饮片用量
     */
    @ExcelProperty("饮片用量*")
    @ColumnWidth(20)
    private BigDecimal herbDosage;

    /**
     * 剂数
     */
    @ExcelProperty("剂数")
    @ColumnWidth(20)
    private Integer doseCount;

    /**
     * 一剂/包
     */
    @ExcelProperty("每剂包数")
    @ColumnWidth(20)
    private Integer oneDose;

    /**
     * 每包/ml
     */
    @ExcelProperty("每包装量")
    @ColumnWidth(20)
    private Integer perPackVolume;

    /**
     * 用药频次
     */
    @ExcelProperty("用药频次")
    @ColumnWidth(20)
    private String dosageFrequency;

    /**
     * 煎药要求
     */
    @ExcelProperty("煎药要求")
    @ColumnWidth(20)
    private String decoctRequire;

    /**
     * 校验数据错误的提示
     */
    @ExcelIgnore
    private String errMsg;


}
