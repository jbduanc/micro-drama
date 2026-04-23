package com.tcm.rx.entity.basicData;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 诊疗开方系统--加工费管理表(ProcessingFee)实体类
 *
 * @author duanqiyuan
 * @since 2025-07-17 15:09:46
 */
@Data
@TableName("rx_processing_fee")
public class ProcessingFee {

    private static final long serialVersionUID = 890865583497599611L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属医联体的id
     */
    private Long medicalGroupId;

    /**
     * 所属医联体的code
     */
    private String medicalGroupCode;

    /**
     * 煎药要求
     */
    private String decoctRequire;

    /**
     * 计价方式
     */
    private String pricingMethod;

    /**
     * 加工费（元）
     */
    private Double fee;

    /**
     * 对应收费项目编码（关联收费项目表）
     */
    private String chargeItemCode;

    /**
     * 是否删除:0.未删除，1.已删除
     */
    private Integer delFlag;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

}

