package com.tcm.rx.vo.hsp.response;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 诊疗开方系统--医疗机构表
 * </p>
 *
 * @author xph
 * @since 2025-07-10
 */
@Data
public class HspImportExcel implements Serializable {

    private static final long serialVersionUID = 5515262290590442021L;

    /**
     * 医疗机构的名称
     */
    @ExcelProperty("*医疗机构名称")
    @ColumnWidth(20)
    private String hspName;

    /**
     * 医疗机构的编码
     */
    @ExcelProperty("*医疗机构编码")
    @ColumnWidth(20)
    private String hspCode;

    /**
     * his系统的名称
     */
    @ExcelProperty("*HIS系统")
    @ColumnWidth(20)
    private String hisSysName;

    /**
     * his医院的编码
     */
    @ExcelProperty("*HIS医院编码")
    @ColumnWidth(20)
    private String hisHspCode;

    /**
     * 手机号
     */
    @ExcelProperty("联系电话")
    @ColumnWidth(20)
    private String phone;

    /**
     * 地址
     */
    @ExcelProperty("地址")
    @ColumnWidth(20)
    private String address;

    /**
     * 是否接收会诊：0.否,1.是
     */
    @ExcelProperty("*是否接收会诊")
    @ColumnWidth(20)
    private String consultName;

    /**
     * 状态(0:禁用,1:启用)
     */
    @ExcelProperty("*是否启用")
    @ColumnWidth(20)
    private String statusName;

}
