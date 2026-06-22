package com.linkvault.linkvaultserver.common;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

    private static final DateTimeFormatter UTC_ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private TimeUtils() {
    }

    /**
     * 返回UTC时区下的当前LocalDateTime，用于写入MySQL DATETIME(3)字段。
     */
    public static LocalDateTime nowUtc() {
        return LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
    }

    /**
     * 将数据库中按UTC解释的LocalDateTime格式化为接口统一时间字符串。
     */
    public static String formatUtc(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return time.format(UTC_ISO_FORMATTER);
    }
}
