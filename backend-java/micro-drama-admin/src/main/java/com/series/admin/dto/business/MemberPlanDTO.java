package com.series.admin.dto.business;

import com.series.common.vo.PageVO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 会员套餐（对应 user_db.member_plan）
 */
@Data
public class MemberPlanDTO extends PageVO {

    private String id;
    private String planName;
    private BigDecimal price;
    private Integer durationDays;
    private Integer status;
}
