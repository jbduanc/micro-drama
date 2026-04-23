package com.tcm.rx.vo.rx.request;

import com.tcm.rx.entity.rx.CompatTabooMgmt;
import com.tcm.rx.entity.rx.CompatTabooMgmtDetail;
import com.tcm.rx.entity.rx.RxMgmt;
import com.tcm.rx.entity.rx.RxMgmtDetail;
import lombok.Data;

import java.util.List;

@Data
public class CompatTabooMgmtSaveVO {
    /**
     * 处方主表信息
     */
    private CompatTabooMgmt compatTabooMgmt;

    /**
     * 处方明细表信息
     */
    private List<CompatTabooMgmtDetail> detailList;
}
