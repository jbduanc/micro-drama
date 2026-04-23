package com.tcm.rx.service.basicData.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.enums.BooleanEnum;
import com.tcm.common.enums.StatusEnum;
import com.tcm.common.exception.ServiceException;
import com.tcm.common.utils.CommonUtil;
import com.tcm.common.utils.EasyExcelUtil;
import com.tcm.rx.entity.basicData.ChargeItem;
import com.tcm.rx.enums.ChargeItemType;
import com.tcm.rx.mapper.basicData.ChargeItemMapper;
import com.tcm.rx.service.basicData.IChargeItemService;
import com.tcm.rx.vo.basicData.request.ChargeItemImportVO;
import com.tcm.rx.vo.basicData.request.ChargeItemQueryVO;
import com.tcm.rx.vo.basicData.response.ChargeItemVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChargeItemServiceImpl extends ServiceImpl<ChargeItemMapper, ChargeItem> implements IChargeItemService {

    @Override
    public List<ChargeItemVO> queryPage(ChargeItemQueryVO queryVO) {
        // 补充机构信息
        BaseUser user = UserContextHolder.getUserInfoContext();
        queryVO.setMedicalGroupId(user.getMedicalGroupId());
        List<ChargeItem> list = baseMapper.selectByQuery(queryVO);
        return list.stream().map(record -> {
            ChargeItemVO chargeItemVO = new ChargeItemVO();
            BeanUtils.copyProperties(record, chargeItemVO);
            chargeItemVO.setStatusName(StatusEnum.CODE_MAP.get(record.getStatus()));
            chargeItemVO.setItemTypeName(ChargeItemType.CODE_MAP.get(record.getItemType()));
            return chargeItemVO;
        }).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long add(ChargeItemVO vo) {
        BaseUser user = UserContextHolder.getUserInfoContext();
        validateAddData(vo, user);
        ChargeItem entity = new ChargeItem();
        BeanUtils.copyProperties(vo, entity);
        // 设置机构信息和创建人
        CommonUtil.setRxBaseEntity(entity, user); // 复用基础字段设置工具类
        save(entity);
        return entity.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long update(ChargeItemVO vo) {
        if (Objects.isNull(vo.getId())) {
            throw new ServiceException("ID不能为空");
        }
        ChargeItem entity = getById(vo.getId());
        if (Objects.isNull(entity)) {
            throw new ServiceException("数据不存在");
        }
        BeanUtils.copyProperties(vo, entity);
        entity.setUpdateBy(UserContextHolder.getUserInfoContext().getId().toString());
        entity.setUpdateTime(new Date());
        saveOrUpdate(entity);
        return entity.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Long id) {
        ChargeItem entity = getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException("数据不存在");
        }
        entity.setDelFlag(BooleanEnum.TRUE.getNum());
        entity.setUpdateTime(new Date());
        saveOrUpdate(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean batchInsert(List<ChargeItemImportVO> importVOS) {
        if(ObjectUtil.isEmpty(importVOS)){
            return false;
        }
        BaseUser user = UserContextHolder.getUserInfoContext();
        List<ChargeItem> chargeItemList = new ArrayList<>();
        importVOS.forEach(importVO -> {
            ChargeItem entity = new ChargeItem();
            BeanUtils.copyProperties(importVO, entity);
            // 枚举转换：字符串 -> 编码（0/1）
            entity.setItemType(ChargeItemType.NAME_MAP.get(importVO.getItemType()));
            entity.setStatus(StatusEnum.NAME_MAP.get(importVO.getStatus()));
            // 数值转换
            entity.setUnitPrice(new BigDecimal(importVO.getUnitPrice()));
            // 机构信息
            entity.setMedicalGroupId(user.getMedicalGroupId());
            CommonUtil.setRxBaseEntity(entity, user);
            // 处理更新逻辑（根据项目编码判断是否存在）
            ChargeItem exist = getOne(new LambdaQueryWrapper<ChargeItem>()
                    .eq(ChargeItem::getMedicalGroupId, user.getMedicalGroupId())
                    .eq(ChargeItem::getItemCode, importVO.getItemCode())
                    .eq(ChargeItem::getDelFlag, false));
            if (Objects.nonNull(exist)) {
                entity.setId(exist.getId());
            }
            chargeItemList.add(entity);
        });
       return saveOrUpdateBatch(chargeItemList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<ChargeItemImportVO> importData(MultipartFile file) throws Exception {
        List<ChargeItemImportVO> importList = EasyExcelUtil.importService(file, ChargeItemImportVO.class);
        if (importList.size() > 1000) {
            throw new ServiceException("一次最多导入1000条数据");
        }
        if (CollectionUtils.isEmpty(importList)) {
            throw new ServiceException("未检测到有效数据");
        }
        List<ChargeItemImportVO> importVOS = new ArrayList<>();
        for (ChargeItemImportVO importVO : importList) {
            validateImportData(importVO); // 校验导入数据
            importVOS.add(importVO);
        }
        return importVOS;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        // 实现同Controller中的示例，可复用
    }

    // 新增校验
    private void validateAddData(ChargeItemVO vo, BaseUser user) {
        // 项目编码唯一性校验
        long count = count(new LambdaQueryWrapper<ChargeItem>()
                .eq(ChargeItem::getMedicalGroupId, user.getMedicalGroupId())
                .eq(ChargeItem::getItemCode, vo.getItemCode())
                .eq(ChargeItem::getDelFlag, false));
        if (count > 0) {
            throw new ServiceException("项目编码已存在");
        }
        // 枚举值校验（针对code）
        if (Objects.nonNull(vo.getItemType()) && !ChargeItemType.CODE_MAP.containsKey(vo.getItemType())) {
            throw new ServiceException("项目类型错误，可选值：0（适宜技术）、1（煎药费）");
        }
        if (Objects.nonNull(vo.getStatus()) && !StatusEnum.CODE_MAP.containsKey(vo.getStatus())) {
            throw new ServiceException("状态错误，可选值：0（启用）、1（禁用）");
        }
    }

    // 导入校验
    private void validateImportData(ChargeItemImportVO importVO) {
        StringBuilder error = new StringBuilder();
        // 1. 必填项校验
        if (StringUtils.isBlank(importVO.getItemCode())) error.append("项目编码不能为空/n");
        if (StringUtils.isBlank(importVO.getItemName())) error.append("项目名称不能为空/n");
        if (StringUtils.isBlank(importVO.getItemType())) error.append("项目类型不能为空/n");
        if (StringUtils.isBlank(importVO.getPriceUnit())) error.append("计价单位不能为空/n");
        if (StringUtils.isBlank(importVO.getUnitPrice())) error.append("单价不能为空/n");
        if (StringUtils.isBlank(importVO.getStatus())) error.append("状态不能为空/n");

        // 2. 枚举值校验（针对名称）
        if (!ChargeItemType.NAME_MAP.containsKey(importVO.getItemType())) {
            error.append("项目类型错误，可选值：适宜技术、煎药费/n");
        }
        if (!StatusEnum.NAME_MAP.containsKey(importVO.getStatus())) {
            error.append("状态错误，可选值：启用、禁用/n");
        }

        // 3. 格式校验
        try {
            new BigDecimal(importVO.getUnitPrice());
        } catch (NumberFormatException e) {
            error.append("单价格式错误，需为数字/n");
        }
        importVO.setErrMsg(error.toString());
    }
}