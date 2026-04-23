package com.tcm.rx.service.dept;

import com.tcm.rx.entity.dept.Dept;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.rx.vo.dept.request.DeptQueryVO;
import com.tcm.rx.vo.dept.response.DeptVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 诊疗开方系统--科室表 服务类
 * </p>
 *
 * @author xph
 * @since 2025-07-10
 */
public interface IDeptService extends IService<Dept> {

    /**
     * 查询科室的数据
     */
    List<DeptVO> deptList(DeptQueryVO queryVO);

    /**
     * 科室管理--新增科室
     */
    Long insertDept(DeptVO deptVO);

    /**
     * 科室管理--批量新增科室
     */
    Boolean batchInsertDept(List<DeptVO> deptVOList);

    /**
     * 科室管理--更新科室
     */
    Long updateDept(DeptVO deptVO);

    /**
     *  科室管理--删除科室
     */
    void deleteDept(Long id);

    /**
     * 科室管理--下载导入模版
     */
    void downloadTemplate(HttpServletResponse response) throws IOException;

    /**
     * 科室管理--导入数据
     */
    List<DeptVO> importInfo(MultipartFile file) throws IOException;

    /**
     * 科室管理--导出数据
     */
    void export(HttpServletResponse response, DeptQueryVO queryVO) throws IOException;

}
