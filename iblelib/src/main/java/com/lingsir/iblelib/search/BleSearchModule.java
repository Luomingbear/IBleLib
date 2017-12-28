package com.lingsir.iblelib.search;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.lingsir.iblelib.info.IBleInfo;

/**
 * Ble搜索蓝牙设备
 * Created by luoming on 2017/11/27.
 */

public class BleSearchModule extends BaseSearchModule {
    private static final String TAG = "BleSearchModule";

    @Override
    int searchTime() {
        return 10 * 1000;
    }

    @Override
    public void startSearch(Context activity, OnBaseSearchListener listener) {
        this.mListener = listener;
//        mBluetoothAdapter.startDiscovery();

        //手机版本低于18则取消ble搜索
        if (Build.VERSION.SDK_INT < 18) {
            if (mListener != null)
                mListener.onComplete();
            return;
        }

        //蓝牙不可用
        if (mBluetoothAdapter == null)
            return;


        //开始扫描
        mBluetoothAdapter.startLeScan(leScanCallback);

        //停止扫描
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopSearch();
                if (mListener != null)
                    mListener.onComplete();
            }
        }, searchTime());

        Log.d(TAG, "startSearch: BleSearchModule");
    }

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            //已经扫描到的地址就不再加入回调
            for (String mac : macList) {
                if (mac.equals(device.getAddress())) {
                    return;
                }
            }

            //加入已经扫描的列表
            macList.add(device.getAddress());

            //回调
            if (mListener != null)
                mListener.onSearched(new IBleInfo(device.getName(), device.getAddress(), rssi));
        }
    };

    @Override
    public void stopSearch() {
        if (Build.VERSION.SDK_INT >= 18)
            mBluetoothAdapter.stopLeScan(leScanCallback);
    }
}
