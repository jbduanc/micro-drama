package com.tcm.rx.entity.rx;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.math.BigDecimal;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

import static com.baomidou.mybatisplus.annotation.FieldStrategy.IGNORED;

/**
 * <p>
 * 诊疗开方系统--协定方主表
 * </p>
 *
 * @author shouhan
 * @since 2025-07-17
 */
@Data
@TableName("rx_agreement_rx")
public class AgreementRx extends Model<AgreementRx> {

    private static final long serialVersionUID = 1L;

    /**
     * 协定方ID
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
     * 医疗机构的id
     */
    private Long hspId;

    /**
     * 医疗机构的code
     */
    private String hspCode;

    /**
     * 医疗机构名称
     */
    private String hspName;

    /**
     * 处方名称
     */
    private String rxName;

    /**
     * 协定方类型 0-个人 1-通用 2-科室
     */
    private Integer type;

    /**
     * 科室ID
     */
    private String deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 归属人账号
     */
    @TableField(updateStrategy = IGNORED)
    private Long ownerAccount;

    /**
     * 归属人账号名称
     */
    private String ownerAccountName;

    /**
     * 方剂组成
     */
    private String formulaComposition;

    /**
     * 状态 0-启用 1-禁用
     */
    private Integer status;

    /**
     * 剂数
     */
    private Integer doseCount;

    /**
     * 主要功效
     */
    private String mainEfficacy;

    /**
     * 适应症
     */
    private String adaptation;

    /**
     * 处方总金额
     */
    private BigDecimal totalMoney;

    /**
     * 单剂总金额
     */
    private BigDecimal doseTotalMoney;

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
    private Date createTime;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;
}
