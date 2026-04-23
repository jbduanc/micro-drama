package com.tcm.rx.vo.rx.request;

import com.tcm.common.vo.PageVO;
import lombok.Data;

import java.util.List;

@Data
public class CompatTabooMgmtQueryVO extends PageVO {

    /**
     * 方剂ID
     */
    private Long id;

    /**
     * 医联体ID
     */
    private Long medicalGroupId;

    /**
     * 禁忌编码
     */
    private String tabooCode;

    /**
     * 禁忌名称
     */
    private String tabooName;

    /**
     * 禁忌类型
     */
    private String tabooType;

}
