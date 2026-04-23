package com.tcm.rx.vo.basicData.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class HerbVO {
    /**
     * 饮片主键ID
     */
    private Long id;

    /**
     * 饮片编码，用于系统内部识别的唯一编码
     */
    private String herbCode;

    /**
     * 饮片名称，中药饮片的名称
     */
    private String herbName;

    /**
     * 饮片规格，如片、克、袋等具体规格
     */
    private String spec;

    /**
     * 饮片类型，如植物类、动物类、矿物类等分类
     */
    private String type;

    /**
     * 单位，饮片的计量单元
     */
    private String unit;

    /**
     * 成本价，饮片的采购成本价格
     */
    private BigDecimal costPrice;

    /**
     * 毛利率，饮片销售的毛利率
     */
    private BigDecimal grossMargin;

    /**
     * 零售价，饮片的零售价格
     */
    private BigDecimal retailPrice;

    /**
     * 助记码，用于快速检索的拼音或缩写码
     */
    private String helpCode;

    /**
     * 医保类型，标识饮片是否属于医保范围及医保类型
     */
    private Integer medicalInsurance;

    /**
     * 状态，标识饮片的启用或停用状态
     */
    private String status;

    /**
     * 医保编码，医保系统中对应的编码
     */
    private String medicalCode;

    /**
     * 单位重量，单个包装或单位的重量
     */
    private String unitWeight;

    /**
     * 性味，中医药理论中的四气五味属性
     */
    private String propertyFlavor;

    /**
     * 归经，中医药理论中药物作用的脏腑经络定位
     */
    private String meridianTropism;

    /**
     * 主治功能，饮片的主要治疗功效
     */
    private String therapFunction;

    /**
     * 使用禁忌，饮片的使用注意事项和禁忌
     */
    private String usageForbidden;

    /**
     * 存储条件，饮片的保存条件要求
     */
    private String storageCondition;

    /**
     * HIS饮片ID，医院信息系统中的饮片标识
     */
    private String hisHerbId;

    /**
     * 所属医疗机构ID，标识饮片所属的医疗机构
     */
    private String hspIds;

    /**
     * 医疗机构的code
     */
    private String hspCodes;

    /**
     * 医疗机构名称多个逗号分割
     */
    private String hspNames;

    /**
     * 所属医联体的id
     */
    private Long medicalGroupId;

    /***
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date updateTime;
}