package com.tcm.rx.feign.manage.vo;

import com.tcm.common.vo.PageVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 西医疾病管理(Diagnosis)实体类
 *
 * @author duanqiyuan
 * @since 2025-09-02 18:29:10
 */
@Data
public class DiagnosisQueryVO extends PageVO implements Serializable {

    /**
     * 西医病名
     */
    private String name;

    /**
     * 西医id批量查询
     */
    private String idList;

    /**
     * 西医名称批量查询
     */
    private String nameList;

    /**
     * 国际编码
     */
    private String nationCode;
    /**
     * 西医疾病助记码
     */
    private String helpCode;

    /**
     * 类别名称
     */
    private String categoryName;
    /**
     * 0 启用 1 禁用
     */
    private Integer status;
}

