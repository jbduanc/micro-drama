package com.tcm.rx.vo.rx.request;

import cn.hutool.core.util.ObjectUtil;
import com.tcm.common.vo.PageVO;
import lombok.Data;

import java.util.List;

@Data
public class RxQueryVO extends PageVO {

    /**
     * 处方ID
     */
    private String id;

    /**
     * 医联体id
     */
    private Long medicalGroupId;

    /**
     * 状态 0-待审核 1-待缴费 2-已缴费 3-已上传 4-已执行 5-已作废 6-已退费
     */
    private List<Integer> statusList;

    /**
     * 处方来源
     */
    private List<Integer> sourceList;

    /**
     * 医疗机构的id
     */
    private Long hspId;

    /**
     * 医疗机构的名称
     */
    private String hspName;

    /**
     * 患者id
     */
    private String patientId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 西医诊断
     */
    private String diagnosis;

    /**
     * 中医病名
     */
    private String tcmDisease;

    /**
     * 中医证候
     */
    private String tcmPattern;

    /**
     * 西医疾病IdList
     */
    private List<Long> diagnosisIdList;

    /**
     * 中医疾病IdList
     */
    private List<Long> tcmDiseaseIdList;

    /**
     * 中医证候IdList
     */
    private List<Long> tcmPatternIdList;

    /**
     * 开方人id
     */
    private Long prescriberId;

    /**
     * 开方人名称
     */
    private String prescriberName;

    /**
     * 开方时间 yyyy-MM-dd
     */
    private String prescriptionTime;

    /**
     * 上传时间 yyyy-MM-dd
     */
    private String uploadTime;

    /**
     * 执行时间 yyyy-MM-dd
     */
    private String executionTime;

    /**
     * 0-开方时间 1-上传时间 2-执行时间
     */
    private Integer dateType;

    /**
     * yyyy-MM-dd
     */
    private String startDate;

    /**
     * yyyy-MM-dd
     */
    private String endDate;

    public String getStartDate() {
        if (ObjectUtil.isEmpty(startDate)) {
            return "";
        }
        return startDate.concat(" 00:00:00");
    }

    public String getEndDate() {
        if (ObjectUtil.isEmpty(endDate)) {
            return "";
        }
        return endDate.concat(" 23:59:59");
    }
}
