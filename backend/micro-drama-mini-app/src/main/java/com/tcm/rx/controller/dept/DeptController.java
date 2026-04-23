package com.tcm.rx.controller.dept;

import com.github.pagehelper.Page;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.rx.entity.dept.Dept;
import com.tcm.rx.service.dept.IDeptService;
import com.tcm.rx.vo.dept.request.DeptQueryVO;
import com.tcm.rx.vo.dept.response.DeptVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.github.pagehelper.page.PageMethod.startPage;

/**
 * <p>
 * 诊疗开方系统--科室表 前端控制器
 * </p>
 *
 * @author xph
 * @since 2025-07-10
 */
@RestController
@RequestMapping("/dept")
public class DeptController {

    @Resource
    private IDeptService deptService;

    /**
     * 查询科室的数据（分页查询）
     */
    @PostMapping("/deptPageList")
    public TablePageInfo<DeptVO> deptPageList(@RequestBody DeptQueryVO queryVO) {
        Page page = startPage(queryVO.getPage(),queryVO.getSize());
        List<DeptVO> resultList = deptService.deptList(queryVO);
        return new TablePageInfo(resultList, Math.toIntExact(page.getTotal()));
    }

    /**
     * 根据id查询科室的数据
     */
    @GetMapping(value = "/{id}")
    public DeptVO deptById(@PathVariable("id") Long id){
        DeptVO result = new DeptVO();
        Dept dept = deptService.getById(id);
        if(Objects.nonNull(dept)){
            BeanUtils.copyProperties(dept, result);
        }
        return result;
    }

    /**
     * 科室管理--新增科室
     */
    @PostMapping(value = "/insertDept")
    public Long insertDept(@RequestBody DeptVO deptVO){
        return deptService.insertDept(deptVO);
    }

    /**
     * 科室管理--批量新增科室
     */
    @PostMapping(value = "/batchInsertDept")
    public Boolean batchInsertDept(@RequestBody List<DeptVO> deptVOList){
        return deptService.batchInsertDept(deptVOList);
    }

    /**
     * 科室管理--更新科室
     */
    @PostMapping(value = "/updateDept")
    public Long updateDept(@RequestBody DeptVO deptVO){
        return deptService.updateDept(deptVO);
    }

    /**
     * 科室管理--删除科室
     */
    @GetMapping(value = "/delete/{id}")
    public void deleteDept(@PathVariable("id") Long id) {
        deptService.deleteDept(id);
    }

    /**
     * 科室管理--下载导入模版
     */
    @PostMapping(value = "/download/templateDept")
    public void downloadTemplateDept(HttpServletResponse response) throws IOException {
        deptService.downloadTemplate(response);
    }

    /**
     * 科室管理--导入数据
     */
    @PostMapping("/importInfoDept")
    public List<DeptVO> importInfoDept(MultipartFile file) throws Exception {
        return deptService.importInfo(file);
    }

    /**
     * 科室管理--导出数据
     */
    @PostMapping("/exportDept")
    public void export(HttpServletResponse response, @RequestBody DeptQueryVO queryVO) throws IOException {
        deptService.export(response, queryVO);
    }

}

