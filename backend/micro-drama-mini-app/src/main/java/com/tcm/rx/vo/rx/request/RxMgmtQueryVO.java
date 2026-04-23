package com.tcm.rx.vo.rx.request;

import com.tcm.common.vo.PageVO;
import lombok.Data;

import java.util.List;

@Data
public class RxMgmtQueryVO extends PageVO {

    /**
     * 方剂ID
     */
    private Long id;

    /**
     * 医联体ID
     */
    private Long medicalGroupId;

    /**
     * 方剂类型:1.经古名方,2.专病专方,3.辩证实施
     */
    private Integer type;

    /**
     * 方剂名称
     */
    private String name;

    /**
     * 方剂组成 多个饮片逗号分割
     */
    private String formulas;

    /**
     * 方剂组成 多个饮片逗号分割
     */
    private String formula;

    /**
     * 状态:0.禁用,1.启用
     */
    private Integer status;

    /**
     * 治疗疾病
     */
    private String treatmentDisease;

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
}
