package com.lingsir.iblelib.search;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.lingsir.iblelib.info.IBleInfo;

/**
 * 广播搜索蓝牙设备
 * Created by luoming on 2017/11/27.
 */

public class BroadcastSearchModule extends BaseSearchModule {
    private final static int REQUEST_PERMISSION_ACCESS_LOCATION = 0x0022;
    private static final String TAG = "BroadcastSearchModule";
    private Context mContext; //


    @Override
    int searchTime() {
        return 15 * 1000;
    }

    @Override
    public void startSearch(Context context, OnBaseSearchListener listener) {
        this.mListener = listener;

        //
        if (context != null) {
            if (mContext == null) {
                mContext = context;
                registerBroadcast();
            } else {
                mContext.unregisterReceiver(broadcastReceiver);
                mContext = context;
                registerBroadcast();
            }
        }
        Log.i(TAG, "startSearch: BroadcastSearchModule");

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopSearch();
                if (mListener != null)
                    mListener.onComplete();

            }
        }, searchTime());
    }

    private void registerBroadcast() {

        //定位权限
//        if (Build.VERSION.SDK_INT >= 23) {
//            int checkAccessFinePermission = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION);
//            if (checkAccessFinePermission != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(mContext.getApplicationInfo(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                        REQUEST_PERMISSION_ACCESS_LOCATION);
//                Log.e(TAG, "registerBroadcast: 没有定位权限");
//                return;
//            }
//        }

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                IntentFilter filter = new IntentFilter();
                //发现设备
                filter.addAction(BluetoothDevice.ACTION_FOUND);
//                //设备连接状态改变
                filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
//                //蓝牙设备状态改变
//                filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

                if (mContext != null)
                    mContext.registerReceiver(broadcastReceiver, filter);

                //搜索
                if (mBluetoothAdapter != null)
                    mBluetoothAdapter.startDiscovery();

            }
        });
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "mBluetoothReceiver action = " + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {//每扫描到一个设备，系统都会发送此广播。

                //获取蓝牙设备
                BluetoothDevice scanDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (scanDevice == null || scanDevice.getName() == null) return;

                //信号强度。
                int rssi = intent.getExtras().getInt(BluetoothDevice.EXTRA_RSSI);

                //已经扫描到的地址就不再加入回调
                for (String mac : macList) {
                    if (mac.equals(scanDevice.getAddress())) {
                        return;
                    }
                }

                //加入已经扫描的列表
                macList.add(scanDevice.getAddress());


                if (mListener != null) {
                    mListener.onSearched(new IBleInfo(scanDevice.getName(), scanDevice.getAddress(), rssi));
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //

            }
        }
    };

    @Override
    public void stopSearch() {
        try {
            if (mContext != null)
                mContext.unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (mBluetoothAdapter != null)
            mBluetoothAdapter.cancelDiscovery();
    }
}
