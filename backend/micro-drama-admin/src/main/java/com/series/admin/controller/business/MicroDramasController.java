package com.series.admin.controller.business;

import com.github.pagehelper.Page;
import com.series.admin.dto.business.MicroDramaDTO;
import com.series.admin.entity.business.MicroDramas;
import com.series.admin.service.business.IMicroDramasService;
import com.series.common.entity.Result;
import com.series.common.entity.TablePageInfo;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;
import static com.github.pagehelper.page.PageMethod.startPage;

/**
 * <p>
 * 短剧主表 前端控制器
 * </p>
 *
 * @author djbo
 * @since 2026-04-15
 */
@RestController
@RequestMapping("/microDramas")
public class MicroDramasController {

    @Resource
    private IMicroDramasService microDramasService;

    /**
     * 分页查询短剧列表
     */
    @PostMapping("/pageList")
    public TablePageInfo<MicroDramas> pageList(@RequestBody MicroDramaDTO queryVO) {
        Page page = startPage(queryVO.getPage(), queryVO.getSize());
        List<MicroDramas> resultList = microDramasService.list(queryVO);
        return new TablePageInfo<>(resultList, Math.toIntExact(page.getTotal()));
    }

    /**
     * 新增/编辑短剧（含剧集信息）
     */
    @PostMapping("/saveOrUpdate")
    public Result<Boolean> saveOrUpdate(@RequestBody MicroDramaDTO dto) {
        return Result.ok(microDramasService.saveOrUpdateMicroDrama(dto));
    }

    /**
     * 根据ID查询短剧详情（含剧集列表）
     */
    @GetMapping("/detail/{dramaId}")
    public Result<MicroDramaDTO> getDetail(@PathVariable Integer dramaId) {
        MicroDramaDTO detail = microDramasService.getMicroDramaDetailById(dramaId);
        return detail != null ? Result.ok(detail) : Result.error("短剧不存在");
    }

    /**
     * 根据ID删除短剧（级联删除所有剧集）
     */
    @PostMapping("/delete/{dramaId}")
    public Result<Boolean> delete(@PathVariable Integer dramaId) {
        return Result.ok(microDramasService.removeMicroDrama(dramaId));
    }
}