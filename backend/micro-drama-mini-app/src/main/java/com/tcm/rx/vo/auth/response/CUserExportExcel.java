package com.tcm.rx.vo.auth.response;

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
public class CUserExportExcel implements Serializable {

    private static final long serialVersionUID = -9014217915150119567L;

    /**
     * 序号
     */
    @ExcelProperty("序号")
    @ColumnWidth(20)
    private Integer rowNo;

    /**
     * 用户账号
     */
    @ExcelProperty("账号")
    @ColumnWidth(20)
    private String userAccount;

    /**
     * 真实姓名
     */
    @ExcelProperty("姓名")
    @ColumnWidth(20)
    private String realName;

    /**
     * 医疗机构的名称
     */
    @ExcelProperty("医疗机构")
    @ColumnWidth(20)
    private String hspName;

    /**
     * 科室名称（聚合数据，用","分隔）
     */
    @ExcelProperty("科室")
    @ColumnWidth(20)
    private String deptNames;

    /**
     * 角色名称（聚合数据，用","分隔）
     */
    @ExcelProperty("角色")
    @ColumnWidth(20)
    private String roleNames;

    /**
     * 状态(0:禁用,1:启用)
     */
    @ExcelProperty("状态")
    @ColumnWidth(20)
    private String statusName;

    /**
     * 是否接收会诊：0.否,1.是
     */
    @ExcelProperty("是否接收会诊")
    @ColumnWidth(20)
    private String consultName;

}
