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
import org.springframework.beans.BeanUtils;

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

    @Override
    public void pageList(MicroDramaPageListRequest request, StreamObserver<MicroDramaPageListResponse> responseObserver) {
        MicroDramaDTO q = new MicroDramaDTO();
        q.setPage(request.getPage());
        q.setSize(request.getSize());

        // 这里复用原 service.list（目前只是按创建时间倒序），分页由 PageHelper 在 HTTP Controller 里触发；
        // gRPC 版先做简化：返回全量 list + total。后续可在 service 层统一分页实现。
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
        MicroDramaDTO dto = microDramasService.getMicroDramaDetailById((int) request.getDramaId());
        MicroDramaDetailResponse resp = MicroDramaDetailResponse.newBuilder()
                .setDrama(dto == null ? MicroDramaDetail.getDefaultInstance() : toDetail(dto))
                .build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void delete(MicroDramaIdRequest request, StreamObserver<BoolResult> responseObserver) {
        boolean ok = microDramasService.removeMicroDrama((int) request.getDramaId());
        BoolResult resp = BoolResult.newBuilder()
                .setOk(ok)
                .setMsg(ok ? "OK" : "FAILED")
                .build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    private MicroDramaItem toItem(MicroDramas d) {
        MicroDramaItem.Builder b = MicroDramaItem.newBuilder();
        b.setDramaId(d.getDramaId() == null ? 0L : d.getDramaId());
        b.setTitle(d.getTitle() == null ? "" : d.getTitle());
        b.setCoverUrl(d.getCoverUrl() == null ? "" : d.getCoverUrl());
        b.setDescription(d.getDescription() == null ? "" : d.getDescription());
        b.setTotalEpisodes(d.getTotalEpisodes() == null ? 0 : d.getTotalEpisodes());
        b.setSingleDramaPrice(d.getSingleDramaPrice() == null ? "" : d.getSingleDramaPrice().toPlainString());
        b.setStatus(d.getStatus() == null ? 0 : d.getStatus());
        b.setSort(d.getSort() == null ? 0 : d.getSort());
        b.setCreateTime(d.getCreateTime() == null ? "" : DF.format(d.getCreateTime()));
        b.setUpdateTime(d.getUpdateTime() == null ? "" : DF.format(d.getUpdateTime()));
        return b.build();
    }

    private MicroDramaDetail toDetail(MicroDramaDTO dto) {
        MicroDramaItem base = MicroDramaItem.newBuilder()
                .setDramaId(dto.getDramaId() == null ? 0L : dto.getDramaId())
                .setTitle(dto.getTitle() == null ? "" : dto.getTitle())
                .setCoverUrl(dto.getCoverUrl() == null ? "" : dto.getCoverUrl())
                .setDescription(dto.getDescription() == null ? "" : dto.getDescription())
                .setTotalEpisodes(dto.getTotalEpisodes() == null ? 0 : dto.getTotalEpisodes())
                .setSingleDramaPrice(dto.getSingleDramaPrice() == null ? "" : dto.getSingleDramaPrice().toPlainString())
                .setStatus(dto.getStatus() == null ? 0 : dto.getStatus())
                .setSort(dto.getSort() == null ? 0 : dto.getSort())
                .build();

        MicroDramaDetail.Builder b = MicroDramaDetail.newBuilder().setBase(base);
        if (dto.getEpisodes() != null) {
            b.addAllEpisodes(dto.getEpisodes().stream().map(ep -> com.series.common.grpc.content.v1.Episode.newBuilder()
                    .setEpisodeId(ep.getEpisodeId() == null ? 0 : ep.getEpisodeId())
                    .setEpisodeNum(ep.getEpisodeNum() == null ? 0 : ep.getEpisodeNum())
                    .setEpisodeTitle(ep.getEpisodeTitle() == null ? "" : ep.getEpisodeTitle())
                    .setVideoUrl(ep.getVideoUrl() == null ? "" : ep.getVideoUrl())
                    .setSingleEpisodePrice(ep.getSingleEpisodePrice() == null ? "" : ep.getSingleEpisodePrice().toPlainString())
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
        dto.setDramaId(base.getDramaId() == 0 ? null : base.getDramaId());
        dto.setTitle(base.getTitle());
        dto.setCoverUrl(base.getCoverUrl());
        dto.setDescription(base.getDescription());
        dto.setTotalEpisodes(base.getTotalEpisodes());
        dto.setStatus(base.getStatus());
        dto.setSort(base.getSort());
        if (base.getSingleDramaPrice() != null && !base.getSingleDramaPrice().isEmpty()) {
            try {
                dto.setSingleDramaPrice(new BigDecimal(base.getSingleDramaPrice()));
            } catch (NumberFormatException ignored) {
                // ignore
            }
        }

        if (detail.getEpisodesCount() > 0) {
            List<DramaEpisodeDTO> eps = detail.getEpisodesList().stream().map(ep -> {
                DramaEpisodeDTO e = new DramaEpisodeDTO();
                e.setEpisodeId(ep.getEpisodeId() == 0 ? null : ep.getEpisodeId());
                e.setEpisodeNum(ep.getEpisodeNum());
                e.setEpisodeTitle(ep.getEpisodeTitle());
                e.setVideoUrl(ep.getVideoUrl());
                e.setDuration(ep.getDuration());
                if (ep.getSingleEpisodePrice() != null && !ep.getSingleEpisodePrice().isEmpty()) {
                    try {
                        e.setSingleEpisodePrice(new BigDecimal(ep.getSingleEpisodePrice()));
                    } catch (NumberFormatException ignored) {
                        // ignore
                    }
                }
                return e;
            }).collect(Collectors.toList());
            dto.setEpisodes(eps);
        }
        return dto;
    }
}

