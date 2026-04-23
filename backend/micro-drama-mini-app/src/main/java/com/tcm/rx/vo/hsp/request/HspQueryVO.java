package com.tcm.rx.vo.hsp.request;

import com.tcm.common.vo.PageVO;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 诊疗开方系统--医疗机构表
 * </p>
 *
 * @author xph
 * @since 2025-07-10
 */
@Data
public class HspQueryVO extends PageVO implements Serializable {

    private static final long serialVersionUID = -3013118241318225923L;

    /**
     * 主键id
     */
    private Long id;

    /**
     * 所属医联体的id
     */
    private Long medicalGroupId;

    /**
     * 所属医联体的code
     */
    private String medicalGroupCode;

    /**
     * 医疗机构的名称
     */
    private String hspName;

    /**
     * 医疗机构的code
     */
    private String hspCode;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

}
