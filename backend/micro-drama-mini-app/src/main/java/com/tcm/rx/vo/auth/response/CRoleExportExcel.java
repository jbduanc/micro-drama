package com.tcm.rx.vo.auth.response;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 诊疗开方系统--角色表
 * </p>
 *
 * @author xph
 * @since 2025-07-08
 */
@Data
public class CRoleExportExcel implements Serializable {

    private static final long serialVersionUID = -8126775455633166451L;

    /**
     * 序号
     */
    @ExcelProperty("序号")
    @ColumnWidth(20)
    private Integer rowNo;

    /**
     * 角色名称
     */
    @ExcelProperty("角色名称")
    @ColumnWidth(20)
    private String roleName;

    /**
     * 角色编码/权限字符
     */
    @ExcelProperty("权限字符")
    @ColumnWidth(20)
    private String roleCode;

    /**
     * 排序
     */
    @ExcelProperty("显示顺序")
    @ColumnWidth(20)
    private Integer sort;

    /**
     * 状态(0:禁用,1:启用)
     */
    @ExcelProperty("状态")
    @ColumnWidth(20)
    private String statusName;

    /**
     * 创建时间
     */
    @ExcelProperty("创建时间")
    @ColumnWidth(20)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date createTime;

}
