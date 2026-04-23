package com.tcm.rx.service.rx.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.enums.BooleanEnum;
import com.tcm.common.exception.ServiceException;
import com.tcm.common.utils.AssertUtil;
import com.tcm.common.utils.CommonUtil;
import com.tcm.common.utils.EasyExcelUtil;
import com.tcm.rx.entity.basicData.Herb;
import com.tcm.rx.entity.rx.CompatTabooMgmt;
import com.tcm.rx.entity.rx.CompatTabooMgmtDetail;
import com.tcm.rx.entity.rx.RxMgmtDetail;
import com.tcm.rx.feign.dict.vo.DictDataQueryVO;
import com.tcm.rx.mapper.rx.CompatTabooMgmtMapper;
import com.tcm.rx.service.basicData.IHerbService;
import com.tcm.rx.service.dict.DictService;
import com.tcm.rx.service.rx.ICompatTabooMgmtDetailService;
import com.tcm.rx.service.rx.ICompatTabooMgmtService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.rx.util.TabooCodeGenerator;
import com.tcm.rx.vo.rx.request.CompatTabCheckReqVO;
import com.tcm.rx.vo.rx.request.CompatTabImportVO;
import com.tcm.rx.vo.rx.request.CompatTabooMgmtQueryVO;
import com.tcm.rx.vo.rx.request.CompatTabooMgmtSaveVO;
import com.tcm.rx.vo.rx.response.CompatTabCheckResVO;
import com.tcm.rx.vo.rx.response.CompatTabooMgmtDetailVO;
import com.tcm.rx.vo.rx.response.CompatTabooMgmtVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * <p>
 * 诊疗开方系统--配伍禁忌管理表 服务实现类
 * </p>
 *
 * @author djbo
 * @since 2025-09-10
 */
@Service
public class CompatTabooMgmtServiceImpl extends ServiceImpl<CompatTabooMgmtMapper, CompatTabooMgmt> implements ICompatTabooMgmtService {

    @Resource
    private ICompatTabooMgmtDetailService compatTabooMgmtDetailService;

    @Resource
    private IHerbService herbService;

    @Resource
    private TabooCodeGenerator tabooCodeGenerator;

    @Resource
    private DictService dictService;

    /**
     * 分页查询
     */
    public List<CompatTabooMgmtVO> pageList(CompatTabooMgmtQueryVO queryVO) {
        BaseUser currentUser = UserContextHolder.getUserInfoContext();
        queryVO.setMedicalGroupId(currentUser.getMedicalGroupId());
        return this.getBaseMapper().selectByQuery(queryVO);
    }

    /**
     * 查看详情
     */
    public CompatTabooMgmtVO getInfo(Long id) {
        BaseUser currentUser = UserContextHolder.getUserInfoContext();
        CompatTabooMgmtQueryVO queryVO = new CompatTabooMgmtQueryVO();
        queryVO.setMedicalGroupId(currentUser.getMedicalGroupId());
        queryVO.setId(id);
        List<CompatTabooMgmtVO> compatTabooMgmtVOS = this.getBaseMapper().selectByQuery(queryVO);
        if (ObjectUtil.isEmpty(compatTabooMgmtVOS)) {
            return new CompatTabooMgmtVO();
        }
        List<CompatTabooMgmtDetailVO> detailVOList = compatTabooMgmtDetailService.lambdaQuery()
                .eq(CompatTabooMgmtDetail::getTabooMgmtId, compatTabooMgmtVOS.get(0).getId())
                .list().stream().map(record -> {
                    CompatTabooMgmtDetailVO detailVO = new CompatTabooMgmtDetailVO();
                    BeanUtil.copyProperties(record, detailVO);
                    return detailVO;
                }).collect(Collectors.toList());
        compatTabooMgmtVOS.get(0).setDetailList(detailVOList);
        return compatTabooMgmtVOS.get(0);
    }

    /**
     * 新增配伍禁忌
     */
    @Transactional(rollbackFor = Exception.class)
    public Long saveCompatTab(CompatTabooMgmtSaveVO saveVO) {
        CompatTabooMgmt compatTabooMgmt = saveVO.getCompatTabooMgmt();
        List<CompatTabooMgmtDetail> detailList = saveVO.getDetailList();
        if (!ObjectUtil.isAllNotEmpty(compatTabooMgmt, detailList)) {
            throw new ServiceException("配伍禁忌内容不可为空");
        }
        List<String> herbCodeList = detailList.stream().collect(Collectors.groupingBy(CompatTabooMgmtDetail::getHerbCode)).entrySet()
                .stream().filter(entry -> entry.getValue().size() > 1).map(entry -> entry.getKey()).collect(Collectors.toList());
        if (ObjectUtil.isNotEmpty(herbCodeList)) {
            throw new ServiceException(String.join("、", herbCodeList) + " 饮片重复");
        }
        AssertUtil.assertArgs()
                .notEmpty(compatTabooMgmt.getTabooName(), "禁忌名称")
                .check();
        BaseUser currentUser = UserContextHolder.getUserInfoContext();
        if (ObjectUtil.isEmpty(compatTabooMgmt.getId())) {
            CommonUtil.setRxBaseEntity(compatTabooMgmt, currentUser);
            Optional<CompatTabooMgmt> optionalRxMgmt = this.lambdaQuery().eq(CompatTabooMgmt::getMedicalGroupId, currentUser.getMedicalGroupId())
                    .eq(CompatTabooMgmt::getTabooName, compatTabooMgmt.getTabooName()).list().stream().findFirst();
            if (optionalRxMgmt.isPresent()) {
                throw new ServiceException("禁忌名称已存在");
            }
            compatTabooMgmt.setTabooCode(tabooCodeGenerator.generateTabooCode());
            this.save(compatTabooMgmt);
        } else {
            Optional<CompatTabooMgmt> optCompatTabMgmt = this.lambdaQuery().eq(CompatTabooMgmt::getMedicalGroupId, currentUser.getMedicalGroupId())
                    .eq(CompatTabooMgmt::getTabooName, compatTabooMgmt.getTabooName()).list().stream().findFirst();
            if (optCompatTabMgmt.isPresent() && !(optCompatTabMgmt.get().getId().longValue() == compatTabooMgmt.getId())) {
                throw new ServiceException("禁忌名称已存在");
            }
            compatTabooMgmt.setUpdateBy(currentUser.getId().toString());
            compatTabooMgmt.setUpdateTime(new Date());
            this.updateById(compatTabooMgmt);
        }
        for (CompatTabooMgmtDetail detail : detailList) {
            detail.setTabooMgmtId(compatTabooMgmt.getId());
            CommonUtil.setRxBaseEntity(detail, currentUser);
        }
        compatTabooMgmtDetailService.remove(new LambdaQueryWrapper<CompatTabooMgmtDetail>()
                .eq(CompatTabooMgmtDetail::getTabooMgmtId, compatTabooMgmt.getId()));
        compatTabooMgmtDetailService.saveBatch(detailList);

        return compatTabooMgmt.getId();
    }

    /**
     * 删除配伍禁忌
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        if (ObjectUtil.isEmpty(this.getById(id))) {
            throw new ServiceException("配伍禁忌不存在");
        }
        this.removeById(id);
        compatTabooMgmtDetailService.remove(new LambdaQueryWrapper<CompatTabooMgmtDetail>()
                .eq(CompatTabooMgmtDetail::getTabooMgmtId, id));
        return true;
    }

    /**
     * 批量新增配伍禁忌
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchInsertCompatTab(List<CompatTabImportVO> compatTabImportVOS) {
        if (ObjectUtil.isEmpty(compatTabImportVOS)) {
            return false;
        }
        Map<String, List<CompatTabImportVO>> tabNameMap = compatTabImportVOS.stream()
                .collect(Collectors.groupingBy(CompatTabImportVO::getTabooName));
        BaseUser currentUser = UserContextHolder.getUserInfoContext();
        tabNameMap.entrySet().forEach(entry -> {
            CompatTabooMgmt compatTabooMgmt = new CompatTabooMgmt();
            BeanUtil.copyProperties(entry.getValue().get(0), compatTabooMgmt);
            CommonUtil.setRxBaseEntity(compatTabooMgmt, currentUser);
            Optional<CompatTabooMgmt> optTabMgmt = this.lambdaQuery().eq(CompatTabooMgmt::getMedicalGroupId, currentUser.getMedicalGroupId())
                    .eq(CompatTabooMgmt::getTabooName, entry.getKey()).list().stream().findFirst();
            if (optTabMgmt.isPresent()) {
                compatTabooMgmt.setId(optTabMgmt.get().getId());
            } else {
                compatTabooMgmt.setTabooCode(tabooCodeGenerator.generateTabooCode());
            }
            this.saveOrUpdate(compatTabooMgmt);
            List<CompatTabooMgmtDetail> detailList = entry.getValue().stream().map(record -> {
                CompatTabooMgmtDetail detail = new CompatTabooMgmtDetail();
                detail.setTabooMgmtId(compatTabooMgmt.getId());
                detail.setHerbCode(record.getHerbCode());
                detail.setHerbName(record.getHerbName());
                detail.setType(record.getType());
                detail.setSpec(record.getSpec());
                CommonUtil.setRxBaseEntity(detail, currentUser);
                return detail;
            }).collect(Collectors.toList());
            compatTabooMgmtDetailService.remove(new LambdaQueryWrapper<CompatTabooMgmtDetail>()
                    .eq(CompatTabooMgmtDetail::getTabooMgmtId, compatTabooMgmt.getId()));
            compatTabooMgmtDetailService.saveBatch(detailList);
        });
        return true;
    }

    /**
     * 配伍禁忌导入
     */
    public List<CompatTabImportVO> compatTabImport(MultipartFile file) throws Exception {
        List<CompatTabImportVO> importList = EasyExcelUtil.importService(file, CompatTabImportVO.class);
        if (importList.size() > 1000) {
            throw new ServiceException("一次最多导入1000条数据，请拆分文件后重试");
        }
        if (importList.isEmpty()) {
            throw new ServiceException("未检测到有效数据，请检查模板填写");
        }
        BaseUser user = UserContextHolder.getUserInfoContext();
        List<String> herbCodeList = importList.stream().filter(record -> ObjectUtil.isNotEmpty(record.getHerbCode()))
                .map(record -> record.getHerbCode())
                .collect(Collectors.toList());
        Map<String, Herb> herbMap = new HashMap<>();
        if (ObjectUtil.isNotEmpty(herbCodeList)) {
            herbMap = herbService.lambdaQuery().eq(Herb::getMedicalGroupId, user.getMedicalGroupId())
                    .in(Herb::getHerbCode, herbCodeList).list()
                    .stream().collect(Collectors.toMap(Herb::getHerbCode, H -> H));
        }
        for (CompatTabImportVO importVO : importList) {
            validateImportData(importVO, herbMap);
        }
        // 设置错误提示
        setCompatTagErrInfo(importList);
        return importList;
    }

    /**
     * 设置错误提示
     *
     * @param importVOList
     */
    public void setCompatTagErrInfo(List<CompatTabImportVO> importVOList) {
        Map<String, List<CompatTabImportVO>> nameMap = importVOList.stream().collect(Collectors.groupingBy(CompatTabImportVO::getTabooName));
        nameMap.entrySet().forEach(entry -> {
            String tabooType = Optional.ofNullable(entry.getValue().get(0).getTabooType()).orElse("");
            String isPregnant = Optional.ofNullable(entry.getValue().get(0).getIsPregnant()).orElse("");
            entry.getValue().forEach(record -> {
                if (!StringUtils.equals(tabooType, Optional.ofNullable(record.getTabooType()).orElse(""))) {
                    setErrMsg(record, "同一个禁忌名称，禁忌类型必须相同");
                }
                if (!StringUtils.equals(isPregnant, Optional.ofNullable(record.getIsPregnant()).orElse(""))) {
                    setErrMsg(record, "同一个禁忌名称，是否妊娠必须相同");
                }
                Map<String, List<CompatTabImportVO>> importVOMap = entry.getValue().stream()
                        .filter(vo -> ObjectUtil.isNotEmpty(vo.getHerbCode()))
                        .collect(Collectors.groupingBy(record1 -> record1.getHerbCode()));
                importVOMap.entrySet().forEach(entry1 -> {
                    if (entry1.getValue().size() > 1) {
                        entry1.getValue().forEach(record2 -> setErrMsg(record2, "饮片编码重复/n"));
                    }
                });
            });
        });
    }

    /**
     * 校验导入数据
     *
     * @param importVO
     * @param herbMap
     */
    private void validateImportData(CompatTabImportVO importVO, Map<String, Herb> herbMap) {
        StringBuilder error = new StringBuilder();
        if (StringUtils.isBlank(importVO.getTabooName())) {
            error.append("禁忌名称不能为空/n");
        }
        if (StringUtils.isBlank(importVO.getTabooType())) {
            error.append("禁忌类型不能为空/n");
        }
        if (StringUtils.isBlank(importVO.getIsPregnant())) {
            error.append("是否妊娠不能为空/n");
        }
        if (StringUtils.isBlank(importVO.getHerbCode())) {
            error.append("饮片编码不能为空/n");
        }
        if (StringUtils.isBlank(importVO.getHerbName())) {
            error.append("饮片名称不能为空/n");
        }
        if (StringUtils.isBlank(importVO.getType())) {
            error.append("饮片类型不能为空/n");
        }
        if (StringUtils.isBlank(importVO.getSpec())) {
            error.append("饮片规格不能为空/n");
        }
        Herb herb = herbMap.get(importVO.getHerbCode());
        if (ObjectUtil.isEmpty(herb)) {
            error.append("饮片编码不存在/n");
        } else {
            if (!StringUtils.equals(herb.getHerbName(), importVO.getHerbName())) {
                error.append("饮片名称错误/n");
            }
            if (!StringUtils.equals(herb.getType(), importVO.getType())) {
                error.append("饮片类型错误/n");
            }
            if (!StringUtils.equals(herb.getSpec(), importVO.getSpec())) {
                error.append("饮片规格错误/n");
            }
        }
        DictDataQueryVO queryVO = new DictDataQueryVO();
        queryVO.setDictType("tabooType");
        Map<String, String> dictDataMap = dictService.getDictDataMap(queryVO, false);
        if (ObjectUtil.isNotEmpty(importVO.getTabooType()) && !dictDataMap.keySet().contains(importVO.getTabooType())) {
            error.append("禁忌类型有误/n");
        }
        // 禁忌类型校验
        importVO.setErrMsg(error.toString());
    }

    /**
     * 设置错误提示
     *
     * @param importVO
     * @param errInfo
     */
    public void setErrMsg(CompatTabImportVO importVO, String errInfo) {
        StringBuilder error = new StringBuilder();
        String errMsg = importVO.getErrMsg();
        if (ObjectUtil.isNotEmpty(errMsg)) {
            error.append(errMsg);
        }
        error.append(errInfo);
        importVO.setErrMsg(error.toString());
    }

    /**
     * 配伍禁忌检查
     *
     * @param compatTabCheckVO
     * @return
     */
    public List<CompatTabCheckResVO> compatTabCheck(CompatTabCheckReqVO compatTabCheckVO) {
        BaseUser currentUser = UserContextHolder.getUserInfoContext();
        List<CompatTabooMgmtVO> compatTabList = this.lambdaQuery().eq(CompatTabooMgmt::getMedicalGroupId,
                currentUser.getMedicalGroupId()).list().stream().map(compatTag -> {
            CompatTabooMgmtVO compatTabooMgmtVO = new CompatTabooMgmtVO();
            BeanUtil.copyProperties(compatTag, compatTabooMgmtVO);
            return compatTabooMgmtVO;
        }).collect(Collectors.toList());
        if (ObjectUtil.isEmpty(compatTabList)) {
            return new ArrayList<>();
        }
        Map<Long, List<CompatTabooMgmtDetailVO>> compatTabMap = compatTabooMgmtDetailService.lambdaQuery()
                .in(CompatTabooMgmtDetail::getTabooMgmtId,
                        compatTabList.stream().map(compatTag -> compatTag.getId()).collect(Collectors.toList()))
                .list().stream().map(detail -> {
                    CompatTabooMgmtDetailVO detailVO = new CompatTabooMgmtDetailVO();
                    BeanUtil.copyProperties(detail, detailVO);
                    return detailVO;
                }).collect(Collectors.groupingBy(CompatTabooMgmtDetailVO::getTabooMgmtId));
        for (CompatTabooMgmtVO compatTabooMgmtVO : compatTabList) {
            compatTabooMgmtVO.setDetailList(compatTabMap.get(compatTabooMgmtVO.getId()));
        }
        List<CompatTabCheckResVO> resVOS = new ArrayList<>();
        compatTabList.forEach(compatTab -> {
            List<String> tabHerbCodeList = Optional.ofNullable(compatTab.getDetailList()).orElse(new ArrayList<>())
                    .stream().map(detail -> detail.getHerbCode()).collect(Collectors.toList());
            String herbName = getCharsBeforeFan(compatTab.getTabooName(),
                    String.valueOf(compatTab.getTabooType().charAt(compatTab.getTabooType().length() - 1)));
            Optional<CompatTabooMgmtDetailVO> herbOptional = Optional.ofNullable(compatTab.getDetailList()).orElse(new ArrayList<>())
                    .stream().filter(detail -> detail.getHerbName().equals(herbName)).findFirst();
            compatTabCheckVO.getHerbCodeList().forEach(herbCode -> {
                if (Lists.newArrayList("妊娠慎用", "妊娠禁用", "毒性").contains(compatTab.getTabooType())) {
                    if (tabHerbCodeList.contains(herbCode)) {
                        CompatTabCheckResVO resVO = new CompatTabCheckResVO();
                        BeanUtil.copyProperties(compatTab, resVO);
                        resVO.setHerbCode(herbCode);
                        if ("妊娠慎用".equals(compatTab.getTabooType()) && "是".equals(compatTabCheckVO.getIsPregnant())) {
                            resVO.setTips("属于妊娠慎用饮片，患者是孕妇请您注意使用!");
                            resVOS.add(resVO);
                        } else if ("妊娠禁用".equals(compatTab.getTabooType()) && "是".equals(compatTabCheckVO.getIsPregnant())) {
                            resVO.setTips("属于妊娠禁用饮片，患者是孕妇，请您换其他饮片使用!");
                            resVOS.add(resVO);
                        } else if ("毒性".equals(compatTab.getTabooType())) {
                            resVO.setTips("属于毒性饮片，请您注意使用!");
                            resVOS.add(resVO);
                        }
                    }
                }
                if (Lists.newArrayList("十八反", "十九畏").contains(compatTab.getTabooType())) {
                    if (herbOptional.isPresent()
                            && !herbCode.equals(herbOptional.get().getHerbCode())) {
                        if (tabHerbCodeList.contains(herbCode) && compatTabCheckVO.getHerbCodeList().contains(herbOptional.get().getHerbCode())) {
                            CompatTabCheckResVO resVO = new CompatTabCheckResVO();
                            BeanUtil.copyProperties(compatTab, resVO);
                            resVO.setHerbCode(herbCode);
                            resVO.setCompHerbCode(herbOptional.get().getHerbCode());
                            resVO.setTips(String.format("%s，请您注意使用!", compatTab.getTabooName()));
                            resVOS.add(resVO);
                        }
                    }
                }
            });
        });
        // 参照物饮片提示
        resVOS.stream().filter(resVO -> ObjectUtil.isNotEmpty(resVO.getTabooType()))
                .collect(Collectors.groupingBy(CompatTabCheckResVO::getTabooType))
                        .entrySet().forEach(entry1 -> {
                    entry1.getValue().stream().filter(resVO -> ObjectUtil.isNotEmpty(resVO.getCompHerbCode()))
                            .collect(Collectors.groupingBy(CompatTabCheckResVO::getCompHerbCode))
                            .entrySet().forEach(entry2 -> {
                                CompatTabCheckResVO resVO = new CompatTabCheckResVO();
                                BeanUtil.copyProperties(entry2.getValue().get(0), resVO);
                                resVO.setHerbCode(entry2.getValue().get(0).getCompHerbCode());
                                resVO.setCompHerbCode(null);
                                resVO.setTips(String.format("%s，请您注意使用!", entry2.getValue().get(0).getTabooName()));
                                resVOS.add(resVO);
                            });
                });
        return resVOS;
    }

    public static String getCharsBeforeFan(String str, String indexName) {
        if(ObjectUtil.isEmpty(indexName)){
            return "";
        }
        // 查找"反"字位置
        int fanIndex = str.indexOf(indexName);
        // 当"反"字不存在（索引为-1）或在开头（索引为0）时，返回空字符串
        if (fanIndex <= 0) {
            return "";
        }
        // 截取"反"字前面的所有字符
        return str.substring(0, fanIndex);
    }
}
