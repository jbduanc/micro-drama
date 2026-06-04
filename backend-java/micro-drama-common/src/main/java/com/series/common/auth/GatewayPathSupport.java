package com.series.common.auth;

import javax.servlet.http.HttpServletRequest;

/**
 * 统一处理 Kong 路由前缀（/admin-api、/user-api 等）与公开登录路径。
 */
public final class GatewayPathSupport {

    private GatewayPathSupport() {
    }

    public static String normalizeServletPath(HttpServletRequest request) {
        String servletPath = request.getServletPath() != null ? request.getServletPath() : "";
        String pathInfo = request.getPathInfo() != null ? request.getPathInfo() : "";
        return stripGatewayPrefix(servletPath + pathInfo);
    }

    public static String stripGatewayPrefix(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        for (String prefix : new String[]{"/admin-api", "/user-api", "/content-api"}) {
            if (path.startsWith(prefix + "/")) {
                path = path.substring(prefix.length());
                break;
            }
            if (path.equals(prefix)) {
                path = "/";
                break;
            }
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path;
    }

    public static boolean isKongProxied(HttpServletRequest request) {
        String requestId = request.getHeader("X-Kong-Request-Id");
        return requestId != null && !requestId.isEmpty();
    }
}
