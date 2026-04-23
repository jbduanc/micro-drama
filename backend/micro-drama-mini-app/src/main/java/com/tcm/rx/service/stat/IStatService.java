package com.tcm.rx.service.stat;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.rx.entity.stat.Stat;
import com.tcm.rx.vo.stat.request.HerbStorageReqVO;
import com.tcm.rx.vo.stat.request.StatRequestVO;
import com.tcm.rx.vo.stat.response.*;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 综合查询服务类
 * </p>
 *
 * @author shouhan
 * @since 2025-07-21
 */
public interface IStatService extends IService<Stat> {

    /**
     * 医院数据统计
     *
     * @param statRequestVO
     * @return
     */
    List<HspRxExportVO> hspRxList(StatRequestVO statRequestVO);

    /**
     * 医院数据统计导出
     *
     * @param statRequestVO
     * @param response
     */
    void hspRxListExport(StatRequestVO statRequestVO, HttpServletResponse response) throws IOException;

    /**
     * 饮片使用明细
     *
     * @param statRequestVO
     * @return
     */
    List<HerbUseExportVO> herbUseList(StatRequestVO statRequestVO);

    /**
     * 饮片使用明细导出
     *
     * @param statRequestVO
     * @param response
     */
    void herbUseListExport(StatRequestVO statRequestVO, HttpServletResponse response) throws IOException;

    /**
     * 适宜技术统计
     *
     * @param statRequestVO
     * @return
     */
    List<ChargeExportVO> rxChargeList(StatRequestVO statRequestVO);

    /**
     * 适宜技术统计导出
     *
     * @param statRequestVO
     * @param response
     * @throws IOException
     */
    void rxChargeListExport(StatRequestVO statRequestVO, HttpServletResponse response) throws IOException;

    /**
     * 适宜技术明细
     *
     * @param statRequestVO
     * @return
     */
    List<ChargeDetailExportVO> rxChargeDetailList(StatRequestVO statRequestVO);

    /**
     * 适宜技术明细导出
     *
     * @param statRequestVO
     * @param response
     * @throws IOException
     */
    void rxChargeDetailListExport(StatRequestVO statRequestVO, HttpServletResponse response) throws IOException;

    /**
     * 医疗机构下拉选列表
     *
     * @return
     */
    Map<String,Long> getHspList();

    /**
     * 历史适宜技术
     *
     * @param statRequestVO
     * @return
     */
    List<HistoryChargeExportVO> historyChargeList(StatRequestVO statRequestVO);

    /**
     * 历史适宜技术导出
     *
     * @param statRequestVO
     * @param response
     * @throws IOException
     */
    void historyChargeListExport(StatRequestVO statRequestVO, HttpServletResponse response) throws IOException;


    TablePageInfo<HerbStorageVO> getHerbStorageList(HerbStorageReqVO storageReqVO);

    /**
     * 无编码饮片明细
     *
     * @param statRequestVO
     * @return
     */
    List<NoCodeHerbDetailVO> getNoCodeHerbDetail(StatRequestVO statRequestVO);
}
