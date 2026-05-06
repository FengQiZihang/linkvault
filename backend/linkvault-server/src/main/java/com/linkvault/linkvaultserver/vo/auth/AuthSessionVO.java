package com.linkvault.linkvaultserver.vo.auth;

public class AuthSessionVO {
    private String accessToken;
    private Integer expiresIn;
    private Boolean isNewUser;
    private UserVO user;

    public AuthSessionVO() {
    }

    public AuthSessionVO(String accessToken, Integer expiresIn, Boolean isNewUser, UserVO user) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.isNewUser = isNewUser;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Boolean getNewUser() {
        return isNewUser;
    }

    public void setNewUser(Boolean newUser) {
        isNewUser = newUser;
    }

    public UserVO getUser() {
        return user;
    }

    public void setUser(UserVO user) {
        this.user = user;
    }
}
