package com.tcm.rx.vo.herb.response;

import lombok.Data;

import java.util.List;

@Data
public class HerbSyncDataVO {
    /**
     * 业务日期
     */
    private String businessDate;

    /**
     * 单号
     */
    private String purchaseNo;

    /**
     * 同步状态
     */
    private Integer syncStatus;

    /**
     * 推送时间
     */
    private String pushDate;

    List<HerbSyncDetailVO> herbSyncDetailVOList;
}
