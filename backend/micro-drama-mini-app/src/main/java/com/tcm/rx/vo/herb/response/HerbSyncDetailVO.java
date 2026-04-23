package com.tcm.rx.vo.herb.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HerbSyncDetailVO {
    private String customerCode;

    private String customerHerbCode;

    private String customerHerbName;

    private String customerHerbSpec;

    private String customerHerbUnit;

    private BigDecimal customerHerbNumber;

    private BigDecimal costPrice;

    private BigDecimal retailPrice;
}
