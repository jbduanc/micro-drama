package com.series.content.service.business;

import com.baomidou.mybatisplus.extension.service.IService;
import com.series.content.dto.business.MicroDramaDTO;
import com.series.content.entity.business.MicroDramas;

import java.util.List;

public interface IMicroDramasService extends IService<MicroDramas> {
    List<MicroDramas> list(MicroDramaDTO queryVO);

    boolean saveOrUpdateMicroDrama(MicroDramaDTO dto);

    MicroDramaDTO getMicroDramaDetailById(String dramaId);

    boolean removeMicroDrama(String dramaId);
}
