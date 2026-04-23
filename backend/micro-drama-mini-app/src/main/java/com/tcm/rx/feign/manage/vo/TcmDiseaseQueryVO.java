package com.tcm.rx.feign.manage.vo;

import com.tcm.common.vo.PageVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 中医疾病管理(TcmDisease)实体类
 *
 * @author duanqiyuan
 * @since 2025-09-02 18:28:40
 */
@Data
public class TcmDiseaseQueryVO extends PageVO implements Serializable {

    /**
     * 中医病名
     */
    private String name;

    private String idList;

    private String nameList;
    /**
     * 标准编码
     */
    private String nationCode;
    /**
     * 中医疾病助记码
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

