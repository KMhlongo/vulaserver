package com.vula.constants;

public enum HttpVersion {
    
    HTTP_VERSION_1("HTTP/1.1");

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
        return null;
    }

    @Override
    public String toString() {
        return this.version;
    }

}
