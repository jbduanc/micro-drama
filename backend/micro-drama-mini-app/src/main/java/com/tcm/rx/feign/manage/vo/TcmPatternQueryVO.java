package com.tcm.rx.feign.manage.vo;

import com.tcm.common.vo.PageVO;
import lombok.Data;
import java.util.List;

import java.io.Serializable;

/**
 * 中医证候管理(TcmPattern)实体类
 *
 * @author duanqiyuan
 * @since 2025-09-02 18:29:38
 */
@Data
public class TcmPatternQueryVO extends PageVO implements Serializable {

    /**
     * 中医证候
     */
    private String name;

    private String idList;

    private String nameList;
    /**
     * 标准编码
     */
    private String nationCode;
    /**
     * 中医证候助记码
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

