package com.tcm.rx.vo.basicData.request;

import com.tcm.common.vo.PageVO;
import lombok.Data;

import java.util.List;

@Data
public class HerbQueryVO extends PageVO {
    /**
     * 饮片编码
     */
    private String herbCode;

    /**
     * 饮片名称
     */
    private String herbName;

    /**
     * 饮片名称list
     */
    private List<String> herbNameList;

    /**
     * 饮片类型
     */
    private String type;

    /**
     * 状态（0-启用 1-禁用）
     */
    private Boolean status;

    /**
     * 医保类型（0-是 1-否 2-单独使用不予支付）
     */
    private Integer medicalInsurance;

    /**
     * 所属医疗机构ID
     */
    private String hspId;

    /**
     * 饮片机构id
     */
    private String herbHspId;

    /**
     * 医联体ID
     */
    private Long medicalGroupId;

    /**
     * 所属医疗机构code
     */
    private String hspCode;

    /**
     * 关键字搜索
     */
    private String keywords;

}
