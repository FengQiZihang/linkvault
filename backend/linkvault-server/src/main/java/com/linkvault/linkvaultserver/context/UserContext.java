package com.linkvault.linkvaultserver.context;

public class UserContext {

    private static final ThreadLocal<CurrentUserInfo> CURRENT_USER = new ThreadLocal<>(); // 当前线程绑定的登录用户信息

    private UserContext() {
    }

    /**
     * 将拦截器解析出的当前用户信息写入线程上下文，供Service层读取。
     */
    public static void setCurrentUser(CurrentUserInfo currentUser) {
        CURRENT_USER.set(currentUser);
    }

    /**
     * 获取当前请求线程中的登录用户信息。
     */
    public static CurrentUserInfo getCurrentUser() {
        return CURRENT_USER.get();
    }

    /**
     * 请求结束后清理ThreadLocal，避免线程复用时串用户。
     */
    public static void clear() {
        CURRENT_USER.remove();
    }
}
