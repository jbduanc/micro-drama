package com.tcm.rx.controller.auth;

import com.google.common.collect.Maps;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.entity.LoginBody;
import com.tcm.common.entity.Result;
import com.tcm.common.exception.ServiceException;
import com.tcm.common.patient.response.PatientHisRespDTO;
import com.tcm.common.utils.RsaUtils;
import com.tcm.rx.entity.auth.CUser;
import com.tcm.rx.service.auth.ICUserService;
import com.tcm.rx.service.auth.impl.SysLoginService;
import com.tcm.rx.vo.auth.request.UserQueryVO;
import com.tcm.rx.vo.auth.response.CUserVO;
import com.tcm.rx.vo.auth.response.CaptchaCodeVO;
import com.tcm.rx.vo.auth.response.RouterVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * <p>
 * 诊疗系统--登录用户 前端控制器
 * </p>
 *
 * @author shouhan
 * @since 2025-06-17
 */
@Slf4j
@RestController
@RequestMapping("/c")
public class CLoginController {

    @Resource
    private SysLoginService loginService;

    @Resource
    private ICUserService cUserService;

    /**
     * 获取RSA公钥
     */
    @GetMapping(value = "/getRsaPublicKey")
    public String getRsaPublicKey() {
        return RsaUtils.getRsaPublicKey();
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginBody loginBody) throws Exception {
        Result result = Result.ok();
        // 解密密码的值
        if (StringUtils.isNotBlank(loginBody.getPassword())){
            loginBody.setPassword(RsaUtils.decrypt(loginBody.getPassword(),
                    RsaUtils.getPrivateKey(RsaUtils.getRsaPrivateKey())));
        }

        // 生成令牌
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid());
        result.setData(token);
        return result;
    }

    /**
     * 用户登录（对接诊疗系统上传医生和患者信息时，自动登录）
     */
    @PostMapping("/autoLogin")
    public Result autoLogin(@RequestBody PatientHisRespDTO patientHisRespDTO) throws Exception {
        if (Objects.isNull(patientHisRespDTO)){
            throw new ServiceException("参数对象不能为空");
        }
        if (StringUtils.isBlank(patientHisRespDTO.getUserId())){
            throw new ServiceException("用户id参数不能为空");
        }
        if (StringUtils.isBlank(patientHisRespDTO.getUserAccount())){
            throw new ServiceException("用户账号参数不能为空");
        }

        Result result = Result.ok();
        // 解密参数值
        patientHisRespDTO.setUserId(RsaUtils.decrypt(patientHisRespDTO.getUserId(),
                RsaUtils.getPrivateKey(RsaUtils.getRsaPrivateKey())));
        patientHisRespDTO.setUserAccount(RsaUtils.decrypt(patientHisRespDTO.getUserAccount(),
                RsaUtils.getPrivateKey(RsaUtils.getRsaPrivateKey())));

        // 校验自动登录的用户
        BaseUser baseUser = cUserService.checkRxAutoLogin(patientHisRespDTO);
        // 生成令牌
        String token = loginService.createToken(baseUser);
        result.setData(token);
        return result;
    }

    /**
     * 获取登录用户信息
     */
    @GetMapping("/getInfo")
    public Map<String, Object> getInfo() {
        Map<String, Object> result = Maps.newHashMap();

        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        if (Objects.isNull(loginUser) || Objects.isNull(loginUser.getId()) || loginUser.getId() < 0){
            return result;
        }

        // 查询用户信息
        CUserVO userVO = cUserService.userByCondition(new UserQueryVO() {{
            setUserId(loginUser.getId());
        }});
        // 角色集合
        Set<String> roles = cUserService.getRolePermission(userVO);
        // 权限集合
        Set<String> permissions = cUserService.getMenuPermission(userVO);

        result.put("user", userVO);
        result.put("roles", roles);
        result.put("permissions", permissions);

        return result;
    }

    /**
     * 获取路由信息
     */
    @GetMapping("/getRouters")
    public List<RouterVO> getRouters() {
        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        if (Objects.isNull(loginUser) || Objects.isNull(loginUser.getId()) || loginUser.getId() < 0){
            return Collections.emptyList();
        }

        // 查询用户信息
        CUser user = cUserService.getById(loginUser.getId());
        CUserVO userVO = new CUserVO();
        if (Objects.nonNull(user)){
            BeanUtils.copyProperties(user, userVO);
        }
        // 获取路由信息
        return cUserService.getRoutersByUser(userVO);
    }

    /**
     * 获取图形验证码
     */
    @GetMapping("/getCaptchaCode")
    public CaptchaCodeVO getCaptchaCode() {
        return loginService.getCaptchaCode();
    }

    /**
     * 校验图形验证码
     */
    @PostMapping("/checkCaptchaCode")
    public Boolean checkCaptchaCode(@RequestBody CaptchaCodeVO captchaCodeVO) {
        return loginService.checkCaptchaCode(captchaCodeVO);
    }

}

