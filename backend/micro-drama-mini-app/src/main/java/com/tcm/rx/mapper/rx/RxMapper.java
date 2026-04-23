package com.tcm.rx.mapper.rx;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcm.rx.entity.rx.Rx;
import com.tcm.rx.vo.rx.request.RxQueryVO;
import com.tcm.rx.vo.rx.response.RxVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 诊疗开方系统--处方主表 Mapper 接口
 * </p>
 *
 * @author shouhan
 * @since 2025-07-21
 */
public interface RxMapper extends BaseMapper<Rx> {

    List<RxVO> queryRxList(@Param("queryVO") RxQueryVO rxQueryVO);
}
