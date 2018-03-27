package com.lingsir.iblelib.connect;

import android.content.Context;

import com.lingsir.iblelib.read.OnIBleReadListener;

import java.util.UUID;

/**
 * 蓝牙连接的基类
 * Created by luoming on 2017/11/28.
 */

public abstract class BaseBleConnectModule {
    protected OnBleConnectModuleListener mListener;
    protected boolean isConnect = false;
    private String code; //

    public BaseBleConnectModule(String code) {
        this.code = code;
    }

    public static final int GATT = 0x5566; //使用gatt连接
    public static final int SOCKET = 0x5567; //使用socket连接

    public void setOnBleConnectListener(OnBleConnectModuleListener listener) {
        this.mListener = listener;
    }

    public String getCode() {
        return code;
    }

    public abstract int getMode();

    public abstract void startConnect(Context context, String mac, UUID uuid);

    public abstract boolean disConnect(Context context);

    public abstract boolean isConnected();

    //蓝牙连接返回接口
    public interface OnBleConnectModuleListener {

        //成功
        void onSucceed(String mac, UUID uuid, int mode);

        //失败
        void onFailed(String mac, int mode);
    }
}
