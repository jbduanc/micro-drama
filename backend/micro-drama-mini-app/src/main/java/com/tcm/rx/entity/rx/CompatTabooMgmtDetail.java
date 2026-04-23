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
 * 诊疗开方系统--配伍禁忌管理明细表
 * </p>
 *
 * @author djbo
 * @since 2025-09-10
 */
@Data
@TableName("rx_compat_taboo_mgmt_detail")
public class CompatTabooMgmtDetail extends RxBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 明细ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 配伍禁忌关联ID
     */
    private Long tabooMgmtId;

    /**
     * 序号
     */
    private Integer serialNum;

    /**
     * 饮片编码
     */
    private String herbCode;

    /**
     * 饮片名称
     */
    private String herbName;

    /**
     * 规格
     */
    private String spec;

    /**
     * 饮片类型
     */
    private String type;
}
