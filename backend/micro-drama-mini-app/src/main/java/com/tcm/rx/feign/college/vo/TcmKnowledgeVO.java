package com.tcm.rx.feign.college.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.tcm.common.vo.PageVO;
import lombok.Data;

@Data
public class TcmKnowledgeVO extends PageVO {
    private Long id;

    private String herbName;

    private String alias;

    /**
     * 饮片简介
     */
    private String herbIntroduce;

    /**
     * 炮制
     */
    private String process;

    /**
     * 性状
     */
    private String property;

    /**
     * 性味与归经
     */
    private String tasteTropism;

    /**
     * 功能与主治
     */
    private String therapyFunction;

    /**
     * 用法与用量
     */
    @TableField(value = "`usage`")
    private String usage;

    /**
     * 贮藏
     */
    private String storageCondition;

    private String errorMsg;

}
