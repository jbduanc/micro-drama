package com.series.content.service.business.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.series.common.typehandler.UuidTypeHandlerSupport;
import com.series.content.dto.business.DramaEpisodeDTO;
import com.series.content.dto.business.MicroDramaDTO;
import com.series.content.entity.business.DramaEpisodes;
import com.series.content.entity.business.MicroDramas;
import com.series.content.mapper.business.DramaEpisodesMapper;
import com.series.content.mapper.business.MicroDramasMapper;
import com.series.content.service.business.IMicroDramasService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MicroDramasServiceImpl extends ServiceImpl<MicroDramasMapper, MicroDramas> implements IMicroDramasService {

    @Resource
    private DramaEpisodesMapper dramaEpisodesMapper;

    @Override
    public List<MicroDramas> list(MicroDramaDTO queryVO) {
        return this.lambdaQuery()
                .orderByDesc(MicroDramas::getCreateTime)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateMicroDrama(MicroDramaDTO dto) {
        MicroDramas microDramas = new MicroDramas();
        BeanUtils.copyProperties(dto, microDramas, "id");
        if (!StringUtils.hasText(dto.getId())) {
            microDramas.setId(UUID.randomUUID());
            microDramas.setCreateTime(new Date());
        } else {
            microDramas.setId(UuidTypeHandlerSupport.toUuid(dto.getId()));
            microDramas.setUpdateTime(new Date());
        }
        boolean dramaSaved = this.saveOrUpdate(microDramas);
        if (!dramaSaved) {
            return false;
        }
        UUID dramaId = microDramas.getId();

        LambdaQueryWrapper<DramaEpisodes> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(DramaEpisodes::getDramaId, dramaId);
        dramaEpisodesMapper.delete(deleteWrapper);

        if (!CollectionUtils.isEmpty(dto.getEpisodes())) {
            List<DramaEpisodes> episodesList = dto.getEpisodes().stream().map(epDto -> {
                DramaEpisodes ep = new DramaEpisodes();
                BeanUtils.copyProperties(epDto, ep, "id", "videoAssetId");
                if (StringUtils.hasText(epDto.getId())) {
                    ep.setId(UuidTypeHandlerSupport.toUuid(epDto.getId()));
                } else {
                    ep.setId(UUID.randomUUID());
                }
                if (StringUtils.hasText(epDto.getVideoAssetId())) {
                    ep.setVideoAssetId(UuidTypeHandlerSupport.toUuid(epDto.getVideoAssetId()));
                }
                ep.setDramaId(dramaId);
                ep.setCreateTime(new Date());
                return ep;
            }).collect(Collectors.toList());
            for (DramaEpisodes episode : episodesList) {
                dramaEpisodesMapper.insert(episode);
            }
        }

        int episodeCount = dto.getEpisodes() == null ? 0 : dto.getEpisodes().size();
        microDramas.setTotalEpisodes(episodeCount);
        this.updateById(microDramas);

        return true;
    }

    @Override
    public MicroDramaDTO getMicroDramaDetailById(String dramaId) {
        UUID id = UuidTypeHandlerSupport.toUuid(dramaId);
        if (id == null) {
            return null;
        }
        MicroDramaDTO dto = new MicroDramaDTO();
        MicroDramas microDramas = this.getById(id);
        if (microDramas == null) {
            return null;
        }
        BeanUtils.copyProperties(microDramas, dto, "id");
        dto.setId(microDramas.getId().toString());

        LambdaQueryWrapper<DramaEpisodes> episodeWrapper = new LambdaQueryWrapper<>();
        episodeWrapper.eq(DramaEpisodes::getDramaId, id)
                .orderByAsc(DramaEpisodes::getEpisodeNum);
        List<DramaEpisodes> episodes = dramaEpisodesMapper.selectList(episodeWrapper);

        List<DramaEpisodeDTO> episodeDTOList = episodes.stream().map(ep -> {
            DramaEpisodeDTO epDto = new DramaEpisodeDTO();
            BeanUtils.copyProperties(ep, epDto, "id", "videoAssetId");
            epDto.setId(ep.getId() != null ? ep.getId().toString() : null);
            epDto.setVideoAssetId(ep.getVideoAssetId() != null ? ep.getVideoAssetId().toString() : null);
            return epDto;
        }).collect(Collectors.toList());
        dto.setEpisodes(episodeDTOList);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeMicroDrama(String dramaId) {
        UUID id = UuidTypeHandlerSupport.toUuid(dramaId);
        if (id == null) {
            return false;
        }
        LambdaQueryWrapper<DramaEpisodes> episodeWrapper = new LambdaQueryWrapper<>();
        episodeWrapper.eq(DramaEpisodes::getDramaId, id);
        dramaEpisodesMapper.delete(episodeWrapper);
        return this.removeById(id);
    }
}
