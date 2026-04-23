package com.series.common.constants;

public interface CommonConstants {
    public interface SysAuthConstants{
        /**
         * 是否菜单外链（否）
         */
        public static final String NO_FRAME = "1";

        /**
         * 菜单类型（目录）
         */
        public static final String TYPE_DIR = "M";

        /**
         * 菜单类型（菜单）
         */
        public static final String TYPE_MENU = "C";

        /**
         * 菜单类型（按钮）
         */
        public static final String TYPE_BUTTON = "F";

        /**
         * Layout组件标识
         */
        public final static String LAYOUT = "Layout";

        /**
         * ParentView组件标识
         */
        public final static String PARENT_VIEW = "ParentView";

        /**
         * InnerLink组件标识
         */
        public final static String INNER_LINK = "InnerLink";


        /**
         * www主域
         */
        public static final String WWW = "www.";

        /**
         * http请求
         */
        public static final String HTTP = "http://";

        /**
         * https请求
         */
        public static final String HTTPS = "https://";


        public static final String LOGIN_TOKEN_KEY = "login_tokens:";
        /**
         * 令牌前缀
         */
        public static final String TOKEN_PREFIX = "Bearer ";

        /**
         * 令牌前缀
         */
        public static final String LOGIN_USER_KEY = "login_user_key";



        // 令牌秘钥
        public static final String secret = "abcdefghijklmnopqrstuvwxyz";

        // 令牌有效期（默认30分钟）
        public static final int  expireTime = 180;

        public static final String  tokenHeader = "Token";
        public static final String  USER_HEADER = "UserInfo";

        public static final long MILLIS_SECOND = 1000;
        public static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;
    }
}
