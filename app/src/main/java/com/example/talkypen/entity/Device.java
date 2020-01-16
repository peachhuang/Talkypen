package com.example.talkypen.entity;

import org.litepal.crud.LitePalSupport;

/**
 * user的实体类
 */
public class Device extends LitePalSupport {

    //devicename
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    //deviceSecret
    private String des;

//    public Device(String name, String des){
//        this.name = name;
//        this.des = des;
//    }


}
