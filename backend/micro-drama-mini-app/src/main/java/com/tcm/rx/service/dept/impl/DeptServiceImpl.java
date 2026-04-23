package com.tcm.rx.service.dept.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.enums.BooleanEnum;
import com.tcm.common.exception.ServiceException;
import com.tcm.common.utils.EasyExcelUtil;
import com.tcm.rx.entity.dept.Dept;
import com.tcm.rx.mapper.dept.DeptMapper;
import com.tcm.rx.service.dept.IDeptService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.rx.service.hsp.IHspService;
import com.tcm.rx.vo.dept.request.DeptQueryVO;
import com.tcm.rx.vo.dept.request.DeptExportExcel;
import com.tcm.rx.vo.dept.response.DeptImportExcel;
import com.tcm.rx.vo.dept.response.DeptVO;
import com.tcm.rx.vo.hsp.request.HspQueryVO;
import com.tcm.rx.vo.hsp.response.HspVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
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
 * 诊疗开方系统--科室表 服务实现类
 * </p>
 *
 * @author xph
 * @since 2025-07-10
 */
@Service
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements IDeptService {

    @Resource
    private IHspService hspService;

    @Override
    public List<DeptVO> deptList(DeptQueryVO queryVO) {
        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        if ((Objects.isNull(queryVO.getMedicalGroupId()) || queryVO.getMedicalGroupId() < 1)
                && StringUtils.isBlank(queryVO.getMedicalGroupCode())) {
            queryVO.setMedicalGroupId(loginUser.getMedicalGroupId());
            queryVO.setMedicalGroupCode(loginUser.getMedicalGroupCode());
        }
        if (Objects.nonNull(loginUser)
                && BooleanEnum.FALSE.getNum().longValue() != loginUser.getHspId()
                && !BooleanEnum.FALSE.getNumStr().equals(loginUser.getHspCode())){
            queryVO.setHspId(loginUser.getHspId());
            queryVO.setHspCode(loginUser.getHspCode());
        }
        return this.baseMapper.deptList(queryVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertDept(DeptVO deptVO) {
        if (Objects.isNull(deptVO) || StringUtils.isBlank(deptVO.getHspCode())) {
            throw new ServiceException("参数不能为空");
        }

        if (StringUtils.isBlank(deptVO.getDeptName())) {
            throw new ServiceException("科室名称参数不能为空");
        }

        if (StringUtils.isBlank(deptVO.getDeptCode())) {
            throw new ServiceException("科室编码参数不能为空");
        }

        List<Dept> deptNameList = this.lambdaQuery()
                .eq(Objects.nonNull(deptVO.getMedicalGroupId()) && deptVO.getMedicalGroupId() > 0,
                        Dept::getMedicalGroupId, deptVO.getMedicalGroupId())
                .eq(StringUtils.isNotBlank(deptVO.getMedicalGroupCode()),
                        Dept::getMedicalGroupCode, deptVO.getMedicalGroupCode())
                .eq(Objects.nonNull(deptVO.getHspId()) && deptVO.getHspId() > 0,
                        Dept::getHspId, deptVO.getHspId())
                .eq(StringUtils.isNotBlank(deptVO.getHspCode()),
                        Dept::getHspCode, deptVO.getHspCode())
                .eq(Dept::getDeptName, deptVO.getDeptName())
                .eq(Dept::getDelFlag, BooleanEnum.FALSE.getNum())
                .list();
        if (CollectionUtils.isNotEmpty(deptNameList)) {
            throw new ServiceException("科室名称，已存在");
        }
        List<Dept> deptCodeList = this.lambdaQuery()
                .eq(Objects.nonNull(deptVO.getMedicalGroupId()) && deptVO.getMedicalGroupId() > 0,
                        Dept::getMedicalGroupId, deptVO.getMedicalGroupId())
                .eq(StringUtils.isNotBlank(deptVO.getMedicalGroupCode()),
                        Dept::getMedicalGroupCode, deptVO.getMedicalGroupCode())
                .eq(Objects.nonNull(deptVO.getHspId()) && deptVO.getHspId() > 0,
                        Dept::getHspId, deptVO.getHspId())
                .eq(StringUtils.isNotBlank(deptVO.getHspCode()),
                        Dept::getHspCode, deptVO.getHspCode())
                .eq(Dept::getDeptCode, deptVO.getDeptCode())
                .eq(Dept::getDelFlag, BooleanEnum.FALSE.getNum())
                .list();
        if (CollectionUtils.isNotEmpty(deptCodeList)) {
            throw new ServiceException("科室编码，已存在");
        }

        Dept dept = new Dept();
        BeanUtils.copyProperties(deptVO, dept);

        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        dept.setCreateBy(loginUser.getId().toString());
        dept.setUpdateBy(loginUser.getId().toString());

        Date now = new Date();
        dept.setCreateTime(now);
        dept.setUpdateTime(now);

        int insertResult = this.getBaseMapper().insert(dept);
        return insertResult > 0 ? dept.getId() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchInsertDept(List<DeptVO> deptVOList) {
        if (CollectionUtils.isEmpty(deptVOList)) {
            throw new ServiceException("参数不能为空");
        }

        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        List<Dept> deptList = Lists.newArrayList();
        deptVOList.forEach(deptVO -> {
            Dept dept = new Dept();
            BeanUtils.copyProperties(deptVO, dept);

            dept.setMedicalGroupId(loginUser.getMedicalGroupId());
            dept.setMedicalGroupCode(loginUser.getMedicalGroupCode());
            dept.setCreateBy(loginUser.getId().toString());
            dept.setUpdateBy(loginUser.getId().toString());

            Date now = new Date();
            dept.setCreateTime(now);
            dept.setUpdateTime(now);
            deptList.add(dept);
        });

        return this.saveBatch(deptList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long updateDept(DeptVO deptVO) {
        if (Objects.isNull(deptVO) || Objects.isNull(deptVO.getId())) {
            throw new ServiceException("参数不能为空");
        }

        List<Dept> hspList = this.lambdaQuery()
                .ne(Dept::getId, deptVO.getId())
                .eq(Objects.nonNull(deptVO.getMedicalGroupId()) && deptVO.getMedicalGroupId() > 0,
                        Dept::getMedicalGroupId, deptVO.getMedicalGroupId())
                .eq(StringUtils.isNotBlank(deptVO.getMedicalGroupCode()),
                        Dept::getMedicalGroupCode, deptVO.getMedicalGroupCode())
                .eq(Objects.nonNull(deptVO.getHspId()) && deptVO.getHspId() > 0,
                        Dept::getHspId, deptVO.getHspId())
                .eq(StringUtils.isNotBlank(deptVO.getHspCode()),
                        Dept::getHspCode, deptVO.getHspCode())
                .eq(Dept::getDeptName, deptVO.getDeptName())
                .eq(Dept::getDelFlag, BooleanEnum.FALSE.getNum())
                .list();
        if (CollectionUtils.isNotEmpty(hspList)) {
            throw new ServiceException("科室名称，已存在");
        }
        List<Dept> deptCodeList = this.lambdaQuery()
                .ne(Dept::getId, deptVO.getId())
                .eq(Objects.nonNull(deptVO.getMedicalGroupId()) && deptVO.getMedicalGroupId() > 0,
                        Dept::getMedicalGroupId, deptVO.getMedicalGroupId())
                .eq(StringUtils.isNotBlank(deptVO.getMedicalGroupCode()),
                        Dept::getMedicalGroupCode, deptVO.getMedicalGroupCode())
                .eq(Objects.nonNull(deptVO.getHspId()) && deptVO.getHspId() > 0,
                        Dept::getHspId, deptVO.getHspId())
                .eq(StringUtils.isNotBlank(deptVO.getHspCode()),
                        Dept::getHspCode, deptVO.getHspCode())
                .eq(Dept::getDeptCode, deptVO.getDeptCode())
                .eq(Dept::getDelFlag, BooleanEnum.FALSE.getNum())
                .list();
        if (CollectionUtils.isNotEmpty(deptCodeList)) {
            throw new ServiceException("科室编码，已存在");
        }

        Dept dept = new Dept();
        BeanUtils.copyProperties(deptVO, dept);

        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        dept.setUpdateBy(loginUser.getId().toString());

        dept.setUpdateTime(new Date());
        return this.saveOrUpdate(dept) ? dept.getId() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDept(Long id) {
        if (Objects.isNull(id) || id <= 0){
            throw new ServiceException("id参数不能为空");
        }

        Dept dept = this.getById(id);
        if (ObjectUtils.isEmpty(dept)){
            throw new ServiceException("数据不存在");
        }

        this.getBaseMapper().deleteById(id);
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        List<DeptImportExcel> excelList = Lists.newArrayList();
        // 下载导入模板
        EasyExcelUtil.exportService(response,"科室管理模板","科室",
                DeptImportExcel.class, excelList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DeptVO> importInfo(MultipartFile file) throws IOException {
        // 待导入的数据
        List<DeptVO> resultList = Lists.newArrayList();
        // 解析数据
        List fileList = EasyExcelUtil.importService(file, DeptImportExcel.class);
        if (CollectionUtils.isEmpty(fileList)) {
            throw new ServiceException("导入数据不能为空");
        }

        BaseUser loginUser = UserContextHolder.getUserInfoContext();

        // 获取医联体下的医疗机构数据
        Map<String, HspVO> hspVOMap = Maps.newHashMap();
        List<HspVO> hspVOList = hspService.hspList(new HspQueryVO() {{
            setMedicalGroupId(loginUser.getMedicalGroupId());
            setMedicalGroupCode(loginUser.getMedicalGroupCode());
        }});
        if (CollectionUtils.isNotEmpty(hspVOList)) {
            hspVOMap = hspVOList.stream()
                    .collect(Collectors.toMap(HspVO::getHspName, Function.identity(), (k1, k2) -> k1));
        }

        // 转换数据
        for (Object obj : fileList) {
            DeptImportExcel excel = (DeptImportExcel) obj;
            if (Objects.isNull(excel)){
                continue;
            }

            DeptVO deptVO = new DeptVO();
            BeanUtils.copyProperties(excel, deptVO);

            // 校验导入数据参数
            this.checkImportData(deptVO, excel, loginUser, hspVOMap);

            resultList.add(deptVO);
        }

        return resultList;
    }

    @Override
    public void export(HttpServletResponse response, DeptQueryVO queryVO) throws IOException {
        // 查询数据
        List<DeptVO> deptVOList = this.deptList(queryVO);
        if (CollectionUtils.isEmpty(deptVOList)) {
            throw new ServiceException("导出数据不能为空");
        }

        List<DeptExportExcel> excelList = Lists.newArrayList();
        deptVOList.forEach(o -> {
            DeptExportExcel excel = new DeptExportExcel();
            BeanUtils.copyProperties(o, excel);
            excel.setStatusName(BooleanEnum.TRUE.getNum().equals(o.getStatus()) ? "是" : "否");
            excelList.add(excel);
        });

        //导出
        EasyExcelUtil.exportService(response,"科室管理","科室",
                DeptExportExcel.class, excelList);
    }

    /**
     * 校验导入数据参数
     */
    private void checkImportData(DeptVO deptVO, DeptImportExcel excel, BaseUser loginUser, Map<String, HspVO> hspVOMap){
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isBlank(deptVO.getHspName())) {
            sb.append("医疗机构名称不能为空/n");
        }
        if (StringUtils.isBlank(deptVO.getDeptName())) {
            sb.append("科室名称不能为空/n");
        }

        if (StringUtils.isNotBlank(excel.getSort()) && excel.getSort().matches("\\d+")) {
            deptVO.setSort(Integer.parseInt(excel.getSort()));
        } else {
            sb.append("排序不能为空，且必须是正整数/n");
        }

        if (StringUtils.isNotBlank(excel.getStatusName())) {
            if ("是".equals(excel.getStatusName()) || "否".equals(excel.getStatusName())) {
                // 状态(0:禁用,1:启用)
                deptVO.setStatus("是".equals(excel.getStatusName())
                        ? BooleanEnum.TRUE.getNum() : BooleanEnum.FALSE.getNum());
            } else {
                sb.append("是否启用的值错误，只能填写“是”或者“否”/n");
            }
        }

        // 根据医疗机构名称获取医疗机构的信息
        HspVO hspVO = hspVOMap.getOrDefault(deptVO.getHspName(), null);
        if (StringUtils.isNotBlank(deptVO.getHspName())) {
            if (Objects.nonNull(hspVO)
                    && StringUtils.isNotBlank(hspVO.getHspCode())){
                // 设置医疗机构的参数
                deptVO.setHspId(hspVO.getId());
                deptVO.setHspCode(hspVO.getHspCode());
            } else {
                sb.append("医疗机构名称 " + deptVO.getHspName() + " 不存在/n");
            }
        }

        if (StringUtils.isNotBlank(deptVO.getDeptName())){
            LambdaQueryWrapper<Dept> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Dept::getDeptName, deptVO.getDeptName())
                    .eq(Objects.nonNull(loginUser.getMedicalGroupId()) && loginUser.getMedicalGroupId() > 0,
                            Dept::getMedicalGroupId, loginUser.getMedicalGroupId())
                    .eq(StringUtils.isNotBlank(loginUser.getMedicalGroupCode()),
                            Dept::getMedicalGroupCode, loginUser.getMedicalGroupCode());
            if (Objects.nonNull(hspVO)){
                queryWrapper.eq(Objects.nonNull(hspVO.getId()) && hspVO.getId() > 0,
                        Dept::getHspId, hspVO.getId())
                        .eq(StringUtils.isNotBlank(hspVO.getHspCode()),
                                Dept::getHspCode, hspVO.getHspCode());
            }
            queryWrapper.eq(Dept::getDelFlag, BooleanEnum.FALSE.getNum())
                    .last("limit 1");
            Dept dept = this.getOne(queryWrapper);
            if (Objects.nonNull(dept) && StringUtils.isNotBlank(dept.getDeptName())){
                sb.append("科室名称 " + deptVO.getHspName() + " 已存在，不能重复/n");
            }
        }

        deptVO.setErrMsg(sb.toString());
    }

}
