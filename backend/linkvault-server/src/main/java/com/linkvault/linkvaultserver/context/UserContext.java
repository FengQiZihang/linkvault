package com.linkvault.linkvaultserver.context;

public class UserContext {

    private static final ThreadLocal<CurrentUserInfo> CURRENT_USER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setCurrentUser(CurrentUserInfo currentUser) {
        CURRENT_USER.set(currentUser);
    }

    public static CurrentUserInfo getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
