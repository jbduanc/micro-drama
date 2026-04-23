package com.tcm.rx.entity.auth;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * <p>
 * 诊疗开方系统--菜单表
 * </p>
 *
 * @author xph
 * @since 2025-07-08
 */
@Data
@TableName("rx_c_menu")
public class CMenu extends Model<CMenu> {

    private static final long serialVersionUID = -1911731793974068845L;

    /**
     * 菜单id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

//    /**
//     * 所属医联体的id
//     */
//    private Long medicalGroupId;
//
//    /**
//     * 所属医联体的code
//     */
//    private String medicalGroupCode;
//
//    /**
//     * 医疗机构的id
//     */
//    private Long hspId;
//
//    /**
//     * 医疗机构的code
//     */
//    private String hspCode;

    /**
     * 父菜单id
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 路由参数/菜单别名
     */
    private String query;

    /**
     * 菜单类型：M.目录，C.菜单，F.按钮
     */
    private String menuType;

    /**
     * 是否外链：0.是，1.否
     */
    private Integer isFrame;

    /**
     * 是否缓存：0.缓存，1.不缓存
     */
    private Integer isCache;

    /**
     * 显示状态：0.显示，1.隐藏
     */
    private Integer visible;

    /**
     * 权限标识
     */
    private String perms;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 是否删除:0.未删除，1.已删除
     */
    private Integer delFlag;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date createTime;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date updateTime;

}
