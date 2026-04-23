package com.tcm.rx.service.rx.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.util.concurrent.AtomicDouble;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.entity.Result;
import com.tcm.common.enums.BooleanEnum;
import com.tcm.common.enums.StatusEnum;
import com.tcm.common.exception.ServiceException;
import com.tcm.common.utils.AssertUtil;
import com.tcm.common.utils.CommonUtil;
import com.tcm.common.utils.EasyExcelUtil;
import com.tcm.rx.entity.auth.CUser;
import com.tcm.rx.entity.basicData.Herb;
import com.tcm.rx.entity.dept.Dept;
import com.tcm.rx.entity.hsp.Hsp;
import com.tcm.rx.entity.rx.RxMgmt;
import com.tcm.rx.entity.rx.RxMgmtDetail;
import com.tcm.rx.enums.HerbTypeEnum;
import com.tcm.rx.enums.HerbUnitEnum;
import com.tcm.rx.enums.MedicalInsuranceEnum;
import com.tcm.rx.feign.dict.vo.DictDataQueryVO;
import com.tcm.rx.feign.dict.vo.DictDataVO;
import com.tcm.rx.feign.herb.HerbStockFeignClient;
import com.tcm.rx.feign.herb.vo.CustomerHerbVO;
import com.tcm.rx.mapper.rx.RxMgmtMapper;
import com.tcm.rx.service.auth.ICUserService;
import com.tcm.rx.service.basicData.IHerbService;
import com.tcm.rx.service.basicData.IManageService;
import com.tcm.rx.service.dept.IDeptService;
import com.tcm.rx.service.dict.DictService;
import com.tcm.rx.service.hsp.IHspService;
import com.tcm.rx.service.rx.IRxMgmtDetailService;
import com.tcm.rx.service.rx.IRxMgmtService;
import com.tcm.rx.vo.basicData.request.HerbImportVO;
import com.tcm.rx.vo.basicData.request.HerbQueryVO;
import com.tcm.rx.vo.rx.request.*;
import com.tcm.rx.vo.rx.response.RxMgmtDetailVO;
import com.tcm.rx.vo.rx.response.RxMgmtVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 诊疗开方系统--方剂管理 服务实现类
 * </p>
 *
 * @author djbo
 * @since 2025-09-08
 */
@Slf4j
@Service
public class RxMgmtServiceImpl extends ServiceImpl<RxMgmtMapper, RxMgmt> implements IRxMgmtService {
    @Resource
    private ICUserService userService;
    @Resource
    private IRxMgmtDetailService rxMgmtDetailService;
    @Resource
    private IHerbService herbService;
    @Resource
    private HerbStockFeignClient herbStockFeignClient;
    @Resource
    private IDeptService deptService;
    @Resource
    private IManageService manageService;
    @Resource
    private IHspService hspService;
    @Resource
    private DictService dictService;

    /**
     * 分页查询
     */
    public List<RxMgmtVO> queryRxMgmtPage(RxMgmtQueryVO queryVO) {
        BaseUser currentUser = UserContextHolder.getUserInfoContext();
        queryVO.setMedicalGroupId(currentUser.getMedicalGroupId());
        List<RxMgmtVO> rxMgmtVOS = setRxMgmtInfo(this.getBaseMapper().queryRxMgmtPage(queryVO));
        // 库存是否充足
        setIsStockSuff(rxMgmtVOS);
        return rxMgmtVOS;
    }

    /**
     * 单剂总金额
     */
    /*public void setDoseTotalMoney(List<RxMgmtVO> rxMgmtVOS) {
        if (CollectionUtils.isEmpty(rxMgmtVOS)) {
            return;
        }
        List<String> herbCodeList = rxMgmtVOS.stream()
                .flatMap(rxMgmtVO -> {
                    List<RxMgmtDetailVO> details = rxMgmtVO.getRxMgmtDetailVOList();
                    return details != null ? details.stream() : Stream.empty();
                })
                .map(RxMgmtDetailVO::getHerbCode)
                .filter(herbCode -> herbCode != null)
                .collect(Collectors.toList());
        Map<String, Herb> herbHspMap = herbService.lambdaQuery()
                .in(Herb::getHerbCode, herbCodeList).list()
                .stream().collect(Collectors.toMap(Herb::getHerbCode, Herb -> Herb));
        Map<Long, List<RxMgmtDetail>> rxMgmtDetailMap = rxMgmtDetailService.lambdaQuery().in(RxMgmtDetail::getRxMgmtId,
                        rxMgmtVOS.stream().map(rxMgmt -> rxMgmt.getId()).collect(Collectors.toList()))
                .list().stream().collect(Collectors.groupingBy(RxMgmtDetail::getRxMgmtId));
        rxMgmtVOS.forEach(record -> {
            List<RxMgmtDetail> rxMgmtDetails = rxMgmtDetailMap.get(record.getId());
            if (ObjectUtil.isNotEmpty(rxMgmtDetails)) {
                BigDecimal doseTotalMoney = rxMgmtDetails.stream().map(rxDetail ->
                                Optional.ofNullable(rxDetail.getHerbDosage()).orElse(BigDecimal.ZERO)
                                        .divide(Optional.ofNullable(Optional.ofNullable(herbHspMap.get(rxDetail.getHerbCode()))
                                                .orElse(new Herb()).getRetailPrice()).orElse(BigDecimal.ZERO)))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                record.setDoseTotalMoney(doseTotalMoney);
            }
        });
    }*/

    /**
     * 设置库存是否充足
     *
     * @param rxMgmtVOS
     */
    private void setIsStockSuff(List<RxMgmtVO> rxMgmtVOS) {
        if (CollectionUtils.isEmpty(rxMgmtVOS)) {
            return;
        }
        rxMgmtVOS.forEach(rxMgmtVO -> {
            List<RxMgmtDetailVO> rxMgmtDetailVOS = rxMgmtDetailService.lambdaQuery().eq(RxMgmtDetail::getRxMgmtId, rxMgmtVO.getId()).list()
                    .stream().map(record -> {
                        RxMgmtDetailVO detailVO = new RxMgmtDetailVO();
                        BeanUtil.copyProperties(record, detailVO);
                        return detailVO;
                    }).collect(Collectors.toList());
            rxMgmtVO.setRxMgmtDetailVOList(rxMgmtDetailVOS);
        });
        BaseUser user = UserContextHolder.getUserInfoContext();
        List<CustomerHerbVO> customerHerbVOS = new ArrayList<>();
        Map<String, List<Long>> herbCodeToRxIdMap = new HashMap<>();
        rxMgmtVOS.forEach(rxMgmtVO -> {
            List<RxMgmtDetail> rxMgmtDetailList = getTransDetails(rxMgmtVO.getId());
            if (ObjectUtil.isNotEmpty(rxMgmtDetailList)) {
                rxMgmtDetailList.forEach(detail -> {
                    herbCodeToRxIdMap.computeIfAbsent(detail.getHerbCode(), k -> new ArrayList<>()).add(rxMgmtVO.getId());
                });
            }
        });

        log.info("setIsStockSuff.herbCodeToRxIdMap:{}", JSON.toJSONString(herbCodeToRxIdMap));
        Map<String, String> herbHspMap = new HashMap<>();
        if (ObjectUtil.isNotEmpty(herbCodeToRxIdMap)) {
            List<Herb> herbList = herbService.lambdaQuery()
                    .in(Herb::getHerbCode, herbCodeToRxIdMap.keySet()).list();
            herbList.forEach(herb -> herb.setHspIds(Optional.ofNullable(herb.getHspIds()).orElse("")));
            herbHspMap = herbList.stream().collect(Collectors.toMap(Herb::getHerbCode, Herb::getHspIds, (existingValue, newValue) -> newValue));
        }
        Map<String, String> finalHerbHspMap = herbHspMap;
        herbCodeToRxIdMap.keySet().forEach(herbCode -> {
            CustomerHerbVO customerHerbVO = new CustomerHerbVO();
            customerHerbVO.setMedicalGroupCode(user.getMedicalGroupCode());
            customerHerbVO.setCustomerHerbCode(herbCode);
            if (ObjectUtil.isNotEmpty(finalHerbHspMap.get(herbCode))
                    && finalHerbHspMap.get(herbCode).trim().split(",").length > 1) {
                String customerCode = "0".equals(user.getHspCode()) ? null : user.getHspCode();
                customerHerbVO.setCustomerCode(customerCode);
            } else {
                String hspId = "0".equals(finalHerbHspMap.get(herbCode)) ? null : finalHerbHspMap.get(herbCode);
                if(ObjectUtil.isNotEmpty(hspId)){
                    Hsp hsp = Optional.ofNullable(hspService.getById(Long.parseLong(hspId))).orElse(new Hsp());
                    customerHerbVO.setCustomerCode(hsp.getHspCode());
                }
            }
            customerHerbVOS.add(customerHerbVO);
        });
        log.info("setIsStockSuff.customerHerbVOS:{}", JSON.toJSONString(customerHerbVOS));
        Result result = herbStockFeignClient.queryCustomerHerbStock(customerHerbVOS);
        log.info("setIsStockSuff.result:{}", JSON.toJSONString(result));
        if (ObjectUtil.isEmpty(result.getData())) {
            return;
        }
        List<CustomerHerbVO> resultList = JSONArray.parseArray(JSON.toJSONString(result.getData())).toJavaList(CustomerHerbVO.class);
        log.info("setIsStockSuff.resultList:{}", JSON.toJSONString(resultList));
        Map<Long, RxMgmtVO> rxMgmtVOMap = rxMgmtVOS.stream()
                .collect(Collectors.toMap(RxMgmtVO::getId, rxMgmtVO -> rxMgmtVO));
        log.info("setIsStockSuff.rxMgmtVOMap:{}", JSON.toJSONString(rxMgmtVOMap));
        resultList.forEach(record -> {
            if (Optional.ofNullable(record.getUsableStock()).orElse(BigDecimal.ZERO).doubleValue() <= 0) {
                List<Long> rxIdList = herbCodeToRxIdMap.get(record.getCustomerHerbCode());
                if (ObjectUtil.isNotEmpty(rxIdList)) {
                    for (Long rxId : rxIdList) {
                        RxMgmtVO rxMgmtVO = rxMgmtVOMap.get(rxId);
                        if (ObjectUtil.isNotEmpty(rxMgmtVO)) {
                            rxMgmtVO.setIsStockSuff(false);
                        }
                    }
                }
            }
        });
    }

    /**
     * setRxMgmtInfo
     */
    private List<RxMgmtVO> setRxMgmtInfo(List<RxMgmtVO> rxMgmtVOS) {
        if (CollectionUtils.isEmpty(rxMgmtVOS)) {
            return new ArrayList<>();
        }
        List<Long> userIdList = rxMgmtVOS.stream()
                .flatMap(record -> Stream.of(Optional.ofNullable(record.getCreateBy()).orElse("-1"),
                                record.getUpdateBy())
                        .filter(Objects::nonNull))
                .map(record -> Long.parseLong(record)).collect(Collectors.toList());
        Map<Long, String> userMap = userService.listByIds(userIdList)
                .stream().collect(Collectors.toMap(CUser::getId, CUser::getRealName));
        for (RxMgmtVO rxMgmtVO : rxMgmtVOS) {
            if (ObjectUtil.isNotEmpty(rxMgmtVO.getCreateBy())) {
                rxMgmtVO.setCreateByName(userMap.get(Long.parseLong(rxMgmtVO.getCreateBy())));
            }
            if (ObjectUtil.isNotEmpty(rxMgmtVO.getUpdateBy())) {
                rxMgmtVO.setUpdateByName(userMap.get(Long.parseLong(rxMgmtVO.getUpdateBy())));
            }
        }
        return rxMgmtVOS;
    }

    /**
     * 查询详情
     *
     * @param id
     * @return
     */
    public RxMgmtVO getInfo(Long id) {
        if (ObjectUtil.isEmpty(this.getById(id))) {
            throw new ServiceException("处方不存在");
        }
        BaseUser user = UserContextHolder.getUserInfoContext();
        RxMgmtQueryVO queryVO = new RxMgmtQueryVO();
        queryVO.setId(id);
        queryVO.setMedicalGroupId(user.getMedicalGroupId());
        List<RxMgmtVO> rxMgmtVOS = this.getBaseMapper().queryRxMgmtPage(queryVO);
        if (ObjectUtil.isEmpty(rxMgmtVOS)) {
            return new RxMgmtVO();
        }
        setRxMgmtInfo(rxMgmtVOS);
        List<RxMgmtDetailVO> rxMgmtDetailVOList = rxMgmtDetailService.lambdaQuery().eq(RxMgmtDetail::getRxMgmtId, rxMgmtVOS.get(0).getId())
                .list().stream().map(record -> {
                    RxMgmtDetailVO rxMgmtDetailVO = new RxMgmtDetailVO();
                    BeanUtil.copyProperties(record, rxMgmtDetailVO);
                    return rxMgmtDetailVO;
                }).collect(Collectors.toList());
        rxMgmtVOS.get(0).setRxMgmtDetailVOList(rxMgmtDetailVOList);
        return rxMgmtVOS.get(0);
    }

    /**
     * 保存
     *
     * @param rxMgmtSaveVO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Long saveRxMgmt(RxMgmtSaveVO rxMgmtSaveVO) {
        RxMgmt rxMgmt = rxMgmtSaveVO.getRxMgmt();
        List<RxMgmtDetail> rxMgmtDetails = rxMgmtSaveVO.getRxMgmtDetails();
        if (!ObjectUtil.isAllNotEmpty(rxMgmt, rxMgmtDetails)) {
            throw new ServiceException("处方内容不可为空");
        }
        rxMgmtDetails = rxMgmtDetails.stream().filter(detail -> ObjectUtil.isNotEmpty(detail.getHerbName())).collect(Collectors.toList());
        List<String> herbNameList = rxMgmtDetails.stream().collect(Collectors.groupingBy(RxMgmtDetail::getHerbName)).entrySet()
                .stream().filter(entry -> entry.getValue().size() > 1).map(entry -> entry.getKey()).collect(Collectors.toList());
        if (ObjectUtil.isNotEmpty(herbNameList)) {
            throw new ServiceException(String.join("、", herbNameList) + " 饮片重复");
        }
        AssertUtil.assertArgs()
                .notEmpty(rxMgmt.getType(), "方剂类型")
                .notEmpty(rxMgmt.getName(), "方剂名称")
                .check();
        BaseUser currentUser = UserContextHolder.getUserInfoContext();
        String formula = String.join("、", rxMgmtDetails.stream()
                .map(detail -> Optional.ofNullable(detail.getHerbName()).orElse("")
                        .concat(Optional.ofNullable(detail.getHerbDosage())
                                .orElse(BigDecimal.ZERO).toString())
                        .concat(Optional.ofNullable(detail.getUnit()).orElse(""))).collect(Collectors.toList()));
        rxMgmt.setFormula(formula);
        if (ObjectUtil.isEmpty(rxMgmt.getId())) {
            CommonUtil.setRxBaseEntity(rxMgmt, currentUser);
            Optional<RxMgmt> optionalRxMgmt = this.lambdaQuery().eq(RxMgmt::getMedicalGroupId, currentUser.getMedicalGroupId())
                    .eq(RxMgmt::getType, rxMgmt.getType())
                    .eq(RxMgmt::getName, rxMgmt.getName()).list().stream().findFirst();
            if (optionalRxMgmt.isPresent()) {
                throw new ServiceException("方剂名称已存在");
            }
            this.save(rxMgmt);
        } else {
            Optional<RxMgmt> optionalRxMgmt = this.lambdaQuery().eq(RxMgmt::getMedicalGroupId, currentUser.getMedicalGroupId())
                    .eq(RxMgmt::getType, rxMgmt.getType())
                    .eq(RxMgmt::getName, rxMgmt.getName()).list().stream().findFirst();
            if (optionalRxMgmt.isPresent() && !(optionalRxMgmt.get().getId().longValue() == rxMgmt.getId())) {
                throw new ServiceException("方剂名称已存在");
            }
            rxMgmt.setUpdateBy(currentUser.getId().toString());
            rxMgmt.setUpdateTime(new Date());
            this.updateById(rxMgmt);
        }
        for (RxMgmtDetail rxMgmtDetail : rxMgmtDetails) {
            rxMgmtDetail.setRxMgmtId(rxMgmt.getId());
            CommonUtil.setRxBaseEntity(rxMgmtDetail, currentUser);
        }
        rxMgmtDetailService.remove(new LambdaQueryWrapper<RxMgmtDetail>()
                .eq(RxMgmtDetail::getRxMgmtId, rxMgmt.getId()));
        rxMgmtDetailService.saveBatch(rxMgmtDetails);
        return rxMgmt.getId();
    }

    /**
     * 删除
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        if (ObjectUtil.isEmpty(this.getById(id))) {
            throw new ServiceException("处方不存在");
        }
        this.removeById(id);
        rxMgmtDetailService.remove(new LambdaQueryWrapper<RxMgmtDetail>()
                .eq(RxMgmtDetail::getRxMgmtId, id));
        return true;
    }

    /**
     * 批量写入
     *
     * @param rxMgmts
     * @return
     */
    public Boolean batchInsertRxMgmt(RxMgmtImportVO rxMgmts) {
        if (ObjectUtil.isEmpty(rxMgmts.getList())) {
            return false;
        }
        BaseUser currentUser = UserContextHolder.getUserInfoContext();
        Map<String, List<JSONObject>> rxNameMap = rxMgmts.getList().stream()
                .collect(Collectors.groupingBy(json ->
                        json.getString("name")
                ));
        Map<Long, String> deptMap = deptService.lambdaQuery().eq(Dept::getMedicalGroupId, currentUser.getMedicalGroupId())
                .list().stream().collect(Collectors.toMap(Dept::getId, Dept::getDeptName, (k1, k2) -> k1));
        Map<Long, String> tcmDiseaseMap = manageService.tcmDiseaseMap();
        log.info("tcmDiseaseMap:{}", JSON.toJSONString(tcmDiseaseMap));
        Map<Long, String> tcmPatternMap = manageService.tcmPatternMap();
        log.info("tcmPatternMap:{}", JSON.toJSONString(tcmPatternMap));
        Map<Long, String> diagnosisMap = manageService.diagnosisMap();
        log.info("diagnosisMap:{}", JSON.toJSONString(diagnosisMap));
        rxNameMap.entrySet().forEach(entry -> {
            List<JSONObject> jsonObjList = entry.getValue();
            RxMgmt rxMgmt = jsonObjList.get(0).toJavaObject(RxMgmt.class);
            List<RxMgmtDetail> rxMgmtDetails = jsonObjList.stream().map(jsonObj -> {
                RxMgmtDetail rxMgmtDetail = jsonObj.toJavaObject(RxMgmtDetail.class);
                rxMgmtDetail.setRxMgmtId(rxMgmt.getId());
                CommonUtil.setRxBaseEntity(rxMgmtDetail, currentUser);
                return rxMgmtDetail;
            }).collect(Collectors.toList());
            CommonUtil.setRxBaseEntity(rxMgmt, currentUser);
            Optional<RxMgmt> optionalRxMgmt = this.lambdaQuery().eq(RxMgmt::getMedicalGroupId, currentUser.getMedicalGroupId())
                    .eq(RxMgmt::getType, rxMgmt.getType())
                    .eq(RxMgmt::getName, rxMgmt.getName()).list().stream().findFirst();
            if (optionalRxMgmt.isPresent()) {
                rxMgmt.setId(optionalRxMgmt.get().getId());
            }
            String formula = String.join("、", rxMgmtDetails.stream()
                    .map(detail -> Optional.ofNullable(detail.getHerbName()).orElse("")
                            .concat(Optional.ofNullable(detail.getHerbDosage())
                                    .orElse(BigDecimal.ZERO).toString()).concat(detail.getUnit())).collect(Collectors.toList()));
            rxMgmt.setFormula(formula);
            if (ObjectUtil.isNotEmpty(rxMgmt.getDeptNames())) {
                rxMgmt.setDeptIds(convert(deptMap, rxMgmt.getDeptNames(), true));
            }
            if (ObjectUtil.isNotEmpty(rxMgmt.getTcmDisease())) {
                rxMgmt.setTcmDiseaseIds(convert(tcmDiseaseMap, rxMgmt.getTcmDisease(), true));
            }
            if (ObjectUtil.isNotEmpty(rxMgmt.getTcmPattern())) {
                rxMgmt.setTcmPatternIds(convert(tcmPatternMap, rxMgmt.getTcmPattern(), true));
            }
            if (ObjectUtil.isNotEmpty(rxMgmt.getDiagnosis())) {
                rxMgmt.setDiagnosisIds(convert(diagnosisMap, rxMgmt.getDiagnosis(), true));
            }
            this.saveOrUpdate(rxMgmt);
            rxMgmtDetailService.remove(new LambdaQueryWrapper<RxMgmtDetail>()
                    .eq(RxMgmtDetail::getRxMgmtId, rxMgmt.getId()));
            rxMgmtDetails.forEach(record -> {
                record.setRxMgmtId(rxMgmt.getId());
                HerbQueryVO queryVO = new HerbQueryVO();
                queryVO.setHerbName(record.getHerbName());
                List<Herb> otherHerbList = herbService.herbNameMatch(queryVO);
                if (ObjectUtil.isNotEmpty(otherHerbList)) {
                    record.setSpec(otherHerbList.get(0).getSpec());
                    record.setPrice(otherHerbList.get(0).getRetailPrice());
                    BigDecimal money = Optional.ofNullable(otherHerbList.get(0).getRetailPrice()).orElse(BigDecimal.ZERO)
                            .multiply(Optional.ofNullable(record.getHerbDosage()).orElse(BigDecimal.ZERO));
                    record.setMoney(money);
                }
            });
            rxMgmtDetailService.saveBatch(rxMgmtDetails);
        });
        return true;
    }

    /**
     * 部门ID与名称的双向转换（map固定为key=ID(Long)，value=名称(String)）
     *
     * @param deptMap 部门映射关系，key为部门ID（Long），value为部门名称（String）
     * @param input   逗号分隔的输入字符串（名称或ID字符串）
     * @param isNames 输入类型标识：true表示输入的是名称，false表示输入的是ID字符串
     * @return 转换后的结果字符串（逗号分隔的ID或名称）
     */
    public static String convert(Map<Long, String> deptMap, String input, boolean isNames) {
        if (deptMap == null || input == null || input.trim().isEmpty()) {
            return "";
        }
        if (isNames) {
            Map<String, Long> nameToIdMap = deptMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey,
                            (existingValue, newValue) -> newValue));
            return Arrays.stream(input.split(","))
                    .map(String::trim)
                    .filter(name -> !name.isEmpty() && nameToIdMap.containsKey(name))
                    .map(name -> nameToIdMap.get(name).toString())
                    .collect(Collectors.joining(","));
        } else {
            return Arrays.stream(input.split(","))
                    .map(String::trim)
                    .filter(idStr -> !idStr.isEmpty())
                    .map(idStr -> {
                        try {
                            return Long.parseLong(idStr);
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    })
                    .filter(id -> id != null && deptMap.containsKey(id))
                    .map(deptMap::get)
                    .collect(Collectors.joining(","));
        }
    }

    /**
     * 经古名方导入
     *
     * @param file
     * @return
     * @throws Exception
     */
    public List rxMgmtImport(Integer type, MultipartFile file) throws Exception {
        List<Map<String, Object>> importList = new ArrayList<>();
        try {
            if (type == 1) {
                List<AncProvRxImportVO> importVOList = EasyExcelUtil.importService(file, AncProvRxImportVO.class);
                for (AncProvRxImportVO vo : importVOList) {
                    Map<String, Object> map = (JSONObject) JSON.toJSON(vo);
                    importList.add(map);
                }
            } else if (type == 2) {
                List<SpecDisRxImportVO> importVOList = EasyExcelUtil.importService(file, SpecDisRxImportVO.class);
                for (SpecDisRxImportVO vo : importVOList) {
                    Map<String, Object> map = (JSONObject) JSON.toJSON(vo);
                    importList.add(map);
                }
            } else if (type == 3) {
                List<SynDiffImportVO> importVOList = EasyExcelUtil.importService(file, SynDiffImportVO.class);
                for (SynDiffImportVO vo : importVOList) {
                    Map<String, Object> map = (JSONObject) JSON.toJSON(vo);
                    importList.add(map);
                }
            }
        } catch (Exception ex) {
            log.error("Excel导入失败，模板校验异常", ex);
            throw new ServiceException("模板校验异常");
        }

        if (importList.size() > 1000) {
            throw new ServiceException("一次最多导入1000条数据，请拆分文件后重试");
        }
        if (importList.isEmpty()) {
            throw new ServiceException("未检测到有效数据，请检查模板填写");
        }
        BaseUser user = UserContextHolder.getUserInfoContext();
        List<String> herbNameList = importList.stream().filter(record -> ObjectUtil.isNotEmpty(record.get("herbName")))
                .map(record -> record.get("herbName").toString())
                .collect(Collectors.toList());
        Map<String, Herb> herbMap = new HashMap<>();
        if (ObjectUtil.isNotEmpty(herbNameList)) {
            herbMap = herbService.lambdaQuery().eq(Herb::getMedicalGroupId, user.getMedicalGroupId())
                    .eq(Herb::getDelFlag, BooleanEnum.FALSE.getNum())
                    .in(Herb::getHerbName, herbNameList).list()
                    .stream().collect(Collectors.toMap(Herb::getHerbName, H -> H, (k1, k2) -> k1));
        }
        List herbList = new ArrayList<>();
        for (Map<String, Object> importMap : importList) {
            validateImportData(importMap, herbMap, type);
            herbList.add(importMap);
        }
        // 设置错误提示
        setRxMgmtErrInfo(herbList);
        return herbList;
    }

    private void validateField(Map<String, Object> importMap, String fieldName, String errorMsg, StringBuilder error) {
        if (ObjectUtil.isEmpty(importMap.get(fieldName))) {
            error.append(errorMsg).append("/n");
        }
    }

    /**
     * 校验导入数据
     *
     * @param importMap
     * @param herbMap
     */
    private void validateImportData(Map<String, Object> importMap, Map<String, Herb> herbMap, Integer type) {
        StringBuilder error = new StringBuilder();
        validateField(importMap, "name", "方剂名称不能为空", error);
        validateField(importMap, "helpCode", "助记码不能为空", error);
        validateField(importMap, "tcmDisease", "适应症不能为空", error);
        validateField(importMap, "tcmDiseaseHelpCode", "适应症助记码不能为空", error);
        validateField(importMap, "herbName", "饮片名称不能为空", error);
        validateField(importMap, "herbDosage", "饮片用量不能为空", error);
        validateField(importMap, "unit", "饮片单位不能为空", error);
        if (type == 1 || type == 2) {
            validateField(importMap, "mainEfficacy", "主治功效不能为空", error);
        }
        if (type == 2) {
            validateField(importMap, "treatmentDisease", "治疗疾病不能为空", error);
        }
        if (type == 3) {
            validateField(importMap, "alias", "方剂简称不能为空", error);
            validateField(importMap, "tcmPattern", "适应证候不能为空", error);
        }
        if (ObjectUtil.isNotEmpty(importMap.get("unit"))) {
            DictDataQueryVO decoctRequireVO = new DictDataQueryVO();
            decoctRequireVO.setDictType("decoctRequire");
            List<String> unitList = Optional.ofNullable(dictService.dictDataList(decoctRequireVO)).orElse(new ArrayList<>()).stream().map(item -> item.getDictLabel()).collect(Collectors.toList());
            if(!unitList.contains(importMap.get("unit"))) {
                error.append("饮片单位错误 可选值 " + String.join(",", unitList) + "/n");
            }
        }
        importMap.put("type", type);
        // 适应症、适应症助记码校验
        importMap.put("errMsg", error.toString());
    }

    /**
     * 设置错误提示
     *
     * @param mapList
     */
    public void setRxMgmtErrInfo(List<Map<String, Object>> mapList) {
        mapList.stream()
                .filter(map -> ObjectUtil.isNotEmpty(map.get("name")))
                .collect(Collectors.groupingBy(map -> (String) map.get("name")))
                .entrySet()
                .forEach(entry -> {
                    String deptNames = Optional.ofNullable(entry.getValue().get(0).get("deptNames")).orElse("").toString();
                    String alias = Optional.ofNullable(entry.getValue().get(0).get("alias")).orElse("").toString();
                    String helpCode = Optional.ofNullable(entry.getValue().get(0).get("helpCode")).orElse("").toString();
                    String treatmentDisease = Optional.ofNullable(entry.getValue().get(0).get("treatmentDisease")).orElse("").toString();
                    String mainEfficacy = Optional.ofNullable(entry.getValue().get(0).get("mainEfficacy")).orElse("").toString();
                    String diagnosis = Optional.ofNullable(entry.getValue().get(0).get("diagnosis")).orElse("").toString();
                    String tcmDisease = Optional.ofNullable(entry.getValue().get(0).get("tcmDisease")).orElse("").toString();
                    String tcmPattern = Optional.ofNullable(entry.getValue().get(0).get("tcmPattern")).orElse("").toString();
                    String diagnosisHelpCode = Optional.ofNullable(entry.getValue().get(0).get("diagnosisHelpCode")).orElse("").toString();
                    String tcmDiseaseHelpCode = Optional.ofNullable(entry.getValue().get(0).get("tcmDiseaseHelpCode")).orElse("").toString();
                    String source = Optional.ofNullable(entry.getValue().get(0).get("source")).orElse("").toString();
                    String usage = Optional.ofNullable(entry.getValue().get(0).get("usage")).orElse("").toString();
                    String formula = Optional.ofNullable(entry.getValue().get(0).get("formula")).orElse("").toString();
                    String taboo = Optional.ofNullable(entry.getValue().get(0).get("taboo")).orElse("").toString();
                    String cookingMethod = Optional.ofNullable(entry.getValue().get(0).get("cookingMethod")).orElse("").toString();
                    String similarChinesePatent = Optional.ofNullable(entry.getValue().get(0).get("similarChinesePatent")).orElse("").toString();
                    String rxExplanation = Optional.ofNullable(entry.getValue().get(0).get("rxExplanation")).orElse("").toString();
                    String remark = Optional.ofNullable(entry.getValue().get(0).get("remark")).orElse("").toString();
                    entry.getValue().forEach(record1 -> {
                        if (!StringUtils.equals(deptNames, Optional.ofNullable(record1.get("deptNames")).orElse("").toString())) {
                            setErrMsg(record1, "同一个方剂名称，科室名称必须相同");
                        }
                        if (!StringUtils.equals(alias, Optional.ofNullable(record1.get("alias")).orElse("").toString())) {
                            setErrMsg(record1, "同一个方剂名称，简称必须相同");
                        }
                        if (!StringUtils.equals(helpCode, Optional.ofNullable(record1.get("helpCode")).orElse("").toString())) {
                            setErrMsg(record1, "同一个方剂名称，助记码必须相同");
                        }
                        if (!StringUtils.equals(treatmentDisease, Optional.ofNullable(record1.get("treatmentDisease")).orElse("").toString())) {
                            setErrMsg(record1, "同一个方剂名称，治疗疾病必须相同");
                        }
                        if (!StringUtils.equals(mainEfficacy, Optional.ofNullable(record1.get("mainEfficacy")).orElse("").toString())) {
                            setErrMsg(record1, "同一个方剂名称，主治功效必须相同");
                        }
                        if (!StringUtils.equals(diagnosis, Optional.ofNullable(record1.get("diagnosis")).orElse("").toString())) {
                            setErrMsg(record1, "同一个方剂名称，西医疾病必须相同");
                        }
                        if (!StringUtils.equals(tcmDisease, Optional.ofNullable(record1.get("tcmDisease")).orElse("").toString())) {
                            setErrMsg(record1, "同一个方剂名称，中医疾病必须相同");
                        }
                        if (!StringUtils.equals(tcmPattern, Optional.ofNullable(record1.get("tcmPattern")).orElse("").toString())) {
                            setErrMsg(record1, "同一个方剂名称，中医证候必须相同");
                        }
                        if (!StringUtils.equals(diagnosisHelpCode, Optional.ofNullable(record1.get("diagnosisHelpCode")).orElse("").toString())) {
                            setErrMsg(record1, "同一个方剂名称，西医疾病助记码必须相同");
                        }
                        if (!StringUtils.equals(tcmDiseaseHelpCode, Optional.ofNullable(record1.get("tcmDiseaseHelpCode")).orElse("").toString())) {
                            setErrMsg(record1, "同一个方剂名称，中医疾病助记码必须相同");
                        }
                        if (!StringUtils.equals(source, Optional.ofNullable(record1.get("source")).orElse("").toString())) {
                            setErrMsg(record1, "同一个方剂名称，方剂来源必须相同");
                        }
                        if (!StringUtils.equals(usage, Optional.ofNullable(record1.get("usage")).orElse("").toString())) {
                            setErrMsg(record1, "同一个方剂名称，用法必须相同");
                        }
                        if (!StringUtils.equals(formula, Optional.ofNullable(record1.get("formula")).orElse("").toString())) {
                            setErrMsg(record1, "同一个方剂名称，方剂组成必须相同");
                        }
                        if (!StringUtils.equals(taboo, Optional.ofNullable(record1.get("taboo")).orElse("").toString())) {
                            setErrMsg(record1, "同一个方剂名称，禁忌必须相同");
                        }
                        if (!StringUtils.equals(cookingMethod, Optional.ofNullable(record1.get("cookingMethod")).orElse("").toString())) {
                            setErrMsg(record1, "同一个方剂名称，熟煮方法必须相同");
                        }
                        if (!StringUtils.equals(similarChinesePatent, Optional.ofNullable(record1.get("similarChinesePatent")).orElse("").toString())) {
                            setErrMsg(record1, "同一个方剂名称，同类中成药必须相同");
                        }
                        if (!StringUtils.equals(rxExplanation, Optional.ofNullable(record1.get("rxExplanation")).orElse("").toString())) {
                            setErrMsg(record1, "同一个方剂名称，方解必须相同");
                        }
                        if (!StringUtils.equals(remark, Optional.ofNullable(record1.get("remark")).orElse("").toString())) {
                            setErrMsg(record1, "同一个方剂名称，按语必须相同");
                        }
                    });
                    Map<String, List<Map<String, Object>>> importVOMap = entry.getValue().stream()
                            .filter(map -> ObjectUtil.isNotEmpty(map.get("herbName")))
                            .collect(Collectors.groupingBy(map -> (String) map.get("herbName")));
                    importVOMap.entrySet().forEach(entry1 -> {
                        if (entry1.getValue().size() > 1) {
                            entry1.getValue().forEach(map -> {
                                StringBuilder error = new StringBuilder();
                                Object errMsgObj = map.get("errMsg");
                                if (ObjectUtil.isNotEmpty(errMsgObj)) {
                                    error.append(errMsgObj.toString());
                                }
                                error.append("饮片名称重复/n");
                                map.put("errMsg", error.toString());
                            });
                        }
                    });
                });
    }

    /**
     * 设置错误提示
     *
     * @param importMap
     * @param errInfo
     */
    public void setErrMsg(Map<String, Object> importMap, String errInfo) {
        StringBuilder error = new StringBuilder();
        String errMsg = Optional.ofNullable(importMap.get("errMsg")).orElse("").toString();
        if (ObjectUtil.isNotEmpty(errMsg)) {
            error.append(errMsg);
        }
        error.append(errInfo);
        importMap.put("errMsg", error.toString());
    }

    /**
     * 专病专方疾病列表
     *
     * @return
     */
    public List<String> getTreatDis(String treatDis) {
        RxMgmtQueryVO queryVO = new RxMgmtQueryVO();
        queryVO.setTreatmentDisease(treatDis);
        queryVO.setType(2);
        return queryRxMgmtPage(queryVO).stream()
                .filter(record -> ObjectUtil.isNotEmpty(record.getTreatmentDisease()))
                .map(record -> record.getTreatmentDisease()).distinct().collect(Collectors.toList());
    }

    public List<RxMgmtDetail> getTransDetails(Long id) {
        List<RxMgmtDetail> details = rxMgmtDetailService.lambdaQuery().eq(RxMgmtDetail::getRxMgmtId, id).list();
        if(ObjectUtil.isEmpty(details)){
            return new ArrayList<>();
        }
        List<RxMgmtDetail> detailList = new ArrayList<>();
        if(ObjectUtil.isNotEmpty(details)) {
            for(RxMgmtDetail rxMgmtDetail : details) {
                HerbQueryVO queryVO = new HerbQueryVO();
                queryVO.setHerbName(rxMgmtDetail.getHerbName());
                List<Herb> otherHerbList = herbService.herbNameMatch(queryVO);
                if(ObjectUtil.isNotEmpty(otherHerbList)) {
                    RxMgmtDetail detail = new RxMgmtDetail();
                    detail.setHerbName(otherHerbList.get(0).getHerbName());
                    detail.setHerbCode(otherHerbList.get(0).getHerbCode());
                    detail.setSpec(otherHerbList.get(0).getSpec());
                    detail.setUnit(otherHerbList.get(0).getUnit());
                    detail.setPrice(otherHerbList.get(0).getRetailPrice());
                    detail.setHerbDosage(rxMgmtDetail.getHerbDosage());
                    BigDecimal money = Optional.ofNullable(detail.getPrice()).orElse(BigDecimal.ZERO)
                            .multiply(Optional.ofNullable(detail.getHerbDosage()).orElse(BigDecimal.ZERO));
                    detail.setMoney(money);
                    detailList.add(detail);
                }
            }
        }
        return detailList;
    }
}
