package com.tcm.rx.mapper.hsp;

import com.tcm.rx.entity.hsp.Hsp;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcm.rx.vo.hsp.request.HspQueryVO;
import com.tcm.rx.vo.hsp.response.HspVO;

import java.util.List;

/**
 * <p>
 * 诊疗开方系统--医疗机构表 Mapper 接口
 * </p>
 *
 * @author xph
 * @since 2025-07-10
 */
public interface HspMapper extends BaseMapper<Hsp> {

    /**
     * 查询医疗机构信息的数据
     */
    List<HspVO> hspList(HspQueryVO queryVO);

}
