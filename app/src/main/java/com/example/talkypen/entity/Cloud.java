package com.example.talkypen.entity;

import org.litepal.crud.LitePalSupport;

/**
 * 本地内容列表的实体类
 */
public class Cloud extends LitePalSupport {

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
}
