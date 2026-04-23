package com.tcm.rx.vo.rx.request;

import com.tcm.rx.entity.rx.Rx;
import com.tcm.rx.entity.rx.RxDetail;
import com.tcm.rx.entity.rx.RxFee;
import lombok.Data;

import java.util.List;

@Data
public class RxSaveVO {
    /**
     * 处方主表信息
     */
    private Rx rx;

    /**
     * 处方明细表信息
     */
    private List<RxDetail> rxDetails;

    /**
     * 处方费用表信息
     */
    private List<RxFee> rxFees;

    /**
     * 会诊ID
     */
    private Long consId;
}
