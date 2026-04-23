package com.tcm.rx.controller.patient;

import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.Page;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.rx.entity.auth.CUser;
import com.tcm.rx.entity.patient.PatientVisit;
import com.tcm.rx.service.auth.ICUserService;
import com.tcm.rx.service.patient.IPatientVisitService;
import com.tcm.rx.vo.patient.request.PatientVisitQueryVO;
import com.tcm.rx.vo.patient.response.PatientVisitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import com.alibaba.fastjson2.JSONArray;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.github.pagehelper.page.PageMethod.startPage;

/**
 * <p>
 * 诊疗开方系统--患者就诊流水表 前端控制器
 * </p>
 *
 * @author xph
 * @since 2025-07-23
 */
@RestController
@RequestMapping("/patientVisit")
public class PatientVisitController {

    @Resource
    private IPatientVisitService patientVisitService;

    @Resource
    private ICUserService userService;

    /**
     * 查询患者就诊流水数据（分页查询）
     */
    @PostMapping("/patientVisitPageList")
    public TablePageInfo<PatientVisitVO> patientVisitPageList(@RequestBody PatientVisitQueryVO queryVO) {
        Page page = startPage(queryVO.getPage(),queryVO.getSize());
        List<PatientVisitVO> resultList = patientVisitService.patientVisitList(queryVO);
        return new TablePageInfo(resultList, Math.toIntExact(page.getTotal()));
    }

    /**
     * 根据id查询患者就诊流水的数据
     */
    @GetMapping(value = "/{id}")
    public PatientVisitVO patientVisitById(@PathVariable("id") Long id) {
        PatientVisitVO result = new PatientVisitVO();
        PatientVisit patientVisit = patientVisitService.getById(id);
        if (Objects.nonNull(patientVisit)) {
            BeanUtils.copyProperties(patientVisit, result);
        }
        String attendingDoctorId = result.getAttendingDoctorId();
        if (attendingDoctorId != null && ObjectUtil.isNotEmpty(attendingDoctorId.trim())) {
            try {
                JSONArray doctorIdArray = JSONArray.parseArray(attendingDoctorId);
                if (doctorIdArray != null && !doctorIdArray.isEmpty()) {
                    Object firstIdObj = doctorIdArray.get(0);
                    if (firstIdObj != null) {
                        Long doctorId = Long.valueOf(firstIdObj.toString());
                        CUser user = Optional.ofNullable(userService.getById(doctorId)).orElse(new CUser());
                        result.setDoctorName(user.getRealName());
                    }
                } else {
                    result.setDoctorName(null);
                }
            } catch (Exception e) {
                result.setDoctorName(null);
            }
        }
        return result;
    }

    /**
     * 更新患者就诊流水
     */
    @PostMapping(value = "/updatePatientVisit")
    public Long updatePatientVisit(@RequestBody PatientVisitVO patientVisitVO){
        return patientVisitService.updatePatientVisit(patientVisitVO);
    }

}

