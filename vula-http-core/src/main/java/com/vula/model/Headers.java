package com.vula.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Represents headers sent from an incoming request. 
 */
public class Headers {

    private HashMap<String, List<String>> headersMap = new HashMap<>();

    public List<String> getHeader(String name) {
        return headersMap.get(name.toLowerCase());
    }

    public boolean containsHeader(String key) {
        return headersMap.containsKey(key.toLowerCase());
    }

    public void addHeader(String name, String value) {
        name = name.toLowerCase();
        if (headersMap.containsKey(name)) {
            headersMap.get(name).add(value);
        } else {
            List<String> values = new ArrayList<>();
            values.add(value);
            headersMap.put(name, values);
        }
    }

    public Set<String> getKeySet() {
        return headersMap.keySet();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String k : getKeySet()) {
            sb.append(k + ": " + getHeader(k) + "\n");
        }
        return sb.toString();
    }
}
