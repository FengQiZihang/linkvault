package com.linkvault.linkvaultserver.common;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TimeUtils {

    private TimeUtils() {
    }

    /**
     * 返回UTC时区下的当前LocalDateTime，用于写入MySQL DATETIME(3)字段。
     */
    public static LocalDateTime nowUtc() {
        return LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
    }
}
