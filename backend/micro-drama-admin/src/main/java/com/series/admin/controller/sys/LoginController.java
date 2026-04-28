package com.series.admin.controller.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.series.admin.dto.sys.UserInfoDTO;
import com.series.admin.entity.sys.SysUser;
import com.series.admin.service.sys.ISysUserService;
import com.series.admin.utils.JwtUtil;
import com.series.admin.utils.SecurityUserUtils;
import com.series.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
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
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/oauth2")
// 临时跨域（生产可配置全局跨域）
@CrossOrigin(origins = "${cors.allowed-origins}")
public class LoginController {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    // 新增：注入JWT过期时间配置
    @Value("${jwt.access-expire}")
    private Long accessExpire;

    /**
     * 前端调用：获取 Google 授权跳转地址
     */
    @GetMapping("/authorize-url")
    public Result<String> getGoogleAuthorizeUrl(
            @RequestParam(value = "redirectUri", required = false) String redirectUri,
            HttpServletRequest request
    ) {
        ClientRegistration googleClient = clientRegistrationRepository.findByRegistrationId("google");

        String resolvedRedirectUri = resolveRedirectUri(request, redirectUri, googleClient.getRedirectUriTemplate());
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
    public Result<String> googleLogin(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String code = payload.get("code");
        if (code == null || code.isEmpty()) {
            return Result.error("授权码不能为空");
        }
        String redirectUri = payload.get("redirectUri");

        // 1. 获取 Google 客户端配置
        ClientRegistration googleClient = clientRegistrationRepository.findByRegistrationId("google");
        String resolvedRedirectUri = resolveRedirectUri(request, redirectUri, googleClient.getRedirectUriTemplate());

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
            user.setGoogleEmail(email);
            user.setNickname(name);
            user.setAvatar(avatar);
            user.setStatus(1);
            user.setCreateTime(new Date());
            user.setUpdateTime(new Date());
            sysUserService.save(user);
        } else {
            user.setUpdateTime(new Date());
            sysUserService.updateById(user);
        }

        // 5. 生成 JWT 并存入 Redis
        String token = jwtUtil.generateToken(email);
        String redisKey = "admin:login:token:" + email;
        redisTemplate.opsForValue().set(redisKey, token, accessExpire, TimeUnit.MILLISECONDS);

        return Result.ok(token);
    }

    // 注销：加入黑名单 + 删除Redis登录态
    @PostMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        // 解析token获取用户邮箱
        String email = jwtUtil.getEmail(token);

        // 1. 删除Redis中的登录态缓存
        if (email != null) {
            redisTemplate.delete("admin:login:token:" + email);
        }
        // 2. 加入黑名单（兜底，防止token未过期但已登出）
        redisTemplate.opsForValue().set("admin:blacklist:" + token, "1", 15, TimeUnit.MINUTES);

        return Result.ok("注销成功");
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
        userInfo.setId(sysUser.getId());
        userInfo.setNickname(sysUser.getNickname());
        userInfo.setGoogleEmail(sysUser.getGoogleEmail());
        userInfo.setAvatar(sysUser.getAvatar());
        userInfo.setStatus(sysUser.getStatus());

        return Result.ok(userInfo);
    }

    private String resolveRedirectUri(HttpServletRequest request, String redirectUri, String redirectUriTemplate) {
        // 1) Prefer explicit redirectUri passed from frontend.
        if (redirectUri != null && !redirectUri.isBlank() && !redirectUri.endsWith("/undefined")) {
            return redirectUri;
        }

        // 2) If redirectUriTemplate is already a concrete URL (no placeholders), use it.
        if (redirectUriTemplate != null && !redirectUriTemplate.isBlank()
                && !redirectUriTemplate.contains("{") && !redirectUriTemplate.contains("}")) {
            return redirectUriTemplate;
        }

        // 3) Fallback: infer from request host (supports reverse-proxy via X-Forwarded-*).
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null || scheme.isBlank()) {
            scheme = request.getScheme();
        }

        String host = request.getHeader("X-Forwarded-Host");
        if (host == null || host.isBlank()) {
            host = request.getHeader("Host");
        }
        if (host == null || host.isBlank()) {
            host = request.getServerName() + (request.getServerPort() > 0 ? ":" + request.getServerPort() : "");
        }

        return scheme + "://" + host + "/oauth/callback";
    }
}
