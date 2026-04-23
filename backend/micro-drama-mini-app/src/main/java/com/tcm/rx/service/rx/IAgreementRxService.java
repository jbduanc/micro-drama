package com.tcm.rx.service.rx;

import com.tcm.rx.entity.rx.AgreementRx;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.rx.entity.rx.AgreementRxDetail;
import com.tcm.rx.entity.rx.RxMgmtDetail;
import com.tcm.rx.vo.rx.request.AgreementRxExcelAddVO;
import com.tcm.rx.vo.rx.request.AgreementRxQueryVO;
import com.tcm.rx.vo.rx.request.SaveAgreementRxVO;
import com.tcm.rx.vo.rx.response.AgreementRxDetailVO;
import com.tcm.rx.vo.rx.response.AgreementRxListVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 诊疗开方系统--协定方主表 服务类
 * </p>
 *
 * @author shouhan
 * @since 2025-07-17
 */
public interface IAgreementRxService extends IService<AgreementRx> {

    /**
     * 协定方新增/修改
     */
    Long save(SaveAgreementRxVO saveVo);

    /**
     * 分页查询协定方
     * @param queryVO
     * @return
     */
    List<AgreementRxListVO> agreementRxList(AgreementRxQueryVO queryVO);

    /**
     * 详情
     */
    AgreementRxDetailVO detail(Long id);

    /**
     * 删除
     */
    void delete(Long id);

    /**
     * 下载模版
     *
     * @Param isCurrDoctor true 个人模版导入 false 协定方管理导入
     */
    void downloadTemplate(HttpServletResponse response, Boolean isCurrDoctor) throws IOException;

    /**
     * 导入（返回列表）
     *
     * @Param isCurrDoctor true 个人模版导入 false 协定方管理导入
     */
    List importAgreementRx(MultipartFile file, Boolean isCurrDoctor) throws IOException;

    /**
     * 导入确认新增
     */
    Boolean importAdd(AgreementRxExcelAddVO vo);

    List<AgreementRxDetail> getTransDetails(Long id);
}
