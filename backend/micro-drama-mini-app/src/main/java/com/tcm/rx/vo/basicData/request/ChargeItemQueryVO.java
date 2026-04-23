package com.tcm.rx.vo.basicData.request;

import com.tcm.common.vo.PageVO;
import lombok.Data;

/**
 * 收费项目查询值对象，用于封装查询收费项目列表时的筛选条件和分页信息。
 */
@Data
public class ChargeItemQueryVO extends PageVO {
    /**
     * 医联体id
     */
    private Long medicalGroupId;
    /**
     * 项目名称，用于模糊查询收费项目。
     */
    private String itemName;

    /**
     * 项目类型，可选值：0-适宜技术，1-煎药费。
     */
    private Integer itemType;

    /**
     * 状态，可选值：0-禁用，1-启用。
     */
    private Integer status;
}