package com.tcm.rx.vo.cons.request;

import lombok.Data;

@Data
public class EndConsVO {
    /**
     * 会诊ID（必填）
     */
    private Long id;

    /**
     * 会诊结果（必填，如“审核通过”“调整处方：减少天麻用量至5g”）
     */
    private String consResult;
}
