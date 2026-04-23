package com.tcm.rx.vo.msg.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MsgUpdateVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Long> idList;

    /**
     * 操作类型 1-标记为已读 2-删除
     */
    private Integer optionType;

}
