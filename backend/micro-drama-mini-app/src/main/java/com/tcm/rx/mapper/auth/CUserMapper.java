package com.tcm.rx.mapper.auth;

import com.tcm.rx.entity.auth.CUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcm.rx.vo.auth.request.UserQueryVO;
import com.tcm.rx.vo.auth.response.CUserVO;

import java.util.List;

/**
 * <p>
 * 诊疗开方系统--用户表 Mapper 接口
 * </p>
 *
 * @author xph
 * @since 2025-07-07
 */
public interface CUserMapper extends BaseMapper<CUser> {

    /**
     * 查询用户信息的数据
     */
    List<CUserVO> userList(UserQueryVO queryVO);

    /**
     * 根据条件查询用户信息的数据
     */
    CUserVO userByCondition(UserQueryVO queryVO);

}
