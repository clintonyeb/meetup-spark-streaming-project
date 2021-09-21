package com.clinton;

import com.google.common.base.Strings;

public abstract class Util {
    public static String getEnv(String key) {
        String value = System.getenv(key);
        if (Strings.isNullOrEmpty(value)) throw new RuntimeException("Environment value was not found");
        return value;
    }
}
