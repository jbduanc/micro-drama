package com.tcm.rx.feign.area.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_district")
public class District {
    private Long id;
    private String code;
    private String name;
    private Long cityId;
}
