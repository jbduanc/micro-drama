package com.tcm.rx.service.basicData.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.entity.Result;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.common.enums.BooleanEnum;
import com.tcm.common.enums.StatusEnum;
import com.tcm.common.enums.SyncStatusEnum;
import com.tcm.common.exception.ServiceException;
import com.tcm.common.mo.PurchaseMO;
import com.tcm.common.utils.CommonUtil;
import com.tcm.common.utils.EasyExcelUtil;
import com.tcm.rx.entity.auth.CUser;
import com.tcm.rx.entity.basicData.Herb;
import com.tcm.rx.entity.dept.Dept;
import com.tcm.rx.entity.hsp.Hsp;
import com.tcm.rx.enums.HerbTypeEnum;
import com.tcm.rx.enums.HerbUnitEnum;
import com.tcm.rx.enums.MedicalInsuranceEnum;
import com.tcm.rx.feign.customer.CustomerFeignClient;
import com.tcm.rx.feign.customer.vo.CustomerVO;
import com.tcm.rx.feign.herb.herbCustomer.HerbCustomerFeignClient;
import com.tcm.rx.feign.herb.herbCustomer.vo.HerbCustomer;
import com.tcm.rx.mapper.basicData.HerbMapper;
import com.tcm.rx.service.auth.ICUserService;
import com.tcm.rx.service.basicData.IHerbService;
import com.tcm.rx.service.hsp.IHspService;
import com.tcm.rx.vo.basicData.request.HerbImportVO;
import com.tcm.rx.vo.basicData.request.HerbQueryVO;
import com.tcm.rx.vo.basicData.response.HerbExportVO;
import com.tcm.rx.vo.basicData.response.HerbSyncExportVO;
import com.tcm.rx.vo.basicData.response.HerbVO;
import com.tcm.rx.vo.herb.request.HerbSyncVO;
import com.tcm.rx.vo.herb.response.HerbSyncDataVO;
import com.tcm.rx.vo.herb.response.HerbSyncDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HerbServiceImpl extends ServiceImpl<HerbMapper, Herb> implements IHerbService {

    @Resource
    private IHspService hspService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CustomerFeignClient customerFeignClient;

    @Autowired
    private HerbCustomerFeignClient herbCustomerFeignClient;

    @Resource
    private ICUserService cUserService;

    @Override
    public List<HerbVO> queryPage(HerbQueryVO queryVO) {
        // 补充当前登录用户的机构信息
        BaseUser user = UserContextHolder.getUserInfoContext();
        queryVO.setMedicalGroupId(user.getMedicalGroupId());
        if (ObjectUtil.isNotEmpty(queryVO.getHspCode()) && ObjectUtil.isEmpty(queryVO.getHspId())) {
            Optional<Hsp> optionalHsp = hspService.lambdaQuery().eq(Hsp::getMedicalGroupId, user.getMedicalGroupId())
                    .eq(Hsp::getHspCode, queryVO.getHspCode())
                    .list().stream().findFirst();
            if (optionalHsp.isPresent()) {
                queryVO.setHspId(optionalHsp.get().getId().toString());
                queryVO.setHspCode(null);
            }
        }
        // 查询数据并转换为VO
        List<Herb> herbList = baseMapper.selectByQuery(queryVO);
        Map<Long, String> hspMap = hspService.lambdaQuery().eq(Hsp::getMedicalGroupId, user.getMedicalGroupId())
                .list().stream().collect(Collectors.toMap(Hsp::getId, Hsp::getHspName));
        return herbList.stream().map(herb -> {
            HerbVO vo = new HerbVO();
            BeanUtils.copyProperties(herb, vo);
            vo.setStatus(StatusEnum.CODE_MAP.get(herb.getStatus()));
            if (ObjectUtil.isNotEmpty(herb.getHspIds())) {
                List<String> hspNameList = new ArrayList<>();
                for (String hspId : herb.getHspIds().split(",")) {
                    if(ObjectUtil.isNotEmpty(hspMap.get(Long.parseLong(hspId)))){
                        hspNameList.add(hspMap.get(Long.parseLong(hspId)));
                    }
                }
                vo.setHspNames(String.join(",", hspNameList));
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void export(HerbQueryVO queryVO, HttpServletResponse response) throws IOException {
        List<HerbVO> dataList = this.queryPage(queryVO);
        if (CollectionUtils.isEmpty(dataList)) {
            throw new ServiceException("导出数据为空");
        }
        BaseUser user = UserContextHolder.getUserInfoContext();
        List<HerbExportVO> exportList = dataList.stream().map(vo -> {
            HerbExportVO exportVO = new HerbExportVO();
            BeanUtils.copyProperties(vo, exportVO);
            exportVO.setMedicalInsurance(MedicalInsuranceEnum.CODE_MAP.get(vo.getMedicalInsurance()));
            if (ObjectUtil.isNotEmpty(vo.getUpdateTime())) {
                exportVO.setUpdateTime(new DateTime(vo.getUpdateTime()).toString("yyyy-MM-dd HH:mm:ss"));
            }
            return exportVO;
        }).collect(Collectors.toList());

        // 导出文件
        EasyExcelUtil.exportService(response, "饮片管理数据", "饮片列表", HerbExportVO.class, exportList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long add(HerbVO herbVO) {
        this.validateAddData(herbVO);
        BaseUser user = UserContextHolder.getUserInfoContext();
        Herb herb = new Herb();
        BeanUtils.copyProperties(herbVO, herb);
        if (ObjectUtil.isNotEmpty(this.lambdaQuery().eq(Herb::getHerbCode, herb.getHerbCode()).eq(Herb::getDelFlag, BooleanEnum.FALSE.getNum()).list())) {
            throw new ServiceException("饮片编码重复");
        }
        if (ObjectUtil.isNotEmpty(herb.getHspIds())) {
            Map<Long, String> hspMap = hspService.lambdaQuery().eq(Hsp::getMedicalGroupId, user.getMedicalGroupId())
                    .list().stream().collect(Collectors.toMap(Hsp::getId, Hsp::getHspCode));
            List<String> hspCodeList = new ArrayList<>();
            for (String hspId : herb.getHspIds().split(",")) {
                if(ObjectUtil.isNotEmpty(hspMap.get(hspId))){
                    hspCodeList.add(hspMap.get(hspId));
                }
            }
            herb.setHspCodes(String.join(",", hspCodeList));
        }
        CommonUtil.setRxBaseEntity(herb, user);
        if(ObjectUtil.isNotEmpty(herb.getHerbCode())){
            herb.setHerbCode(herb.getHerbCode().trim());
        }
        this.save(herb);
        return herb.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long update(Herb herb) {
        if (Objects.isNull(herb.getId())) {
            throw new ServiceException("ID不能为空");
        }
        if (ObjectUtil.isNotEmpty(herb.getHspIds())) {
            BaseUser user = UserContextHolder.getUserInfoContext();
            Map<Long, String> hspMap = hspService.lambdaQuery().eq(Hsp::getMedicalGroupId, user.getMedicalGroupId())
                    .list().stream().collect(Collectors.toMap(Hsp::getId, Hsp::getHspCode));
            List<String> hspCodeList = new ArrayList<>();
            for (String hspId : herb.getHspIds().split(",")) {
                if(ObjectUtil.isNotEmpty(hspMap.get(hspId))){
                    hspCodeList.add(hspMap.get(hspId));
                }
            }
            herb.setHspCodes(String.join(",", hspCodeList));
        }
        herb.setUpdateBy(UserContextHolder.getUserInfoContext().getId().toString());
        herb.setUpdateTime(new Date());
        this.updateById(herb);
        return herb.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Long id) {
        Herb herb = this.getById(id);
        if (Objects.isNull(herb)) {
            throw new ServiceException("数据不存在");
        }
        // 逻辑删除
        herb.setDelFlag(BooleanEnum.TRUE.getNum());
        herb.setUpdateTime(new Date());
        this.saveOrUpdate(herb);
    }

    @Override
    public Map<String, Herb> getMapByCodes(List<String> herbCodes) {
        List<Herb> herbList = this.lambdaQuery().in(Herb::getHerbCode, herbCodes).eq(Herb::getDelFlag, BooleanEnum.FALSE.getNum()).list();
        Map<String, Herb> herbMap = herbList.stream().collect(Collectors.toMap(Herb::getHerbCode, v1 -> v1, (k1, k2) -> k1));
        return herbMap;
    }

    @Override
    public TablePageInfo<HerbSyncDataVO> syncHerbList(HerbSyncVO syncVO) {
        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        Sort sort = Sort.by(Sort.Order.asc("syncStatus"), Sort.Order.desc("createTime"));
        Pageable pageable = PageRequest.of(syncVO.getPage()-1,syncVO.getSize(), sort);
        Criteria criteria = Criteria
                .where("medicalGroupCode").is(loginUser.getMedicalGroupCode());
        if (StringUtils.isNotEmpty(syncVO.getStartTime()) && StringUtils.isNotEmpty(syncVO.getEndTime())) {
            try {
                criteria.and("createTime").gte(DateUtils.parseDate(syncVO.getStartTime() + " 00:00:00", "yyyy-MM-dd HH:mm:ss").getTime()).lte(DateUtils.parseDate(syncVO.getEndTime() + " 23:59:59", "yyyy-MM-dd HH:mm:ss").getTime());
            } catch (ParseException e) {
                throw new ServiceException(e.getMessage());
            }
        }
        if (StringUtils.isNotEmpty(syncVO.getPurchaseNo())) {
            criteria.and("purchaseNo").regex(syncVO.getPurchaseNo());
        }
        Query query= new Query(criteria);
        Long total = mongoTemplate.count(query, PurchaseMO.class);
        List<PurchaseMO> purchaseMOS = mongoTemplate.find(query.with(pageable), PurchaseMO.class);
        List<HerbSyncDataVO> result = new ArrayList<>();
        purchaseMOS.forEach(p -> {
            HerbSyncDataVO herbSyncDataVO = new HerbSyncDataVO();
            herbSyncDataVO.setBusinessDate(p.getBusinessDate());
            herbSyncDataVO.setPurchaseNo(p.getPurchaseNo());
            herbSyncDataVO.setSyncStatus(p.getSyncStatus());
            herbSyncDataVO.setPushDate(p.getCreateTimeStr());
            List<HerbSyncDetailVO> detailList = new ArrayList<>();
            p.getDetails().forEach(detail -> {
                HerbSyncDetailVO herbSyncDetailVO = new HerbSyncDetailVO();
                herbSyncDetailVO.setCustomerCode(p.getCustomerCode());
                herbSyncDetailVO.setCustomerHerbCode(detail.getCustomerHerbCode());
                herbSyncDetailVO.setCustomerHerbSpec(detail.getCustomerHerbSpec());
                herbSyncDetailVO.setCustomerHerbUnit(detail.getCustomerHerbUnit());
                herbSyncDetailVO.setCostPrice(detail.getCostPrice());
                herbSyncDetailVO.setRetailPrice(detail.getRetailPrice());
                herbSyncDetailVO.setCustomerHerbNumber(detail.getCustomerHerbNumber());
                herbSyncDetailVO.setCustomerHerbName(detail.getCustomerHerbName());
                detailList.add(herbSyncDetailVO);
            });
            herbSyncDataVO.setHerbSyncDetailVOList(detailList);
            result.add(herbSyncDataVO);
        });
        return new TablePageInfo(result, total.intValue());
    }

    @Override
    @Transactional
    public Boolean syncHerb(HerbSyncDataVO syncDataVO) {
        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        List<Herb> herbList = new ArrayList<>();
        syncDataVO.getHerbSyncDetailVOList().forEach(detail -> {
             Herb herb = this.lambdaQuery()
                     .eq(Herb::getHerbCode, detail.getCustomerHerbCode())
                     .eq(Herb::getMedicalGroupCode, loginUser.getMedicalGroupCode())
                     .eq(Herb::getDelFlag, BooleanEnum.FALSE.getNum())
                     .one();
             if (ObjectUtil.isEmpty(herb)) {
                 herbList.add(setHerb(detail, loginUser, syncDataVO.getPurchaseNo()));
             } else {
                 herb.setCostPrice(detail.getCostPrice());
                 herb.setRetailPrice(detail.getRetailPrice());
                 herb.setHerbName(detail.getCustomerHerbName());
                 herb.setSpec(detail.getCustomerHerbSpec());
                 herb.setUnit(detail.getCustomerHerbUnit());
                 herb.setHisHerbId(syncDataVO.getPurchaseNo());
                 herb.setUpdateBy(String.valueOf(loginUser.getId()));
                 herb.setUpdateTime(new Date());
                 herbList.add(herb);
             }
        });
        if (CollectionUtil.isNotEmpty(herbList)){
            saveOrUpdateBatch(herbList);
        }
        Criteria criteria = Criteria
                .where("syncStatus").is(SyncStatusEnum.UNSYNC.getCode())
                .and("medicalGroupCode").is(loginUser.getMedicalGroupCode())
                .and("purchaseNo").is(syncDataVO.getPurchaseNo());
        Query query = new Query(criteria);
        mongoTemplate.updateFirst(query, new Update().set("syncStatus", SyncStatusEnum.SYNC.getCode())
                .set("syncTime", new DateTime().toString("yyyy-MM-dd HH:mm:ss")), PurchaseMO.class);
        return Boolean.TRUE;
    }

    private Herb setHerb(HerbSyncDetailVO detail, BaseUser loginUser, String purchaseNo) {
        Herb herb = new Herb();
        herb.setHerbCode(detail.getCustomerHerbCode());
        herb.setHerbName(detail.getCustomerHerbName());
        herb.setSpec(detail.getCustomerHerbSpec());
        herb.setUnit(detail.getCustomerHerbUnit());
        herb.setType("中草药");
        herb.setHisHerbId(purchaseNo);
        herb.setMedicalCode(loginUser.getMedicalGroupCode());
        herb.setMedicalGroupId(loginUser.getMedicalGroupId());
        herb.setCostPrice(detail.getCostPrice());
        herb.setRetailPrice(detail.getRetailPrice());
        CommonUtil.setRxBaseEntity(herb, loginUser);
        return herb;
    }

    private void checkHerbCustomerData(HerbSyncDetailVO detail, String medicalGroupCode) {
        HerbCustomer herbCustomer = new HerbCustomer();
        herbCustomer.setMedicalGroupCode(medicalGroupCode);
        herbCustomer.setCustomerCode(detail.getCustomerCode());
        herbCustomer.setCustomerGoodsCode(detail.getCustomerHerbCode());
        Result result = herbCustomerFeignClient.findHerbCustomer(herbCustomer);
        log.info("远程调用客户对应关系结果为" + JSONObject.toJSONString(result));
        List<HerbCustomer> herbCustomers = JSONUtil.toList(JSONUtil.toJsonStr(result.getData()), HerbCustomer.class);
        if (CollectionUtil.isEmpty(herbCustomers)) {
            throw new ServiceException("医疗机构饮片关系不存在");
        }
    }

    private List<CustomerVO> checkCustomerExist(String customerCode, String medicalGroupCode)  {
        Result result = customerFeignClient.customerList(medicalGroupCode);
        log.info("远程调用客户结果为" + JSONObject.toJSONString(result));
        List<CustomerVO> customerVOS = JSONUtil.toList(JSONUtil.toJsonStr(result.getData()), CustomerVO.class);
        List<String> customerCodes = customerVOS.stream().map(CustomerVO::getCustomerCode).collect(Collectors.toList());
        if (!customerCodes.contains(customerCode)) {
            throw new ServiceException("医疗机构不存在");
        }
        return customerVOS;
    }

    // 私有方法：校验新增数据
    private void validateAddData(HerbVO herbVO) {
        if (StringUtils.isBlank(herbVO.getHerbCode())) {
            throw new ServiceException("饮片编码不能为空");
        }
        // 校验编码唯一性
        long count = this.count(new LambdaQueryWrapper<Herb>()
                .eq(Herb::getMedicalGroupId, herbVO.getMedicalGroupId())
                .eq(Herb::getHerbCode, herbVO.getHerbCode())
                .eq(Herb::getDelFlag, BooleanEnum.FALSE.getNum()));
        if (count > 0) {
            throw new ServiceException("饮片编码已存在");
        }
    }

    public Boolean batchInsertHerb(List<HerbImportVO> herbVOList) {
        if (ObjectUtil.isEmpty(herbVOList)) {
            return false;
        }
        BaseUser user = UserContextHolder.getUserInfoContext();
        List<Herb> herbList = new ArrayList<>();
        Map<String, Hsp> hspMap = hspService.lambdaQuery().eq(Hsp::getMedicalGroupId, user.getMedicalGroupId())
                .list().stream().collect(Collectors.toMap(Hsp::getHspName, H -> H, (k1, k2) -> k1));
        herbVOList.forEach(importVO -> {
            // 4. 转换为Herb实体（处理枚举映射）
            Herb herb = new Herb();
            BeanUtils.copyProperties(importVO, herb);

            // 数字字段转换（成本价、零售价、毛利率）
            herb.setCostPrice(new BigDecimal(importVO.getCostPrice()));
            herb.setRetailPrice(new BigDecimal(importVO.getRetailPrice()));
            if (StringUtils.isNotBlank(importVO.getGrossMargin())) {
                herb.setGrossMargin(new BigDecimal(importVO.getGrossMargin()));
            }
            // 状态转换："启用"→false（对应StatusEnum.ENABLED），"禁用"→true（对应StatusEnum.DISABLED）
            herb.setStatus(StatusEnum.NAME_MAP.get(importVO.getStatus()));
            herb.setMedicalInsurance(MedicalInsuranceEnum.NAME_MAP.get(importVO.getMedicalInsurance()));
            // 机构信息填充
            if (ObjectUtil.isNotEmpty(importVO.getHspNames())) {
                List<String> hspIdList = new ArrayList<>();
                List<String> hspCodeList = new ArrayList<>();
                for (String hspName : importVO.getHspNames().trim().split(",")) {
                    if(ObjectUtil.isNotEmpty(hspMap.get(hspName.trim()))){
                        hspIdList.add(hspMap.get(hspName.trim()).getId().toString());
                        hspCodeList.add(hspMap.get(hspName.trim()).getHspCode());
                    }
                }
                herb.setHspIds(String.join(",", hspIdList));
                herb.setHspCodes(String.join(",", hspCodeList));
            }
            CommonUtil.setRxBaseEntity(herb, user);

            Optional<Herb> dbHerbOptional = this.lambdaQuery()
                    .eq(Herb::getDelFlag, BooleanEnum.FALSE.getNum())
                    .eq(Herb::getMedicalGroupId, herb.getMedicalGroupId())
                    .eq(Herb::getHerbCode, herb.getHerbCode()).list().stream().findFirst();
            if (dbHerbOptional.isPresent()) {
                herb.setId(dbHerbOptional.get().getId());
            }
            herbList.add(herb);
        });
        return this.saveOrUpdateBatch(herbList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<HerbImportVO> importData(MultipartFile file) throws Exception {
        // 1. 导入并过滤空白行（编码非空才处理）
        List<HerbImportVO> importList = EasyExcelUtil.importService(file, HerbImportVO.class);

        // 2. 数据量校验（≤1000条）
        if (importList.size() > 1000) {
            throw new ServiceException("一次最多导入1000条数据，请拆分文件后重试");
        }
        if (importList.isEmpty()) {
            throw new ServiceException("未检测到有效数据，请检查模板填写");
        }
        BaseUser user = UserContextHolder.getUserInfoContext();
        Map<String, Hsp> hspMap = hspService.lambdaQuery().eq(Hsp::getMedicalGroupId, user.getMedicalGroupId())
                .list().stream().collect(Collectors.toMap(Hsp::getHspName, H -> H, (k1, k2) -> k1));
        Map<String,String> herbMap = this.lambdaQuery().eq(Herb::getMedicalGroupId, user.getMedicalGroupId()).list().stream()
                .collect(Collectors.toMap(Herb::getHerbCode, Herb::getHerbName, (k1, k2) -> k1));

        List<HerbImportVO> herbList = new ArrayList<>();
        for (HerbImportVO importVO : importList) {
            // 3. 单条数据校验（覆盖枚举/接口规则）
            validateImportData(importVO, hspMap, herbMap);
            herbList.add(importVO);
        }
        return herbList;
    }

    private void validateImportData(HerbImportVO importVO, Map<String, Hsp> hspMap, Map<String,String> herbMap) {
        StringBuilder error = new StringBuilder();

        // 1. 必填项校验（原型要求的9个字段）
        if (StringUtils.isBlank(importVO.getHerbCode())) error.append("饮片编码不能为空/n");
        if (StringUtils.isBlank(importVO.getHerbName())) error.append("饮片名称不能为空/n");
        if (StringUtils.isBlank(importVO.getSpec())) error.append("规格不能为空/n");
        if (StringUtils.isBlank(importVO.getType())) error.append("饮片类型不能为空/n");
        if (StringUtils.isBlank(importVO.getUnit())) error.append("单位不能为空/n");
        if (StringUtils.isBlank(importVO.getCostPrice())) error.append("成本价不能为空/n");
        if (StringUtils.isBlank(importVO.getRetailPrice())) error.append("零售价不能为空/n");
        if (StringUtils.isBlank(importVO.getStatus())) error.append("状态不能为空/n");
        if (StringUtils.isBlank(importVO.getMedicalInsurance())) error.append("是否医保不能为空/n");

        // 2. 格式校验（数字字段）
        if (StringUtils.isNotBlank(importVO.getCostPrice()) && !isValidDecimal(importVO.getCostPrice())) {
            error.append("成本价格式错误（需为数字）/n");
        }
        if (StringUtils.isNotBlank(importVO.getRetailPrice()) && !isValidDecimal(importVO.getRetailPrice())) {
            error.append("零售价格式错误（需为数字）/n");
        }
        if (StringUtils.isNotBlank(importVO.getGrossMargin()) && !isValidDecimal(importVO.getGrossMargin())) {
            error.append("毛利率格式错误（需为数字）/n");
        }
        // 3. 枚举/接口值校验
        // （1）饮片类型
        List<String> herbTypeList = Arrays.stream(HerbTypeEnum.values()).map(record -> record.getDescription()).collect(Collectors.toList());
        if (!herbTypeList.contains(importVO.getType())) {
            error.append("饮片类型错误，可选值：").append(String.join("、", herbTypeList)).append("/n");
        }
        // （2）饮片单位
        List<String> herbUnitList = Arrays.stream(HerbUnitEnum.values()).map(record -> record.getUnit()).collect(Collectors.toList());
        if (!herbUnitList.contains(importVO.getUnit())) {
            error.append("饮片单位错误，可选值：").append(String.join("、", herbUnitList)).append("/n");
        }
        // （3）状态（仅允许启用/禁用）
        if (!StatusEnum.NAME_MAP.keySet().contains(importVO.getStatus())) {
            error.append("状态错误，可选值：启用、禁用/n");
        }
        // （4）医保类型（选填，但填写必须合法）
        if (StringUtils.isNotBlank(importVO.getMedicalInsurance())
                && !MedicalInsuranceEnum.NAME_MAP.keySet().contains(importVO.getMedicalInsurance())) {
            error.append("是否医保错误，可选值：").append(String.join("、", MedicalInsuranceEnum.NAME_MAP.keySet())).append("/n");
        }

        /*if (StringUtils.isNotBlank(importVO.getHerbCode()) && ObjectUtil.isNotEmpty(herbMap.get(importVO.getHerbCode()))) {
            error.append("饮片编码已存在/n");
        }*/

        if (StringUtils.isNotBlank(importVO.getHspNames())){
            for (String hspName : importVO.getHspNames().split(",")) {
                if(!hspMap.keySet().contains(hspName.trim())){
                    error.append("医疗机构编码错误，可选值：").append(String.join("、", hspMap.keySet())).append("/n");
                }
            }
        }
        importVO.setErrMsg(error.toString());
    }

    private boolean isValidDecimal(String value) {
        try {
            new BigDecimal(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 采购单下载
     *
     * @param syncDataVO
     */
    public void purchaseDownload(HerbSyncDataVO syncDataVO, HttpServletResponse response) throws IOException {
        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        Criteria criteria = Criteria.where("medicalGroupCode").is(loginUser.getMedicalGroupCode());
        criteria.and("purchaseNo").regex(syncDataVO.getPurchaseNo());
        Query query = new Query(criteria);
        List<PurchaseMO> purchaseMOS = mongoTemplate.find(query, PurchaseMO.class);
        if (ObjectUtil.isEmpty(purchaseMOS) || ObjectUtil.isEmpty(purchaseMOS.get(0).getDetails())) {
            throw new ServiceException("采购单内容为空");
        }
        List<HerbSyncExportVO> exportVOList = new ArrayList<>();
        CopyOptions options = new CopyOptions();
        options.setIgnoreError(true);
        purchaseMOS.get(0).getDetails().forEach(detail -> {
            HerbSyncExportVO exportVO = new HerbSyncExportVO();
            BeanUtil.copyProperties(detail, exportVO, options);
            BeanUtil.copyProperties(purchaseMOS.get(0), exportVO, options);
            exportVOList.add(exportVO);
        });
        EasyExcelUtil.exportService(response, "饮片列表", "饮片列表", HerbSyncExportVO.class, exportVOList);
    }

    /**
     * 饮片名称匹配
     *
     * @param queryVO
     * @return
     */
    public List<Herb> herbNameMatch(HerbQueryVO queryVO) {
        BaseUser user = UserContextHolder.getUserInfoContext();
        queryVO.setMedicalGroupId(user.getMedicalGroupId());
        queryVO.setHerbHspId(Optional.ofNullable(user.getHspId()).orElse(0L).toString());
        return this.getBaseMapper().herbNameMatch(queryVO);
    }
}
