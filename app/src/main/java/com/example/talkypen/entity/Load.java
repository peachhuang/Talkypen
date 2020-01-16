package com.example.talkypen.entity;

import org.litepal.crud.LitePalSupport;

public class Load extends LitePalSupport {

    private String did;

    private String name;

    private String state;

    private String current_size;

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCurrent_size() {
        return current_size;
    }

    public void setCurrent_size(String current_size) {
        this.current_size = current_size;
    }

    public String getTotal_size() {
        return total_size;
    }

    public void setTotal_size(String total_size) {
        this.total_size = total_size;
    }

    private String total_size;
}
