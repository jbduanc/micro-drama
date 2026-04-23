package com.tcm.rx.vo.basicData.response;

import com.tcm.common.enums.StatusEnum;
import com.tcm.rx.entity.basicData.ChargeItem;
import com.tcm.rx.enums.ChargeItemType;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;

/**
 * 收费项目视图对象，用于前端展示和数据传输。
 * 该类包含了收费项目的详细信息，包括从实体类转换而来的枚举名称。
 */
@Data
public class ChargeItemVO {
    /**
     * 收费项目ID，主键
     */
    private Long id;

    /**
     * 项目编码，唯一标识一个收费项目
     */
    private String itemCode;

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 项目类型，数值类型：0-适宜技术，1-煎药费
     */
    private Integer itemType;

    /**
     * 项目类型名称，由数值类型转换为对应的文本描述（如"适宜技术"）
     */
    private String itemTypeName;

    /**
     * 计价单位（如：剂、次、盒等）
     */
    private String priceUnit;

    /**
     * 单价，使用BigDecimal保证精度
     */
    private BigDecimal unitPrice;

    /**
     * 状态，数值类型：0-启用，1-禁用
     */
    private Integer status;

    /**
     * 状态名称，由数值类型转换为对应的文本描述（如"启用"）
     */
    private String statusName;

    /**
     * 适应证描述，可选字段，用于业务描述
     */
    private String adaptation;

    /**
     * 项目详细说明，支持富文本格式（如包含图片、表格等）
     */
    private String description;

    /**
     * 从ChargeItem实体类转换为ChargeItemVO视图对象
     *
     * @param entity 收费项目实体对象
     * @return 转换后的视图对象，包含枚举名称转换
     */
    public static ChargeItemVO fromEntity(ChargeItem entity) {
        ChargeItemVO vo = new ChargeItemVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setItemTypeName(ChargeItemType.CODE_MAP.get(entity.getItemType()));
        vo.setStatusName(StatusEnum.CODE_MAP.get(entity.getStatus()));
        return vo;
    }
}