package com.tcm.rx.service.rx.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.entity.Result;
import com.tcm.common.enums.DeliveryMethodEnum;
import com.tcm.common.enums.ExtractStatusEnum;
import com.tcm.common.enums.RxSourceEnum;
import com.tcm.common.enums.TreatmentTypeEnum;
import com.tcm.common.exception.ServiceException;
import com.tcm.common.mo.RxDetailMO;
import com.tcm.common.mo.RxMO;
import com.tcm.common.utils.AssertUtil;
import com.tcm.common.utils.CommonUtil;
import com.tcm.common.utils.EasyExcelUtil;
import com.tcm.rx.entity.auth.CUser;
import com.tcm.rx.entity.basicData.ChargeItem;
import com.tcm.rx.entity.cons.Consultation;
import com.tcm.rx.entity.hsp.Hsp;
import com.tcm.rx.entity.patient.PatientBase;
import com.tcm.rx.entity.patient.PatientVisit;
import com.tcm.rx.entity.rx.*;
import com.tcm.rx.enums.RxStatusEnum;
import com.tcm.rx.feign.dataBase.RemoteDataFeignClient;
import com.tcm.rx.mapper.rx.RxMapper;
import com.tcm.rx.service.auth.ICUserService;
import com.tcm.rx.service.basicData.IChargeItemService;
import com.tcm.rx.service.basicData.processFee.ProcessingFeeService;
import com.tcm.rx.service.cons.IConsultationService;
import com.tcm.rx.service.hsp.IHspService;
import com.tcm.rx.service.patient.IPatientBaseService;
import com.tcm.rx.service.patient.IPatientVisitService;
import com.tcm.rx.service.rx.*;
import com.tcm.rx.util.RxIdGenerator;
import com.tcm.rx.vo.rx.request.RxQueryVO;
import com.tcm.rx.vo.rx.request.RxSaveVO;
import com.tcm.rx.vo.rx.request.RxStatusVO;
import com.tcm.rx.vo.rx.response.RxDetailExportVO;
import com.tcm.rx.vo.rx.response.RxFeeVO;
import com.tcm.rx.vo.rx.response.RxVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.tcm.common.enums.DeliveryMethodEnum.EXPRESS;
import static com.tcm.common.enums.DeliveryMethodEnum.HOSPITAL;

/**
 * <p>
 * 诊疗开方系统--处方主表 服务实现类
 * </p>
 *
 * @author shouhan
 * @since 2025-07-21
 */
@Slf4j
@Service
public class RxServiceImpl extends ServiceImpl<RxMapper, Rx> implements IRxService {
    @Autowired
    private IRxDetailService rxDetailService;
    @Autowired
    private IRxFeeService rxFeeService;
    @Resource
    private RxIdGenerator rxIdGenerator;
    @Resource
    private ICUserService userService;
    @Resource
    private IHspService hspService;
    @Resource
    private IChargeItemService chargeItemService;
    @Resource
    private RemoteDataFeignClient remoteDataFeignClient;
    @Resource
    private IPatientVisitService patientVisitService;
    @Resource
    private IPatientBaseService patientBaseService;
    @Resource
    private ICompatTabooMgmtService compatTabooMgmtService;
    @Resource
    private ICompatTabooMgmtDetailService compatTabooMgmtDetailService;
    @Resource
    private IConsultationService consultationService;

    /**
     * 分页查询
     *
     * @param queryVO
     * @return
     */
    public List<RxVO> queryPage(RxQueryVO queryVO) {
        List<RxVO> rxVOList = this.getBaseMapper().queryRxList(queryVO);
        if (ObjectUtil.isEmpty(rxVOList)) {
            return new ArrayList<>();
        }
        List<String> userIdList = rxVOList.stream()
                .flatMap(record -> Stream.of(record.getUpdateBy(),
                                record.getCreateBy())
                        .filter(Objects::nonNull)).collect(Collectors.toList());
        Map<Long, String> userMap = userService.listByIds(userIdList.stream().map(idStr -> Long.parseLong(idStr)).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(CUser::getId, CUser::getRealName));
        Map<Long, String> hspMap = hspService.listByIds(rxVOList.stream().map(record -> Optional.ofNullable(record.getHspId()).orElse(-1L)).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(Hsp::getId, Hsp::getHspName));
        Map<String, List<Consultation>> consMap = consultationService.lambdaQuery().in(Consultation::getRxId,
                        rxVOList.stream().map(rxVO -> rxVO.getId()).collect(Collectors.toList())).list().stream()
                .collect(Collectors.groupingBy(Consultation::getRxId));
        return rxVOList.stream().map(rxVo -> getRxVo(rxVo.getId(), rxVo, userMap, hspMap, consMap)).collect(Collectors.toList());
    }

    /**
     * 处方详情
     *
     * @param id
     * @param rxVO
     * @return
     */
    public RxVO getRxVo(String id, RxVO rxVO, Map<Long, String> userMap, Map<Long, String> hspMap, Map<String, List<Consultation>> consMap) {
        BaseUser user = UserContextHolder.getUserInfoContext();
        if (ObjectUtil.isEmpty(rxVO)) {
            RxQueryVO queryVO = new RxQueryVO();
            queryVO.setMedicalGroupId(user.getMedicalGroupId());
            queryVO.setId(id);
            List<RxVO> rxVOList = this.getBaseMapper().queryRxList(queryVO);
            if (ObjectUtil.isEmpty(rxVOList)) {
                return new RxVO();
            }
            rxVO = rxVOList.get(0);
        }
        if (ObjectUtil.isEmpty(userMap)) {
            List<String> userIdList = new ArrayList<>();
            userIdList.add(rxVO.getCreateBy());
            userIdList.add(rxVO.getUpdateBy());
            userMap = userService.listByIds(userIdList.stream().filter(record -> ObjectUtil.isNotEmpty(record)).map(idStr -> Long.parseLong(idStr)).collect(Collectors.toList()))
                    .stream().collect(Collectors.toMap(CUser::getId, CUser::getRealName));
        }
        if (ObjectUtil.isEmpty(hspMap)) {
            Hsp hsp = Optional.ofNullable(hspService.getById(Optional.ofNullable(rxVO.getHspId()).orElse(-1L))).orElse(new Hsp());
            hspMap = new HashMap<>();
            hspMap.put(hsp.getId(), hsp.getHspName());
        }

        if (ObjectUtil.isEmpty(consMap)) {
            consMap = consultationService.lambdaQuery().eq(Consultation::getRxId, rxVO.getId()).list().stream()
                    .collect(Collectors.groupingBy(Consultation::getRxId));
        }
        List<RxDetail> rxDetails = rxDetailService.lambdaQuery().eq(RxDetail::getRxId, rxVO.getId()).orderByAsc(RxDetail::getSerialNum).list();
        rxVO.setRxDetails(rxDetails);
        List<RxFee> rxFeeList = rxFeeService.lambdaQuery().eq(RxFee::getRxId, rxVO.getId()).orderByAsc(RxFee::getId).list();
        if (ObjectUtil.isNotEmpty(rxFeeList)) {
            Map<String, ChargeItem> chargeItemMap = chargeItemService.lambdaQuery().eq(ChargeItem::getMedicalGroupId, user.getMedicalGroupId()).in(ChargeItem::getItemCode,
                            rxFeeList.stream().map(record -> record.getChargeItemCode()).distinct().collect(Collectors.toList())).list()
                    .stream().collect(Collectors.toMap(ChargeItem::getItemCode, ChargeItem -> ChargeItem));
            rxVO.setRxFees(rxFeeList.stream().map(record -> {
                ChargeItem chargeItem = chargeItemMap.get(record.getChargeItemCode());
                RxFeeVO rxFeeVO = new RxFeeVO();
                BeanUtil.copyProperties(record, rxFeeVO);
                rxFeeVO.setUnitPrice(chargeItem.getUnitPrice());
                rxFeeVO.setItemName(chargeItem.getItemName());
                rxFeeVO.setPriceUnit(chargeItem.getPriceUnit());
                return rxFeeVO;
            }).collect(Collectors.toList()));
        }
        rxVO.setRxHerbNames(rxDetails.stream().map(detail -> detail.getHerbName()).collect(Collectors.joining("、")));
        rxVO.setStatusName(RxStatusEnum.CODE_MAP.get(rxVO.getStatus()));
        rxVO.setSourceName(RxSourceEnum.CODE_MAP.get(rxVO.getSource()));
        if (ObjectUtil.isNotEmpty(rxVO.getTreatmentType())) {
            rxVO.setTreatmentTypeName(TreatmentTypeEnum.CODE_MAP.get(Integer.parseInt(rxVO.getTreatmentType())));
        }
        if (ObjectUtil.isNotEmpty(consMap.get(rxVO.getId()))) {
            List<String> consOrgNameList = consMap.get(rxVO.getId()).stream().map(cons -> {
                Hsp hsp = hspService.getById(Optional.ofNullable(cons.getPropinvConsHspId()).orElse(-1L));
                if (ObjectUtil.isNotEmpty(hsp)) {
                    return hsp.getHspName();
                }
                return "";
            }).collect(Collectors.toList());
            rxVO.setConsultationOrg(String.join(",", consOrgNameList));
            List<String> propinvConsUserIdList = consMap.get(rxVO.getId()).stream()
                    .map(cons -> Optional.ofNullable(cons.getPropinvConsUserId()).orElse(-1L).toString()).collect(Collectors.toList());
            rxVO.setConsultationUserId(String.join(",", propinvConsUserIdList));
            List<String> propinvConsUserNameList = consMap.get(rxVO.getId()).stream()
                    .map(cons -> {
                        CUser cuser = userService.getById(Optional.ofNullable(cons.getPropinvConsUserId()).orElse(-1L));
                        if (ObjectUtil.isNotEmpty(cuser)) {
                            return cuser.getRealName();
                        }
                        return "";
                    }).collect(Collectors.toList());
            rxVO.setConsultationUserName(String.join(",", propinvConsUserNameList));
        }
        rxVO.setUpdateByName(userMap.get(rxVO.getCreateBy()));
        rxVO.setCreateByName(userMap.get(rxVO.getUpdateBy()));
        rxVO.setHspName(hspMap.get(rxVO.getHspId()));
        return rxVO;
    }

    @Transactional(rollbackFor = Exception.class)
    public String saveRx(RxSaveVO dto) {
        // 1. 校验必填项
        Rx rx = dto.getRx();
        AssertUtil.assertArgs()
                .notEmpty(rx, "处方")
                .notEmpty(rx.getDiagnosis(), "西医诊断")
                .notEmpty(rx.getIsExpress(), "是否快递")
                .check();
        BaseUser user = UserContextHolder.getUserInfoContext();
        log.info("user info:{}", JSON.toJSONString(user));
        if (ObjectUtil.isNotEmpty(rx.getId())) {
            rx.setUpdateTime(new Date());
            rx.setUpdateBy(user.getId().toString());
            this.updateById(rx);
        } else {
            CommonUtil.setRxBaseEntity(rx, user);
            rx.setSource(RxSourceEnum.TCM_CLINIC_SYSTEM.getCode());
            rx.setId(rxIdGenerator.generateRxId());
            rx.setStatus(RxStatusEnum.PENDING_REVIEW.getCode());
            rx.setHspCode(user.getHspCode());
            rx.setHspId(user.getHspId());
            rx.setPrescriberId(user.getId());
            rx.setPrescriberName(user.getRealName());
            rx.setPrescriptionTime(new Date());
            this.save(rx);
        }
        List<RxDetail> details = dto.getRxDetails();
        // 3. 保存处方明细表
        if (ObjectUtil.isNotEmpty(details)) {
            if ("是".equals(rx.getIsPregnant())) {
                compatTabCheck(details);
            }
            rxDetailService.remove(new LambdaQueryWrapper<RxDetail>().eq(RxDetail::getRxId, rx.getId()));
            checkHerbRepeact(details);
            details.forEach(record -> {
                record.setRxId(rx.getId());
                CommonUtil.setRxBaseEntity(record, user);
                AssertUtil.assertArgs()
                        .notEmpty(record.getSerialNum(), "序号")
                        .notEmpty(record.getHerbCode(), "饮片编码")
                        .notEmpty(record.getHerbName(), "饮片名称")
                        .notEmpty(record.getSpec(), "规格")
                        .notEmpty(record.getHerbDosage(), "饮片用量")
                        .notEmpty(record.getUnit(), "单位")
                        .notEmpty(record.getPrice(), "单价")
                        .notEmpty(record.getMoney(), "单剂金额")
                        .notEmpty(record.getAdjustRequire(), "调配要求")
                        .notEmpty(record.getDoseCount(), "剂数")
                        .notEmpty(record.getOneDose(), "一剂/包")
                        .notEmpty(record.getPerPackVolume(), "每包/ml")
                        .notEmpty(record.getUsage(), "用法")
                        .notEmpty(record.getDecoctRequire(), "煎药要求")
                        .notEmpty(record.getDosageFrequency(), "用药频次")
                        .check();
            });
            rxDetailService.saveBatch(details);
        }
        // 4. 保存处方费用表
        List<RxFee> fees = Optional.ofNullable(dto.getRxFees()).orElse(new ArrayList<>()).stream()
                .filter(record -> ObjectUtil.isNotEmpty(record.getChargeItemCode())).collect(Collectors.toList());
        if (ObjectUtil.isNotEmpty(fees)) {
            rxFeeService.remove(new LambdaQueryWrapper<RxFee>().eq(RxFee::getRxId, rx.getId()));
            fees.forEach(fee -> {
                fee.setRxId(Long.valueOf(rx.getId()));
                CommonUtil.setRxBaseEntity(fee, user);
            });
            rxFeeService.saveBatch(fees);
        }
        try {
            RxVO rxVO = getRxVo(rx.getId(), null, null, null, null);
            rxVO.setSource(RxSourceEnum.TCM_CLINIC_SYSTEM.getCode());
            JSONObject requestData = JSON.parseObject(JSON.toJSONString(rxVO));
            Result result = remoteDataFeignClient.remoteData("/send/his/pushRx", requestData, user.getMedicalGroupCode());
            log.info("pushHis result:" + JSON.toJSONString(result));
        } catch (Exception e) {
            log.error("pushHis error:", e);
            throw new ServiceException("his服务推送异常");
        }
        if (ObjectUtil.isNotEmpty(dto.getConsId())) {
            Consultation consultation = new Consultation();
            consultation.setId(dto.getConsId());
            consultation.setRxId(rx.getId());
            consultationService.updateById(consultation);
        }
        return rx.getId();
    }

    /**
     * 配伍禁忌校验
     *
     * @param details
     */
    public void compatTabCheck(List<RxDetail> details) {
        List<Long> compatTabIdList = compatTabooMgmtService.lambdaQuery().eq(CompatTabooMgmt::getTabooType, "妊娠禁用").list()
                .stream().map(compatTab -> compatTab.getId()).collect(Collectors.toList());
        if (ObjectUtil.isEmpty(compatTabIdList)) {
            return;
        }
        Map<Long, List<CompatTabooMgmtDetail>> tabDetailMap = compatTabooMgmtDetailService.lambdaQuery()
                .in(CompatTabooMgmtDetail::getTabooMgmtId, compatTabIdList)
                .list().stream().collect(Collectors.groupingBy(CompatTabooMgmtDetail::getTabooMgmtId));
        details.forEach(detail -> {
            tabDetailMap.entrySet().forEach(tabDetailEntry -> {
                List<String> tabHerbCodeList = tabDetailEntry.getValue().stream()
                        .map(tabDetail -> tabDetail.getHerbCode()).collect(Collectors.toList());
                if (tabHerbCodeList.contains(detail.getHerbCode())) {
                    throw new ServiceException(String.format("%s属于妊娠禁用饮片，患者是孕妇，请换其他饮片使用!", detail.getHerbName()));
                }
            });
        });
    }

    /**
     * 校验饮片是否重复
     *
     * @param details
     */
    private static void checkHerbRepeact(List<RxDetail> details) {
        Map<String, Long> herbCodeCountMap = details.stream()
                .map(RxDetail::getHerbCode)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        List<String> duplicateCodes = herbCodeCountMap.entrySet().stream()
                .filter(entry -> entry.getValue() > 1) // 只保留出现次数超过1的编码
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if (ObjectUtil.isNotEmpty(duplicateCodes)) {
            throw new ServiceException("处方中存在重复的饮片编码：" + String.join(",", duplicateCodes) + "，请勿重复添加！");
        }
    }

    /**
     * exportRxDetail
     *
     * @param queryVO
     * @param response
     */
    public void exportRxDetail(RxQueryVO queryVO, HttpServletResponse response) throws IOException {
        List<RxVO> rxVOList = queryPage(queryVO);
        if (CollectionUtils.isEmpty(rxVOList)) {
            throw new ServiceException("导出数据为空");
        }
        List<RxDetailExportVO> exportVOList = rxVOList.stream().map(record -> {
            RxDetailExportVO exportVO = new RxDetailExportVO();
            exportVO.setPatientName(record.getPatientName());
            exportVO.setHspName(record.getHspName());
            exportVO.setSourceName(record.getSourceName());
            exportVO.setPrescriberName(record.getPrescriberName());
            exportVO.setStatusName(record.getStatusName());
            exportVO.setDrugCost(record.getDrugCost());
            if (ObjectUtil.isNotEmpty(record.getRxDetails())) {
                exportVO.setDoseCount(record.getRxDetails().get(0).getDoseCount());
                exportVO.setAdjustRequire(record.getRxDetails().get(0).getAdjustRequire());
            }
            exportVO.setTotalMoney(record.getTotalMoney());
            exportVO.setConsultationOrg(record.getConsultationOrg());
            exportVO.setConsultationUserName(record.getConsultationUserName());
            return exportVO;
        }).collect(Collectors.toList());
        // 导出文件
        EasyExcelUtil.exportService(response, "处方明细数据", "处方明细", RxDetailExportVO.class, exportVOList);
    }

    /**
     * 发药接口
     *
     * @param rxStatusVO
     * @return
     */
    public Boolean dispenser(RxStatusVO rxStatusVO) {
        Rx rx = this.getById(rxStatusVO.getId());
        if (ObjectUtil.isEmpty(rx) || RxStatusEnum.PAID.getCode() == rxStatusVO.getStatus()) {
            throw new ServiceException("处方不存在或者不是已缴费状态");
        }
        BaseUser user = Optional.ofNullable(UserContextHolder.getUserInfoContext()).orElse(new BaseUser());
        String userId = Optional.ofNullable(user.getId()).orElse(-1L).toString();
        Rx updateRx = new Rx();
        updateRx.setId(rx.getId());
        updateRx.setStatus(rxStatusVO.getStatus());
        updateRx.setUpdateBy(userId);
        updateRx.setUpdateTime(new Date());
        updateRx.setDispenserId(user.getId());
        updateRx.setDispenserName(user.getRealName());
        updateRx.setDispenserTime(new Date());
        try {
            RxVO rxVO = getRxVo(rx.getId(), null, null, null, null);
            log.info("rxVOInfo:{}", JSON.toJSONString(rxVO));
            RxMO rxMO = buildRxMO(rxVO);
            log.info("rxMOInfo:{}", JSON.toJSONString(rxMO));
            JSONObject requestData = JSON.parseObject(JSON.toJSONString(rxMO));
            log.info("requestDataInfo:{}", JSON.toJSONString(requestData));
            Result result = remoteDataFeignClient.remoteData("/receive/his/rx/upload", requestData, user.getMedicalGroupCode());
            log.info("uploadRx result:" + JSON.toJSONString(result));
        } catch (Exception e) {
            log.error("uploadRx error:", e);
            throw new ServiceException("上传处方信息推送异常");
        }
        return this.updateById(updateRx);
    }

    /**
     * 修改处方状态
     *
     * @param rxStatusVO
     * @return
     */
    public Boolean updateRxStatus(RxStatusVO rxStatusVO) {
        Rx rx = this.getById(rxStatusVO.getId());
        if (ObjectUtil.isEmpty(rx)) {
            return false;
        }
        BaseUser user = Optional.ofNullable(UserContextHolder.getUserInfoContext()).orElse(new BaseUser());
        String userId = Optional.ofNullable(user.getId()).orElse(-1L).toString();
        Rx updateRx = new Rx();
        updateRx.setId(rx.getId());
        updateRx.setStatus(rxStatusVO.getStatus());
        updateRx.setUpdateBy(userId);
        updateRx.setUpdateTime(new Date());
        switch (RxStatusEnum.CODE_MAP1.get(rxStatusVO.getStatus())) {
            case PENDING_PAYMENT: // 待缴费
                updateRx.setAuditTime(new Date());
                updateRx.setAuditComments(rxStatusVO.getAuditComments());
                break;
            case PAID: // 已缴费
                updateRx.setPaymentTime(new Date());
                break;
            case UPLOADED: // 已上传
                updateRx.setDispenserId(user.getId());
                updateRx.setDispenserName(user.getRealName());
                updateRx.setDispenserTime(new Date());
                break;
            case EXECUTED: // 已执行
                updateRx.setExecutionTime(new Date());
                break;
            case INVALIDATED: // 已作废
                updateRx.setInvalidTime(new Date());
                updateRx.setInvalidUserId(userId);
                updateRx.setInvalidUserName(user.getRealName());
                break;
            case REFUNDED: // 已退费
                updateRx.setRefundTime(new Date());
                break;
            default:
                throw new ServiceException("处方状态不存在");
        }
        return this.updateById(updateRx);
    }

    /**
     * 基础信息
     *
     * @param rxVO
     * @return
     */
    public Map<String, String> getPatientInfo(RxVO rxVO) {
        Map<String, String> map = new HashMap<>();
        if (ObjectUtil.isEmpty(rxVO.getInpatientCode())) {
            return map;
        }
        Optional<PatientVisit> visitOptional = patientVisitService.lambdaQuery()
                .eq(PatientVisit::getInpatientCode, rxVO.getInpatientCode()).list().stream().findFirst();
        if (!visitOptional.isPresent()) {
            return map;
        }
        map.put("inpatientNumber", visitOptional.get().getInpatientNumber());
        map.put("wardCode", visitOptional.get().getWardCode());
        map.put("wardName", visitOptional.get().getWardName());
        if (ObjectUtil.isNotEmpty(visitOptional.get().getPatientBaseId())) {
            Optional<PatientBase> patientBaseOptional = patientBaseService.lambdaQuery().eq(PatientBase::getId,
                    visitOptional.get().getPatientBaseId()).list().stream().findFirst();
            if (patientBaseOptional.isPresent()) {
                map.put("clinicCode", patientBaseOptional.get().getClinicCode());
            }
        }
        return map;
    }

    public RxMO buildRxMO(RxVO rxVO) {
        // 创建RxMO对象
        RxMO rxMO = new RxMO();
        // 设置提取状态（0-未提取 1-已提取）
        rxMO.setExtractStatus(ExtractStatusEnum.UNEXTRACTED.getCode());
        // 设置医联体编码
        rxMO.setMedicalGroupCode(rxVO.getMedicalGroupCode());
        // 设置煎药中心编码
        // rxMO.setDecoctCode("DC20001");
        rxMO.setSource(RxSourceEnum.TCM_CLINIC_SYSTEM.getCode());
        // 设置客户编码
        rxMO.setCustomerCode(rxVO.getHspCode());
        // 设置客户名称
        rxMO.setCustomerName(rxVO.getHspName());
        // 设置客户处方ID
        rxMO.setCustomerRxId(rxVO.getId());
        // 设置客户处方编号
        rxMO.setCustomerRxCode(rxVO.getId());
        // 设置客户处方序号
        // rxMO.setCustomerRxSeq("001");
        // 设置客户处方总金额（元）
        rxMO.setCustomerTotalMoney(rxVO.getTotalMoney());
        // 设置患者处方日期
        if (ObjectUtil.isNotEmpty(rxVO.getPrescriptionTime())) {
            rxMO.setPatientRxDate(new DateTime(rxVO.getPrescriptionTime()).toString("yyyy-MM-dd"));
            rxMO.setBillTime(new DateTime(rxVO.getPrescriptionTime()).toString("yyyy-MM-dd"));
        }
        // 设置支付状态
        // rxMO.setPayStatus("已支付");
        // 设置缴费时间
        // rxMO.setPayTime("2025-07-21 09:20:00");
        // 设置退费时间（未退费则设为null或空字符串）
        // rxMO.setRefundTime(null);
        // 设置就诊类型
        if (ObjectUtil.isNotEmpty(rxVO.getTreatmentType())) {
            rxMO.setVisitType(TreatmentTypeEnum.CODE_MAP.get(Integer.parseInt(rxVO.getTreatmentType())));
        }
        // 设置患者id
        rxMO.setPatientId(rxVO.getPatientId());
        // 设置患者姓名
        rxMO.setPatientName(rxVO.getPatientName());
        // 设置患者性别
        rxMO.setPatientGender(rxVO.getPatientGender());
        // 设置患者年龄
        rxMO.setPatientAge(rxVO.getPatientAge());
        // 设置床位号（门诊患者可设为空）
        rxMO.setBedNo(rxVO.getBedNo());
        // 设置患者档案编号
        // rxMO.setPatientArchiveCode();
        // 设置中医诊断病名
        rxMO.setTcmDisease(rxVO.getTcmDisease());
        // 设置中医诊断证候
        rxMO.setTcmPattern(rxVO.getTcmPattern());
        Map<String, String> patientInfoMap = getPatientInfo(rxVO);
        // 病区编码
        rxMO.setWardCode(patientInfoMap.get("wardCode"));
        // 病区名称
        rxMO.setWardName(patientInfoMap.get("wardName"));
        // 住院号
        rxMO.setInpatientNumber(patientInfoMap.get("inpatientNumber"));
        // 就诊卡号
        rxMO.setClinicCode(patientInfoMap.get("clinicCode"));
        // 设置门诊/科室
        rxMO.setDeptName(rxVO.getDepartment());
        // 设置是否妊娠（是/否）
        rxMO.setIsPregnant(rxVO.getIsPregnant());
        // 设置处方医师
        rxMO.setRxDoctor(rxVO.getPrescriberName());
        // 设置收货人
        rxMO.setConsignee(rxVO.getConsignee());
        // 设置收货联系电话
        rxMO.setConsigneePhone(rxVO.getConsigneePhone());
        if (ObjectUtil.isNotEmpty(rxVO.getReceiptRegion())) {
            String[] regionArr = rxVO.getReceiptRegion().split("-");
            if (regionArr.length == 2) {
                // 设置收货人所在省
                rxMO.setReceiverProvince(regionArr[0]);
                // 设置收货人所在市
                rxMO.setReceiverCity(regionArr[1]);
                // 设置收货人所在区
                rxMO.setReceiverDistrict(regionArr[2]);
            }
        }
        // 设置收货地址
        rxMO.setReceiptAddress(rxVO.getReceiptAddress());
        // 设置预计交货日期
        // rxMO.setExpectedDeliveryDate("2025-07-23");
        // 设置备注
        rxMO.setRemark(rxVO.getRemark());
        // 设置配送方式
        int deliveryMethodCode = Optional.ofNullable(rxVO.getIsExpress()).orElse("0").equals("1") ? EXPRESS.getCode() : HOSPITAL.getCode();
        rxMO.setDeliveryMethod(String.valueOf(deliveryMethodCode));
        // 设置是否快递（0.否,1.是）
        rxMO.setIsExpress(rxVO.getIsExpress());
        // 设置是否加急
        // rxMO.setIsUrgent(rxVO.get);
        // 设置接方员
        // rxMO.setReceiverId();
        // 设置当前时间的时间戳
        rxMO.setTimeStamp(String.valueOf(new Date().getTime()));
        // 设置签名
        // rxMO.setSign();
        if (ObjectUtil.isNotEmpty(rxVO.getRxDetails())) {
            // 设置煎药方案
            // rxMO.setDecoctScheme("微压(密闭)解表15min");
            // 设置服法
            rxMO.setTakingMethod(rxVO.getRxDetails().get(0).getDosageFrequency());
            // 设置煎药次数
            // rxMO.setDecoctTimes();
            // 设置剂数/帖数
            rxMO.setDoseCount(rxVO.getRxDetails().get(0).getDoseCount());
            // 设置一剂/贴
            rxMO.setOneDose(rxVO.getRxDetails().get(0).getOneDose());
            // 设置每包装量（ml）
            rxMO.setPerPackVolume(rxVO.getRxDetails().get(0).getPerPackVolume());
            // 设置处方类型
            rxMO.setRxType(rxVO.getRxDetails().get(0).getUsage());
            // 设置煎药要求
            rxMO.setDecoctRequire(rxVO.getRxDetails().get(0).getDecoctRequire());
        }
        List<RxDetailMO> rxDetailMOS = Optional.ofNullable(rxVO.getRxDetails()).orElse(new ArrayList<>()).stream().map(record -> {
            // 第一个明细项
            RxDetailMO detail = new RxDetailMO();
            // 客户饮片编码
            detail.setCustomerHerbCode(record.getHerbCode());
            // 客户饮片名称
            detail.setCustomerHerbName(record.getHerbName());
            // 饮片类型
            // detail.setHerbType();
            // 客户饮片用量（例如10.5克）
            detail.setCustomerHerbDosage(record.getHerbDosage());
            // 客户单位
            detail.setCustomerUnit(record.getUnit());
            // 客户规格
            detail.setCustomerSpec(record.getSpec());
            // 调配要求
            detail.setAdjustRequire(record.getAdjustRequire());
            // 调配位置
            // detail.setDeploymentPosition();
            // 客户单价
            detail.setCustomerPrice(record.getPrice());
            // 客户单剂金额
            detail.setCustomerMoney(record.getMoney());
            return detail;
        }).collect(Collectors.toList());
        // 设置到RxMO对象中
        rxMO.setDetails(rxDetailMOS);
        return rxMO;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDispenser(List<RxStatusVO> rxStatusVOList) {
        if (ObjectUtil.isEmpty(rxStatusVOList)) {
            return false;
        }
        rxStatusVOList.forEach(recordc -> {
            dispenser(recordc);
        });
        return true;
    }
}
