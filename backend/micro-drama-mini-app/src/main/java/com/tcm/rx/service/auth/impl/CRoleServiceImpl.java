package com.tcm.rx.service.auth.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.enums.BooleanEnum;
import com.tcm.common.exception.ServiceException;
import com.tcm.common.utils.EasyExcelUtil;
import com.tcm.rx.entity.auth.CRole;
import com.tcm.rx.entity.auth.CRoleMenu;
import com.tcm.rx.mapper.auth.CRoleMapper;
import com.tcm.rx.service.auth.ICMenuService;
import com.tcm.rx.service.auth.ICRoleMenuService;
import com.tcm.rx.service.auth.ICRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.rx.vo.auth.request.MenuQueryVO;
import com.tcm.rx.vo.auth.request.RoleQueryVO;
import com.tcm.rx.vo.auth.response.CRoleExportExcel;
import com.tcm.rx.vo.auth.response.CRoleVO;
import com.tcm.rx.vo.hsp.request.HspExportExcel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 诊疗开方系统--角色表 服务实现类
 * </p>
 *
 * @author xph
 * @since 2025-07-08
 */
@Service
public class CRoleServiceImpl extends ServiceImpl<CRoleMapper, CRole> implements ICRoleService {

    @Resource
    private ICRoleMenuService cRoleMenuService;

    @Resource
    private ICMenuService cMenuService;

    @Override
    public List<CRoleVO> roleList(RoleQueryVO queryVO) {
        if ((Objects.isNull(queryVO.getMedicalGroupId()) || queryVO.getMedicalGroupId() < 1)
                && StringUtils.isBlank(queryVO.getMedicalGroupCode())) {
            BaseUser loginUser = UserContextHolder.getUserInfoContext();
            queryVO.setMedicalGroupId(loginUser.getMedicalGroupId());
            queryVO.setMedicalGroupCode(loginUser.getMedicalGroupCode());
        }
        return this.getBaseMapper().roleList(queryVO);
    }

    @Override
    public CRoleVO roleById(RoleQueryVO queryVO) {
        if (Objects.isNull(queryVO) || Objects.isNull(queryVO.getRoleId()) || queryVO.getRoleId() <= 0) {
            throw new ServiceException("id参数不能为空");
        }

        CRole role = this.getById(queryVO.getRoleId());
        if (Objects.isNull(role)) {
            throw new ServiceException("数据不存在");
        }

        CRoleVO roleVO = new CRoleVO();
        BeanUtils.copyProperties(role, roleVO);

        MenuQueryVO mDto = new MenuQueryVO() {{
//            setMedicalGroupId(role.getMedicalGroupId());
//            setMedicalGroupCode(role.getMedicalGroupCode());
//            setRxId(role.getRxId());
//            setRxCode(role.getRxCode());
            setStatus(BooleanEnum.TRUE.getNum());
            setRoleIds(Lists.newArrayList(role.getId()));
        }};

        roleVO.setMenuList(cMenuService.menuByRole(mDto));
        return roleVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertRole(CRoleVO roleVO) {
        if (Objects.isNull(roleVO)) {
            throw new ServiceException("参数不能为空");
        }
        if (Objects.isNull(roleVO.getMedicalGroupId()) && StringUtils.isBlank(roleVO.getMedicalGroupCode())) {
            throw new ServiceException("医联体参数不能为空");
        }
//        if (Objects.isNull(roleVO.getHspId()) && StringUtils.isBlank(roleVO.getHspCode())) {
//            throw new ServiceException("医疗机构参数不能为空");
//        }

        if (StringUtils.isNotBlank(roleVO.getRoleName())) {
            List<CRole> roleNameList = this.lambdaQuery()
                    .eq(Objects.nonNull(roleVO.getMedicalGroupId()) && roleVO.getMedicalGroupId() > 0,
                            CRole::getMedicalGroupId, roleVO.getMedicalGroupId())
                    .eq(StringUtils.isNotBlank(roleVO.getMedicalGroupCode()),
                            CRole::getMedicalGroupCode, roleVO.getMedicalGroupCode())
                    .eq(CRole::getRoleName, roleVO.getRoleName())
                    .eq(CRole::getDelFlag, BooleanEnum.FALSE.getNum())
                    .list();
            if (CollectionUtils.isNotEmpty(roleNameList)) {
                throw new ServiceException("角色名称，已存在");
            }
        }
        if (StringUtils.isNotBlank(roleVO.getRoleCode())) {
            List<CRole> roleNameList = this.lambdaQuery()
                    .eq(Objects.nonNull(roleVO.getMedicalGroupId()) && roleVO.getMedicalGroupId() > 0,
                            CRole::getMedicalGroupId, roleVO.getMedicalGroupId())
                    .eq(StringUtils.isNotBlank(roleVO.getMedicalGroupCode()),
                            CRole::getMedicalGroupCode, roleVO.getMedicalGroupCode())
                    .eq(CRole::getRoleCode, roleVO.getRoleCode())
                    .eq(CRole::getDelFlag, BooleanEnum.FALSE.getNum())
                    .list();
            if (CollectionUtils.isNotEmpty(roleNameList)) {
                throw new ServiceException("权限字符，已存在");
            }
        }

        CRole role = new CRole();
        BeanUtils.copyProperties(roleVO, role);

        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        role.setCreateBy(loginUser.getId().toString());
        role.setUpdateBy(loginUser.getId().toString());
        // 直接取当前用户的类型：P.集团Saas系统管理，B.煎药系统，C.诊疗系统
//        role.setDataSource(loginUser.getPlatformType());

        Date now = new Date();
        role.setCreateTime(now);
        role.setUpdateTime(now);

        int insertResult = this.getBaseMapper().insert(role);
        if (insertResult > 0 && CollectionUtils.isNotEmpty(roleVO.getMenuIds())) {
            List<CRoleMenu> roleMenuList = Lists.newArrayList();
            roleVO.getMenuIds().forEach(menuId -> {
                roleMenuList.add(new CRoleMenu(){{
                    setRoleId(role.getId());
                    setMenuId(menuId);
                }});
            });
            cRoleMenuService.saveBatch(roleMenuList);
        }

        return insertResult > 0 ? role.getId() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long updateRole(CRoleVO roleVO) {
        if (Objects.isNull(roleVO) || Objects.isNull(roleVO.getId())) {
            throw new ServiceException("参数不能为空");
        }

        if (StringUtils.isNotBlank(roleVO.getRoleName())) {
            List<CRole> roleNameList = this.lambdaQuery()
                    .ne(CRole::getId, roleVO.getId())
                    .eq(Objects.nonNull(roleVO.getMedicalGroupId()) && roleVO.getMedicalGroupId() > 0,
                            CRole::getMedicalGroupId, roleVO.getMedicalGroupId())
                    .eq(StringUtils.isNotBlank(roleVO.getMedicalGroupCode()),
                            CRole::getMedicalGroupCode, roleVO.getMedicalGroupCode())
                    .eq(CRole::getRoleName, roleVO.getRoleName())
                    .eq(CRole::getDelFlag, BooleanEnum.FALSE.getNum())
                    .list();
            if (CollectionUtils.isNotEmpty(roleNameList)) {
                throw new ServiceException("角色名称，已存在");
            }
        }
        if (StringUtils.isNotBlank(roleVO.getRoleCode())) {
            List<CRole> roleNameList = this.lambdaQuery()
                    .ne(CRole::getId, roleVO.getId())
                    .eq(Objects.nonNull(roleVO.getMedicalGroupId()) && roleVO.getMedicalGroupId() > 0,
                            CRole::getMedicalGroupId, roleVO.getMedicalGroupId())
                    .eq(StringUtils.isNotBlank(roleVO.getMedicalGroupCode()),
                            CRole::getMedicalGroupCode, roleVO.getMedicalGroupCode())
                    .eq(CRole::getRoleCode, roleVO.getRoleCode())
                    .eq(CRole::getDelFlag, BooleanEnum.FALSE.getNum())
                    .list();
            if (CollectionUtils.isNotEmpty(roleNameList)) {
                throw new ServiceException("权限字符，已存在");
            }
        }

        CRole role = new CRole();
        BeanUtils.copyProperties(roleVO, role);

        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        role.setUpdateBy(loginUser.getId().toString());
        role.setUpdateTime(new Date());

        int updateResult = this.getBaseMapper().updateById(role);
        if (updateResult > 0) {
            // 删除已有的关联数据
            cRoleMenuService.remove(new LambdaQueryWrapper<CRoleMenu>()
                    .eq(CRoleMenu::getRoleId, role.getId()));
            if (CollectionUtils.isNotEmpty(roleVO.getMenuIds())) {
                // 新增新的数据
                List<CRoleMenu> roleMenuList = Lists.newArrayList();
                roleVO.getMenuIds().forEach(menuId -> {
                    roleMenuList.add(new CRoleMenu(){{
                        setRoleId(roleVO.getId());
                        setMenuId(menuId);
                    }});
                });
                cRoleMenuService.saveBatch(roleMenuList);
            }
        }
        return updateResult > 0 ? role.getId() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDeleteRole(RoleQueryVO queryVO) {
        if (Objects.isNull(queryVO) || CollectionUtils.isEmpty(queryVO.getRoleIds())) {
            throw new ServiceException("ids参数不能为空");
        }

        boolean removeResult = this.remove(new LambdaQueryWrapper<CRole>()
                .in(CRole::getId, queryVO.getRoleIds()));
        if (removeResult) {
            // 删除角色关联的菜单权限数据
            cRoleMenuService.remove(new LambdaQueryWrapper<CRoleMenu>()
                    .in(CRoleMenu::getRoleId, queryVO.getRoleIds()));
        }

        return removeResult;
    }

    @Override
    public Boolean batchUpdateStatus(RoleQueryVO queryVO) {
        if (CollectionUtils.isEmpty(queryVO.getRoleIds())
                || Objects.isNull(queryVO.getStatus()) || queryVO.getStatus() < 0){
            throw new ServiceException("ids参数为空或状态值错误");
        }

        BaseUser loginUser = UserContextHolder.getUserInfoContext();

        return this.lambdaUpdate()
                .set(CRole::getStatus, queryVO.getStatus())
                .set(Objects.nonNull(loginUser), CRole::getUpdateBy, loginUser.getId())
                .set(CRole::getUpdateTime, new Date())
                .in(CRole::getId, queryVO.getRoleIds())
                .update();
    }

    @Override
    public List<CRoleVO> rolesByUser(RoleQueryVO queryVO) {
        return this.getBaseMapper().rolesByUser(queryVO);
    }

    @Override
    public void export(HttpServletResponse response, RoleQueryVO queryVO) throws IOException {
        // 查询数据
        List<CRoleVO> roleVOList = this.roleList(queryVO);
        if (CollectionUtils.isEmpty(roleVOList)) {
            throw new ServiceException("导出数据不能为空");
        }

        List<CRoleExportExcel> excelList = Lists.newArrayList();
        roleVOList.forEach(o -> {
            CRoleExportExcel excel = new CRoleExportExcel();
            BeanUtils.copyProperties(o, excel);
            excel.setStatusName(BooleanEnum.TRUE.getNum().equals(o.getStatus()) ? "是" : "否");
            excelList.add(excel);
        });

        //导出
        EasyExcelUtil.exportService(response,"角色管理","角色信息",
                HspExportExcel.class, excelList);
    }

}
