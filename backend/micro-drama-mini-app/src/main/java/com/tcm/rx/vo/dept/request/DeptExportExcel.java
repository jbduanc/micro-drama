package com.tcm.rx.vo.dept.request;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 诊疗开方系统--科室表
 * </p>
 *
 * @author xph
 * @since 2025-07-10
 */
@Data
public class DeptExportExcel implements Serializable {

    private static final long serialVersionUID = -4026001812129319630L;

    /**
     * 序号
     */
    @ExcelProperty("序号")
    @ColumnWidth(20)
    private Integer rowNo;

    /**
     * 科室的名称
     */
    @ExcelProperty("科室名称")
    @ColumnWidth(20)
    private String deptName;

    /**
     * 医疗机构的名称
     */
    @ExcelProperty("所属机构")
    @ColumnWidth(20)
    private String hspName;

    /**
     * 科室的code
     */
    @ExcelProperty("科室编码")
    @ColumnWidth(20)
    private String deptCode;

    /**
     * 排序
     */
    @ExcelProperty("排序")
    @ColumnWidth(20)
    private Integer sort;

    /**
     * 科室名称首拼
     */
    @ExcelProperty("科室名称首拼")
    @ColumnWidth(20)
    private String deptNameInitial;

    /**
     * 状态(0:禁用,1:启用)
     */
    @ExcelProperty("是否启用")
    @ColumnWidth(20)
    private String statusName;

}
