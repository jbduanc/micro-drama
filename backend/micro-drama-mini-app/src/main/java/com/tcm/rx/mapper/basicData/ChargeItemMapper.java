package com.tcm.rx.mapper.basicData;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcm.rx.entity.basicData.ChargeItem;
import com.tcm.rx.vo.basicData.request.ChargeItemQueryVO;
import java.util.List;

public interface ChargeItemMapper extends BaseMapper<ChargeItem> {
    List<ChargeItem> selectByQuery(ChargeItemQueryVO queryVO);
}