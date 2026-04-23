package com.series.admin.service.business;

import com.baomidou.mybatisplus.extension.service.IService;
import com.series.admin.dto.business.MicroDramaDTO;
import com.series.admin.entity.business.MicroDramas;
import java.util.List;

public interface IMicroDramasService extends IService<MicroDramas> {

    /**
     * 分页条件查询短剧列表
     */
    List<MicroDramas> list(MicroDramaDTO queryVO);

    /**
     * 保存或更新短剧（同时处理关联剧集）
     */
    boolean saveOrUpdateMicroDrama(MicroDramaDTO dto);

    /**
     * 根据ID查询短剧详情（包含剧集列表）
     */
    MicroDramaDTO getMicroDramaDetailById(Integer dramaId);

    /**
     * 删除短剧（同时删除所有关联剧集）
     */
    boolean removeMicroDrama(Integer dramaId);
}