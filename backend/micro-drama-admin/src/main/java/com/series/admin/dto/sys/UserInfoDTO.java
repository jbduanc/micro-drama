package com.series.admin.dto.sys;

import com.series.common.vo.PageVO;
import lombok.Data;

/**
 * 用户信息返回DTO
 */
@Data
public class UserInfoDTO extends PageVO {
    /** 用户ID */
    private Long id;
    /** 昵称 */
    private String nickname;
    /** 谷歌邮箱 */
    private String googleEmail;
    /** 头像 */
    private String avatar;
    /** 状态（1-正常） */
    private Integer status;
}
