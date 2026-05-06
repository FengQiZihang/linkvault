package com.linkvault.linkvaultserver.vo.auth;

public class SendSmsCodeResponseVO {

    private Integer expiresIn;
    private Integer cooldownSeconds;

    public SendSmsCodeResponseVO() {
    }

    public SendSmsCodeResponseVO(Integer expiresIn, Integer cooldownSeconds) {
        this.expiresIn = expiresIn;
        this.cooldownSeconds = cooldownSeconds;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Integer getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(Integer cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }
}
