package com.series.admin.entity.business;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 会员套餐表
 */
@Data
@TableName("t_member_plans")
public class MemberPlans {

    /**
     * 会员套餐ID(主键)
     */
    @TableId(type = IdType.AUTO)
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

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date createTime;
}
