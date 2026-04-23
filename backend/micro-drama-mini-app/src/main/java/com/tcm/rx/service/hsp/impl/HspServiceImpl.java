package com.tcm.rx.service.hsp.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.enums.BooleanEnum;
import com.tcm.common.exception.ServiceException;
import com.tcm.common.utils.EasyExcelUtil;
import com.tcm.rx.entity.auth.CUser;
import com.tcm.rx.entity.auth.CUserDept;
import com.tcm.rx.entity.auth.CUserRole;
import com.tcm.rx.entity.dept.Dept;
import com.tcm.rx.entity.hsp.Hsp;
import com.tcm.rx.feign.dict.vo.DictDataQueryVO;
import com.tcm.rx.feign.dict.vo.DictDataVO;
import com.tcm.rx.mapper.hsp.HspMapper;
import com.tcm.rx.service.auth.*;
import com.tcm.rx.service.dept.IDeptService;
import com.tcm.rx.service.dict.DictService;
import com.tcm.rx.service.hsp.IHspService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.rx.vo.hsp.request.HspQueryVO;
import com.tcm.rx.vo.hsp.request.HspExportExcel;
import com.tcm.rx.vo.hsp.response.HspImportExcel;
import com.tcm.rx.vo.hsp.response.HspVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 诊疗开方系统--医疗机构表 服务实现类
 * </p>
 *
 * @author xph
 * @since 2025-07-10
 */
@Service
public class HspServiceImpl extends ServiceImpl<HspMapper, Hsp> implements IHspService {

    @Resource
    private ICUserService cUserService;

    @Resource
    private IDeptService deptService;

    @Resource
    private ICUserRoleService cUserRoleService;

    @Resource
    private ICUserDeptService cUserDeptService;

    @Resource
    private DictService dictService;

    @Override
    public List<HspVO> hspList(HspQueryVO queryVO) {
        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        if ((Objects.isNull(queryVO.getMedicalGroupId()) || queryVO.getMedicalGroupId() < 1)
                && StringUtils.isBlank(queryVO.getMedicalGroupCode())) {
            queryVO.setMedicalGroupId(loginUser.getMedicalGroupId());
            queryVO.setMedicalGroupCode(loginUser.getMedicalGroupCode());
        }
        return this.baseMapper.hspList(queryVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertHsp(HspVO hspVO) {
        if (Objects.isNull(hspVO) || StringUtils.isBlank(hspVO.getMedicalGroupCode())) {
            throw new ServiceException("参数不能为空");
        }

        if (StringUtils.isBlank(hspVO.getHspName())) {
            throw new ServiceException("医疗机构名称参数不能为空");
        }
        if (StringUtils.isBlank(hspVO.getHspCode())) {
            throw new ServiceException("医疗机构编码参数不能为空");
        }
        if (StringUtils.isBlank(hspVO.getHisSysName())) {
            throw new ServiceException("HIS系统不能为空");
        }
        if (StringUtils.isBlank(hspVO.getHisHspCode())) {
            throw new ServiceException("HIS医院编码不能为空");
        }

        List<Hsp> hspNameList = this.lambdaQuery()
                .eq(Objects.nonNull(hspVO.getMedicalGroupId()) && hspVO.getMedicalGroupId() > 0,
                        Hsp::getMedicalGroupId, hspVO.getMedicalGroupId())
                .eq(StringUtils.isNotBlank(hspVO.getMedicalGroupCode()),
                        Hsp::getMedicalGroupCode, hspVO.getMedicalGroupCode())
                .eq(Hsp::getHspName, hspVO.getHspName())
                .eq(Hsp::getDelFlag, BooleanEnum.FALSE.getNum())
                .list();
        if (CollectionUtils.isNotEmpty(hspNameList)) {
            throw new ServiceException("医疗机构名称，已存在");
        }
        List<Hsp> hspCodeList = this.lambdaQuery()
                .eq(Objects.nonNull(hspVO.getMedicalGroupId()) && hspVO.getMedicalGroupId() > 0,
                        Hsp::getMedicalGroupId, hspVO.getMedicalGroupId())
                .eq(StringUtils.isNotBlank(hspVO.getMedicalGroupCode()),
                        Hsp::getMedicalGroupCode, hspVO.getMedicalGroupCode())
                .eq(Hsp::getHspCode, hspVO.getHspCode())
                .eq(Hsp::getDelFlag, BooleanEnum.FALSE.getNum())
                .list();
        if (CollectionUtils.isNotEmpty(hspCodeList)) {
            throw new ServiceException("医疗机构编码，已存在");
        }

        Hsp hsp = new Hsp();
        BeanUtils.copyProperties(hspVO, hsp);

        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        hsp.setCreateBy(loginUser.getId().toString());
        hsp.setUpdateBy(loginUser.getId().toString());

        Date now = new Date();
        hsp.setCreateTime(now);
        hsp.setUpdateTime(now);

        int insertResult = this.getBaseMapper().insert(hsp);
        return insertResult > 0 ? hsp.getId() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchInsertHsp(List<HspVO> hspVOList) {
        if (CollectionUtils.isEmpty(hspVOList)) {
            throw new ServiceException("参数不能为空");
        }

        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        List<Hsp> hspList = Lists.newArrayList();
        hspVOList.forEach(hspVO -> {
            Hsp hsp = new Hsp();
            BeanUtils.copyProperties(hspVO, hsp);

            hsp.setMedicalGroupId(loginUser.getMedicalGroupId());
            hsp.setMedicalGroupCode(loginUser.getMedicalGroupCode());
            hsp.setCreateBy(loginUser.getId().toString());
            hsp.setUpdateBy(loginUser.getId().toString());

            Date now = new Date();
            hsp.setCreateTime(now);
            hsp.setUpdateTime(now);
            hspList.add(hsp);
        });

        return this.saveBatch(hspList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long updateHsp(HspVO hspVO) {
        if (Objects.isNull(hspVO) || Objects.isNull(hspVO.getId())) {
            throw new ServiceException("参数不能为空");
        }

        List<Hsp> hspNameList = this.lambdaQuery()
                .ne(Hsp::getId, hspVO.getId())
                .eq(Objects.nonNull(hspVO.getMedicalGroupId()) && hspVO.getMedicalGroupId() > 0,
                        Hsp::getMedicalGroupId, hspVO.getMedicalGroupId())
                .eq(StringUtils.isNotBlank(hspVO.getMedicalGroupCode()),
                        Hsp::getMedicalGroupCode, hspVO.getMedicalGroupCode())
                .eq(Hsp::getHspName, hspVO.getHspName())
                .eq(Hsp::getDelFlag, BooleanEnum.FALSE.getNum())
                .list();
        if (CollectionUtils.isNotEmpty(hspNameList)) {
            throw new ServiceException("医疗机构名称，已存在");
        }
        List<Hsp> hspCodeList = this.lambdaQuery()
                .ne(Hsp::getId, hspVO.getId())
                .eq(Objects.nonNull(hspVO.getMedicalGroupId()) && hspVO.getMedicalGroupId() > 0,
                        Hsp::getMedicalGroupId, hspVO.getMedicalGroupId())
                .eq(StringUtils.isNotBlank(hspVO.getMedicalGroupCode()),
                        Hsp::getMedicalGroupCode, hspVO.getMedicalGroupCode())
                .eq(Hsp::getHspCode, hspVO.getHspCode())
                .eq(Hsp::getDelFlag, BooleanEnum.FALSE.getNum())
                .list();
        if (CollectionUtils.isNotEmpty(hspCodeList)) {
            throw new ServiceException("医疗机构编码，已存在");
        }

        Hsp hsp = new Hsp();
        BeanUtils.copyProperties(hspVO, hsp);

        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        hsp.setUpdateBy(loginUser.getId().toString());

        hsp.setUpdateTime(new Date());
        return this.saveOrUpdate(hsp) ? hsp.getId() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteHsp(Long id) {
        if (Objects.isNull(id) || id <= 0){
            throw new ServiceException("id参数不能为空");
        }

        Hsp hsp = this.getById(id);
        if (ObjectUtils.isEmpty(hsp)){
            throw new ServiceException("数据不存在");
        }

        // 删除医疗机构相关的用户、科室数据

        List<CUser> cUserList = cUserService.lambdaQuery()
                .eq(Objects.nonNull(hsp.getMedicalGroupId()) && hsp.getMedicalGroupId() > 0,
                        CUser::getMedicalGroupId, hsp.getMedicalGroupId())
                .eq(StringUtils.isNotBlank(hsp.getMedicalGroupCode()),
                        CUser::getMedicalGroupCode, hsp.getMedicalGroupCode())
                .eq(Objects.nonNull(hsp.getId()) && hsp.getId() > 0,
                        CUser::getHspId, hsp.getId())
                .eq(StringUtils.isNotBlank(hsp.getHspCode()),
                        CUser::getHspCode, hsp.getHspCode())
                .list();
        if (CollectionUtils.isNotEmpty(cUserList)){
            List<Long> bUserIds = cUserList.stream().map(CUser::getId).collect(Collectors.toList());
            // 删除用户关联角色的数据
            cUserRoleService.remove(new LambdaQueryWrapper<CUserRole>()
                    .in(CUserRole::getUserId, bUserIds));
            // 删除用户关联科室的数据
            cUserDeptService.remove(new LambdaQueryWrapper<CUserDept>()
                    .in(CUserDept::getUserId, bUserIds));
        }

        // 删除用户的数据
        cUserService.remove(new LambdaQueryWrapper<CUser>()
                .eq(Objects.nonNull(hsp.getMedicalGroupId()) && hsp.getMedicalGroupId() > 0,
                        CUser::getMedicalGroupId, hsp.getMedicalGroupId())
                .eq(StringUtils.isNotBlank(hsp.getMedicalGroupCode()),
                        CUser::getMedicalGroupCode, hsp.getMedicalGroupCode())
                .eq(Objects.nonNull(hsp.getId()) && hsp.getId() > 0,
                        CUser::getHspId, hsp.getId())
                .eq(StringUtils.isNotBlank(hsp.getHspCode()),
                        CUser::getHspCode, hsp.getHspCode()));

        // 删除科室的数据
        deptService.remove(new LambdaQueryWrapper<Dept>()
                .eq(Objects.nonNull(hsp.getMedicalGroupId()) && hsp.getMedicalGroupId() > 0,
                        Dept::getMedicalGroupId, hsp.getMedicalGroupId())
                .eq(StringUtils.isNotBlank(hsp.getMedicalGroupCode()),
                        Dept::getMedicalGroupCode, hsp.getMedicalGroupCode())
                .eq(Objects.nonNull(hsp.getId()) && hsp.getId() > 0,
                        Dept::getHspId, hsp.getId())
                .eq(StringUtils.isNotBlank(hsp.getHspCode()),
                        Dept::getHspCode, hsp.getHspCode()));

        this.getBaseMapper().deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteHspByMedicalGroup(Long id) {
        if (Objects.isNull(id) || id <= 0){
            return;
        }

        List<Hsp> hspList = this.lambdaQuery()
                .eq(Hsp::getMedicalGroupId, id)
                .list();
        if (CollectionUtils.isEmpty(hspList)){
            return;
        }

        List<Long> hspIdList = hspList.stream().map(Hsp::getId).collect(Collectors.toList());
        hspIdList.forEach(this::deleteHsp);

        this.removeByIds(hspIdList);
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        List<HspImportExcel> excelList = Lists.newArrayList();
        // 下载导入模板
        EasyExcelUtil.exportService(response,"医疗机构管理模板","医疗机构",
                HspImportExcel.class, excelList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<HspVO> importInfo(MultipartFile file) throws IOException {
        // 待导入的数据
        List<HspVO> resultList = Lists.newArrayList();
        // 解析数据
        List fileList = EasyExcelUtil.importService(file, HspImportExcel.class);
        if (CollectionUtils.isEmpty(fileList)) {
            throw new ServiceException("导入数据不能为空");
        }

        // 获取字典的his系统配置数据
        Map<String, DictDataVO> dictDataVOMap = Maps.newHashMap();
        List<DictDataVO> dictDataVOList = dictService.dictDataList(new DictDataQueryVO() {{
            setDictType("hisSystem");
        }});
        if (CollectionUtils.isNotEmpty(dictDataVOList)) {
            dictDataVOMap = dictDataVOList
                    .stream()
                    .collect(Collectors.toMap(DictDataVO::getDictLabel, Function.identity(), (k1, k2) -> k1));
        }

        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        // 转换数据
        for (Object obj : fileList) {
            HspImportExcel excel = (HspImportExcel) obj;
            if (Objects.isNull(excel)){
                continue;
            }

            HspVO hspVO = new HspVO();
            BeanUtils.copyProperties(excel, hspVO);

            // 校验导入数据参数
            this.checkImportData(hspVO, excel, loginUser, dictDataVOMap);

            resultList.add(hspVO);
        }

        return resultList;
    }

    @Override
    public void export(HttpServletResponse response, HspQueryVO queryVO) throws IOException {
        // 查询数据
        List<HspVO> hspVOList = this.hspList(queryVO);
        if (CollectionUtils.isEmpty(hspVOList)) {
            throw new ServiceException("导出数据不能为空");
        }

        List<HspExportExcel> excelList = Lists.newArrayList();
        hspVOList.forEach(o -> {
            HspExportExcel excel = new HspExportExcel();
            BeanUtils.copyProperties(o, excel);
            excel.setConsultName(BooleanEnum.TRUE.getNum().equals(o.getIsConsult()) ? "是" : "否");
            excel.setStatusName(BooleanEnum.TRUE.getNum().equals(o.getStatus()) ? "是" : "否");
            excelList.add(excel);
        });

        //导出
        EasyExcelUtil.exportService(response,"医疗机构管理","医疗机构",
                HspExportExcel.class, excelList);
    }

    /**
     * 校验导入数据参数
     */
    private void checkImportData(HspVO hspVO, HspImportExcel excel, BaseUser loginUser, Map<String, DictDataVO> dictDataVOMap){
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isBlank(hspVO.getHspName())) {
            sb.append("医疗机构名称不能为空/n");
        }
        if (StringUtils.isBlank(hspVO.getHspCode())) {
            sb.append("医疗机构编码不能为空/n");
        }
        if (StringUtils.isBlank(hspVO.getHisSysName())) {
            sb.append("HIS系统不能为空/n");
        } else {
            if (!dictDataVOMap.containsKey(hspVO.getHisSysName())) {
                sb.append("HIS系统填写的值，在字典HIS系统枚举中不存在/n");
            }
        }
        if (StringUtils.isBlank(hspVO.getHisHspCode())) {
            sb.append("HIS医院编码不能为空/n");
        }

        if (StringUtils.isBlank(excel.getConsultName())) {
            sb.append("是否接收会诊不能为空/n");
        } else {
            if ("是".equals(excel.getConsultName()) || "否".equals(excel.getConsultName())) {
                // 是否接受会诊：0.否,1.是
                hspVO.setIsConsult("是".equals(excel.getConsultName())
                        ? BooleanEnum.TRUE.getNum() : BooleanEnum.FALSE.getNum());
            } else {
                sb.append("是否接收会诊的值错误，只能填写“是”或者“否”/n");
            }
        }

        if (StringUtils.isBlank(excel.getStatusName())) {
            sb.append("是否启用不能为空/n");
        } else {
            if ("是".equals(excel.getStatusName()) || "否".equals(excel.getStatusName())) {
                // 状态(0:禁用,1:启用)
                hspVO.setStatus("是".equals(excel.getStatusName())
                        ? BooleanEnum.TRUE.getNum() : BooleanEnum.FALSE.getNum());
            } else {
                sb.append("是否启用的值错误，只能填写“是”或者“否”/n");
            }
        }

        if (StringUtils.isNotBlank(hspVO.getHspName())){
            Hsp hsp = this.lambdaQuery()
                    .eq(Hsp::getHspName, hspVO.getHspName())
                    .eq(Objects.nonNull(loginUser.getMedicalGroupId()) && loginUser.getMedicalGroupId() > 0,
                            Hsp::getMedicalGroupId, loginUser.getMedicalGroupId())
                    .eq(StringUtils.isNotBlank(loginUser.getMedicalGroupCode()),
                            Hsp::getMedicalGroupCode, loginUser.getMedicalGroupCode())
                    .eq(Hsp::getDelFlag, BooleanEnum.FALSE.getNum())
                    .last("limit 1")
                    .one();
            if (Objects.nonNull(hsp) && StringUtils.isNotBlank(hsp.getHspName())){
                sb.append("医疗机构名称 " + hspVO.getHspName() + " 已存在，不能重复/n");
            }
        }

        if (StringUtils.isNotBlank(hspVO.getHspCode())){
            Hsp hsp = this.lambdaQuery()
                    .eq(Hsp::getHspCode, hspVO.getHspCode())
                    .eq(Objects.nonNull(loginUser.getMedicalGroupId()) && loginUser.getMedicalGroupId() > 0,
                            Hsp::getMedicalGroupId, loginUser.getMedicalGroupId())
                    .eq(StringUtils.isNotBlank(loginUser.getMedicalGroupCode()),
                            Hsp::getMedicalGroupCode, loginUser.getMedicalGroupCode())
                    .eq(Hsp::getDelFlag, BooleanEnum.FALSE.getNum())
                    .last("limit 1")
                    .one();
            if (Objects.nonNull(hsp) && StringUtils.isNotBlank(hsp.getHspCode())){
                sb.append("医疗机构编码 " + hspVO.getHspName() + " 已存在，不能重复/n");
            }
        }

        hspVO.setErrMsg(sb.toString());
    }

}
