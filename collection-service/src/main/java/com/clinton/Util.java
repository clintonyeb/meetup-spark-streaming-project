package com.clinton;

import io.netty.util.internal.StringUtil;

public abstract class Util {
    public static String getEnv(String key) {
        String value = System.getenv(key);
        if (StringUtil.isNullOrEmpty(value)) throw new RuntimeException("Environment value was not found");
        return value;
    }
}
