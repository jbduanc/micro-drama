package com.series.admin.service.business;

import com.baomidou.mybatisplus.extension.service.IService;
import com.series.admin.dto.business.MicroDramaDTO;
import com.series.admin.entity.business.MicroDramas;

import java.util.List;

public interface IMicroDramasService extends IService<MicroDramas> {

    List<MicroDramas> list(MicroDramaDTO queryVO);

    boolean saveOrUpdateMicroDrama(MicroDramaDTO dto);

    MicroDramaDTO getMicroDramaDetailById(String dramaId);

    boolean removeMicroDrama(String dramaId);
}
