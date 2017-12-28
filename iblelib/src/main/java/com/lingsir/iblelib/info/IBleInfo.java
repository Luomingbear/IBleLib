package com.lingsir.iblelib.info;

/**
 * 蓝牙的基本信息
 * Created by luoming on 2017/11/27.
 */

public class IBleInfo {
    private String name; //名字
    private String mac; //地址
    private float rssi; //信号强度

    public IBleInfo() {
    }

    public IBleInfo(String name, String mac, float srris) {
        this.name = name;
        this.mac = mac;
        this.rssi = srris;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public float getRssi() {
        return rssi;
    }

    public void setRssi(float rssi) {
        this.rssi = rssi;
    }
}
