package com.tcm.rx.vo.rx.request;

import com.tcm.rx.entity.rx.*;
import lombok.Data;

import java.util.List;

@Data
public class RxMgmtSaveVO {
    /**
     * 处方主表信息
     */
    private RxMgmt rxMgmt;

    /**
     * 处方明细表信息
     */
    private List<RxMgmtDetail> rxMgmtDetails;
}
