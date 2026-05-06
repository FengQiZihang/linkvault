package com.linkvault.linkvaultserver.context;

public class CurrentUserInfo {

    private final Long userId;
    private final String phone;

    public CurrentUserInfo(Long userId, String phone) {
        this.userId = userId;
        this.phone = phone;
    }

    public Long getUserId() {
        return userId;
    }

    public String getPhone() {
        return phone;
    }
}
