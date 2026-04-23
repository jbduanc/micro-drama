package com.tcm.rx.vo.cons.request;

import com.tcm.common.vo.PageVO;
import lombok.Data;

@Data
public class ConsQueryVO extends PageVO {

    /**
     * 会诊ID
     */
    private Long consId;

    /**
     * 患者姓名（模糊查询）
     */
    private String patientName;

    /**
     * 会诊状态（0-草稿 1-待会诊 2-会诊中 3-会诊结束）
     */
    private Integer status;

    /**
     * 会诊类型（0-远程会诊 1-处方会诊）
     */
    private Integer consType;

    /**
     * 发起时间-开始时间 yyyy-MM-dd
     */
    private String consInitStartTime;

    /**
     * 发起时间-结束时间 yyyy-MM-dd
     */
    private String consInitEndTime;

    /**
     * 发起会诊用户ID
     */
    private Long consInitUserId;

    /**
     * 邀请会诊用户ID
     */
    private Long propinvConsUserId;

    /**
     * 所属医联体ID（自动填充，无需前端传）
     */
    private Long medicalGroupId;

    /**
     * 当前登录用户ID
     */
    private Long currentUserId;

    /**
     * 查询类型：0-当前用户发起的会诊 1-当前用户接收的会诊
     */
    private Integer queryType;
}
