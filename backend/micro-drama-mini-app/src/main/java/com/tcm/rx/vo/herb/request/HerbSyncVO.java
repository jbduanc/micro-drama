package com.tcm.rx.vo.herb.request;

import lombok.Data;

@Data
public class HerbSyncVO {
    private String startTime;

    private String endTime;

    /**
     * 采购单号
     */
    private String purchaseNo;

    private Integer page;

    private Integer size;


}
