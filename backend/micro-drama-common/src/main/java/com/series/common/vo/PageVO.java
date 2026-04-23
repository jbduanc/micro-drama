package com.series.common.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 处方表实体类
 */
@Data
public class PageVO implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 分页号
     */
    private Integer page;
    
    /**
     * 每页条目数
     */
    private Integer size;
}