package com.tcm.rx.vo.stat.response;

import lombok.Data;

@Data
public class HerbStorageVO {
    /**
     * 业务日期
     */
    private String businessDate;

    /**
     * 单据编号
     */
    private String purchaseNo;

    /**
     * 诊疗系统饮片同步状态
     */
    private Integer zlSyncStatus;

    /**
     * 煎药系统饮片同步状态
     */
    private Integer jySyncStatus;

    /**
     * 煎药系统库存同步状态
     */
    private Integer jyInventorySyncStatus;

}
