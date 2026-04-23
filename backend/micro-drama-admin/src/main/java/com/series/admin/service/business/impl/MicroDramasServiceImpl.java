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
        // 简单示例：按创建时间倒序查询，可根据实际需求添加条件
        return this.lambdaQuery()
                .orderByDesc(MicroDramas::getCreateTime)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateMicroDrama(MicroDramaDTO dto) {
        // 1. 保存或更新短剧基本信息
        MicroDramas microDramas = new MicroDramas();
        BeanUtils.copyProperties(dto, microDramas);
        if (dto.getDramaId() == null) {
            microDramas.setCreateTime(new Date());
        } else {
            microDramas.setUpdateTime(new Date());
        }
        boolean dramaSaved = this.saveOrUpdate(microDramas);
        if (!dramaSaved) {
            return false;
        }
        Long dramaId = microDramas.getDramaId();

        // 2. 处理剧集信息：先删除原有剧集，再插入新提交的剧集列表
        //    （也可采用更精细的增量更新，此处简化处理）
        LambdaQueryWrapper<DramaEpisodes> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(DramaEpisodes::getDramaId, dramaId);
        dramaEpisodesMapper.delete(deleteWrapper);

        if (!CollectionUtils.isEmpty(dto.getEpisodes())) {
            List<DramaEpisodes> episodesList = dto.getEpisodes().stream().map(epDto -> {
                DramaEpisodes ep = new DramaEpisodes();
                BeanUtils.copyProperties(epDto, ep);
                ep.setDramaId(dramaId);
                ep.setCreateTime(new Date());
                return ep;
            }).collect(Collectors.toList());
            // 批量插入剧集
            for (DramaEpisodes episode : episodesList) {
                dramaEpisodesMapper.insert(episode);
            }
        }

        // 3. 更新短剧的总集数字段（保持一致性）
        int episodeCount = dto.getEpisodes() == null ? 0 : dto.getEpisodes().size();
        microDramas.setTotalEpisodes(episodeCount);
        this.updateById(microDramas);

        return true;
    }

    @Override
    public MicroDramaDTO getMicroDramaDetailById(Integer dramaId) {
        MicroDramaDTO dto = new MicroDramaDTO();
        // 1. 查询短剧基本信息
        MicroDramas microDramas = this.getById(dramaId);
        if (microDramas == null) {
            return null;
        }
        BeanUtils.copyProperties(microDramas, dto);

        // 2. 查询关联剧集列表
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
    public boolean removeMicroDrama(Integer dramaId) {
        // 先删除关联剧集
        LambdaQueryWrapper<DramaEpisodes> episodeWrapper = new LambdaQueryWrapper<>();
        episodeWrapper.eq(DramaEpisodes::getDramaId, dramaId);
        dramaEpisodesMapper.delete(episodeWrapper);
        // 再删除短剧
        return this.removeById(dramaId);
    }
}