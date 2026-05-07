package com.series.admin.controller.business;

import com.series.admin.grpc.ContentMicroDramasGrpcClient;
import com.series.common.entity.Result;
import com.series.common.entity.TablePageInfo;
import lombok.var;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.*;

import com.series.admin.dto.business.MicroDramaDTO;
import com.series.admin.entity.business.MicroDramas;

import com.series.common.grpc.content.v1.MicroDramaIdRequest;
import com.series.common.grpc.content.v1.MicroDramaPageListRequest;
import com.series.common.grpc.content.v1.MicroDramaSaveOrUpdateRequest;

import java.util.stream.Collectors;

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

    private final ObjectProvider<ContentMicroDramasGrpcClient> contentGrpcProvider;

    public MicroDramasController(ObjectProvider<ContentMicroDramasGrpcClient> contentGrpcProvider) {
        this.contentGrpcProvider = contentGrpcProvider;
    }

    private ContentMicroDramasGrpcClient contentGrpc() {
        ContentMicroDramasGrpcClient client = contentGrpcProvider.getIfAvailable();
        if (client == null) {
            throw new IllegalStateException("content gRPC client 未启用：请配置 grpc.client.content.enabled=true");
        }
        return client;
    }

    /**
     * 分页查询短剧列表
     */
    @PostMapping("/pageList")
    public TablePageInfo<MicroDramas> pageList(@RequestBody MicroDramaDTO queryVO) {
        MicroDramaPageListRequest req = MicroDramaPageListRequest.newBuilder()
                .setPage(queryVO.getPage() == null ? 1 : queryVO.getPage())
                .setSize(queryVO.getSize() == null ? 10 : queryVO.getSize())
                .build();
        var resp = contentGrpc().stub().pageList(req);

        // 兼容旧返回结构：TablePageInfo<MicroDramas>
        TablePageInfo<MicroDramas> out = new TablePageInfo<>();
        out.setTotal(resp.getTotal());
        out.setData(resp.getDataList().stream().map(item -> {
            MicroDramas d = new MicroDramas();
            d.setDramaId(item.getDramaId());
            d.setTitle(item.getTitle());
            d.setCoverUrl(item.getCoverUrl());
            d.setDescription(item.getDescription());
            d.setTotalEpisodes(item.getTotalEpisodes());
            d.setStatus(item.getStatus());
            d.setSort(item.getSort());
            return d;
        }).collect(Collectors.toList()));
        return out;
    }

    /**
     * 新增/编辑短剧（含剧集信息）
     */
    @PostMapping("/saveOrUpdate")
    public Result<Boolean> saveOrUpdate(@RequestBody MicroDramaDTO dto) {
        // 简化：先只传 base（episodes 后续补齐映射）
        var base = com.series.common.grpc.content.v1.MicroDramaItem.newBuilder()
                .setDramaId(dto.getDramaId() == null ? 0L : dto.getDramaId())
                .setTitle(dto.getTitle() == null ? "" : dto.getTitle())
                .setCoverUrl(dto.getCoverUrl() == null ? "" : dto.getCoverUrl())
                .setDescription(dto.getDescription() == null ? "" : dto.getDescription())
                .setTotalEpisodes(dto.getTotalEpisodes() == null ? 0 : dto.getTotalEpisodes())
                .setStatus(dto.getStatus() == null ? 0 : dto.getStatus())
                .setSort(dto.getSort() == null ? 0 : dto.getSort())
                .build();
        var detail = com.series.common.grpc.content.v1.MicroDramaDetail.newBuilder()
                .setBase(base)
                .build();
        MicroDramaSaveOrUpdateRequest req = MicroDramaSaveOrUpdateRequest.newBuilder().setDrama(detail).build();
        var resp = contentGrpc().stub().saveOrUpdate(req);
        return Result.ok(resp.getOk());
    }

    /**
     * 根据ID查询短剧详情（含剧集列表）
     */
    @GetMapping("/detail/{dramaId}")
    public Result<MicroDramaDTO> getDetail(@PathVariable Integer dramaId) {
        var resp = contentGrpc().stub().detail(MicroDramaIdRequest.newBuilder().setDramaId(dramaId).build());
        if (!resp.hasDrama() || !resp.getDrama().hasBase()) {
            return Result.error("短剧不存在");
        }
        var base = resp.getDrama().getBase();
        MicroDramaDTO out = new MicroDramaDTO();
        out.setDramaId(base.getDramaId());
        out.setTitle(base.getTitle());
        out.setCoverUrl(base.getCoverUrl());
        out.setDescription(base.getDescription());
        out.setTotalEpisodes(base.getTotalEpisodes());
        out.setStatus(base.getStatus());
        out.setSort(base.getSort());
        // episodes 映射后续补齐
        return Result.ok(out);
    }

    /**
     * 根据ID删除短剧（级联删除所有剧集）
     */
    @PostMapping("/delete/{dramaId}")
    public Result<Boolean> delete(@PathVariable Integer dramaId) {
        var resp = contentGrpc().stub().delete(MicroDramaIdRequest.newBuilder().setDramaId(dramaId).build());
        return Result.ok(resp.getOk());
    }
}