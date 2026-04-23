package com.tcm.rx.service.basicData;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.rx.entity.basicData.Herb;
import com.tcm.rx.vo.basicData.request.HerbImportVO;
import com.tcm.rx.vo.basicData.request.HerbQueryVO;
import com.tcm.rx.vo.basicData.response.HerbVO;
import com.tcm.rx.vo.herb.request.HerbSyncVO;
import com.tcm.rx.vo.herb.response.HerbSyncDataVO;
import com.tcm.rx.vo.herb.response.HerbSyncDetailVO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IHerbService extends IService<Herb> {

    // 分页查询
    List<HerbVO> queryPage(HerbQueryVO queryVO);

    // 导出
    void export(HerbQueryVO queryVO, HttpServletResponse response) throws IOException;

    // 导入
    List<HerbImportVO> importData(MultipartFile file) throws Exception;

    Boolean batchInsertHerb(List<HerbImportVO> herbVOList);

    // 新增
    Long add(HerbVO herbVO);

    // 编辑
    Long update(Herb herb);

    // 删除
    void delete(Long id);

    Map<String, Herb> getMapByCodes(List<String> collect);

    TablePageInfo<HerbSyncDataVO> syncHerbList(HerbSyncVO syncVO);

    Boolean syncHerb(HerbSyncDataVO syncDataVO);

    /**
     * 采购单下载
     *
     * @param syncDataVO
     */
    public void purchaseDownload(HerbSyncDataVO syncDataVO, HttpServletResponse response) throws IOException;

    /**
     * 饮片名称匹配
     *
     * @param queryVO
     * @return
     */
    public List<Herb> herbNameMatch(HerbQueryVO queryVO);
}
