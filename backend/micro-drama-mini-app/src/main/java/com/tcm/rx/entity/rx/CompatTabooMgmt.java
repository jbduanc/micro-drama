package com.tcm.rx.entity.rx;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.tcm.common.entity.BaseEntity;
import com.tcm.common.entity.RxBaseEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 诊疗开方系统--配伍禁忌管理表
 * </p>
 *
 * @author djbo
 * @since 2025-09-10
 */
@Data
@TableName("rx_compat_taboo_mgmt")
public class CompatTabooMgmt extends RxBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 禁忌编码
     */
    private String tabooCode;

    /**
     * 禁忌名称
     */
    private String tabooName;

    /**
     * 禁忌类型
     */
    private String tabooType;

    /**
     * 是否怀孕
     */
    private String isPregnant;
}
