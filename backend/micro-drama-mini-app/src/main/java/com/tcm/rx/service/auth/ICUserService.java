package com.tcm.rx.service.auth;

import com.tcm.common.entity.BaseUser;
import com.tcm.common.patient.response.PatientHisRespDTO;
import com.tcm.rx.entity.auth.CUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.rx.vo.auth.request.UserQueryVO;
import com.tcm.rx.vo.auth.response.CUserVO;
import com.tcm.rx.vo.auth.response.RouterVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 诊疗开方系统--用户表 服务类
 * </p>
 *
 * @author xph
 * @since 2025-07-07
 */
public interface ICUserService extends IService<CUser> {

    /**
     * 查询用户信息的数据
     */
    List<CUserVO> userList(UserQueryVO queryVO);

    /**
     * 根据id查询用户信息的数据
     */
    CUserVO userById(Long id);

    /**
     * 根据条件查询用户信息的数据
     */
    CUserVO userByCondition(UserQueryVO queryVO);

    /**
     * 新增用户信息的数据
     */
    Long insertUser(CUserVO userVO) throws Exception;

    /**
     * 账号管理--批量新增用户信息的数据
     */
    Boolean batchInsertUser(List<CUserVO> userVOList);

    /**
     * 更新用户信息的数据
     */
    Long updateUser(CUserVO userVO);

    /**
     * 更新用户的密码
     */
    Long updatePassword(CUserVO userVO) throws Exception;

    /**
     * 重置密码
     */
    Long defaultPassword(CUserVO userVO);

    /**
     * 更新用户关联角色数据
     */
    Boolean updateUserRoles(CUserVO userVO);

    /**
     * 批量删除用户信息的数据
     */
    Boolean batchDeleteUser(UserQueryVO queryVO);

    /**
     * 批量更新用户信息的状态
     */
    Boolean batchUpdateStatus(UserQueryVO queryVO);

    /**
     * 账号管理--下载导入模版
     */
    void downloadTemplate(HttpServletResponse response) throws IOException;

    /**
     * 账号管理--导入数据
     */
    List<CUserVO> importInfo(MultipartFile file) throws IOException;

    /**
     * 账号管理--导出数据
     */
    void export(HttpServletResponse response, UserQueryVO queryVO) throws IOException;

    /**
     * 获取角色数据权限
     */
    Set<String> getRolePermission(CUserVO userVO);

    /**
     * 获取菜单数据权限
     */
    Set<String> getMenuPermission(CUserVO userVO);

    /**
     * 获取路由信息
     */
    List<RouterVO> getRoutersByUser(CUserVO userVO);

    /**
     * 校验自动登录的用户（对接诊疗系统上传医生和患者信息时，自动登录的用户）
     */
    BaseUser checkRxAutoLogin(PatientHisRespDTO patientHisRespDTO);

}
