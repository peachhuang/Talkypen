package com.example.talkypen.entity;

import org.litepal.crud.LitePalSupport;

public class Cloudcontent extends LitePalSupport {

    private String name;

    private String version;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getOnline_flag() {
        return online_flag;
    }

    public void setOnline_flag(int online_flag) {
        this.online_flag = online_flag;
    }

    private int online_flag;
}
