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
 * 短剧主表（内容域），表 drama / episode
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
    public Result<String> saveOrUpdate(@RequestBody MicroDramaDTO dto) {
        String dramaId = microDramasService.saveOrUpdateMicroDrama(dto);
        return dramaId != null ? Result.ok(dramaId) : Result.error("保存失败");
    }

    /** 预分配新剧集 ID（新增剧集弹框打开时调用，便于 OSS 直传路径与落库 id 一致） */
    @GetMapping("/episodes/new-id")
    public Result<String> newEpisodeId() {
        return Result.ok(microDramasService.generateNewEpisodeId());
    }

    /** 删除单集（级联删除关联视频资产） */
    @PostMapping("/episodes/delete/{episodeId}")
    public Result<Boolean> deleteEpisode(@PathVariable String episodeId) {
        return microDramasService.removeEpisode(episodeId)
                ? Result.ok(true)
                : Result.error("剧集不存在或删除失败");
    }

    @GetMapping("/detail/{dramaId}")
    public Result<MicroDramaDTO> getDetail(@PathVariable String dramaId) {
        MicroDramaDTO detail = microDramasService.getMicroDramaDetailById(dramaId);
        return detail != null ? Result.ok(detail) : Result.error("短剧不存在");
    }

    @PostMapping("/delete/{dramaId}")
    public Result<Boolean> delete(@PathVariable String dramaId) {
        return Result.ok(microDramasService.removeMicroDrama(dramaId));
    }
}
