package com.tcm.rx.service.msg;

import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.rx.entity.msg.Msg;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.rx.vo.msg.request.MsgQueryVO;
import com.tcm.rx.vo.msg.request.MsgUpdateVO;
import com.tcm.rx.vo.msg.response.MsgVO;

import java.util.List;

/**
 * <p>
 * 诊疗开方系统--消息表 服务类
 * </p>
 *
 * @author djbo
 * @since 2025-09-11
 */
public interface IMsgService extends IService<Msg> {

    /**
     * 分页查询
     */
    public List<MsgVO> pageList(MsgQueryVO msgQueryVO);

    /**
     * 修改消息
     *
     * @param msgUpdateVO
     * @return
     */
    public Boolean updateMsg(MsgUpdateVO msgUpdateVO);

    /**
     * 获取用户未读消息数量
     *
     * @param userId 用户ID
     * @return 未读消息数量
     */
    public Integer getUnreadCount(Long userId);

    /**
     * 获取用户最新消息列表
     *
     * @param userId 用户ID
     * @param limit 限制条数
     * @return 消息列表
     */
    public List<Msg> getLatestMessages(Long userId, int limit);
}
