package com.tcm.rx.feign.customer.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

@Data
public class CustomerVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 客户id
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
     * 所属煎药中心的id
     */
    private Long decoctId;

    /**
     * 所属煎药中心的code
     */
    private String decoctCode;

    /**
     * 客户的名称
     */
    private String customerName;

    /**
     * 客户的简称
     */
    private String customerNameShort;

    /**
     * 客户的code
     */
    private String customerCode;

    /**
     * 客户的助记码
     */
    private String helpCode;

    /**
     * 客户的类型：三甲医院、民营医院、二甲医院、二甲以下、中医馆、社区、门诊、单体药店、连锁药店、卫生院、其他
     */
    private String customerType;

    /**
     * 配方间的id
     */
    private Long formulaRoomId;

    /**
     * 配方间的code
     */
    private String formulaRoomCode;

    /**
     * 配方间的名称
     */
    private String formulaRoomName;

    /**
     * 开户日期
     */
    private LocalDate openDate;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 地址
     */
    private String address;

    /**
     *  是否客户：0.否,1.是(表示基层医院)
     */
    private Integer isCustomer;

    /**
     *  是否供应商：0.否,1.是(表示供应商)
     */
    private Integer isSupplier;

    /**
     * 客户校验码
     */
    private String customerCheckCode;

    /**
     * 客户内码
     */
    private String customerInternalCode;

    /**
     * 煎药方案的id
     */
    private Long decoctRecipeId;

    /**
     * 煎药方案的名称
     */
    private String decoctRecipeName;

    /**
     *  是否中心医院：0.否,1.是(表示煎药中心的实体医院)
     */
    private Integer isCentralHsp;

    /**
     *  煎药次数：1.一煎、2.二煎、3.三煎
     */
    private String decoctNumber;

    /**
     * 医疗机构代码
     */
    private String medicalInstitutionCode;

    /**
     *  默认配送方式：1.送医院、2.快递、3.同城速递
     */
    private Integer distributionMode;

    /**
     * 状态(0:禁用,1:启用,2:锁定)
     */
    private Integer status;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private String errMsg;
}
