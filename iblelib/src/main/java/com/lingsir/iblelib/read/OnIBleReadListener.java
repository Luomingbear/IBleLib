package com.lingsir.iblelib.read;

/**
 * 读取数据的监听接口
 * Created by luoming on 2017/11/29.
 */

public interface OnIBleReadListener {
    void onRead(String code, byte[] bytes);

    void disConnect(String mac);
}
