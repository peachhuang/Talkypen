package com.example.talkypen.entity;

import org.litepal.crud.LitePalSupport;

/**
 * 点读笔设备的实体类
 */
public class TalkypenDevice extends LitePalSupport {

    private String did;

    private String mac;

    private String version;

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsed_size() {
        return used_size;
    }

    public void setUsed_size(String used_size) {
        this.used_size = used_size;
    }

    public String getTotal_size() {
        return total_size;
    }

    public void setTotal_size(String total_size) {
        this.total_size = total_size;
    }

    private String status;

    private String used_size;

    private String total_size;
}
