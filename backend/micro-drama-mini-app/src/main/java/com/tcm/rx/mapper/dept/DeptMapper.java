package com.tcm.rx.mapper.dept;

import com.tcm.rx.entity.dept.Dept;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcm.rx.vo.dept.request.DeptQueryVO;
import com.tcm.rx.vo.dept.response.DeptVO;

import java.util.List;

/**
 * <p>
 * 诊疗开方系统--科室表 Mapper 接口
 * </p>
 *
 * @author xph
 * @since 2025-07-10
 */
public interface DeptMapper extends BaseMapper<Dept> {

    /**
     * 查询科室信息的数据
     */
    List<DeptVO> deptList(DeptQueryVO queryVO);

}
