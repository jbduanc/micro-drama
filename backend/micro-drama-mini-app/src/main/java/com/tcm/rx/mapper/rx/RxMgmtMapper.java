package com.tcm.rx.mapper.rx;

import com.tcm.rx.entity.rx.RxMgmt;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcm.rx.vo.rx.request.RxMgmtQueryVO;
import com.tcm.rx.vo.rx.response.RxMgmtVO;

import java.util.List;

/**
 * <p>
 * 诊疗开方系统--方剂管理 Mapper 接口
 * </p>
 *
 * @author djbo
 * @since 2025-09-08
 */
public interface RxMgmtMapper extends BaseMapper<RxMgmt> {

    List<RxMgmtVO> queryRxMgmtPage(RxMgmtQueryVO rxMgmtQueryVO);

}
