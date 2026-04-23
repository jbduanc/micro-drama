package com.tcm.rx.vo.rx.request;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RxMgmtImportVO implements Serializable {

    private static final long serialVersionUID = -4026001812129319630L;

    /**
     * 处方类型
     */
    private Integer type;

    /**
     * 处方list
     */
    private List<JSONObject> list;
}
