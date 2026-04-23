package com.tcm.rx.service.auth.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tcm.common.config.sys.CurrSysConfig;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.enums.BooleanEnum;
import com.tcm.common.exception.ServiceException;
import com.tcm.common.patient.response.PatientHisRespDTO;
import com.tcm.common.utils.EasyExcelUtil;
import com.tcm.common.utils.RsaUtils;
import com.tcm.rx.entity.auth.CRole;
import com.tcm.rx.entity.auth.CUser;
import com.tcm.rx.entity.auth.CUserDept;
import com.tcm.rx.entity.auth.CUserRole;
import com.tcm.rx.mapper.auth.CUserMapper;
import com.tcm.rx.service.auth.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.rx.service.dept.IDeptService;
import com.tcm.rx.vo.auth.request.CUserImportExcel;
import com.tcm.rx.vo.auth.request.MenuQueryVO;
import com.tcm.rx.vo.auth.request.RoleQueryVO;
import com.tcm.rx.vo.auth.request.UserQueryVO;
import com.tcm.rx.vo.auth.response.*;
import com.tcm.rx.vo.dept.request.DeptQueryVO;
import com.tcm.rx.vo.dept.response.DeptVO;
import com.tcm.rx.vo.hsp.request.HspExportExcel;
import com.tcm.rx.vo.hsp.response.HspImportExcel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 诊疗开方系统--用户表 服务实现类
 * </p>
 *
 * @author xph
 * @since 2025-07-07
 */
@Service
public class CUserServiceImpl extends ServiceImpl<CUserMapper, CUser> implements ICUserService {

    @Resource
    private ICRoleService cRoleService;

    @Resource
    private ICMenuService cMenuService;

    @Resource
    private ICUserRoleService cUserRoleService;

    @Resource
    private ICUserDeptService cUserDeptService;

    @Resource
    private IDeptService deptService;

    @Resource
    private CurrSysConfig currSys;

    // 初始化的密码值
    private static final String initPassword = "666666";

    @Override
    public List<CUserVO> userList(UserQueryVO queryVO) {
        if (!(StringUtils.isNotBlank(queryVO.getUserType()) && "admin".equals(queryVO.getUserType()))
                && (Objects.isNull(queryVO.getMedicalGroupId()) || queryVO.getMedicalGroupId() < 1)
                && StringUtils.isBlank(queryVO.getMedicalGroupCode())) {
            BaseUser loginUser = UserContextHolder.getUserInfoContext();
            queryVO.setMedicalGroupId(loginUser.getMedicalGroupId());
            queryVO.setMedicalGroupCode(loginUser.getMedicalGroupCode());
        }
        return this.getBaseMapper().userList(queryVO);
    }

    @Override
    public CUserVO userById(Long id) {
        CUserVO userVO = this.userByCondition(new UserQueryVO() {{
            setUserId(id);
        }});
        if(Objects.isNull(userVO)){
            throw new ServiceException("用户不存在");
        }

        // 获取角色数据
        List<CRoleVO> roleList = Lists.newArrayList();
        List<CMenuVO> menuList = Lists.newArrayList();
        if (userVO.getUserType().equals("admin")){
            List<CRole> roleLists = cRoleService.lambdaQuery()
                    .eq(Objects.nonNull(userVO.getMedicalGroupId()) && userVO.getMedicalGroupId() > 0,
                            CRole::getMedicalGroupId, userVO.getMedicalGroupId())
                    .eq(StringUtils.isNotBlank(userVO.getMedicalGroupCode()),
                            CRole::getMedicalGroupCode, userVO.getMedicalGroupCode())
//                    .eq(Objects.nonNull(userVO.getHspId()) && userVO.getHspId() > 0,
//                            CRole::getHspId, userVO.getHspId())
//                    .eq(StringUtils.isNotBlank(userVO.getHspCode()),
//                            CRole::getHspCode, userVO.getHspCode())
                    .eq(CRole::getDelFlag, BooleanEnum.FALSE.getNum())
                    .eq(CRole::getStatus, BooleanEnum.TRUE.getNum())
                    .orderByAsc(CRole::getSort)
                    .list();
            if (CollectionUtils.isNotEmpty(roleLists)){
                for (CRole role : roleLists){
                    CRoleVO roleVO = new CRoleVO();
                    BeanUtils.copyProperties(role, roleVO);
                    roleList.add(roleVO);
                }
            }
            MenuQueryVO mDto = new MenuQueryVO() {{
//                setMedicalGroupId(userVO.getMedicalGroupId());
//                setMedicalGroupCode(userVO.getMedicalGroupCode());
//                setRxId(userVO.getRxId());
//                setRxCode(userVO.getRxCode());
                setStatus(BooleanEnum.TRUE.getNum());
            }};
            menuList = cMenuService.menuByUser(mDto);
        } else {
            RoleQueryVO rDto = new RoleQueryVO() {{
                setMedicalGroupId(userVO.getMedicalGroupId());
                setMedicalGroupCode(userVO.getMedicalGroupCode());
//                setHspId(userVO.getHspId());
//                setHspCode(userVO.getHspCode());
                setUserIds(Lists.newArrayList(userVO.getId()));
            }};
            roleList = cRoleService.rolesByUser(rDto);
            if (CollectionUtils.isNotEmpty(roleList)){
                MenuQueryVO mDto = new MenuQueryVO() {{
//                    setMedicalGroupId(userVO.getMedicalGroupId());
//                    setMedicalGroupCode(userVO.getMedicalGroupCode());
//                    setRxId(userVO.getRxId());
//                    setRxCode(userVO.getRxCode());
                    setStatus(BooleanEnum.TRUE.getNum());
                    setUserId(userVO.getId());
                }};
                menuList = cMenuService.menuByUser(mDto);
            }
        }

        if (CollectionUtils.isNotEmpty(menuList)){
            Map<Long, List<CMenuVO>> roleMenuMap = menuList.stream()
                    .collect(Collectors.groupingBy(CMenuVO::getRoleId));
            if (CollectionUtils.isNotEmpty(roleList)){
                for (CRoleVO role : roleList) {
                    role.setMenuList(roleMenuMap.getOrDefault(role.getId(), Lists.newArrayList()));
                }
            }
        }

        userVO.setRoleList(roleList);

        // 查询科室
        List<CUserDept> userDeptList = cUserDeptService.lambdaQuery()
                .eq(CUserDept::getUserId, userVO.getId())
                .list();
        if (CollectionUtils.isNotEmpty(userDeptList)){
            userVO.setDeptIds(userDeptList.stream().map(CUserDept::getDeptId).collect(Collectors.toList()));
        }
        return userVO;
    }

    @Override
    public CUserVO userByCondition(UserQueryVO queryVO) {
        if (Objects.isNull(queryVO.getUserId()) || queryVO.getUserId() < 1) {
            throw new ServiceException("查询参数不能为空");
        }
        return this.getBaseMapper().userByCondition(queryVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertUser(CUserVO userVO) throws Exception {
        if (Objects.isNull(userVO.getMedicalGroupId()) && StringUtils.isBlank(userVO.getMedicalGroupCode())) {
            throw new ServiceException("医联体参数不能为空");
        }
        if ((StringUtils.isBlank(userVO.getUserType()) || "user".equals(userVO.getUserType()))
                && Objects.isNull(userVO.getHspId()) && StringUtils.isBlank(userVO.getHspCode())) {
            throw new ServiceException("医疗机构参数不能为空");
        }

        List<CUser> dbList = this.lambdaQuery()
                .eq(CUser::getUserAccount, userVO.getUserAccount())
                .eq(CUser::getDelFlag, BooleanEnum.FALSE.getNum())
                .list();
        if (CollectionUtils.isNotEmpty(dbList)) {
            throw new ServiceException("用户账号，已存在");
        }

        if (StringUtils.isNotBlank(userVO.getUserType()) && "admin".equals(userVO.getUserType())){
            userVO.setHspId(0L);
            userVO.setHspCode("0");
        }

        CUser user = new CUser();
        BeanUtils.copyProperties(userVO, user);
        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        if (Objects.nonNull(loginUser)) {
            user.setCreateBy(loginUser.getId().toString());
            user.setUpdateBy(loginUser.getId().toString());
        } else {
            user.setCreateBy("0");
            user.setUpdateBy("0");
        }
        // 直接取当前用户的类型：P.集团Saas系统管理，B.煎药系统，C.诊疗系统
//        user.setDataSource(loginUser.getPlatformType());

        Date now = new Date();
        user.setCreateTime(now);
        user.setUpdateTime(now);

        if (Objects.nonNull(user.getStatus()) && 2 == user.getStatus()) {
            user.setQuitDate(now);
        }

        user.setPassword(StringUtils.isNotBlank(user.getPassword()) && user.getPassword().startsWith("$2a$10")
                ? user.getPassword()
                : new BCryptPasswordEncoder().encode(StringUtils.isBlank(user.getPassword())
                ? initPassword : RsaUtils.decrypt(user.getPassword(),RsaUtils.getPrivateKey(RsaUtils.getRsaPrivateKey()))));

        int insertResult = this.getBaseMapper().insert(user);
        // 保存用户与角色、科室的关联关系
        if (insertResult > 0){
            if (CollectionUtils.isNotEmpty(userVO.getRoleIds())) {
                // 保存用户与角色的关联关系
                this.saveBatchUserRole(user.getId(), userVO.getRoleIds());
            }
            if (CollectionUtils.isNotEmpty(userVO.getDeptIds())) {
                // 保存用户与科室的关联关系
                this.saveBatchUserDept(user.getId(), userVO.getDeptIds());
            }
        }
        return insertResult > 0 ? user.getId() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchInsertUser(List<CUserVO> userVOList) {
        if (CollectionUtils.isEmpty(userVOList)) {
            throw new ServiceException("参数不能为空");
        }

        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        List<CUser> userList = Lists.newArrayList();
        userVOList.forEach(userVO -> {
            CUser user = new CUser();
            BeanUtils.copyProperties(userVO, user);

            user.setMedicalGroupId(loginUser.getMedicalGroupId());
            user.setMedicalGroupCode(loginUser.getMedicalGroupCode());
            user.setCreateBy(loginUser.getId().toString());
            user.setUpdateBy(loginUser.getId().toString());

            Date now = new Date();
            user.setCreateTime(now);
            user.setUpdateTime(now);

            user.setPassword(new BCryptPasswordEncoder().encode(initPassword));
            userList.add(user);
        });

        boolean saveResult = this.saveBatch(userList);
        if (saveResult && CollectionUtils.isNotEmpty(userVOList)) {
            userList.forEach(user -> {
                if (CollectionUtils.isNotEmpty(user.getRoleIds())) {
                    // 保存用户与角色的关联关系
                    this.saveBatchUserRole(user.getId(), user.getRoleIds());
                }
                if (CollectionUtils.isNotEmpty(user.getDeptIds())) {
                    // 保存用户与科室的关联关系
                    this.saveBatchUserDept(user.getId(), user.getDeptIds());
                }
            });
        }

        return this.saveBatch(userList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long updateUser(CUserVO userVO) {
        if (Objects.isNull(userVO) || Objects.isNull(userVO.getId())) {
            throw new ServiceException("参数不能为空");
        }

        CUser user = new CUser();
        BeanUtils.copyProperties(userVO, user);

        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        if (Objects.nonNull(loginUser)) {
            user.setUpdateBy(loginUser.getId().toString());
        } else {
            user.setUpdateBy("0");
        }

        Date now = new Date();
        user.setUpdateTime(now);
        if (Objects.nonNull(user.getStatus()) && 2 == user.getStatus()) {
            user.setQuitDate(now);
        }

        boolean updateResult = this.saveOrUpdate(user);
        // 保存用户与角色、科室的关联关系
        if (updateResult) {
            if (CollectionUtils.isNotEmpty(userVO.getRoleIds())){
                // 删除此用户与角色的关联关系
                cUserRoleService.remove(new LambdaQueryWrapper<CUserRole>()
                        .eq(CUserRole::getUserId, user.getId()));
                // 保存用户与角色的关联关系
                this.saveBatchUserRole(user.getId(), userVO.getRoleIds());
            }
            if (CollectionUtils.isNotEmpty(userVO.getDeptIds())) {
                // 删除此用户与科室的关联关系
                cUserDeptService.remove(new LambdaQueryWrapper<CUserDept>()
                        .eq(CUserDept::getUserId, user.getId()));
                // 保存用户与科室的关联关系
                this.saveBatchUserDept(user.getId(), userVO.getDeptIds());
            }
        }
        return updateResult ? user.getId() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long updatePassword(CUserVO userVO) throws Exception {
        if (Objects.isNull(userVO) || Objects.isNull(userVO.getId())) {
            throw new ServiceException("id参数不能为空");
        }

        if (StringUtils.isBlank(userVO.getPassword())) {
            throw new ServiceException("密码参数不能为空");
        }

        CUser dbUser = this.getById(userVO.getId());
        if (Objects.isNull(dbUser) || Objects.isNull(dbUser.getId()) || dbUser.getId() < 0
                || Objects.equals(BooleanEnum.TRUE.getNum(), dbUser.getDelFlag())) {
            throw new ServiceException("用户不存在");
        }

        // 校验原密码
        if (StringUtils.isNotBlank(userVO.getOldPassword())) {
            if (StringUtils.isNotBlank(dbUser.getPassword())
                    && !new BCryptPasswordEncoder().matches(userVO.getOldPassword(), dbUser.getPassword())) {
                throw new ServiceException("原密码不正确，修改密码失败");
            }
        }

        CUser user = new CUser();
        BeanUtils.copyProperties(userVO, user);

        user.setPassword(StringUtils.isNotBlank(user.getPassword()) && user.getPassword().startsWith("$2a$10")
                ? user.getPassword() : new BCryptPasswordEncoder().encode(RsaUtils.decrypt(user.getPassword(),
                RsaUtils.getPrivateKey(RsaUtils.getRsaPrivateKey()))));

        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        user.setUpdateBy(loginUser.getId().toString());
        user.setUpdateTime(new Date());

        int updateResult = this.getBaseMapper().updateById(user);
        return updateResult > 0 ? user.getId() : null;
    }

    @Override
    public Long defaultPassword(CUserVO userVO) {
        if (Objects.isNull(userVO) || Objects.isNull(userVO.getId())) {
            throw new ServiceException("id参数不能为空");
        }

        CUser dbUser = this.getById(userVO.getId());
        if (Objects.isNull(dbUser) || Objects.isNull(dbUser.getId()) || dbUser.getId() < 0
                || Objects.equals(BooleanEnum.TRUE.getNum(), dbUser.getDelFlag())) {
            throw new ServiceException("用户不存在");
        }

        CUser user = new CUser();
        BeanUtils.copyProperties(userVO, user);

        user.setPassword(new BCryptPasswordEncoder().encode(initPassword));

        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        user.setUpdateBy(loginUser.getId().toString());
        user.setUpdateTime(new Date());

        int updateResult = this.getBaseMapper().updateById(user);
        return updateResult > 0 ? user.getId() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUserRoles(CUserVO userVO) {
        if (Objects.isNull(userVO) || Objects.isNull(userVO.getId()) || userVO.getId() <= 0) {
            throw new ServiceException("参数不能为空");
        }

        if (CollectionUtils.isEmpty(userVO.getRoleIds())){
            throw new ServiceException("roleIds参数不能为空");
        }

        // 删除已有的关联数据
        cUserRoleService.remove(new LambdaQueryWrapper<CUserRole>()
                .eq(CUserRole::getUserId, userVO.getId()));

        // 新增新的数据
        List<CUserRole> roleUserList = Lists.newArrayList();
        userVO.getRoleIds().forEach(roleId -> {
            roleUserList.add(new CUserRole(){{
                setRoleId(roleId);
                setUserId(userVO.getId());
            }});
        });
        return cUserRoleService.saveBatch(roleUserList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDeleteUser(UserQueryVO queryVO) {
        if (CollectionUtils.isEmpty(queryVO.getUserIds())){
            throw new ServiceException("ids参数为空或状态值错误");
        }

        boolean removeResult = this.remove(new LambdaQueryWrapper<CUser>()
                .in(CUser::getId, queryVO.getUserIds()));
        if (removeResult) {
            // 删除用户与角色的关联数据
            cUserRoleService.remove(new LambdaQueryWrapper<CUserRole>()
                    .in(CUserRole::getUserId, queryVO.getUserIds()));
        }
        return removeResult;
    }

    @Override
    public Boolean batchUpdateStatus(UserQueryVO queryVO) {
        if (CollectionUtils.isEmpty(queryVO.getUserIds())
                || Objects.isNull(queryVO.getStatus()) || queryVO.getStatus() < 0){
            throw new ServiceException("ids参数为空或状态值错误");
        }

        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        return this.lambdaUpdate()
                .set(CUser::getStatus, queryVO.getStatus())
                .set(Objects.nonNull(loginUser), CUser::getUpdateBy, loginUser.getId())
                .set(CUser::getUpdateTime, new Date())
                .in(CUser::getId, queryVO.getUserIds())
                .update();
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        List<CUserImportExcel> excelList = Lists.newArrayList();
        // 下载导入模板
        EasyExcelUtil.exportService(response,"账号管理模板","账号信息",
                HspImportExcel.class, excelList);
    }

    @Override
    public List<CUserVO> importInfo(MultipartFile file) throws IOException {
        // 待导入的数据
        List<CUserVO> resultList = Lists.newArrayList();
        // 解析数据
        List fileList = EasyExcelUtil.importService(file, CUserImportExcel.class);
        if (CollectionUtils.isEmpty(fileList)) {
            throw new ServiceException("导入数据不能为空");
        }

        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        // 获取医联体下，各医疗机构的科室数据
        Map<String, Set<String>> hspNameDeptNamesMap = Maps.newHashMap();
        List<DeptVO> deptVOList = deptService.deptList(new DeptQueryVO() {{
            setMedicalGroupId(loginUser.getMedicalGroupId());
            setMedicalGroupCode(loginUser.getMedicalGroupCode());
        }});
        if (CollectionUtils.isNotEmpty(deptVOList)) {
            Map<String, List<DeptVO>> hspNameMap = deptVOList.stream()
                    .collect(Collectors.groupingBy(DeptVO::getHspName));
            hspNameMap.forEach((k, v) -> hspNameDeptNamesMap.put(k, v.stream()
                    .map(DeptVO::getDeptName)
                    .collect(Collectors.toSet())));
        }

        // 获取医联体下的角色数据
        List<String> roleNameList = Lists.newArrayList();
        List<CRoleVO> roleVOList = cRoleService.roleList(new RoleQueryVO() {{
            setMedicalGroupId(loginUser.getMedicalGroupId());
            setMedicalGroupCode(loginUser.getMedicalGroupCode());
        }});
        if (CollectionUtils.isNotEmpty(roleVOList)) {
            roleNameList = roleVOList.stream()
                    .map(CRoleVO::getRoleName)
                    .collect(Collectors.toList());
        }

        // 转换数据
        for (Object obj : fileList) {
            CUserImportExcel excel = (CUserImportExcel) obj;
            if (Objects.isNull(excel)){
                continue;
            }

            CUserVO userVO = new CUserVO();
            BeanUtils.copyProperties(excel, userVO);

            // 是否接受会诊：0.否,1.是
            userVO.setIsConsult(StringUtils.isNotBlank(excel.getConsultName()) && "是".equals(excel.getConsultName())
                    ? BooleanEnum.TRUE.getNum() : BooleanEnum.FALSE.getNum());

            // 校验导入数据参数
            this.checkImportData(userVO, hspNameDeptNamesMap, roleNameList);

            resultList.add(userVO);
        }

        return resultList;
    }

    @Override
    public void export(HttpServletResponse response, UserQueryVO queryVO) throws IOException {
        // 查询数据
        List<CUserVO> userVOList = this.userList(queryVO);
        if (CollectionUtils.isEmpty(userVOList)) {
            throw new ServiceException("导出数据不能为空");
        }

        List<CUserExportExcel> excelList = Lists.newArrayList();
        userVOList.forEach(o -> {
            CUserExportExcel excel = new CUserExportExcel();
            BeanUtils.copyProperties(o, excel);
            excel.setStatusName(BooleanEnum.TRUE.getNum().equals(o.getStatus()) ? "是" : "否");
            excel.setConsultName(BooleanEnum.TRUE.getNum().equals(o.getIsConsult()) ? "是" : "否");
            excelList.add(excel);
        });

        //导出
        EasyExcelUtil.exportService(response,"账号管理","账号信息",
                HspExportExcel.class, excelList);
    }

    /**
     * 校验导入数据参数
     */
    private void checkImportData(CUserVO userVO,
                                 Map<String, Set<String>> hspNameDeptNamesMap,
                                 List<String> roleNameList){
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isBlank(userVO.getUserAccount())) {
            sb.append("账号不能为空/n");
        }
        if (StringUtils.isBlank(userVO.getRealName())) {
            sb.append("姓名不能为空/n");
        }
        if (StringUtils.isBlank(userVO.getHspName())) {
            sb.append("医疗机构不能为空/n");
        }
        if (StringUtils.isBlank(userVO.getDeptNames())) {
            sb.append("科室不能为空/n");
        }
        if (StringUtils.isBlank(userVO.getRoleNames())) {
            sb.append("角色不能为空/n");
        }
        if (Objects.isNull(userVO.getIsConsult())) {
            sb.append("是否接受会诊不能为空/n");
        }

        if (StringUtils.isNotBlank(userVO.getHspName())
                && !hspNameDeptNamesMap.containsKey(userVO.getHspName())) {
            sb.append("医疗机构 " + userVO.getHspName() + " 不存在/n");
        }
        if (StringUtils.isNotBlank(userVO.getDeptNames())
                && StringUtils.isNotBlank(userVO.getHspName())
                && hspNameDeptNamesMap.containsKey(userVO.getHspName())) {
            Set<String> dbDeptNameSet = hspNameDeptNamesMap.get(userVO.getHspName());
            Set<String> deptNames = Sets.newHashSet(userVO.getDeptNames().split(","));
            deptNames.forEach(deptName -> {
                if (!dbDeptNameSet.contains(deptName)) {
                    sb.append("医疗机构 "+ userVO.getHspName() + " 里面，科室 " + deptName + " 不存在/n");
                }
            });
        }
        if (StringUtils.isNotBlank(userVO.getRoleNames())){
            Set<String> roleNames = Sets.newHashSet(userVO.getRoleNames().split(","));
            roleNames.forEach(roleName -> {
                if (!roleNameList.contains(roleName)) {
                    sb.append("角色 " + roleName + " 不存在/n");
                }
            });
        }

        if (StringUtils.isNotBlank(userVO.getUserAccount())){
            List<CUser> dbList = this.lambdaQuery()
                    .eq(CUser::getUserAccount, userVO.getUserAccount())
                    .eq(CUser::getDelFlag, BooleanEnum.FALSE.getNum())
                    .list();
            if (CollectionUtils.isNotEmpty(dbList)) {
                sb.append("用户账号 " + userVO.getUserAccount() + " 已存在，不能重复/n");
            }
        }

        userVO.setErrMsg(sb.toString());
    }

    @Override
    public Set<String> getRolePermission(CUserVO userVO) {
        Set<String> roles = Sets.newHashSet();
        if (Objects.isNull(userVO) || Objects.isNull(userVO.getId())) {
            return roles;
        }

        // 管理员拥有所有权限
        if ("admin".equals(userVO.getUserType())) {
            roles.add("admin");
        } else {
            RoleQueryVO rDto = new RoleQueryVO() {{
                setMedicalGroupId(userVO.getMedicalGroupId());
                setMedicalGroupCode(userVO.getMedicalGroupCode());
//                setHspId(userVO.getHspId());
//                setHspCode(userVO.getHspCode());
                setUserIds(Lists.newArrayList(userVO.getId()));
            }};
            List<CRoleVO> roleList = cRoleService.rolesByUser(rDto);
            if (CollectionUtils.isNotEmpty(roleList)) {
                Set<String> roleKeySet = new HashSet<>();
                for (CRoleVO role : roleList) {
                    if (Objects.nonNull(role) && StringUtils.isNotBlank(role.getRoleCode())) {
                        roleKeySet.addAll(Arrays.asList(role.getRoleCode().trim().split(",")));
                    }
                }
                roles.addAll(roleKeySet);
            }
        }
        return roles;
    }

    @Override
    public Set<String> getMenuPermission(CUserVO userVO) {
        Set<String> result = new HashSet<>();
        if (Objects.isNull(userVO) || Objects.isNull(userVO.getId())) {
            return result;
        }
        // 管理员拥有所有权限
        if ("admin".equals(userVO.getUserType())) {
            result.add("*:*:*");
        } else {
            RoleQueryVO rDto = new RoleQueryVO() {{
                setMedicalGroupId(userVO.getMedicalGroupId());
                setMedicalGroupCode(userVO.getMedicalGroupCode());
//                setHspId(userVO.getHspId());
//                setHspCode(userVO.getHspCode());
                setUserIds(Lists.newArrayList(userVO.getId()));
            }};
            List<CRoleVO> roleList = cRoleService.rolesByUser(rDto);

            MenuQueryVO mDto = new MenuQueryVO() {{
//                setMedicalGroupId(userVO.getMedicalGroupId());
//                setMedicalGroupCode(userVO.getMedicalGroupCode());
//                setRxId(userVO.getRxId());
//                setRxCode(userVO.getRxCode());
                setStatus(BooleanEnum.TRUE.getNum());
            }};
            List<CMenuVO> menuList;
            if (CollectionUtils.isNotEmpty(roleList)) {
                Set<Long> roleIds = roleList.stream().map(CRoleVO::getId).collect(Collectors.toSet());
                mDto.setRoleIds(Lists.newArrayList(roleIds));
                menuList = cMenuService.menuByRole(mDto);
            } else {
                mDto.setUserId(userVO.getId());
                menuList = cMenuService.menuByUser(mDto);
            }

            result.addAll(this.handleMenuPerms(menuList));
        }
        return result;
    }

    @Override
    public List<RouterVO> getRoutersByUser(CUserVO userVO) {
        if (Objects.isNull(userVO) || Objects.isNull(userVO.getId())) {
            return Collections.emptyList();
        }

        MenuQueryVO menuQueryVO = new MenuQueryVO() {{
//            setMedicalGroupId(userVO.getMedicalGroupId());
//            setMedicalGroupCode(userVO.getMedicalGroupCode());
//            setRxId(userVO.getRxId());
//            setRxCode(userVO.getRxCode());
            setUserId(userVO.getId());
        }};

        List<CMenuVO> menus = cMenuService.menuTreeByUser(menuQueryVO);
        return cMenuService.buildMenus(menus);
    }

    @Override
    public BaseUser checkRxAutoLogin(PatientHisRespDTO patientHisRespDTO) {
        // 获取用户信息
        CUser user = this.lambdaQuery()
                .eq(CUser::getId, patientHisRespDTO.getUserId())
                .eq(StringUtils.isNotBlank(patientHisRespDTO.getUserAccount()),
                        CUser::getUserAccount, patientHisRespDTO.getUserAccount())
                .orderByDesc(CUser::getId)
                .last("limit 1")
                .one();
        if (ObjectUtils.isEmpty(user)) {
            throw new ServiceException("登录用户：" + patientHisRespDTO.getUserAccount() + " 不存在");
        } else if (BooleanEnum.TRUE.getNum().equals(user.getDelFlag())) {
            throw new ServiceException("对不起，您的账号：" + patientHisRespDTO.getUserAccount() + " 已被删除");
        } else if (BooleanEnum.FALSE.getNum().equals(user.getStatus())) {
            throw new ServiceException("对不起，您的账号：" + patientHisRespDTO.getUserAccount() + " 已停用");
        } else if (2 == user.getStatus()) {
            throw new ServiceException("对不起，您的账号：" + patientHisRespDTO.getUserAccount() + " 已离职");
        }

        BaseUser baseUser = new BaseUser();
        baseUser.setUsername(user.getUserAccount());
        BeanUtils.copyProperties(user, baseUser);
        baseUser.setPlatformType(currSys.getType());
        baseUser.setPassword(user.getPassword());
        return baseUser;
    }

    /**
     * 保存用户与角色的关联关系
     */
    private void saveBatchUserRole(Long userId, List<Long> roleIds){
        List<CUserRole> userRoleList = Lists.newArrayList();
        for (Long roleId : roleIds){
            CUserRole userRole = new CUserRole();
            userRole.setRoleId(roleId);
            userRole.setUserId(userId);
            userRoleList.add(userRole);
        }
        cUserRoleService.saveBatch(userRoleList);
    }

    /**
     * 保存用户与科室的关联关系
     */
    private void saveBatchUserDept(Long userId, List<Long> deptIds){
        List<CUserDept> userDeptList = Lists.newArrayList();
        for (Long deptId : deptIds){
            CUserDept userDept = new CUserDept();
            userDept.setUserId(userId);
            userDept.setDeptId(deptId);
            userDeptList.add(userDept);
        }
        cUserDeptService.saveBatch(userDeptList);
    }

    /**
     * 处理菜单权限的数据
     */
    private Set<String> handleMenuPerms(List<CMenuVO> menuList){
        Set<String> result = Sets.newHashSet();
        if (CollectionUtils.isNotEmpty(menuList)) {
            Set<String> menuPermsSet = new HashSet<>();
            Set<String> roleMenuPerms = menuList.stream().map(CMenuVO::getPerms).collect(Collectors.toSet());
            for (String perm : roleMenuPerms) {
                if (StringUtils.isNotEmpty(perm)) {
                    menuPermsSet.addAll(Arrays.asList(perm.trim().split(",")));
                }
            }
            result.addAll(menuPermsSet);
        }
        return result;
    }

}
