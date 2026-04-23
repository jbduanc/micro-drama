package com.tcm.rx.vo.rx.response;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tcm.common.entity.RxBaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 诊疗开方系统--处方费用表
 * </p>
 *
 * @author shouhan
 * @since 2025-07-21
 */
@Data
public class RxFeeVO {

    private static final long serialVersionUID = 1L;

    /**
     * 费用ID
     */
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
     * 项目名称
     */
    private String itemName;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 计价单位（如：剂、次、盒等）
     */
    private String priceUnit;

    /**
     * 单价，使用BigDecimal保证精度
     */
    private BigDecimal unitPrice;

}
