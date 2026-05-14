package com.series.admin.service.business.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.series.admin.dto.business.DramaEpisodeDTO;
import com.series.admin.dto.business.MicroDramaDTO;
import com.series.admin.entity.business.DramaEpisodes;
import com.series.admin.entity.business.MicroDramas;
import com.series.admin.mapper.business.DramaEpisodesMapper;
import com.series.admin.mapper.business.MicroDramasMapper;
import com.series.admin.service.business.IMicroDramasService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
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
        BeanUtils.copyProperties(dto, microDramas);
        if (!StringUtils.hasText(dto.getId())) {
            microDramas.setId(null);
            microDramas.setCreateTime(new Date());
        } else {
            microDramas.setUpdateTime(new Date());
        }
        boolean dramaSaved = this.saveOrUpdate(microDramas);
        if (!dramaSaved) {
            return false;
        }
        String dramaId = microDramas.getId();

        LambdaQueryWrapper<DramaEpisodes> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(DramaEpisodes::getDramaId, dramaId);
        dramaEpisodesMapper.delete(deleteWrapper);

        if (!CollectionUtils.isEmpty(dto.getEpisodes())) {
            List<DramaEpisodes> episodesList = dto.getEpisodes().stream().map(epDto -> {
                DramaEpisodes ep = new DramaEpisodes();
                BeanUtils.copyProperties(epDto, ep);
                if (!StringUtils.hasText(epDto.getId())) {
                    ep.setId(null);
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
        MicroDramaDTO dto = new MicroDramaDTO();
        MicroDramas microDramas = this.getById(dramaId);
        if (microDramas == null) {
            return null;
        }
        BeanUtils.copyProperties(microDramas, dto);

        LambdaQueryWrapper<DramaEpisodes> episodeWrapper = new LambdaQueryWrapper<>();
        episodeWrapper.eq(DramaEpisodes::getDramaId, dramaId)
                .orderByAsc(DramaEpisodes::getEpisodeNum);
        List<DramaEpisodes> episodes = dramaEpisodesMapper.selectList(episodeWrapper);

        List<DramaEpisodeDTO> episodeDTOList = episodes.stream().map(ep -> {
            DramaEpisodeDTO epDto = new DramaEpisodeDTO();
            BeanUtils.copyProperties(ep, epDto);
            return epDto;
        }).collect(Collectors.toList());
        dto.setEpisodes(episodeDTOList);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeMicroDrama(String dramaId) {
        LambdaQueryWrapper<DramaEpisodes> episodeWrapper = new LambdaQueryWrapper<>();
        episodeWrapper.eq(DramaEpisodes::getDramaId, dramaId);
        dramaEpisodesMapper.delete(episodeWrapper);
        return this.removeById(dramaId);
    }
}
