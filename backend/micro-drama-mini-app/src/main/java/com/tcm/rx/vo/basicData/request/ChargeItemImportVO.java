package com.tcm.rx.vo.basicData.request;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 收费项目导入数据模型（对应Excel导入模板）
 */
@Data
public class ChargeItemImportVO {

    /**
     * 项目编码（必填，对应模板"项目编码"列）
     */
    @ExcelProperty(value = "项目编码", index = 0)
    private String itemCode;

    /**
     * 项目名称（必填，对应模板"项目名称"列）
     */
    @ExcelProperty(value = "项目名称", index = 1)
    private String itemName;

    /**
     * 项目类型（必填，对应模板"项目类型"列，值为"适宜技术"或"煎药费"）
     */
    @ExcelProperty(value = "项目类型", index = 2)
    private String itemType;

    /**
     * 计价单位（必填，对应模板"计价单位"列，如"次""剂"）
     */
    @ExcelProperty(value = "计价单位", index = 3)
    private String priceUnit;

    /**
     * 单价（必填，对应模板"单价"列，字符串格式，导入时转BigDecimal）
     */
    @ExcelProperty(value = "单价", index = 4)
    private String unitPrice;

    /**
     * 状态（对应模板隐含的"状态"列，值为"启用"或"禁用"，必填）
     * 注：若模板中无显式列，可调整index或通过业务默认值处理
     */
    @ExcelProperty(value = "状态", index = 5)
    private String status;

    /**
     * 适应证（选填，对应模板外的扩展列）
     */
    @ExcelProperty(value = "适应证", index = 6)
    private String adaptation;

    /**
     * 项目说明（选填，对应模板"项目说明"列）
     */
    @ExcelProperty(value = "项目说明", index = 7)
    private String description;

    /**
     * 校验数据错误的提示
     */
    @ExcelIgnore
    private String errMsg;
}
