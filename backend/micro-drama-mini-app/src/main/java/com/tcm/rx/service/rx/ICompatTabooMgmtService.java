package com.tcm.rx.service.rx;

import com.tcm.common.entity.Result;
import com.tcm.rx.entity.rx.CompatTabooMgmt;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.rx.vo.rx.request.CompatTabCheckReqVO;
import com.tcm.rx.vo.rx.request.CompatTabImportVO;
import com.tcm.rx.vo.rx.request.CompatTabooMgmtQueryVO;
import com.tcm.rx.vo.rx.request.CompatTabooMgmtSaveVO;
import com.tcm.rx.vo.rx.response.CompatTabCheckResVO;
import com.tcm.rx.vo.rx.response.CompatTabooMgmtVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.github.pagehelper.page.PageMethod.startPage;

/**
 * <p>
 * 诊疗开方系统--配伍禁忌管理表 服务类
 * </p>
 *
 * @author djbo
 * @since 2025-09-10
 */
public interface ICompatTabooMgmtService extends IService<CompatTabooMgmt> {
    /**
     * 分页查询
     */
    List<CompatTabooMgmtVO> pageList(CompatTabooMgmtQueryVO queryVO);

    /**
     * 查看详情
     */
    CompatTabooMgmtVO getInfo(Long id);

    /**
     * 新增配伍禁忌
     */
    Long saveCompatTab(CompatTabooMgmtSaveVO compatTabooMgmtSaveVO);

    /**
     * 删除配伍禁忌
     */
    Boolean delete(Long id);

    /**
     * 批量新增处方管理
     */
    Boolean batchInsertCompatTab(List<CompatTabImportVO> compatTabImportVOS);

    /**
     * 处方管理导入
     */
    List<CompatTabImportVO> compatTabImport(MultipartFile file) throws Exception;

    /**
     * 配伍禁忌检查
     *
     * @param compatTabCheckVO
     * @return
     */
    List<CompatTabCheckResVO> compatTabCheck(CompatTabCheckReqVO compatTabCheckVO);
}
