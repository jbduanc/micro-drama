package com.series.content.controller.business;

import com.github.pagehelper.Page;
import com.series.common.entity.Result;
import com.series.common.entity.TablePageInfo;
import com.series.content.dto.business.MicroDramaDTO;
import com.series.content.entity.business.MicroDramas;
import com.series.content.service.business.IMicroDramasService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static com.github.pagehelper.page.PageMethod.startPage;

/**
 * 短剧主表（内容域）
 *
 * 说明：为保证迁移平滑，接口路径暂时保持与 admin 一致：/microDramas/**
 */
@RestController
@RequestMapping("/microDramas")
public class MicroDramasController {

    @Resource
    private IMicroDramasService microDramasService;

    @PostMapping("/pageList")
    public TablePageInfo<MicroDramas> pageList(@RequestBody MicroDramaDTO queryVO) {
        Page page = startPage(queryVO.getPage(), queryVO.getSize());
        List<MicroDramas> resultList = microDramasService.list(queryVO);
        return new TablePageInfo<>(resultList, Math.toIntExact(page.getTotal()));
    }

    @PostMapping("/saveOrUpdate")
    public Result<Boolean> saveOrUpdate(@RequestBody MicroDramaDTO dto) {
        return Result.ok(microDramasService.saveOrUpdateMicroDrama(dto));
    }

    @GetMapping("/detail/{dramaId}")
    public Result<MicroDramaDTO> getDetail(@PathVariable Integer dramaId) {
        MicroDramaDTO detail = microDramasService.getMicroDramaDetailById(dramaId);
        return detail != null ? Result.ok(detail) : Result.error("短剧不存在");
    }

    @PostMapping("/delete/{dramaId}")
    public Result<Boolean> delete(@PathVariable Integer dramaId) {
        return Result.ok(microDramasService.removeMicroDrama(dramaId));
    }
}

