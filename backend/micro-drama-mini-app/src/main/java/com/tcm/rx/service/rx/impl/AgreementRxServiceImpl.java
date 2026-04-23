package com.tcm.rx.service.rx.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.entity.Result;
import com.tcm.common.enums.BooleanEnum;
import com.tcm.common.exception.ServiceException;
import com.tcm.common.utils.EasyExcelUtil;
import com.tcm.rx.entity.auth.CUser;
import com.tcm.rx.entity.auth.CUserDept;
import com.tcm.rx.entity.basicData.Herb;
import com.tcm.rx.entity.dept.Dept;
import com.tcm.rx.entity.hsp.Hsp;
import com.tcm.rx.entity.rx.AgreementRx;
import com.tcm.rx.entity.rx.AgreementRxDetail;
import com.tcm.rx.entity.rx.RxMgmtDetail;
import com.tcm.rx.feign.dict.DictDataServiceClient;
import com.tcm.rx.feign.dict.vo.DictDataQueryVO;
import com.tcm.rx.feign.dict.vo.DictDataVO;
import com.tcm.rx.mapper.rx.AgreementRxMapper;
import com.tcm.rx.service.auth.ICUserDeptService;
import com.tcm.rx.service.auth.ICUserService;
import com.tcm.rx.service.basicData.IHerbService;
import com.tcm.rx.service.dept.IDeptService;
import com.tcm.rx.service.dict.DictService;
import com.tcm.rx.service.hsp.IHspService;
import com.tcm.rx.service.rx.IAgreementRxDetailService;
import com.tcm.rx.service.rx.IAgreementRxService;
import com.tcm.rx.vo.basicData.request.HerbQueryVO;
import com.tcm.rx.vo.rx.request.*;
import com.tcm.rx.vo.rx.response.AgreementRxDetailVO;
import com.tcm.rx.vo.rx.response.AgreementRxItemVO;
import com.tcm.rx.vo.rx.response.AgreementRxListVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

/**
 * <p>
 * 诊疗开方系统--协定方主表 服务实现类
 * </p>
 *
 * @author shouhan
 * @since 2025-07-17
 */
@Service
@Slf4j
public class AgreementRxServiceImpl extends ServiceImpl<AgreementRxMapper, AgreementRx> implements IAgreementRxService {

    @Autowired
    private IAgreementRxDetailService agreementRxDetailService;

    @Autowired
    private ICUserDeptService userDeptService;

    @Autowired
    private IDeptService deptService;

    @Autowired
    private IHspService hspService;


    @Autowired
    private IHerbService herbService;

    @Autowired
    private ICUserService userService;

    @Resource
    private DictService dictService;

    /**
     * 协定方新增/修改
     */
    @Override
    @Transactional
    public Long save(SaveAgreementRxVO saveVo) {
        if (!ObjectUtil.isAllNotEmpty(saveVo.getType(), saveVo.getRxName(), saveVo.getStatus(), saveVo.getItemList())) {
            throw new ServiceException("必传参数不能空");
        }
        // 检查处方明细是否重复
        Set<String> herbCodeSet = new HashSet<>();
        for (SaveAgreementRxItemVO item : saveVo.getItemList()) {
            if (!herbCodeSet.add(item.getHerbCode())) {
                throw new ServiceException("处方明细已重复: " + item.getHerbName());
            }
        }
        BaseUser user = UserContextHolder.getUserInfoContext();
        AgreementRx agreementRx = BeanUtil.copyProperties(saveVo,AgreementRx.class);
        if (agreementRx.getType() == 0 && ObjectUtil.isEmpty(agreementRx.getHspId())){//个人处方保存，拿当前用户的医疗机构，如果是超管在个人页面新增这块可能无法赋值
            Hsp currHsp = hspService.getById(user.getHspId());
            if (!ObjectUtil.isEmpty(currHsp)){
                agreementRx.setHspId(currHsp.getId());
                agreementRx.setHspCode(currHsp.getHspCode());
                agreementRx.setHspName(currHsp.getHspName());
            }
        }


        //方剂组成
        String formulaComposition = saveVo.getItemList().stream()
                .map(d -> d.getHerbName() + d.getHerbDosage() + d.getUnit())
                .collect(Collectors.joining("_"));
        agreementRx.setFormulaComposition(formulaComposition);

        if (ObjectUtil.isEmpty(agreementRx.getId())){//新增
            AgreementRx old = this.lambdaQuery().eq(AgreementRx::getRxName, agreementRx.getRxName()).eq(AgreementRx::getMedicalGroupId, user.getMedicalGroupId()).one();
            if (ObjectUtil.isNotEmpty(old)){
                throw new ServiceException("处方名称不能重复");
            }

            //todo 走封装逻辑
            agreementRx.setCreateBy(user.getId().toString());
            agreementRx.setUpdateBy(user.getId().toString());
            agreementRx.setMedicalGroupId(user.getMedicalGroupId());
            agreementRx.setMedicalGroupCode(user.getMedicalGroupCode());
            agreementRx.insert();
        }else {//修改
            AgreementRx old = this.lambdaQuery()
                    .ne(AgreementRx::getId, agreementRx.getId())
                    .eq(AgreementRx::getRxName, agreementRx.getRxName())
                    .eq(AgreementRx::getMedicalGroupId, user.getMedicalGroupId())
                    .eq(AgreementRx::getDelFlag,BooleanEnum.FALSE.getNum()).one();
            if (ObjectUtil.isNotEmpty(old)){
                throw new ServiceException("处方名称不能重复");
            }

            agreementRx.setUpdateBy(user.getId().toString());
            agreementRx.setUpdateTime(new Date());
            agreementRx.updateById();

            QueryWrapper<AgreementRxDetail> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("agreement_rx_id", agreementRx.getId());
            agreementRxDetailService.remove(queryWrapper);
        }
        List<AgreementRxDetail> detailList =saveVo.getItemList().stream().map(d->{
            AgreementRxDetail detail = BeanUtil.copyProperties(d, AgreementRxDetail.class);
            detail.setId(null);//防止前端传入id
            detail.setAgreementRxId(agreementRx.getId());
            detail.setMedicalGroupId(user.getMedicalGroupId());
            detail.setMedicalGroupCode(user.getMedicalGroupCode());
            detail.setCreateBy(user.getId().toString());
            detail.setUpdateBy(user.getId().toString());
            return detail;
        }).collect(Collectors.toList());
        agreementRxDetailService.saveBatch(detailList);
        return agreementRx.getId();
    }

    /**
     * 查询协定方数据（分页查询）
     */
    @Override
    public List<AgreementRxListVO> agreementRxList(AgreementRxQueryVO queryVO) {
        BaseUser user = UserContextHolder.getUserInfoContext();
        List<AgreementRx> list = lambdaQuery()
                .eq(AgreementRx::getMedicalGroupId, user.getMedicalGroupId())//todo 是否需要隔离医院
                .eq(AgreementRx::getDelFlag, BooleanEnum.FALSE.getNum())
                .like(ObjectUtil.isNotEmpty(queryVO.getRxName()), AgreementRx::getRxName, queryVO.getRxName())
                .like(ObjectUtil.isNotEmpty(queryVO.getAdaptation()), AgreementRx::getAdaptation, queryVO.getAdaptation())
                .like(ObjectUtil.isNotEmpty(queryVO.getOwnerAccountName()), AgreementRx::getOwnerAccountName, queryVO.getOwnerAccountName())
                .like(ObjectUtil.isNotEmpty(queryVO.getHspName()), AgreementRx::getHspName, queryVO.getHspName())
                .like(ObjectUtil.isNotEmpty(queryVO.getDeptName()), AgreementRx::getDeptName, queryVO.getDeptName())
                .eq(ObjectUtil.isNotEmpty(queryVO.getType()), AgreementRx::getType, queryVO.getType())
                .apply(ObjectUtil.isNotEmpty(queryVO.getDeptId()),
                        "FIND_IN_SET({0}, dept_id) > 0", queryVO.getDeptId())
                .eq(ObjectUtil.isNotEmpty(queryVO.getOwnerAccount()), AgreementRx::getOwnerAccount, queryVO.getOwnerAccount())
                .eq(ObjectUtil.isNotEmpty(queryVO.getStatus()), AgreementRx::getStatus, queryVO.getStatus())
                //以下获取当前医生可使用配方，开方时调用
                .and(queryVO.getListScene() == BooleanEnum.TRUE.getNum(), w -> {
                    List<CUserDept> userDepts = userDeptService.lambdaQuery().eq(CUserDept::getUserId, user.getId()).list();

                    // 添加空集合判断
                    String deptCondition = userDepts.isEmpty() ? "1=0" :
                            String.join(" or ", userDepts.stream()
                                    .map(d -> "FIND_IN_SET(" + d.getDeptId() + ", dept_id) > 0")
                                    .collect(Collectors.toList()));

                    //机构=null，科室=null or 科室 =''
                    log.info("user.getHspId() ======================================{}",user.getHspId());
                    w.apply("(type = 1 and hsp_id is null and dept_id is null or dept_id = '')"
                            //机构！=null，科室！=null,科室！= ''
                            + " or (type = 1 and hsp_id is not null and dept_id is not null  and dept_id !='' and (" + deptCondition + "))"
                            //机构！=null，科室==null or 科室 =''
                            + " or (type = 1 and hsp_id is not null and dept_id is null or dept_id = '' and hsp_id = " + user.getHspId() + ")"
                            + " or (type = 0 and owner_account = " + user.getId() + ")"
                    );
                })
                .orderByDesc(AgreementRx::getCreateTime)
                .list();
        List<AgreementRxListVO> result = BeanUtil.copyToList(list, AgreementRxListVO.class);
        Map<Long, List<AgreementRxDetail>> dMap = new HashMap<>();
        List<Long> agreementRxIds = result.stream().map(AgreementRxListVO::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(agreementRxIds)) {
            List<AgreementRxDetail> details = agreementRxDetailService.lambdaQuery().in(AgreementRxDetail::getAgreementRxId, agreementRxIds).list();
            dMap = Optional.ofNullable(details).orElse(Collections.emptyList()).stream().collect(Collectors.groupingBy(AgreementRxDetail::getAgreementRxId));

        }
//        Map<Long,CUser> userMap = new HashMap<>();
//        List<Long> uIds = result.stream().map(AgreementRxListVO::getOwnerAccount).collect(Collectors.toList());
//        if (!CollectionUtils.isEmpty(uIds)){
//            List<CUser> userList =userService.lambdaQuery().in(CUser::getId, uIds).list();
//            userMap = Optional.ofNullable(userList).orElse(Collections.emptyList()).stream().collect(Collectors.toMap(CUser::getId, v1 -> v1, (k1, k2) -> k1));
//
//        }

//        Map<Long, Hsp> finalHMap = hMap;
        Map<Long, List<AgreementRxDetail>> finalDMap = dMap;
//        Map<Long, CUser> finalUserMap = userMap;
        result.stream().forEach(r->{
//            if (ObjectUtil.isNotEmpty(r.getHspId()) && finalHMap.containsKey(r.getHspId())){
//                r.setHspName(finalHMap.get(r.getHspId()).getHspName());
//            }
            if (finalDMap.containsKey(r.getId())){
                r.setDoseCount(finalDMap.get(r.getId()).get(0).getDoseCount());
                r.setUsage(finalDMap.get(r.getId()).get(0).getUsage());
            }
//            if (ObjectUtil.isNotEmpty(r.getOwnerAccount()) && finalUserMap.containsKey(r.getOwnerAccount())){
//                r.setOwnerAccountName(finalUserMap.get(r.getOwnerAccount()).getRealName());
//            }
        });
        return result;
    }
    /**
     * 详情
     */
    @Override
    public AgreementRxDetailVO detail(Long id) {
        AgreementRx agreementRx = this.getById(id);
        if (ObjectUtil.isEmpty(agreementRx)){
            throw new ServiceException("协定方不存在");
        }
        List<AgreementRxDetail> itemList = agreementRxDetailService.lambdaQuery()
                .eq(AgreementRxDetail::getAgreementRxId,agreementRx.getId())
                .list();
        AgreementRxDetailVO result = BeanUtil.copyProperties(agreementRx, AgreementRxDetailVO.class);
        List<AgreementRxItemVO> itemVOS = BeanUtil.copyToList(itemList, AgreementRxItemVO.class);
        result.setItemVOS(itemVOS);
        return result;
    }

    /**
     * 删除
     */
    @Override
    public void delete(Long id) {
        lambdaUpdate().eq(AgreementRx::getId,id).set(AgreementRx::getDelFlag, BooleanEnum.TRUE.getNum()).update();
    }

    /**
     * 下载模版
     *
     * @Param isCurrDoctor true 个人模版导入 false 协定方管理导入
     */
    @Override
    public void downloadTemplate(HttpServletResponse response, Boolean isCurrDoctor) throws IOException {
        if (isCurrDoctor){
            List<AgreementRxCurrDoctorExcel> excelList = Lists.newArrayList();
            // 下载导入模板
            EasyExcelUtil.exportService(response,"我的协定方","协定方",
                    AgreementRxCurrDoctorExcel.class, excelList);
        }else {
            List<AgreementRxExcel> excelList = Lists.newArrayList();
            // 下载导入模板
            EasyExcelUtil.exportService(response,"协定方管理","协定方",
                    AgreementRxExcel.class, excelList);
        }
    }

    /**
     * 导入（返回列表）
     *
     * @Param isCurrDoctor true 个人模版导入 false 协定方管理导入
     */
    @Override
    public List importAgreementRx(MultipartFile file, Boolean isCurrDoctor) throws IOException {
        List<String> decoctRequireLabel = new ArrayList<>();
        List<String> usageLabel = new ArrayList<>();
        try {
            //煎药要求
            DictDataQueryVO decoctRequireVO = new DictDataQueryVO();
            decoctRequireVO.setDictType("decoctRequire");
            List<DictDataVO> dictDataVOS = dictService.dictDataList(decoctRequireVO);
            decoctRequireLabel = dictDataVOS.stream().filter(v -> v.getDelFlag() == BooleanEnum.FALSE.getNum()).map(DictDataVO::getDictLabel).collect(Collectors.toList());
            //用法
            DictDataQueryVO usageVO = new DictDataQueryVO();
            usageVO.setDictType("usage");
            List<DictDataVO> usageData = dictService.dictDataList(usageVO);
            usageLabel = usageData.stream().filter(v -> v.getDelFlag() == BooleanEnum.FALSE.getNum()).map(DictDataVO::getDictLabel).collect(Collectors.toList());
        }catch (Exception e){
            log.error("查询字典失败",e);
        }

        if (isCurrDoctor){//个人
            List<AgreementRxCurrDoctorExcel> fileList;
            try {
                fileList = EasyExcelUtil.importService(file, AgreementRxCurrDoctorExcel.class);
            }catch (Exception e){
                throw new ServiceException("未检测到有效数据，请检查模板填写");
            }
            if(CollectionUtil.isEmpty(fileList)){
                throw new ServiceException("未检测到有效数据，请检查模板填写");
            }
            Map<String, Herb> herbMap = herbService.getMapByCodes(fileList.stream().map(AgreementRxCurrDoctorExcel::getHerbCode).collect(Collectors.toList()));
            Map<String, List<AgreementRxCurrDoctorExcel>> addMap = fileList.stream().collect(groupingBy(AgreementRxCurrDoctorExcel::getRxName));
            addMap.forEach((k, v) -> {
                Boolean b = v.stream().filter(item -> item.getHerbCode() != null).collect(groupingBy(AgreementRxCurrDoctorExcel::getHerbCode,counting())).values().stream().anyMatch(count->count>1);
                if (b) {
                    v.stream().forEach(e -> e.setErrMsg("同一个方剂中饮片名称不能重复"));
                } else {
                    v.stream().forEach(e -> {
                        if (!ObjectUtil.isAllNotEmpty(e.getRxName(), e.getHerbCode(), e.getHerbName(), e.getHerbDosage())) {
                            e.setErrMsg("必传参数为空");
                        }
                        if (!herbMap.containsKey(e.getHerbCode())) {
                            e.setErrMsg("请输入正确的饮片编码");
                        }
                        // 添加对饮片名称的校验
                        else if (herbMap.containsKey(e.getHerbCode()) && !herbMap.get(e.getHerbCode()).getHerbName().equals(e.getHerbName())) {
                            e.setErrMsg("请输入正确的饮片名称");
                        }
                    });
                }
            });
            for (AgreementRxCurrDoctorExcel ag : fileList) {
                if(StringUtils.isNotEmpty(ag.getDecoctRequire()) && CollectionUtil.isNotEmpty(decoctRequireLabel) && !decoctRequireLabel.contains(ag.getDecoctRequire())){
                    ag.setErrMsg("请输入正确的煎药要求");
                }
                if(StringUtils.isNotEmpty(ag.getUsage()) && CollectionUtil.isNotEmpty(usageLabel) && !usageLabel.contains(ag.getUsage())){
                    ag.setErrMsg("请输入正确的用法");
                }
            }
            return fileList;
        }else {
            List<AgreementRxExcel> fileList = new ArrayList<>();
            try {
                 fileList = EasyExcelUtil.importService(file, AgreementRxExcel.class);
            }catch (Exception e){
                throw new ServiceException("未检测到有效数据，请检查模板填写");
            }
            if(CollectionUtil.isEmpty(fileList)){
                throw new ServiceException("未检测到有效数据，请检查模板填写");
            }
            checkExcel(fileList);
            return fileList;
        }

    }

    private void checkExcel(List<AgreementRxExcel> fileList) {
        BaseUser user = UserContextHolder.getUserInfoContext();
        Map<String, Herb> herbMap = herbService.getMapByCodes(fileList.stream().map(AgreementRxExcel::getHerbCode).collect(Collectors.toList()));

        Map<String, List<AgreementRxExcel>> addMap = fileList.stream().collect(Collectors.groupingBy(AgreementRxExcel::getRxName));
        addMap.forEach((k, v) -> {
            Boolean b = v.stream().filter(item -> item.getHerbCode() != null).collect(groupingBy(AgreementRxExcel::getHerbCode, counting())).values().stream().anyMatch(count -> count > 1);

            Boolean next = BooleanEnum.TRUE.getBool();

            String hspName = v.get(0).getHspName();
            String deptName = v.get(0).getDeptName();
            String ownerAccountName = v.get(0).getOwnerAccountName();

            if (ObjectUtil.isNotEmpty(hspName)) {
                //处理医疗机构
                Hsp hsp = hspService.lambdaQuery()
                        .eq(Hsp::getMedicalGroupId, user.getMedicalGroupId())
                        .eq(Hsp::getHspName, v.get(0).getHspName())
                        .last("limit 1")
                        .eq(Hsp::getDelFlag, BooleanEnum.FALSE.getNum())
                        .one();
                if (ObjectUtil.isEmpty(hsp)){
                    v.stream().forEach(e -> e.setErrMsg("请输入正确的医疗机构"));
                    next = BooleanEnum.FALSE.getBool();
                }else {
                    if (ObjectUtil.isNotEmpty(deptName)){
                        List<Dept> deptList = deptService.lambdaQuery()
                                .in(Dept::getDeptName, deptName.split(","))
                                .eq(Dept::getHspId, hsp.getId())
                                .eq(Dept::getDelFlag, BooleanEnum.FALSE.getNum())
                                .list();
                        if (deptList.size() != v.get(0).getDeptName().split(",").length){
                            v.stream().forEach(e -> e.setErrMsg("导入科室是否属于该医疗机构"));
                            next = BooleanEnum.FALSE.getBool();
                        }
                    }
                }
            }

            if (next){
                if (ObjectUtil.isNotEmpty(ownerAccountName)) {
                    CUser cuser = userService.lambdaQuery()
                            .eq(CUser::getMedicalGroupId, user.getMedicalGroupId())
                            .eq(CUser::getRealName, ownerAccountName)
                            .last("limint 1")
                            .one();
                    if (ObjectUtil.isEmpty(cuser)) {
                        v.stream().forEach(e -> e.setErrMsg("请输入正常的所属人名称"));
                        next = BooleanEnum.FALSE.getBool();
                    }
                }
            }

            if (next){
                if (b) {
                    v.stream().forEach(e -> e.setErrMsg("同一个方剂中饮片名称不能重复"));
                } else {
                    v.stream().forEach(e->{
                        if (!ObjectUtil.isAllNotEmpty(e.getRxName(),e.getType(),e.getHerbCode(),e.getHerbName(),e.getHerbDosage())){
                            e.setErrMsg("必传参数为空");
                        }else if (!herbMap.containsKey(e.getHerbCode())){
                            e.setErrMsg("请输入正确的饮片编码");
                        } else if (!e.getType().equals("个人") && !e.getType().equals("通用")) {
                            e.setErrMsg("请输入准确的类型");
                        } else if  (!Objects.equals(e.getDeptName(), deptName)){
                            e.setErrMsg("同一个处方请传入相同的科室");
                        }else if  (!Objects.equals(e.getOwnerAccountName(), ownerAccountName)){
                            e.setErrMsg("同一个处方请传入相同的所属人");
                        }else if  (!Objects.equals(e.getHspName(), hspName)){
                            e.setErrMsg("同一个处方请传入相同的医疗机构");
                        }
                    });
                }
            }
        });
    }

    /**
     * 导入确认新增
     */
    @Override
    @Transactional
    public Boolean importAdd(AgreementRxExcelAddVO vo) {

        BaseUser user = UserContextHolder.getUserInfoContext();

        if (vo.getIsCurrDoctor()) {//todo 待优化
            vo.getList().stream().forEach(e -> e.setType("个人"));
        }
        checkExcel(vo.getList());
        vo.getList().stream().forEach(e->{
            if (StringUtils.isNotEmpty(e.getErrMsg())){
                throw new ServiceException(e.getErrMsg());
            }
        });

        Map<String, List<AgreementRxExcel>> addMap = vo.getList().stream().collect(groupingBy(AgreementRxExcel::getRxName));
        Date currTime = new Date();
        Hsp currHsp = hspService.getById(user.getHspId());

        addMap.forEach((k, v) -> {
            AgreementRx agreementRx = new AgreementRx();
            agreementRx.setMedicalGroupId(user.getMedicalGroupId());
            agreementRx.setMedicalGroupCode(user.getMedicalGroupCode());
            agreementRx.setRxName(k);
            agreementRx.setStatus(0);
            agreementRx.setMainEfficacy(v.get(0).getMainEfficacy());
            agreementRx.setAdaptation(v.get(0).getAdaptation());
            agreementRx.setCreateBy(user.getId().toString());
            agreementRx.setUpdateBy(user.getId().toString());
            agreementRx.setCreateTime(currTime);
            agreementRx.setUpdateTime(currTime);
            //金额
            final BigDecimal[] totalMoney = {BigDecimal.ZERO};
            final BigDecimal[] doseTotalMoney = {BigDecimal.ZERO};

            if (vo.getIsCurrDoctor()) {//个人
                agreementRx.setType(0);//个人
                if (ObjectUtil.isNotEmpty(currHsp)){
                    agreementRx.setHspId(currHsp.getId());
                    agreementRx.setHspCode(currHsp.getHspCode());
                    agreementRx.setHspName(currHsp.getHspName());
                }
                agreementRx.setOwnerAccount(user.getId());
                agreementRx.setOwnerAccountName(user.getUsername());
            } else {//协定方管理
                agreementRx.setType(v.get(0).getType().equals("个人") ? 0 : 1);//todo 优化

                if (ObjectUtil.isNotEmpty(v.get(0).getHspName())) {
                    //处理医疗机构
                    Hsp hsp = hspService.lambdaQuery()
                            .eq(Hsp::getMedicalGroupId, user.getMedicalGroupId())
                            .eq(Hsp::getHspName, v.get(0).getHspName())
                            .last("limit 1")
                            .eq(Hsp::getDelFlag, BooleanEnum.FALSE.getNum())
                            .one();
                    agreementRx.setHspId(hsp.getId());
                    agreementRx.setHspCode(hsp.getHspCode());
                    agreementRx.setHspName(hsp.getHspName());
                    //处理医疗机构下的科室
                    if (ObjectUtil.isNotEmpty(v.get(0).getDeptName())) {
                        List<Dept> deptList = deptService.lambdaQuery()
                                .in(Dept::getDeptName, v.get(0).getDeptName().split(","))
                                .eq(Dept::getHspId, hsp.getId())
                                .eq(Dept::getDelFlag, BooleanEnum.FALSE.getNum())
                                .list();
                        agreementRx.setDeptId(deptList.stream().map(d -> d.getId().toString()).collect(Collectors.joining(",")));
                        agreementRx.setDeptName(deptList.stream().map(d -> d.getDeptName()).collect(Collectors.joining(",")));
                    }
                }
                if (ObjectUtil.isNotEmpty(v.get(0).getOwnerAccountName())){
                    CUser cuser = userService.lambdaQuery()
                            .eq(CUser::getMedicalGroupId, user.getMedicalGroupId())
                            .eq(CUser::getRealName,Long.valueOf(v.get(0).getOwnerAccountName()))
                            .last("limint 1")
                            .one();
                    if (ObjectUtil.isNotEmpty(cuser)){
                        agreementRx.setOwnerAccount(cuser.getId());
                        agreementRx.setOwnerAccountName(cuser.getRealName());
                    }
                }

            }
            List<String> herbCodes = v.stream().map(AgreementRxExcel::getHerbCode).collect(Collectors.toList());
            Map<String, Herb> herbMap = herbService.getMapByCodes(herbCodes);
            final Integer[] num = {1};
            List<AgreementRxDetail> detailList = v.stream().filter(e -> herbMap.containsKey(e.getHerbCode())).map(e -> {
                Herb herb = herbMap.get(e.getHerbCode());
                AgreementRxDetail agreementRxDetail = new AgreementRxDetail() {{
                    setMedicalGroupCode(user.getMedicalGroupCode());
                    setMedicalGroupId(user.getMedicalGroupId());
                    setSerialNum(num[0]);
                    setHerbCode(herb.getHerbCode());
                    setHerbName(herb.getHerbName());
                    setType(herb.getType());
                    setSpec(herb.getSpec());
                    setHerbDosage(e.getHerbDosage());
                    setUnit(herb.getUnit());
                    setPrice(herb.getRetailPrice());
                    setMoney(herb.getRetailPrice().multiply(e.getHerbDosage()));
                    setAdjustRequire(e.getAdaptation());
                    setDoseCount(e.getDoseCount());
                    setOneDose(e.getOneDose());
                    setPerPackVolume(e.getPerPackVolume());
                    setUsage(e.getUsage());
                    setDecoctRequire(e.getDecoctRequire());
                    setDosageFrequency(e.getDosageFrequency());
                    setCreateBy(user.getId().toString());
                    setUpdateBy(user.getId().toString());
                    setCreateTime(currTime);
                    setUpdateTime(currTime);
                }};
                doseTotalMoney[0] = doseTotalMoney[0].add(agreementRxDetail.getMoney());

                num[0]++;
                agreementRxDetail.setHerbDosage(e.getHerbDosage());
                return agreementRxDetail;
            }).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(detailList) && detailList.get(0).getDoseCount() != null) {
                agreementRx.setTotalMoney(totalMoney[0].multiply(new BigDecimal(detailList.get(0).getDoseCount())));
            } else {
                agreementRx.setTotalMoney(BigDecimal.ZERO);
            }
            agreementRx.setDoseTotalMoney(doseTotalMoney[0]);

            //方剂组成
            String formulaComposition = detailList.stream()
                    .map(d -> d.getHerbName() + d.getHerbDosage() + d.getUnit())
                    .collect(Collectors.joining("_"));
            agreementRx.setFormulaComposition(formulaComposition);

            AgreementRx old = this.lambdaQuery().eq(AgreementRx::getRxName, k).eq(AgreementRx::getMedicalGroupId, user.getMedicalGroupId()).eq(AgreementRx::getDelFlag,BooleanEnum.FALSE.getNum()).one();
            if (ObjectUtil.isNotEmpty(old)) {//修改
                agreementRx.setId(old.getId());
                this.updateById(agreementRx);
                LambdaQueryWrapper<AgreementRxDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(AgreementRxDetail::getAgreementRxId, agreementRx.getId());
                agreementRxDetailService.remove(lambdaQueryWrapper);
                // 添加这一行：确保新明细记录有关联的ID
                detailList.stream().forEach(d -> d.setAgreementRxId(agreementRx.getId()));
            } else {
                this.save(agreementRx);
                detailList.stream().forEach(d -> d.setAgreementRxId(agreementRx.getId()));

            }
            agreementRxDetailService.saveBatch(detailList);
        });
        return BooleanEnum.TRUE.getBool();
    }

    public List<AgreementRxDetail> getTransDetails(Long id) {
        BaseUser currentUser = UserContextHolder.getUserInfoContext();
        List<AgreementRxDetail> details = agreementRxDetailService.lambdaQuery().eq(AgreementRxDetail::getAgreementRxId, id).list();
        if(ObjectUtil.isEmpty(details)){
            return new ArrayList<>();
        }
        Map<String, Herb> herbMap = herbService.lambdaQuery().eq(Herb::getMedicalGroupId, currentUser.getMedicalGroupId())
                .eq(Herb::getDelFlag, BooleanEnum.FALSE.getNum())
                .in(Herb::getHerbCode, details.stream().map(detail -> detail.getHerbCode()).collect(Collectors.toList())).list()
                .stream().collect(Collectors.toMap(Herb::getHerbCode, H -> H, (k1, k2) -> k1));
        List<AgreementRxDetail> detailList = new ArrayList<>();
        if(ObjectUtil.isNotEmpty(details)) {
            for(AgreementRxDetail agreementRxDetail : details) {
                HerbQueryVO queryVO = new HerbQueryVO();
                queryVO.setHerbName(agreementRxDetail.getHerbName());
                List<Herb> otherHerbList = herbService.herbNameMatch(queryVO);
                if(ObjectUtil.isNotEmpty(otherHerbList)) {
                    AgreementRxDetail detail = new AgreementRxDetail();
                    detail.setHerbName(otherHerbList.get(0).getHerbName());
                    detail.setHerbCode(otherHerbList.get(0).getHerbCode());
                    detail.setSpec(otherHerbList.get(0).getSpec());
                    detail.setUnit(otherHerbList.get(0).getUnit());
                    detail.setPrice(otherHerbList.get(0).getRetailPrice());
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
