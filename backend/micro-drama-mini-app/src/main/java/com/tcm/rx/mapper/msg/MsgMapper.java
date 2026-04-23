package com.tcm.rx.mapper.msg;

import com.tcm.rx.entity.msg.Msg;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcm.rx.vo.msg.request.MsgQueryVO;
import com.tcm.rx.vo.msg.response.MsgVO;

import java.util.List;

/**
 * <p>
 * 诊疗开方系统--消息表 Mapper 接口
 * </p>
 *
 * @author djbo
 * @since 2025-09-11
 */
public interface MsgMapper extends BaseMapper<Msg> {

    List<MsgVO> selectByQuery(MsgQueryVO msgQueryVO);
}
