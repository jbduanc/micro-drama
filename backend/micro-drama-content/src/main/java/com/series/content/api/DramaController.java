package com.series.content.api;

import com.series.common.entity.Result;
import com.series.content.api.dto.DramaCreateRequest;
import com.series.content.api.dto.DramaListItem;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/content/drama")
public class DramaController {

    @PostMapping
    public Result<DramaListItem> create(@RequestBody DramaCreateRequest req) {
        // 占位：后续落库 drama/episode 等实体
        DramaListItem item = new DramaListItem();
        item.setDramaId(UUID.randomUUID().toString());
        item.setTitle(req.getTitle());
        item.setCoverUrl(req.getCoverUrl());
        return Result.ok(item);
    }

    @GetMapping("/list")
    public Result<List<DramaListItem>> list() {
        // 占位：后续分页 + 条件查询
        return Result.ok(new ArrayList<>());
    }
}

