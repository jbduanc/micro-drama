package com.tcm.rx.service.hsp;

import com.tcm.rx.entity.hsp.Hsp;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.rx.vo.hsp.request.HspQueryVO;
import com.tcm.rx.vo.hsp.response.HspVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 诊疗开方系统--医疗机构表 服务类
 * </p>
 *
 * @author xph
 * @since 2025-07-10
 */
public interface IHspService extends IService<Hsp> {

    /**
     * 查询医疗机构的数据
     */
    List<HspVO> hspList(HspQueryVO queryVO);

    /**
     * 医疗机构管理--新增医疗机构
     */
    Long insertHsp(HspVO hspVO);

    /**
     * 医疗机构管理--批量新增医疗机构
     */
    Boolean batchInsertHsp(List<HspVO> hspVOList);

    /**
     * 医疗机构管理--更新医疗机构
     */
    Long updateHsp(HspVO hspVO);

    /**
     *  医疗机构管理--删除医疗机构
     */
    void deleteHsp(Long id);

    /**
     *  医疗机构管理--根据医联体，删除医疗机构
     */
    void deleteHspByMedicalGroup(Long id);

    /**
     * 医疗机构管理--下载导入模版
     */
    void downloadTemplate(HttpServletResponse response) throws IOException;

    /**
     * 医疗机构管理--导入数据
     */
    List<HspVO> importInfo(MultipartFile file) throws IOException;

    /**
     * 医疗机构管理--导出数据
     */
    void export(HttpServletResponse response, HspQueryVO queryVO) throws IOException;

}
