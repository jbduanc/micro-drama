package com.tcm.rx.vo.stat.request;

import java.util.List;
import com.tcm.common.vo.PageVO;
import lombok.Data;

import java.io.Serializable;

@Data
public class StatRequestVO extends PageVO implements Serializable {

    private static final long serialVersionUID = -4026001812129319630L;

    /**
     * 所属医联体的id
     */
    private Long medicalGroupId;

    /**
     * 开始时间
     * 格式 yyyy-MM-dd
     */
    private String startDate;

    /**
     * 结束时间
     * 格式 yyyy-MM-dd
     */
    private String endDate;

    /**
     * 医疗机构Id
     */
    private Long hspId;

    /**
     * 饮片名称
     */
    private String herbName;

    /**
     * 饮片编码
     */
    private String herbCode;

    /**
     * 适宜技术名称
     */
    private String chargeName;

    /**
     * 接诊医生
     */
    private String prescriberName;

    /**
     * 会诊医生
     */
    private String consultationUserName;

    /**
     * 状态
     */
    private List<Integer> statusList;

    /**
     * 患者姓名
     */
    private String patientName;


}
