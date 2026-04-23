package com.series.admin.controller.sys;


import com.github.pagehelper.Page;
import com.series.admin.dto.sys.UserInfoDTO;
import com.series.admin.entity.sys.SysUser;
import com.series.admin.service.sys.ISysUserService;
import com.series.common.entity.TablePageInfo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static com.github.pagehelper.page.PageMethod.startPage;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author djbo
 * @since 2026-04-13
 */
@RestController
@RequestMapping("/sysUser")
public class SysUserController {

    @Resource
    private ISysUserService sysUserService;

    /**
     * 分页查询系统用户
     */
    @PostMapping("/pageList")
    public TablePageInfo<SysUser> pageList(@RequestBody UserInfoDTO queryVO) {
        Page page = startPage(queryVO.getPage(),queryVO.getSize());
        List<SysUser> resultList = sysUserService.list(queryVO);
        return new TablePageInfo(resultList, Math.toIntExact(page.getTotal()));
    }
}

