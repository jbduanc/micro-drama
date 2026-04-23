package com.tcm.rx.vo.rx.request;

import lombok.Data;

import java.util.List;

@Data
public class CompatTabCheckReqVO {

    /**
     * 是否怀孕
     */
    private String isPregnant;

    /**
     * 饮片编码List
     */
    private List<String> herbCodeList;

}
