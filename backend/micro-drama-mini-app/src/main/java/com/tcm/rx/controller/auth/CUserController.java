package com.tcm.rx.controller.auth;

import com.github.pagehelper.Page;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.rx.service.auth.ICUserService;
import com.tcm.rx.vo.auth.request.UserQueryVO;
import com.tcm.rx.vo.auth.response.CUserVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.github.pagehelper.page.PageMethod.startPage;

/**
 * <p>
 * 诊疗开方系统--用户表 前端控制器
 * </p>
 *
 * @author xph
 * @since 2025-07-07
 */
@RestController
@RequestMapping("/cUser")
public class CUserController {

    @Resource
    private ICUserService cUserService;

    /**
     * 查询用户信息的数据（分页查询）
     */
    @PostMapping("/userPageList")
    public TablePageInfo<CUserVO> userPageList(@RequestBody UserQueryVO queryVO) {
        if (StringUtils.isBlank(queryVO.getUserType()) || !"admin".equals(queryVO.getUserType())) {
            queryVO.setUserType("user");
        }
        Page page = startPage(queryVO.getPage(),queryVO.getSize());
        List<CUserVO> resultList = cUserService.userList(queryVO);
        return new TablePageInfo(resultList, Math.toIntExact(page.getTotal()));
    }

    /**
     * 根据id查询用户信息的数据
     */
    @GetMapping(value = "/{id}")
    public CUserVO userById(@PathVariable("id") Long id){
        return cUserService.userById(id);
    }

    /**
     * 根据条件查询用户信息的数据
     */
    @PostMapping(value = "/userByCondition")
    public CUserVO userByCondition(@RequestBody UserQueryVO queryVO){
        return cUserService.userByCondition(queryVO);
    }

    /**
     * 新增用户信息的数据
     */
    @PostMapping
    public Long insertUser(@RequestBody CUserVO userVO) throws Exception {
        return cUserService.insertUser(userVO);
    }

    /**
     * 账号管理--批量新增用户信息的数据
     */
    @PostMapping(value = "/batchInsertUser")
    public Boolean batchInsertUser(@RequestBody List<CUserVO> userVOList){
        return cUserService.batchInsertUser(userVOList);
    }

    /**
     * 更新用户信息的数据
     */
    @PostMapping(value = "/update")
    public Long updateUser(@RequestBody CUserVO userVO){
        return cUserService.updateUser(userVO);
    }

    /**
     * 更新用户的密码
     */
    @PostMapping(value = "/updatePassword")
    public Long updatePassword(@RequestBody CUserVO userVO) throws Exception {
        return cUserService.updatePassword(userVO);
    }

    /**
     * 重置密码
     */
    @PostMapping(value = "/defaultPassword")
    public Long defaultPassword(@RequestBody CUserVO userVO) {
        return cUserService.defaultPassword(userVO);
    }

    /**
     * 更新用户关联角色数据
     */
    @PostMapping(value = "/updateUserRoles")
    public Boolean updateUserRoles(@RequestBody CUserVO userVO){
        return cUserService.updateUserRoles(userVO);
    }

    /**
     * 批量删除用户信息的数据
     */
    @PostMapping(value = "/batchDelete")
    public Boolean batchDeleteUser(@RequestBody UserQueryVO queryVO) {
        return cUserService.batchDeleteUser(queryVO);
    }

    /**
     * 批量更新用户信息的状态
     */
    @PostMapping(value = "/batchUpdateStatus")
    public Boolean batchUpdateStatus(@RequestBody UserQueryVO queryVO){
        return cUserService.batchUpdateStatus(queryVO);
    }

    /**
     * 账号管理--下载导入模版
     */
    @PostMapping(value = "/download/templateUser")
    public void downloadTemplateUser(HttpServletResponse response) throws IOException {
        cUserService.downloadTemplate(response);
    }

    /**
     * 账号管理--导入数据
     */
    @PostMapping("/importInfoUser")
    public List<CUserVO> importInfoUser(MultipartFile file) throws Exception {
        return cUserService.importInfo(file);
    }

    /**
     * 账号管理--导出数据
     */
    @PostMapping("/exportUser")
    public void export(HttpServletResponse response, @RequestBody UserQueryVO queryVO) throws IOException {
        cUserService.export(response, queryVO);
    }

}

