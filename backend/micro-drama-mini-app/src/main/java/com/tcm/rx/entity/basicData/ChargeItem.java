package com.tcm.rx.entity.basicData;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.tcm.common.entity.RxBaseEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 诊疗开方系统--收费项目管理表
 * </p>
 *
 * @author shouhan
 * @since 2025-07-18
 */
@TableName("rx_charge_item")
@Data
public class ChargeItem extends RxBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 项目编码
     */
    private String itemCode;

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 项目类型 0-适宜技术 1-煎药费
     */
    private Integer itemType;

    /**
     * 计价单位（如 剂、次、次）
     */
    private String priceUnit;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 状态 0-启用 1-禁用
     */
    private Integer status;

    /**
     * 适应证（选填，业务描述）
     */
    private String adaptation;

    /**
     * 项目说明（支持富文本，如图片/表格）
     */
    private String description;
}
