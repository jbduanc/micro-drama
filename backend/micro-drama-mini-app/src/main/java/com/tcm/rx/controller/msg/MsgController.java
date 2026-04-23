package com.tcm.rx.controller.msg;

import com.github.pagehelper.Page;
import com.tcm.common.entity.Result;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.rx.entity.msg.Msg;
import com.tcm.rx.service.msg.IMsgService;
import com.tcm.rx.vo.msg.request.MsgQueryVO;
import com.tcm.rx.vo.msg.request.MsgUpdateVO;
import com.tcm.rx.vo.msg.response.MsgVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.pagehelper.page.PageMethod.startPage;

/**
 * <p>
 * 诊疗开方系统--消息表 前端控制器
 * </p>
 *
 * @author djbo
 * @since 2025-09-11
 */
@RestController
@RequestMapping("/msg")
public class MsgController {

    @Resource
    private IMsgService msgService;

    /**
     * 分页查询
     */
    @PostMapping("/pageList")
    public TablePageInfo<MsgVO> pageList(@RequestBody MsgQueryVO queryVO) {
        Page<MsgVO> page = startPage(queryVO.getPage(), queryVO.getSize());
        List<MsgVO> msgVOS = msgService.pageList(queryVO);
        return new TablePageInfo<>(msgVOS, Math.toIntExact(page.getTotal()));
    }

    /**
     * 修改消息
     */
    @PostMapping("/updateMsg")
    public Result<Boolean> updateMsg(@RequestBody MsgUpdateVO msgUpdateVO) {
        return Result.ok(msgService.updateMsg(msgUpdateVO));
    }

    /**
     * 获取用户消息
     *
     * @return
     */
    @PostMapping("/userMsgSummary")
    public Result<Map<String, Object>> getUserMsgSummary() {
        BaseUser user = UserContextHolder.getUserInfoContext();
        Long userId = user.getId();
        int unreadCount = msgService.getUnreadCount(userId);
        List<Msg> latestMsgs = msgService.getLatestMessages(userId, 5); // 取最新5条
        Map<String, Object> summary = new HashMap<>();
        summary.put("unreadCount", unreadCount);
        summary.put("latestMsgs", latestMsgs);
        return Result.ok(summary);
    }
}