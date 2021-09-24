package com.clinton;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;

public abstract class Utils {
    public static String getEnv(String key) {
        String value = System.getenv(key);
        if (Strings.isNullOrEmpty(value))
            throw new RuntimeException("Environment value was not found with key: " + key);
        return value;
    }

    public static byte[] serializeStr(final String str) {
        return serializeObj(str);
    }

    public static byte[] serializeObj(final Object obj) {
        try {
            return DI.OBJECT_MAPPER.writeValueAsBytes(obj);
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
        return join.toString();
    }

    public static String encode(String text) {
        try {
            return URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String fullPath(String file) {
        URL url = Utils.class.getClassLoader().getResource(file);
        if (url == null) throw new RuntimeException("Could not find file with path " + file);
        try {
            return url.toURI().toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
