package com.tcm.rx.vo.msg.request;

import com.tcm.common.vo.PageVO;
import lombok.Data;

import java.io.Serializable;

@Data
public class MsgQueryVO extends PageVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 医联体ID
     */
    private Long medicalGroupId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息时间 yyyy-MM-dd
     */
    private String msgTime;
}
