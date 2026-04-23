package com.tcm.rx.vo.patient.request;

import com.tcm.common.vo.PageVO;
import lombok.Data;

import java.util.List;

@Data
public class MedrecTplMgmtQueryVO extends PageVO {

    /**
     * 方剂ID
     */
    private Long id;

    /**
     * 医联体ID
     */
    private Long medicalGroupId;

    /**
     * 模版类型:0.通用,1.个人
     */
    private Integer tplType;

    /**
     * 归属人id
     */
    private Long ownerId;

    /**
     * 模版名称
     */
    private String tplName;

    /**
     * 状态:0.启用,1.禁用
     */
    private Integer status;
}
