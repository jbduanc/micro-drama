package com.tcm.rx.mapper.stat;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcm.rx.entity.stat.Stat;
import com.tcm.rx.vo.stat.request.StatRequestVO;
import com.tcm.rx.vo.stat.response.*;

import java.util.List;

public interface StatMapper extends BaseMapper<Stat> {

    List<HspRxExportVO> hspRxList(StatRequestVO statRequestVO);

    List<HerbUseExportVO> herbUseList(StatRequestVO statRequestVO);

    List<ChargeExportVO> rxChargeList(StatRequestVO statRequestVO);

    List<ChargeDetailExportVO> rxChargeDetailList(StatRequestVO statRequestVO);

    List<HistoryChargeExportVO> historyChargeList(StatRequestVO statRequestVO);

    List<NoCodeHerbDetailVO> getNoCodeHerbDetail(StatRequestVO statRequestVO);
}
