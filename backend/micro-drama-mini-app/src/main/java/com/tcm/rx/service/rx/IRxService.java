package com.tcm.rx.service.rx;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.rx.entity.cons.Consultation;
import com.tcm.rx.entity.rx.Rx;
import com.tcm.rx.vo.rx.request.RxQueryVO;
import com.tcm.rx.vo.rx.request.RxSaveVO;
import com.tcm.rx.vo.rx.request.RxStatusVO;
import com.tcm.rx.vo.rx.response.RxVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 诊疗开方系统--处方主表 服务类
 * </p>
 *
 * @author shouhan
 * @since 2025-07-21
 */
public interface IRxService extends IService<Rx> {

    List<RxVO> queryPage(RxQueryVO queryVO);

    RxVO getRxVo(String id, RxVO rxVO, Map<Long, String> userMap, Map<Long, String> HspMap, Map<String, List<Consultation>> consMap);

    String saveRx(RxSaveVO dto);

    void exportRxDetail(RxQueryVO queryVO, HttpServletResponse response) throws IOException;

    Boolean dispenser(RxStatusVO rxStatusVO);

    Boolean updateRxStatus(RxStatusVO rxStatusVO);

    Boolean batchDispenser(List<RxStatusVO> rxStatusVOList);
}
