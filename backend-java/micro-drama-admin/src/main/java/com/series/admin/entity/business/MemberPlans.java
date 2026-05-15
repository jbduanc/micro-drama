package com.series.admin.entity.business;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 会员套餐（表名 member_plan）。
 * 若运行时报 relation "member_plan" does not exist，请在当前数据源库执行 {@code src/main/resources/db/member_plan.sql}。
 */
@Data
@TableName("member_plan")
public class MemberPlans {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("plan_name")
    private String planName;

    private BigDecimal price;

    @TableField("duration_days")
    private Integer durationDays;

    private Integer status;

    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
