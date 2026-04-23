package com.tcm.rx.service.rx;

import com.alibaba.fastjson2.JSONObject;
import com.tcm.rx.entity.rx.RxMgmt;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.rx.entity.rx.RxMgmtDetail;
import com.tcm.rx.vo.cons.request.ConsAddVO;
import com.tcm.rx.vo.cons.request.ConsQueryVO;
import com.tcm.rx.vo.cons.response.ConsVO;
import com.tcm.rx.vo.rx.request.AncProvRxImportVO;
import com.tcm.rx.vo.rx.request.RxMgmtImportVO;
import com.tcm.rx.vo.rx.request.RxMgmtQueryVO;
import com.tcm.rx.vo.rx.request.RxMgmtSaveVO;
import com.tcm.rx.vo.rx.response.RxMgmtDetailVO;
import com.tcm.rx.vo.rx.response.RxMgmtVO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 诊疗开方系统--方剂管理 服务类
 * </p>
 *
 * @author djbo
 * @since 2025-09-08
 */
public interface IRxMgmtService extends IService<RxMgmt> {

    /**
     * 分页查询
     */
    List<RxMgmtVO> queryRxMgmtPage(RxMgmtQueryVO queryVO);

    /**
     * 查询详情
     *
     * @param id
     * @return
     */
    RxMgmtVO getInfo(Long id);

    /**
     * 保存
     *
     * @param rxMgmtSaveVO
     * @return
     */
    Long saveRxMgmt(RxMgmtSaveVO rxMgmtSaveVO);

    /**
     * 删除
     */
    Boolean delete(Long id);

    /**
     * 批量写入
     *
     * @param rxMgmts
     * @return
     */
    Boolean batchInsertRxMgmt(RxMgmtImportVO rxMgmts);

    /**
     * 导入
     *
     * @param file
     * @return
     * @throws Exception
     */
    List rxMgmtImport(Integer type, MultipartFile file) throws Exception;

    /**
     * 专病专方疾病列表
     *
     * @return
     */
    List<String> getTreatDis(String treatDis);

    List<RxMgmtDetail> getTransDetails(Long id);
}
