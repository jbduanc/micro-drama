package com.tcm.rx.vo.msg.response;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 诊疗开方系统--消息表
 * </p>
 *
 * @author djbo
 * @since 2025-09-11
 */
@Data
public class MsgVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
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
     * 类型
     */
    private String type;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 业务id
     */
    private Long businessId;

    /**
     * 标题
     */
    private String title;

    /**
     * 状态：0-未读，1-已读
     */
    private Integer status;

    /**
     * 创建时间
     */
    private String createTimeStr;

    /**
     * 更新时间
     */
    private String updateTimeStr;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建人Name
     */
    private String createByName;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 修改人Name
     */
    private String updateByName;
}
