package com.series.user.controller;

import com.series.common.auth.TelegramInitDataValidator;
import com.series.common.entity.Result;
import com.series.user.dto.DevInitRequest;
import com.series.user.dto.LoginTokenDTO;
import com.series.user.dto.TelegramLoginRequest;
import com.series.user.dto.UserProfileDTO;
import com.series.user.dto.Web3LoginPlaceholderDTO;
import com.series.user.entity.AppUser;
import com.series.user.service.AuthSessionService;
import com.series.user.service.IAppUserService;
import com.series.user.utils.SecurityUserUtils;
import com.series.user.utils.UserJwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${telegram.bot-token:}")
    private String telegramBotToken;

    @Value("${auth.dev.enabled:false}")
    private boolean devAuthEnabled;

    @Autowired
    private IAppUserService appUserService;

    @Autowired
    private AuthSessionService authSessionService;

    @Autowired
    private UserJwtUtil userJwtUtil;

    /**
     * Telegram Mini App 登录：校验 initData HMAC，upsert 用户并签发 JWT（aud=user）。
     */
    @PostMapping("/telegram")
    public Result<LoginTokenDTO> telegramLogin(@RequestBody TelegramLoginRequest request) {
        if (request == null || request.getInitData() == null || request.getInitData().isEmpty()) {
            return Result.error("initData 不能为空");
        }
        if (telegramBotToken == null || telegramBotToken.isEmpty()) {
            return Result.error("服务端未配置 TELEGRAM_BOT_TOKEN");
        }
        if (!TelegramInitDataValidator.validate(request.getInitData(), telegramBotToken)) {
            return Result.error("Telegram initData 校验失败");
        }
        Long tgId = TelegramInitDataValidator.parseTelegramUserId(request.getInitData());
        if (tgId == null) {
            return Result.error("无法解析 Telegram 用户 ID");
        }
        String telegramId = String.valueOf(tgId);
        AppUser user = appUserService.upsertTelegramUser(
                telegramId,
                request.getNickname(),
                request.getAvatar()
        );
        return Result.ok(authSessionService.issueToken(user));
    }

    /**
     * 开发阶段无 Telegram 账号时初始化用户（需 auth.dev.enabled=true）。
     */
    @PostMapping("/dev/init")
    public Result<LoginTokenDTO> devInit(@RequestBody(required = false) DevInitRequest request) {
        if (!devAuthEnabled) {
            return Result.error("开发登录未启用，请设置 AUTH_DEV_ENABLED=true");
        }
        String telegramId = "dev-local";
        String nickname = "Dev User";
        if (request != null) {
            if (request.getTelegramId() != null && !request.getTelegramId().isEmpty()) {
                telegramId = request.getTelegramId();
            }
            if (request.getNickname() != null && !request.getNickname().isEmpty()) {
                nickname = request.getNickname();
            }
        }
        AppUser user = appUserService.getOrCreateDevUser(telegramId, nickname);
        user.setAuthProvider("dev");
        appUserService.updateById(user);
        return Result.ok(authSessionService.issueToken(user));
    }

    /**
     * Web3 登录预留（后续：nonce 挑战 + 钱包签名 + 绑定 wallet_address）。
     */
    @PostMapping("/web3/challenge")
    public Result<Web3LoginPlaceholderDTO> web3Challenge() {
        Web3LoginPlaceholderDTO dto = new Web3LoginPlaceholderDTO();
        return Result.ok(dto);
    }

    @GetMapping("/user/info")
    public Result<UserProfileDTO> userInfo() {
        AppUser user = SecurityUserUtils.getCurrentUser();
        if (user == null) {
            return Result.error("用户未登录或登录状态失效");
        }
        return Result.ok(AuthSessionService.toProfile(user));
    }

    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error("未提供令牌");
        }
        String token = authHeader.substring(7).trim();
        String userId = userJwtUtil.getUserId(token);
        if (userId != null) {
            userJwtUtil.revokeLoginToken(userId);
        }
        userJwtUtil.blacklistToken(token);
        return Result.ok("注销成功");
    }
}
