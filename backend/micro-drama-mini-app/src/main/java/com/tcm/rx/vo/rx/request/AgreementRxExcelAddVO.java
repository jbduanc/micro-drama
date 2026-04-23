package com.tcm.rx.vo.rx.request;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 诊疗开方系统--协定方excel
 * </p>
 *
 * @author xph
 * @since 2025-07-10
 */
@Data
public class AgreementRxExcelAddVO implements Serializable {

    private static final long serialVersionUID = -4026001812129319630L;

    /**
     * false 医生协定方管理， true 个人协定方管理
     */
    private Boolean isCurrDoctor = false;

    /**
     * 子项（新增项）
     */
   private List<AgreementRxExcel> list;


}
