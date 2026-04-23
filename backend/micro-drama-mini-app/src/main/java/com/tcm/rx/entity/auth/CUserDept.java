package com.tcm.rx.entity.auth;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * <p>
 * 诊疗开方系统--用户科室关联表
 * </p>
 *
 * @author xph
 * @since 2025-07-15
 */
@Data
@TableName("rx_c_user_dept")
public class CUserDept extends Model<CUserDept> {

    private static final long serialVersionUID = 6628470032266063718L;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 科室id
     */
    private Long deptId;

}
