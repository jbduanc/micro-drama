package com.series.content.grpc;

import com.series.common.grpc.content.v1.BoolResult;
import com.series.common.grpc.content.v1.ContentMicroDramasServiceGrpc;
import com.series.common.grpc.content.v1.MicroDramaDetail;
import com.series.common.grpc.content.v1.MicroDramaDetailResponse;
import com.series.common.grpc.content.v1.MicroDramaIdRequest;
import com.series.common.grpc.content.v1.MicroDramaItem;
import com.series.common.grpc.content.v1.MicroDramaPageListRequest;
import com.series.common.grpc.content.v1.MicroDramaPageListResponse;
import com.series.common.grpc.content.v1.MicroDramaSaveOrUpdateRequest;
import com.series.content.dto.business.DramaEpisodeDTO;
import com.series.content.dto.business.MicroDramaDTO;
import com.series.content.entity.business.MicroDramas;
import com.series.content.service.business.IMicroDramasService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class ContentMicroDramasGrpcService extends ContentMicroDramasServiceGrpc.ContentMicroDramasServiceImplBase {

    @Resource
    private IMicroDramasService microDramasService;

    private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static String grpcId(String id) {
        return id == null ? "" : id;
    }

    @Override
    public void pageList(MicroDramaPageListRequest request, StreamObserver<MicroDramaPageListResponse> responseObserver) {
        MicroDramaDTO q = new MicroDramaDTO();
        q.setPage(request.getPage());
        q.setSize(request.getSize());

        List<MicroDramas> list = microDramasService.list(q);

        List<MicroDramaItem> items = list.stream().map(this::toItem).collect(Collectors.toList());
        MicroDramaPageListResponse resp = MicroDramaPageListResponse.newBuilder()
                .setTotal(items.size())
                .addAllData(items)
                .build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void saveOrUpdate(MicroDramaSaveOrUpdateRequest request, StreamObserver<BoolResult> responseObserver) {
        MicroDramaDTO dto = fromDetail(request.getDrama());
        boolean ok = microDramasService.saveOrUpdateMicroDrama(dto);
        BoolResult resp = BoolResult.newBuilder()
                .setOk(ok)
                .setMsg(ok ? "OK" : "FAILED")
                .build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void detail(MicroDramaIdRequest request, StreamObserver<MicroDramaDetailResponse> responseObserver) {
        MicroDramaDTO dto = microDramasService.getMicroDramaDetailById(request.getDramaId());
        MicroDramaDetailResponse resp = MicroDramaDetailResponse.newBuilder()
                .setDrama(dto == null ? MicroDramaDetail.getDefaultInstance() : toDetail(dto))
                .build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void delete(MicroDramaIdRequest request, StreamObserver<BoolResult> responseObserver) {
        boolean ok = microDramasService.removeMicroDrama(request.getDramaId());
        BoolResult resp = BoolResult.newBuilder()
                .setOk(ok)
                .setMsg(ok ? "OK" : "FAILED")
                .build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    private MicroDramaItem toItem(MicroDramas d) {
        MicroDramaItem.Builder b = MicroDramaItem.newBuilder();
        b.setDramaId(grpcId(d.getId()));
        b.setTitle(d.getTitle() == null ? "" : d.getTitle());
        b.setCoverUrl(d.getCoverUrl() == null ? "" : d.getCoverUrl());
        b.setDescription(d.getDescription() == null ? "" : d.getDescription());
        b.setTotalEpisodes(d.getTotalEpisodes() == null ? 0 : d.getTotalEpisodes());
        b.setPrice(d.getPrice() == null ? "" : d.getPrice().toPlainString());
        b.setStatus(d.getStatus() == null ? 0 : d.getStatus());
        b.setSort(d.getSort() == null ? 0 : d.getSort());
        b.setCreateTime(d.getCreateTime() == null ? "" : DF.format(d.getCreateTime()));
        b.setUpdateTime(d.getUpdateTime() == null ? "" : DF.format(d.getUpdateTime()));
        return b.build();
    }

    private MicroDramaDetail toDetail(MicroDramaDTO dto) {
        MicroDramaItem base = MicroDramaItem.newBuilder()
                .setDramaId(grpcId(dto.getId()))
                .setTitle(dto.getTitle() == null ? "" : dto.getTitle())
                .setCoverUrl(dto.getCoverUrl() == null ? "" : dto.getCoverUrl())
                .setDescription(dto.getDescription() == null ? "" : dto.getDescription())
                .setTotalEpisodes(dto.getTotalEpisodes() == null ? 0 : dto.getTotalEpisodes())
                .setPrice(dto.getPrice() == null ? "" : dto.getPrice().toPlainString())
                .setStatus(dto.getStatus() == null ? 0 : dto.getStatus())
                .setSort(dto.getSort() == null ? 0 : dto.getSort())
                .build();

        MicroDramaDetail.Builder b = MicroDramaDetail.newBuilder().setBase(base);
        if (dto.getEpisodes() != null) {
            b.addAllEpisodes(dto.getEpisodes().stream().map(ep -> com.series.common.grpc.content.v1.Episode.newBuilder()
                    .setEpisodeId(grpcId(ep.getId()))
                    .setEpisodeNum(ep.getEpisodeNum() == null ? 0 : ep.getEpisodeNum())
                    .setTitle(ep.getTitle() == null ? "" : ep.getTitle())
                    .setVideoAssetId(ep.getVideoAssetId() == null ? "" : ep.getVideoAssetId())
                    .setPrice(ep.getPrice() == null ? "" : ep.getPrice().toPlainString())
                    .setDuration(ep.getDuration() == null ? 0 : ep.getDuration())
                    .build()
            ).collect(Collectors.toList()));
        }
        return b.build();
    }

    private MicroDramaDTO fromDetail(MicroDramaDetail detail) {
        MicroDramaDTO dto = new MicroDramaDTO();
        if (detail == null || !detail.hasBase()) {
            return dto;
        }
        MicroDramaItem base = detail.getBase();
        dto.setId(base.getDramaId().isEmpty() ? null : base.getDramaId());
        dto.setTitle(base.getTitle());
        dto.setCoverUrl(base.getCoverUrl());
        dto.setDescription(base.getDescription());
        dto.setTotalEpisodes(base.getTotalEpisodes());
        dto.setStatus(base.getStatus());
        dto.setSort(base.getSort());
        if (base.getPrice() != null && !base.getPrice().isEmpty()) {
            try {
                dto.setPrice(new BigDecimal(base.getPrice()));
            } catch (NumberFormatException ignored) {
            }
        }

        if (detail.getEpisodesCount() > 0) {
            List<DramaEpisodeDTO> eps = detail.getEpisodesList().stream().map(ep -> {
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
                return e;
            }).collect(Collectors.toList());
            dto.setEpisodes(eps);
        }
        return dto;
    }
}
