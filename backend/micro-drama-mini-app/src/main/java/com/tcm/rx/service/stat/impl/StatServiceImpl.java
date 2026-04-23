package com.tcm.rx.service.stat.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.entity.Result;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.common.enums.ExtractStatusEnum;
import com.tcm.common.enums.SyncStatusEnum;
import com.tcm.common.exception.ServiceException;
import com.tcm.common.mo.PurchaseMO;
import com.tcm.common.utils.CommonUtil;
import com.tcm.common.utils.EasyExcelUtil;
import com.tcm.rx.entity.auth.CUser;
import com.tcm.rx.entity.hsp.Hsp;
import com.tcm.rx.entity.stat.Stat;
import com.tcm.rx.enums.RxStatusEnum;
import com.tcm.rx.feign.customer.vo.CustomerVO;
import com.tcm.rx.feign.herb.HerbStockFeignClient;
import com.tcm.rx.mapper.stat.StatMapper;
import com.tcm.rx.service.auth.ICUserService;
import com.tcm.rx.service.hsp.IHspService;
import com.tcm.rx.service.stat.IStatService;
import com.tcm.rx.vo.herb.response.HerbSyncDataVO;
import com.tcm.rx.vo.herb.response.HerbSyncDetailVO;
import com.tcm.rx.vo.stat.request.HerbStorageReqVO;
import com.tcm.rx.vo.stat.request.StatRequestVO;
import com.tcm.rx.vo.stat.response.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * <p>
 * 综合查询服务实现类
 * </p>
 *
 * @author shouhan
 * @since 2025-07-21
 */
@Slf4j
@Service
public class StatServiceImpl extends ServiceImpl<StatMapper, Stat> implements IStatService {

    @Resource
    private ICUserService userService;

    @Resource
    private IHspService hspService;

    @Resource
    private MongoTemplate mongoTemplate;

    @Autowired
    private HerbStockFeignClient feignClient;

    /**
     * 医院数据统计
     *
     * @param statRequestVO
     * @return
     */
    public List<HspRxExportVO> hspRxList(StatRequestVO statRequestVO) {
        if (ObjectUtil.isNotEmpty(statRequestVO.getStartDate())) {
            statRequestVO.setStartDate(CommonUtil.getDayStartTime(statRequestVO.getStartDate()));
        } else {
            statRequestVO.setStartDate("1970-01-01 00:00:00");
        }
        if (ObjectUtil.isNotEmpty(statRequestVO.getEndDate())) {
            statRequestVO.setEndDate(CommonUtil.getDayEndTime(statRequestVO.getEndDate()));
        } else {
            statRequestVO.setEndDate("2125-01-01 00:00:00");
        }
        return this.getBaseMapper().hspRxList(statRequestVO);
    }

    /**
     * 医院数据统计导出
     *
     * @param statRequestVO
     * @param response
     */
    public void hspRxListExport(StatRequestVO statRequestVO, HttpServletResponse response) throws IOException {
        List<HspRxExportVO> hspRxList = hspRxList(statRequestVO);
        // 导出文件
        EasyExcelUtil.exportService(response, "医院数据统计", "医院数据统计", HspRxExportVO.class, hspRxList);
    }

    /**
     * 饮片使用明细
     *
     * @param statRequestVO
     * @return
     */
    public List<HerbUseExportVO> herbUseList(StatRequestVO statRequestVO) {
        if (ObjectUtil.isNotEmpty(statRequestVO.getStartDate())) {
            statRequestVO.setStartDate(CommonUtil.getDayStartTime(statRequestVO.getStartDate()));
        }
        if (ObjectUtil.isNotEmpty(statRequestVO.getEndDate())) {
            statRequestVO.setEndDate(CommonUtil.getDayEndTime(statRequestVO.getEndDate()));
        }
        return this.getBaseMapper().herbUseList(statRequestVO);
    }

    /**
     * 饮片使用明细导出
     *
     * @param statRequestVO
     * @param response
     */
    public void herbUseListExport(StatRequestVO statRequestVO, HttpServletResponse response) throws IOException {
        List<HerbUseExportVO> herbUseList = herbUseList(statRequestVO);
        // 导出文件
        EasyExcelUtil.exportService(response, "饮片使用明细", "饮片使用明细", HerbUseExportVO.class, herbUseList);
    }

    /**
     * 适宜技术统计
     *
     * @param statRequestVO
     * @return
     */
    public List<ChargeExportVO> rxChargeList(StatRequestVO statRequestVO) {
        if (ObjectUtil.isNotEmpty(statRequestVO.getStartDate())) {
            statRequestVO.setStartDate(CommonUtil.getDayStartTime(statRequestVO.getStartDate()));
        }
        if (ObjectUtil.isNotEmpty(statRequestVO.getEndDate())) {
            statRequestVO.setEndDate(CommonUtil.getDayEndTime(statRequestVO.getEndDate()));
        }
        return this.getBaseMapper().rxChargeList(statRequestVO);
    }

    /**
     * 适宜技术统计导出
     *
     * @param statRequestVO
     * @param response
     * @throws IOException
     */
    public void rxChargeListExport(StatRequestVO statRequestVO, HttpServletResponse response) throws IOException {
        List<ChargeExportVO> chargeExportVOS = rxChargeList(statRequestVO);
        // 导出文件
        EasyExcelUtil.exportService(response, "适宜技术统计", "适宜技术统计", ChargeExportVO.class, chargeExportVOS);
    }

    /**
     * 适宜技术明细
     *
     * @param statRequestVO
     * @return
     */
    public List<ChargeDetailExportVO> rxChargeDetailList(StatRequestVO statRequestVO) {
        if (ObjectUtil.isNotEmpty(statRequestVO.getStartDate())) {
            statRequestVO.setStartDate(CommonUtil.getDayStartTime(statRequestVO.getStartDate()));
        }
        if (ObjectUtil.isNotEmpty(statRequestVO.getEndDate())) {
            statRequestVO.setEndDate(CommonUtil.getDayEndTime(statRequestVO.getEndDate()));
        }
        List<ChargeDetailExportVO> chargeDetailExportVOS = this.getBaseMapper().rxChargeDetailList(statRequestVO);
        chargeDetailExportVOS.forEach(record -> record.setStatusName(RxStatusEnum.CODE_MAP.get(record.getStatus())));
        return chargeDetailExportVOS;
    }

    /**
     * 适宜技术明细导出
     *
     * @param statRequestVO
     * @param response
     * @throws IOException
     */
    public void rxChargeDetailListExport(StatRequestVO statRequestVO, HttpServletResponse response) throws IOException {
        List<ChargeDetailExportVO> chargeDetailExportVOS = rxChargeDetailList(statRequestVO);
        // 导出文件
        EasyExcelUtil.exportService(response, "适宜技术明细", "适宜技术明细", ChargeDetailExportVO.class, chargeDetailExportVOS);
    }

    /**
     * 医疗机构下拉选列表
     *
     * @return
     */
    public Map<String, Long> getHspList() {
        Map<String, Long> hspMap = new HashMap<>();
        CUser user = userService.getById(UserContextHolder.getUserInfoContext().getId());
        if (ObjectUtil.isEmpty(user)) {
            throw new ServiceException("用户不存在");
        }
        if ("admin".equalsIgnoreCase(user.getUserType())) {
            hspMap.put("医联体", null);
        }
        Hsp hsp = hspService.getById(user.getHspId());
        if (ObjectUtil.isNotEmpty(hsp)) {
            hspMap.put(hsp.getHspName(), hsp.getId());
        }
        return hspMap;
    }

    /**
     * 历史适宜技术
     *
     * @param statRequestVO
     * @return
     */
    public List<HistoryChargeExportVO> historyChargeList(StatRequestVO statRequestVO) {
        if (ObjectUtil.isNotEmpty(statRequestVO.getStartDate())) {
            statRequestVO.setStartDate(CommonUtil.getDayStartTime(statRequestVO.getStartDate()));
        }
        if (ObjectUtil.isNotEmpty(statRequestVO.getEndDate())) {
            statRequestVO.setEndDate(CommonUtil.getDayEndTime(statRequestVO.getEndDate()));
        }
        List<HistoryChargeExportVO> historyChargeList = this.getBaseMapper().historyChargeList(statRequestVO);
        historyChargeList.forEach(record -> record.setStatusName(RxStatusEnum.CODE_MAP.get(record.getStatus())));
        return historyChargeList;
    }

    /**
     * 历史适宜技术导出
     *
     * @param statRequestVO
     * @param response
     * @throws IOException
     */
    public void historyChargeListExport(StatRequestVO statRequestVO, HttpServletResponse response) throws IOException {
        List<HistoryChargeExportVO> historyChargeList = historyChargeList(statRequestVO);
        // 导出文件
        EasyExcelUtil.exportService(response, "历史适宜技术", "历史适宜技术", HistoryChargeExportVO.class, historyChargeList);
    }

    @Override
    public TablePageInfo<HerbStorageVO> getHerbStorageList(HerbStorageReqVO storageReqVO) {
        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        Pageable pageable = PageRequest.of(storageReqVO.getPage() - 1, storageReqVO.getSize());
        Criteria criteria = Criteria
                .where("medicalGroupCode").is(loginUser.getMedicalGroupCode());
        if (StringUtils.isNotEmpty(storageReqVO.getStartTime()) && StringUtils.isNotEmpty(storageReqVO.getEndTime())) {
            //                criteria.and("businessDate").gte(DateUtils.parseDate(storageReqVO.getStartTime() + " 00:00:00", "yyyy-MM-dd HH:mm:ss").getTime()).lte(DateUtils.parseDate(storageReqVO.getEndTime() + " 23:59:59", "yyyy-MM-dd HH:mm:ss").getTime());
            criteria.and("businessDate").gte(storageReqVO.getStartTime()).lte(storageReqVO.getEndTime());
        }
        if (StringUtils.isNotEmpty(storageReqVO.getPurchaseNo())) {
            criteria.and("purchaseNo").regex(storageReqVO.getPurchaseNo());
        }
        Query query = new Query(criteria);
        Long total = mongoTemplate.count(query, PurchaseMO.class);
        List<PurchaseMO> purchaseMOS = mongoTemplate.find(query.with(pageable), PurchaseMO.class);
        List<HerbStorageVO> herbStorageVOS = new ArrayList<>();
        purchaseMOS.forEach(p -> {
            HerbStorageVO storageVO = new HerbStorageVO();
            storageVO.setBusinessDate(p.getBusinessDate());
            storageVO.setPurchaseNo(p.getPurchaseNo());
            storageVO.setZlSyncStatus(p.getSyncStatus());
            storageVO.setJySyncStatus(p.getExtractStatus());
            storageVO.setJyInventorySyncStatus(SyncStatusEnum.UNSYNC.getCode());
            if (ExtractStatusEnum.EXTRACTED.getCode().equals(p.getExtractStatus())) {
                Result result = feignClient.getHerbPurchaseByHisPurchaseNo(p.getPurchaseNo());
                log.info("远程调用采购入库库存为" + JSONObject.toJSONString(result));
                String data = JSONObject.toJSONString(result.getData());
                if ("true".equals(data)) {
                    storageVO.setJyInventorySyncStatus(SyncStatusEnum.SYNC.getCode());
                }
            }
            herbStorageVOS.add(storageVO);
        });

        return new TablePageInfo(herbStorageVOS, total.intValue());
    }

    /**
     * 无编码饮片明细
     *
     * @param statRequestVO
     * @return
     */
    public List<NoCodeHerbDetailVO> getNoCodeHerbDetail(StatRequestVO statRequestVO) {
        return this.getBaseMapper().getNoCodeHerbDetail(statRequestVO);
    }
}
