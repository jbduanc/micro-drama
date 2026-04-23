package com.tcm.rx.feign.herb.herbCustomer.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class HerbCustomer implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
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
     * 所属煎药中心id
     */
    private Long decoctId;

    /**
     * 所属煎药中心的code
     */
    private String decoctCode;

    /**
     * 客户id
     */
    private Long customerId;

    /**
     * 客户编码
     */
    private String customerCode;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 客户商品编号
     */
    private String customerGoodsCode;

    /**
     * 客户计量单位
     */
    private String customerUnit;

    /**
     * 客户商品名称
     */
    private String customerGoodsName;

    /**
     * 助记码
     */
    private String helpCode;

    /**
     * 单剂下限量
     */
    private Integer singleDoseMin;

    /**
     * 饮片编码
     */
    private String herbCode;

    /**
     * 饮片名称
     */
    private String herbName;

    /**
     * 饮片计量单位
     */
    private String herbUnit;

    /**
     * 商品规格
     */
    private String goodsSpec;

    /**
     * 换算系数
     */
    private BigDecimal conversionFactor;

    /**
     * 单剂上限量
     */
    private Integer singleDoseMax;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 修改人
     */
    private String updateBy;

    @TableField(exist = false)
    private String errMsg;
}
