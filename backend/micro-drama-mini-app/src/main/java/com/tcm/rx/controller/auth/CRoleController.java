package com.tcm.rx.controller.auth;

import com.github.pagehelper.Page;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.rx.service.auth.ICRoleService;
import com.tcm.rx.vo.auth.request.RoleQueryVO;
import com.tcm.rx.vo.auth.response.CRoleVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.github.pagehelper.page.PageMethod.startPage;

/**
 * <p>
 * 诊疗开方系统--角色表 前端控制器
 * </p>
 *
 * @author xph
 * @since 2025-07-08
 */
@RestController
@RequestMapping("/cRole")
public class CRoleController {

    @Resource
    private ICRoleService cRoleService;

    /**
     * 查询角色信息的数据（分页查询）
     */
    @PostMapping("/rolePageList")
    public TablePageInfo<CRoleVO> rolePageList(@RequestBody RoleQueryVO queryVO) {
        Page page = startPage(queryVO.getPage(),queryVO.getSize());
        List<CRoleVO> resultList = cRoleService.roleList(queryVO);
        return new TablePageInfo(resultList, Math.toIntExact(page.getTotal()));
    }

    /**
     * 根据id查询角色信息的数据
     */
    @GetMapping(value = "/{id}")
    public CRoleVO roleById(@PathVariable("id") Long id){
        return cRoleService.roleById(new RoleQueryVO(){{
            setRoleId(id);
        }});
    }

    /**
     * 新增角色信息的数据
     */
    @PostMapping
    public Long insertRole(@RequestBody CRoleVO roleVO){
        return cRoleService.insertRole(roleVO);
    }

    /**
     * 更新角色信息的数据
     */
    @PostMapping(value = "/update")
    public Long updateRole(@RequestBody CRoleVO roleVO){
        return cRoleService.updateRole(roleVO);
    }

    /**
     * 批量删除角色信息的数据
     */
    @PostMapping(value = "/batchDeleteRole")
    public void batchDeleteRole(@RequestBody RoleQueryVO queryVO) {
        cRoleService.batchDeleteRole(queryVO);
    }

    /**
     * 批量更新角色信息的状态
     */
    @PostMapping(value = "/batchUpdateStatus")
    public Boolean batchUpdateStatus(@RequestBody RoleQueryVO queryVO){
        return cRoleService.batchUpdateStatus(queryVO);
    }

    /**
     * 角色管理--导出数据
     */
    @PostMapping("/exportRole")
    public void export(HttpServletResponse response, @RequestBody RoleQueryVO queryVO) throws IOException {
        cRoleService.export(response, queryVO);
    }

}

