package com.vula.constants;

public enum HttpHeader {
    // Content headers
    CONTENT_TYPE("Content-Type"),
    CONTENT_LENGTH("Content-Length"),
    CONTENT_ENCODING("Content-Encoding"),
    CONTENT_LANGUAGE("Content-Language"),
    CONTENT_DISPOSITION("Content-Disposition"),

    // Request headers
    HOST("Host"),
    USER_AGENT("User-Agent"),
    ACCEPT("Accept"),
    ACCEPT_LANGUAGE("Accept-Language"),
    ACCEPT_ENCODING("Accept-Encoding"),
    ACCEPT_CHARSET("Accept-Charset"),
    AUTHORIZATION("Authorization"),
    COOKIE("Cookie"),
    REFERER("Referer"),
    ORIGIN("Origin"),
    IF_MODIFIED_SINCE("If-Modified-Since"),
    IF_NONE_MATCH("If-None-Match"),

    // Response headers
    SET_COOKIE("Set-Cookie"),
    LOCATION("Location"),
    WWW_AUTHENTICATE("WWW-Authenticate"),
    ETAG("ETag"),
    LAST_MODIFIED("Last-Modified"),

    // Connection/transport headers
    CONNECTION("Connection"),
    TRANSFER_ENCODING("Transfer-Encoding"),
    KEEP_ALIVE("Keep-Alive"),
    UPGRADE("Upgrade"),

    // Caching headers
    CACHE_CONTROL("Cache-Control"),
    PRAGMA("Pragma"),
    EXPIRES("Expires"),

    // CORS headers
    ACCESS_CONTROL_ALLOW_ORIGIN("Access-Control-Allow-Origin"),
    ACCESS_CONTROL_ALLOW_METHODS("Access-Control-Allow-Methods"),
    ACCESS_CONTROL_ALLOW_HEADERS("Access-Control-Allow-Headers");

    private String name;

    private HttpHeader(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
