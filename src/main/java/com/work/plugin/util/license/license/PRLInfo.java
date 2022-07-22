package com.work.plugin.util.license.license;


public class PRLInfo {
    private String company;
    private String p;
    private String v;
    private String user;
    private String appstart;
    private String append;
    private String svrstart;
    private String svrend;

    public PRLInfo() {
    }

    public PRLInfo(String company,String p, String v, String user, String appstart, String append, String svrstart, String svrend) {
        this.company = company;
        this.p = p;
        this.v = v;
        this.user = user;
        this.appstart = appstart;
        this.append = append;
        this.svrstart = svrstart;
        this.svrend = svrend;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String conpany) {
        this.company = company;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getAppstart() {
        return appstart;
    }

    public void setAppstart(String appstart) {
        this.appstart = appstart;
    }

    public String getAppend() {
        return append;
    }

    public void setAppend(String append) {
        this.append = append;
    }

    public String getSvrstart() {
        return svrstart;
    }

    public void setSvrstart(String svrstart) {
        this.svrstart = svrstart;
    }

    public String getSvrend() {
        return svrend;
    }

    public void setSvrend(String svrend) {
        this.svrend = svrend;
    }
}

