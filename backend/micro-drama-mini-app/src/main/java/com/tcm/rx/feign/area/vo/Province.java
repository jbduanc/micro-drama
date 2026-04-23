package com.tcm.rx.feign.area.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_province")
public class Province {
    private Long id;
    private String code;
    private String name;
}
