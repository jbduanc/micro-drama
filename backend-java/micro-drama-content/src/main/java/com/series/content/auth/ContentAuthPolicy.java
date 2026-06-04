package com.series.content.auth;

/**
 * 内容服务：C 端用户可读；管理端写操作仅允许 aud=admin。
 */
public final class ContentAuthPolicy {

    private ContentAuthPolicy() {
    }

    public static boolean requiresAdmin(String path) {
        if (path == null) {
            return false;
        }
        return path.startsWith("/microDramas/saveOrUpdate")
                || path.startsWith("/microDramas/delete/")
                || path.startsWith("/microDramas/episodes/delete/")
                || path.equals("/microDramas/episodes/new-id")
                || path.equals("/content/drama")
                || path.startsWith("/content/video/upload-url")
                || path.startsWith("/content/video/complete");
    }
}
