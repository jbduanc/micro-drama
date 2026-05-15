package com.series.admin.dto.sys;

import com.series.common.vo.PageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户信息返回DTO（与 platform_db.sys_user 一致）
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserInfoDTO extends PageVO {
    private String id;
    private String nickname;
    private String googleEmail;
    private String avatar;
    private Integer status;
}
