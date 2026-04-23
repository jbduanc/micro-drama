package com.tcm.rx.controller.patient;

import com.github.pagehelper.Page;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.rx.entity.patient.PatientBase;
import com.tcm.rx.service.patient.IPatientBaseService;
import com.tcm.rx.vo.patient.request.PatientBaseQueryVO;
import com.tcm.rx.vo.patient.response.PatientBaseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static com.github.pagehelper.page.PageMethod.startPage;

/**
 * <p>
 * 诊疗开方系统--患者档案信息表 前端控制器
 * </p>
 *
 * @author xph
 * @since 2025-07-23
 */
@RestController
@RequestMapping("/patientBase")
public class PatientBaseController {

    @Resource
    private IPatientBaseService patientBaseService;

    /**
     * 根据条件（患者流水信息）查询患者档案信息数据
     */
    @PostMapping("/patientBasePageListByCondition")
    public TablePageInfo<PatientBaseVO> patientBasePageListByCondition(@RequestBody PatientBaseQueryVO queryVO) {
        Page page = startPage(queryVO.getPage(),queryVO.getSize());
        List<PatientBaseVO> resultList = patientBaseService.patientBaseListByCondition(queryVO);
        return new TablePageInfo(resultList, Math.toIntExact(page.getTotal()));
    }

    /**
     * 根据id查询患者档案信息的数据
     */
    @GetMapping(value = "/{id}")
    public PatientBaseVO patientBaseById(@PathVariable("id") Long id){
        PatientBaseVO result = new PatientBaseVO();
        PatientBase patientBase = patientBaseService.getById(id);
        if(Objects.nonNull(patientBase)){
            BeanUtils.copyProperties(patientBase, result);
        }
        return result;
    }

    /**
     * 更新患者档案信息
     */
    @PostMapping(value = "/updatePatientBase")
    public Long updatePatientBase(@RequestBody PatientBaseVO patientBaseVO){
        return patientBaseService.updatePatientBase(patientBaseVO);
    }

}

