package com.series.admin.controller.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.series.admin.dto.sys.UserInfoDTO;
import com.series.admin.entity.sys.SysUser;
import com.series.admin.service.sys.ISysUserService;
import com.series.admin.utils.JwtUtil;
import com.series.admin.utils.SecurityUserUtils;
import com.series.common.auth.AuthAudience;
import com.series.common.auth.AuthCookieSupport;
import com.series.common.auth.AuthTokenIssueService;
import com.series.common.auth.AuthTokenPair;
import com.series.common.auth.GatewayAuthSupport;
import com.series.common.entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/oauth2")
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    /** Google 可能不返回 name；DB 若 NOT NULL 会插入失败 */
    private static String emptyIfNull(String s) {
        return s == null ? "" : s;
    }

    /** 头像 URL 可能很长，避免超出 varchar 上限 */
    private static String limitLen(String s, int max) {
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max);
    }

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private AuthTokenIssueService authTokenIssueService;

    @Autowired
    private AuthCookieSupport authCookieSupport;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    /**
     * 前端调用：获取 Google 授权跳转地址
     */
    @GetMapping("/authorize-url")
    public Result<String> getGoogleAuthorizeUrl() {
        ClientRegistration googleClient = clientRegistrationRepository.findByRegistrationId("google");

        String resolvedRedirectUri = googleRedirectUri;
        log.info("Google OAuth2 authorize redirect_uri={}", resolvedRedirectUri);

        String authorizeUrl = UriComponentsBuilder
                .fromUriString(googleClient.getProviderDetails().getAuthorizationUri())
                .queryParam("client_id", googleClient.getClientId())
                .queryParam("redirect_uri", resolvedRedirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", String.join(" ", googleClient.getScopes()))
                .queryParam("state", "random_state")
                .build()
                .toUriString();

        return Result.ok(authorizeUrl);
    }

    /**
     * 修复1：必须用 @GetMapping ！！！Google回调是GET请求
     */
    @PostMapping("/login/google")
    public Result<AuthTokenPair> googleLogin(@RequestBody Map<String, String> payload,
                                             HttpServletRequest request,
                                             HttpServletResponse response) {
        String code = payload.get("code");
        if (code == null || code.isEmpty()) {
            return Result.error("授权码不能为空");
        }
        String redirectUri = payload.get("redirectUri");

        // 1. 获取 Google 客户端配置
        ClientRegistration googleClient = clientRegistrationRepository.findByRegistrationId("google");

        String resolvedRedirectUri = googleRedirectUri;
        log.info("Google OAuth2 token redirect_uri={}, payloadRedirectUri={}", resolvedRedirectUri, redirectUri);

        // 2. 用 code 换 access_token
        MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
        tokenParams.add("code", code);
        tokenParams.add("client_id", googleClient.getClientId());
        tokenParams.add("client_secret", googleClient.getClientSecret());
        // 必须与授权请求中的 redirect_uri 及 Google 控制台配置完全一致
        tokenParams.add("redirect_uri", resolvedRedirectUri);
        tokenParams.add("grant_type", "authorization_code");

        Map<String, Object> tokenResponse = restTemplate.postForObject(
                googleClient.getProviderDetails().getTokenUri(),
                tokenParams,
                Map.class
        );
        String accessToken = (String) tokenResponse.get("access_token");

        // 3. 获取用户信息（以下逻辑保持不变）
        String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                GOOGLE_USER_INFO_URL,
                HttpMethod.GET,
                requestEntity,
                Map.class
        );
        Map<String, Object> userInfo = userInfoResponse.getBody();

        if (userInfo == null) {
            return Result.error("获取Google用户信息失败");
        }

        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String avatar = (String) userInfo.get("picture");

        if (email == null || email.isEmpty()) {
            return Result.error("未获取到Google邮箱");
        }

        // 4. 用户入库或更新
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getGoogleEmail, email);
        SysUser user = sysUserService.getOne(wrapper);

        if (user == null) {
            user = new SysUser();
            user.setId(UUID.randomUUID());
            user.setGoogleEmail(email);
            user.setNickname(emptyIfNull(name));
            user.setAvatar(limitLen(emptyIfNull(avatar), 1024));
            user.setStatus(1);
            user.setCreateTime(new Date());
            user.setUpdateTime(new Date());
            sysUserService.save(user);
        } else {
            user.setNickname(emptyIfNull(name));
            user.setAvatar(limitLen(emptyIfNull(avatar), 1024));
            user.setUpdateTime(new Date());
            sysUserService.updateById(user);
        }

        AuthTokenPair tokens = authTokenIssueService.issue(email, AuthAudience.ADMIN);
        authCookieSupport.writeTokenCookies(response, tokens);
        return Result.ok(tokens);
    }

    @PostMapping("/refresh")
    public Result<AuthTokenPair> refresh(@RequestBody(required = false) Map<String, String> body,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        String refresh = resolveRefreshToken(request, body);
        if (refresh == null) {
            return Result.error("缺少 refresh token");
        }
        Optional<AuthTokenPair> pair = authTokenIssueService.refresh(refresh, AuthAudience.ADMIN);
        if (!pair.isPresent()) {
            return Result.error("refresh token 无效或已过期");
        }
        AuthTokenPair tokens = pair.get();
        authCookieSupport.writeTokenCookies(response, tokens);
        return Result.ok(tokens);
    }

    // 注销：加入黑名单 + 删除Redis登录态
    @PostMapping("/logout")
    public Result logout(@RequestHeader(value = "Authorization", required = false) String authHeader,
                         HttpServletRequest request,
                         HttpServletResponse response) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.replace("Bearer ", "").trim();
        }
        String email = token != null ? jwtUtil.getEmail(token) : null;

        if (email != null) {
            authTokenIssueService.revokeAll(email, AuthAudience.ADMIN, token);
        }
        authCookieSupport.clearTokenCookies(response);

        return Result.ok("注销成功");
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

    /**
     * 获取当前登录用户
     *
     * @return
     */
    @GetMapping("/user/info")
    public Result<UserInfoDTO> getUserInfo() {
        // 1. 从Security上下文获取当前登录用户（无需手动解析Token）
        SysUser sysUser = SecurityUserUtils.getCurrentUser();
        if (sysUser == null) {
            return Result.error("用户未登录或登录状态失效");
        }

        // 2. 封装返回DTO
        UserInfoDTO userInfo = new UserInfoDTO();
        userInfo.setId(sysUser.getId() != null ? sysUser.getId().toString() : null);
        userInfo.setNickname(sysUser.getNickname());
        userInfo.setGoogleEmail(sysUser.getGoogleEmail());
        userInfo.setAvatar(sysUser.getAvatar());
        userInfo.setStatus(sysUser.getStatus());

        return Result.ok(userInfo);
    }
}
