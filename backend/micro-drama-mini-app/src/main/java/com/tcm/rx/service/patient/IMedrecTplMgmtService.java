package com.tcm.rx.service.patient;

import com.github.pagehelper.Page;
import com.tcm.common.entity.Result;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.rx.entity.patient.MedrecTplMgmt;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.rx.vo.patient.request.MedrecTplMgmtQueryVO;
import com.tcm.rx.vo.patient.response.MedrecTplMgmtVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

import static com.github.pagehelper.page.PageMethod.startPage;

/**
 * <p>
 * 诊疗开方系统--病历模版管理表 服务类
 * </p>
 *
 * @author djbo
 * @since 2025-09-11
 */
public interface IMedrecTplMgmtService extends IService<MedrecTplMgmt> {

    /**
     * 分页查询
     */
    List<MedrecTplMgmtVO> pageList(MedrecTplMgmtQueryVO queryVO);

    /**
     * 查看详情
     */
    MedrecTplMgmtVO getInfo(Long id);

    /**
     * 新增病历模版
     */
    Long saveMedrecTpl(MedrecTplMgmt medrecTplMgmt);

    /**
     * 删除管理处方
     */
    Boolean delete(Long id);
}
