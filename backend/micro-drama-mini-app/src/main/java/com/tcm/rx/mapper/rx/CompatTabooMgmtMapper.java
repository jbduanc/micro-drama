package com.tcm.rx.mapper.rx;

import com.tcm.rx.entity.rx.CompatTabooMgmt;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcm.rx.vo.rx.request.CompatTabooMgmtQueryVO;
import com.tcm.rx.vo.rx.response.CompatTabooMgmtVO;

import java.util.List;

/**
 * <p>
 * 诊疗开方系统--配伍禁忌管理表 Mapper 接口
 * </p>
 *
 * @author djbo
 * @since 2025-09-10
 */
public interface CompatTabooMgmtMapper extends BaseMapper<CompatTabooMgmt> {

    List<CompatTabooMgmtVO> selectByQuery(CompatTabooMgmtQueryVO compatTabooMgmtQueryVO);

}
