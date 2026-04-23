package com.tcm.rx.service.cons;

import com.tcm.common.entity.TablePageInfo;
import com.tcm.rx.entity.cons.Consultation;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.rx.vo.cons.request.ConsAddVO;
import com.tcm.rx.vo.cons.request.ConsQueryVO;
import com.tcm.rx.vo.cons.request.ConsUpdateVO;
import com.tcm.rx.vo.cons.response.ConsVO;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * <p>
 * 诊疗开方系统--会诊表 服务类
 * </p>
 *
 * @author djbo
 * @since 2025-09-04
 */
public interface IConsultationService extends IService<Consultation> {

    /**
     * 分页查询会诊列表
     */
    List<ConsVO> queryPage(ConsQueryVO queryVO);

    /**
     * 会诊详情
     *
     * @param id
     * @return
     */
    ConsVO getConsInfo(Long id);

    /**
     * 保存会诊
     *
     * @param addVO
     * @return
     */
    Long saveCons(ConsAddVO addVO);

    /**
     * 删除会诊
     */
    Boolean delete(Long id);
}
