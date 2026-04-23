package com.tcm.rx.entity.msg;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.tcm.common.entity.RxBaseEntity;
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
@TableName("rx_msg")
public class Msg extends RxBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 类型 1-接收会诊 2-会诊结果
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
}
