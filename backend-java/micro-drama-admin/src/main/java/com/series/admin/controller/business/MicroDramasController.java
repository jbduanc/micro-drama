package com.series.admin.controller.business;

import com.series.admin.grpc.ContentMicroDramasGrpcClient;
import com.series.common.entity.Result;
import com.series.common.entity.TablePageInfo;
import lombok.var;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.*;

import com.series.admin.dto.business.DramaEpisodeDTO;
import com.series.admin.dto.business.MicroDramaDTO;
import com.series.admin.entity.business.MicroDramas;

import com.series.common.grpc.content.v1.MicroDramaIdRequest;
import com.series.common.grpc.content.v1.MicroDramaPageListRequest;
import com.series.common.grpc.content.v1.MicroDramaSaveOrUpdateRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private static String grpcId(String id) {
        return id == null ? "" : id;
    }

    @PostMapping("/pageList")
    public TablePageInfo<MicroDramas> pageList(@RequestBody MicroDramaDTO queryVO) {
        MicroDramaPageListRequest req = MicroDramaPageListRequest.newBuilder()
                .setPage(queryVO.getPage() == null ? 1 : queryVO.getPage())
                .setSize(queryVO.getSize() == null ? 10 : queryVO.getSize())
                .build();
        var resp = contentGrpc().stub().pageList(req);

        TablePageInfo<MicroDramas> out = new TablePageInfo<>();
        out.setTotal(resp.getTotal());
        out.setData(resp.getDataList().stream().map(item -> {
            MicroDramas d = new MicroDramas();
            d.setId(item.getDramaId().isEmpty() ? null : item.getDramaId());
            d.setTitle(item.getTitle());
            d.setCoverUrl(item.getCoverUrl());
            d.setDescription(item.getDescription());
            d.setTotalEpisodes(item.getTotalEpisodes());
            d.setStatus(item.getStatus());
            d.setSort(item.getSort());
            if (item.getPrice() != null && !item.getPrice().isEmpty()) {
                try {
                    d.setPrice(new BigDecimal(item.getPrice()));
                } catch (NumberFormatException ignored) {
                }
            }
            return d;
        }).collect(Collectors.toList()));
        return out;
    }

    @PostMapping("/saveOrUpdate")
    public Result<Boolean> saveOrUpdate(@RequestBody MicroDramaDTO dto) {
        var base = com.series.common.grpc.content.v1.MicroDramaItem.newBuilder()
                .setDramaId(grpcId(dto.getId()))
                .setTitle(dto.getTitle() == null ? "" : dto.getTitle())
                .setCoverUrl(dto.getCoverUrl() == null ? "" : dto.getCoverUrl())
                .setDescription(dto.getDescription() == null ? "" : dto.getDescription())
                .setTotalEpisodes(dto.getTotalEpisodes() == null ? 0 : dto.getTotalEpisodes())
                .setPrice(dto.getPrice() == null ? "" : dto.getPrice().toPlainString())
                .setStatus(dto.getStatus() == null ? 0 : dto.getStatus())
                .setSort(dto.getSort() == null ? 0 : dto.getSort())
                .build();
        com.series.common.grpc.content.v1.MicroDramaDetail.Builder detailBuilder =
                com.series.common.grpc.content.v1.MicroDramaDetail.newBuilder().setBase(base);
        if (dto.getEpisodes() != null) {
            for (DramaEpisodeDTO ep : dto.getEpisodes()) {
                detailBuilder.addEpisodes(com.series.common.grpc.content.v1.Episode.newBuilder()
                        .setEpisodeId(grpcId(ep.getId()))
                        .setEpisodeNum(ep.getEpisodeNum() == null ? 0 : ep.getEpisodeNum())
                        .setTitle(ep.getTitle() == null ? "" : ep.getTitle())
                        .setVideoAssetId(ep.getVideoAssetId() == null ? "" : ep.getVideoAssetId())
                        .setPrice(ep.getPrice() == null ? "" : ep.getPrice().toPlainString())
                        .setDuration(ep.getDuration() == null ? 0 : ep.getDuration())
                        .build());
            }
        }
        var detail = detailBuilder.build();
        MicroDramaSaveOrUpdateRequest req = MicroDramaSaveOrUpdateRequest.newBuilder().setDrama(detail).build();
        var resp = contentGrpc().stub().saveOrUpdate(req);
        return Result.ok(resp.getOk());
    }

    @GetMapping("/detail/{dramaId}")
    public Result<MicroDramaDTO> getDetail(@PathVariable String dramaId) {
        var resp = contentGrpc().stub().detail(MicroDramaIdRequest.newBuilder().setDramaId(grpcId(dramaId)).build());
        if (!resp.hasDrama() || !resp.getDrama().hasBase()) {
            return Result.error("短剧不存在");
        }
        var drama = resp.getDrama();
        var base = drama.getBase();
        MicroDramaDTO out = new MicroDramaDTO();
        out.setId(base.getDramaId().isEmpty() ? null : base.getDramaId());
        out.setTitle(base.getTitle());
        out.setCoverUrl(base.getCoverUrl());
        out.setDescription(base.getDescription());
        out.setTotalEpisodes(base.getTotalEpisodes());
        out.setStatus(base.getStatus());
        out.setSort(base.getSort());
        if (base.getPrice() != null && !base.getPrice().isEmpty()) {
            try {
                out.setPrice(new BigDecimal(base.getPrice()));
            } catch (NumberFormatException ignored) {
            }
        }
        if (drama.getEpisodesCount() > 0) {
            List<DramaEpisodeDTO> eps = new ArrayList<>();
            drama.getEpisodesList().forEach(ep -> {
                DramaEpisodeDTO e = new DramaEpisodeDTO();
                e.setId(ep.getEpisodeId().isEmpty() ? null : ep.getEpisodeId());
                e.setEpisodeNum(ep.getEpisodeNum());
                e.setTitle(ep.getTitle());
                e.setVideoAssetId(ep.getVideoAssetId());
                e.setDuration(ep.getDuration());
                if (ep.getPrice() != null && !ep.getPrice().isEmpty()) {
                    try {
                        e.setPrice(new BigDecimal(ep.getPrice()));
                    } catch (NumberFormatException ignored) {
                    }
                }
                eps.add(e);
            });
            out.setEpisodes(eps);
        }
        return Result.ok(out);
    }

    @PostMapping("/delete/{dramaId}")
    public Result<Boolean> delete(@PathVariable String dramaId) {
        var resp = contentGrpc().stub().delete(MicroDramaIdRequest.newBuilder().setDramaId(grpcId(dramaId)).build());
        return Result.ok(resp.getOk());
    }
}
