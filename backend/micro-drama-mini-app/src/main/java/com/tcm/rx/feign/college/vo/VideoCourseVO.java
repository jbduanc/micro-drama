package com.tcm.rx.feign.college.vo;

import com.tcm.common.vo.PageVO;
import lombok.Data;

@Data
public class VideoCourseVO extends PageVO {
    private Long id;

    /**
     * 课程名称
     */
    private String name;

    /**
     * 课程类型
     */
    private String type;

    /**
     * 上传视频
     */
    private String videoUrl;


    private Integer status;


}
