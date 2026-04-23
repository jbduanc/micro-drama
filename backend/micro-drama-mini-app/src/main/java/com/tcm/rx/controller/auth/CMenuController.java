package com.tcm.rx.controller.auth;

import com.github.pagehelper.Page;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.rx.entity.auth.CMenu;
import com.tcm.rx.service.auth.ICMenuService;
import com.tcm.rx.vo.auth.request.MenuQueryVO;
import com.tcm.rx.vo.auth.response.CMenuVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static com.github.pagehelper.page.PageMethod.startPage;

/**
 * <p>
 * 诊疗开方系统--菜单表 前端控制器
 * </p>
 *
 * @author xph
 * @since 2025-07-08
 */
@RestController
@RequestMapping("/cMenu")
public class CMenuController {

    @Resource
    private ICMenuService cMenuService;

    /**
     * 查询菜单的数据（分页查询）
     */
    @PostMapping("/menuPageList")
    public TablePageInfo<CMenuVO> menuPageList(@RequestBody MenuQueryVO queryVO) {
        Page page = startPage(queryVO.getPage(),queryVO.getSize());
        List<CMenuVO> resultList = cMenuService.menuList(queryVO);
        return new TablePageInfo(resultList, Math.toIntExact(page.getTotal()));
    }

    /**
     * 根据id查询菜单的数据
     */
    @GetMapping(value = "/{id}")
    public CMenuVO menuById(@PathVariable("id") Long id){
        CMenuVO vo = new CMenuVO();
        CMenu menu = cMenuService.getById(id);
        if(Objects.nonNull(menu)){
            BeanUtils.copyProperties(menu,vo);
        }
        return vo;
    }

    /**
     * 新增菜单的数据
     */
    @PostMapping
    public Long insertMenu(@RequestBody CMenuVO menuVO){
        return cMenuService.insertMenu(menuVO);
    }

    /**
     * 更新菜单的数据
     */
    @PostMapping(value = "/update")
    public Long updateMenu(@RequestBody CMenuVO menuVO){
        return cMenuService.updateMenu(menuVO);
    }

    /**
     * 删除菜单的数据
     */
    @GetMapping(value = "/delete/{id}")
    public void deleteMenu(@PathVariable("id") Long id) {
        cMenuService.deleteMenu(id);
    }

}

