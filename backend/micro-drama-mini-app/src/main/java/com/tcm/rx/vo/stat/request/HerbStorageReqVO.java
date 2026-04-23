package com.tcm.rx.vo.stat.request;

import lombok.Data;

@Data
public class HerbStorageReqVO {
    private String startTime;

    private String endTime;

    private String purchaseNo;

    private Integer page;

    private Integer size;
}
