package com.tcm.rx.vo.rx.request;

import lombok.Data;

@Data
public class RxStatusVO {

    /**
     * 处方ID
     */
    private String id;

    /**
     * 状态 0-待审核 1-待缴费 2-已缴费 3-已上传 4-已执行 5-已作废 6-已退费
     */
    private Integer status;

    /**
     * 审核意见
     */
    private String auditComments;
}
