package com.tcm.rx.vo.auth.request;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 诊疗开方系统--用户表
 * </p>
 *
 * @author xph
 * @since 2025-07-07
 */
@Data
public class CUserImportExcel implements Serializable {

    private static final long serialVersionUID = -5990391840616526540L;

    /**
     * 用户账号
     */
    @ExcelProperty("*账号")
    @ColumnWidth(20)
    private String userAccount;

    /**
     * 真实姓名
     */
    @ExcelProperty("*姓名")
    @ColumnWidth(20)
    private String realName;

    /**
     * 手机号
     */
    @ExcelProperty("手机号")
    @ColumnWidth(20)
    private String phone;

    /**
     * his医生编码
     */
    @ExcelProperty("HIS医生编码")
    @ColumnWidth(20)
    private String hisDoctorCode;

    /**
     * 医疗机构的名称
     */
    @ExcelProperty("*医疗机构")
    @ColumnWidth(20)
    private String hspName;

    /**
     * 科室名称（聚合数据，用","分隔）
     */
    @ExcelProperty("*科室")
    @ColumnWidth(20)
    private String deptNames;

    /**
     * 角色名称（聚合数据，用","分隔）
     */
    @ExcelProperty("*角色")
    @ColumnWidth(20)
    private String roleNames;

    /**
     * 是否接收会诊：0.否,1.是
     */
    @ExcelProperty("*是否接收会诊")
    @ColumnWidth(20)
    private String consultName;

}
