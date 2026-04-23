package com.tcm.rx.entity.rx;

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
 * 诊疗开方系统--方剂管理明细表
 * </p>
 *
 * @author djbo
 * @since 2025-09-08
 */
@Data
@TableName("rx_rx_mgmt_detail")
public class RxMgmtDetail extends RxBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 明细ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 方剂主表关联ID
     */
    private Long rxMgmtId;

    /**
     * 序号
     */
    private Integer serialNum;

    /**
     * 饮片编码
     */
    private String herbCode;

    /**
     * 饮片名称
     */
    private String herbName;

    /**
     * 规格
     */
    private String spec;

    /**
     * 饮片类型
     */
    private String type;

    /**
     * 饮片用量
     */
    private BigDecimal herbDosage;

    /**
     * 单位
     */
    private String unit;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 单剂金额
     */
    private BigDecimal money;

    /**
     * 调配要求
     */
    private String adjustRequire;
}
