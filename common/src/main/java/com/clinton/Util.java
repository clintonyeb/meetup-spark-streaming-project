package com.clinton;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;

public abstract class Util {
    public static String getEnv(String key) {
        String value = System.getenv(key);
        if (Strings.isNullOrEmpty(value)) throw new RuntimeException("Environment value was not found");
        return value;
    }

    public static byte[] serializeStr(ObjectMapper mapper, final String str) {
        return serializeObj(mapper, str);
    }

    public static byte[] serializeObj(ObjectMapper mapper, final Object obj) {
        try {
            return mapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String from(String endpoint, String apiKey, Map<String, String> params) {
        return String.format("%s?%s&apiKey=%s", endpoint, mapToParam(params), apiKey);
    }

    public static String mapToParam(Map<String, String> param) {
        StringJoiner join = new StringJoiner("&");
        for (String key : param.keySet()) {
            join.add(String.format("%s=%s", key, encode(param.get(key))));
        }
        return  join.toString();
    }

    public static String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}
