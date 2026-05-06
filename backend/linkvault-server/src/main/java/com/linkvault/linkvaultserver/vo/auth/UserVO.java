package com.linkvault.linkvaultserver.vo.auth;

public class UserVO {
    private Long userId;
    private String phone;
    private String nickname;
    private String avatarUrl;

    public UserVO() {
    }

    public UserVO(Long userId, String phone, String nickname, String avatarUrl) {
        this.userId = userId;
        this.phone = phone;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
