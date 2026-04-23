package com.series.admin.entity.business;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * <p>
 * 短剧主表
 * </p>
 *
 * @author djbo
 * @since 2026-04-15
 */
@Data
@TableName("t_micro_dramas")
public class MicroDramas extends Model<MicroDramas> {

    private static final long serialVersionUID = 1L;

    /**
     * 短剧ID(主键)
     */
    @TableId(type = IdType.AUTO)
    private Long dramaId;

    /**
     * 短剧名称
     */
    private String title;

    /**
     * 短剧封面图片地址
     */
    private String coverUrl;

    /**
     * 短剧简介
     */
    private String description;

    /**
     * 短剧总集数
     */
    private Integer totalEpisodes;

    /**
     * 单剧订阅价格(单位：TON)
     */
    private BigDecimal singleDramaPrice;

    /**
     * 短剧状态(0-下架 1-上架)
     */
    private Integer status;

    /**
     * 排序权重(数值越大越靠前)
     */
    private Integer sort;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date updateTime;
}
