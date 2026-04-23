package com.tcm.rx.vo.rx.response;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 诊疗开方系统--配伍禁忌管理表
 * </p>
 *
 * @author djbo
 * @since 2025-09-10
 */
@Data
public class CompatTabooMgmtVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 所属医联体ID
     */
    private Long medicalGroupId;

    /**
     * 所属医联体编码
     */
    private String medicalGroupCode;

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

    /**
     * 创建人（用户名/工号）
     */
    private String createBy;

    /**
     * 创建人（用户名/工号）
     */
    private String createByName;

    /**
     * 创建时间（自动填充当前时间）
     */
    private String createTimeStr;

    /**
     * 最后修改人（用户名/工号）
     */
    private String updateBy;

    /**
     * 最后修改人（用户名/工号）
     */
    private String updateByName;

    /**
     * 最后更新时间（修改时自动更新）
     */
    private String updateTimeStr;

    /**
     * 详情list
     */
    private List<CompatTabooMgmtDetailVO> detailList;
}
