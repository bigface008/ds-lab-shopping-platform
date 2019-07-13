package com.dslab;

import java.util.Map;

public class UpdaterConfig {
    private String currency;
    private int period;
    private String url;
    private String znode;
    private Map<String, String> initial;

    public Map<String, String> getInitial() {
        return initial;
    }

    public void setInitial(Map<String, String> initial) {
        this.initial = initial;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getZnode() {
        return znode;
    }

    public void setZnode(String znode) {
        this.znode = znode;
    }
}
