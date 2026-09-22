package com.vula.constants;

public enum HttpVersion {
    
    HTTP_VERSION_1("HTTP/1.1"),
    HTTP_VERSION_2("HTTP/1.2");

    private String version;

    private HttpVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return this.version;
    }

    public static HttpVersion getVersion(String version) {
        for (HttpVersion v : HttpVersion.values()) {
            if (v.getVersion().equals(version)) return v; 
        }
        System.out.println("HttpVersion invalid, returning null");
        return null;
    }

    @Override
    public String toString() {
        return this.version;
    }

}
