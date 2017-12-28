package com.lingsir.iblelib.search;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import com.lingsir.iblelib.info.IBleInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索模块基类
 * Created by luoming on 2017/11/27.
 */

public abstract class BaseSearchModule {
    protected BluetoothAdapter mBluetoothAdapter;
    protected OnBaseSearchListener mListener;

    protected List<String> macList; //已经扫描到的蓝牙设备的地址列表

    public BaseSearchModule() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        macList = new ArrayList<>();
    }

    /**
     * 搜索时间 ms
     */
    abstract int searchTime();

    /**
     * 开始搜索
     *
     * @param listener
     */
    public abstract void startSearch(Context context, OnBaseSearchListener listener);

    /**
     * 停止搜索
     */
    public abstract void stopSearch();

    public interface OnBaseSearchListener {
        void onSearched(IBleInfo bleInfo);

        //搜索结束
        void onComplete();
    }
}
