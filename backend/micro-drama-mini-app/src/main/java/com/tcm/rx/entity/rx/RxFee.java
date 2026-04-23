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
 * 诊疗开方系统--处方费用表
 * </p>
 *
 * @author shouhan
 * @since 2025-07-21
 */
@Data
@TableName("rx_rx_fee")
public class RxFee extends RxBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 费用ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 处方ID
     */
    private Long rxId;

    /**
     * 收费项目编码
     */
    private String chargeItemCode;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 金额
     */
    private BigDecimal amount;
}
