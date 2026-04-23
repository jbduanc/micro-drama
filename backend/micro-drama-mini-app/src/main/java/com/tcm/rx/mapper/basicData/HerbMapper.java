package com.tcm.rx.mapper.basicData;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcm.rx.entity.basicData.Herb;
import com.tcm.rx.vo.basicData.request.HerbQueryVO;
import java.util.List;

public interface HerbMapper extends BaseMapper<Herb> {

    // 分页查询
    List<Herb> selectByQuery(HerbQueryVO queryVO);

    List<Herb> herbNameMatch(HerbQueryVO queryVO);
}