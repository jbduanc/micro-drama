package com.series.user.controller;

import com.series.common.auth.TelegramInitDataValidator;
import com.series.common.auth.AuthAudience;
import com.series.common.auth.AuthCookieSupport;
import com.series.common.auth.AuthTokenIssueService;
import com.series.common.auth.AuthTokenPair;
import com.series.common.auth.GatewayAuthSupport;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;

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

    @Autowired
    private AuthTokenIssueService authTokenIssueService;

    @Autowired
    private AuthCookieSupport authCookieSupport;

    /**
     * Telegram Mini App 登录：校验 initData HMAC，upsert 用户并签发 JWT（aud=user）。
     */
    @PostMapping("/telegram")
    public Result<LoginTokenDTO> telegramLogin(@RequestBody TelegramLoginRequest request,
                                               HttpServletResponse response) {
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
        return Result.ok(writeLoginResponse(response, authSessionService.issueToken(user)));
    }

    /**
     * 开发阶段无 Telegram 账号时初始化用户（需 auth.dev.enabled=true）。
     */
    @PostMapping("/dev/init")
    public Result<LoginTokenDTO> devInit(@RequestBody(required = false) DevInitRequest request,
                                         HttpServletResponse response) {
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
        return Result.ok(writeLoginResponse(response, authSessionService.issueToken(user)));
    }

    @PostMapping("/refresh")
    public Result<LoginTokenDTO> refresh(@RequestBody(required = false) Map<String, String> body,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        String refresh = resolveRefreshToken(request, body);
        if (refresh == null) {
            return Result.error("缺少 refresh token");
        }
        Optional<AuthTokenPair> pair = authTokenIssueService.refresh(refresh, AuthAudience.USER);
        if (!pair.isPresent()) {
            return Result.error("refresh token 无效或已过期");
        }
        AuthTokenPair tokens = pair.get();
        authCookieSupport.writeTokenCookies(response, tokens);
        LoginTokenDTO dto = new LoginTokenDTO(tokens.getAccessToken(), tokens.getRefreshToken(), null);
        return Result.ok(dto);
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
    public Result<String> logout(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7).trim();
        }
        String userId = token != null ? userJwtUtil.getUserId(token) : null;
        if (userId != null) {
            authTokenIssueService.revokeAll(userId, AuthAudience.USER, token);
        }
        authCookieSupport.clearTokenCookies(response);
        return Result.ok("注销成功");
    }

    private LoginTokenDTO writeLoginResponse(HttpServletResponse response, LoginTokenDTO dto) {
        authCookieSupport.writeTokenCookies(response, new AuthTokenPair(dto.getAccessToken(), dto.getRefreshToken()));
        return dto;
    }

    private String resolveRefreshToken(HttpServletRequest request, Map<String, String> body) {
        String refresh = authCookieSupport.readCookie(request, com.series.common.auth.AuthCookieNames.REFRESH);
        if (refresh != null) {
            return refresh;
        }
        if (body != null && body.get("refreshToken") != null) {
            return body.get("refreshToken");
        }
        return request.getHeader(GatewayAuthSupport.REFRESH_HEADER);
    }
}
