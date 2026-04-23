package com.tcm.rx.service.basicData;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.rx.entity.basicData.ChargeItem;
import com.tcm.rx.vo.basicData.request.ChargeItemImportVO;
import com.tcm.rx.vo.basicData.request.ChargeItemQueryVO;
import com.tcm.rx.vo.basicData.response.ChargeItemVO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface IChargeItemService extends IService<ChargeItem> {
    List<ChargeItemVO> queryPage(ChargeItemQueryVO queryVO);
    Long add(ChargeItemVO chargeItemVO);
    Long update(ChargeItemVO chargeItemVO);
    void delete(Long id);
    List<ChargeItemImportVO> importData(MultipartFile file) throws Exception;
    void downloadTemplate(HttpServletResponse response) throws IOException;
    Boolean batchInsert(List<ChargeItemImportVO> importVOS);
}
