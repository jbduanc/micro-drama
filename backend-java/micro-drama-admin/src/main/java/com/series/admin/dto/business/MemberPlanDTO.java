package com.series.admin.dto.business;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.series.common.vo.PageVO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员套餐表
 */
@Data
public class MemberPlanDTO extends PageVO {

    /**
     * 会员套餐ID(主键)
     */
    private Long planId;

    /**
     * 会员套餐名称(月卡/季卡/年卡)
     */
    private String planName;

    /**
     * 套餐价格(单位：TON)
     */
    private BigDecimal price;

    /**
     * 套餐有效天数
     */
    private Integer durationDays;

    /**
     * 套餐状态(1-正常 0-禁用)
     */
    private Integer status;
}
