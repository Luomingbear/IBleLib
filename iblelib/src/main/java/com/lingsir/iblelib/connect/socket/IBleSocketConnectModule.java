package com.lingsir.iblelib.connect.socket;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.lingsir.iblelib.connect.BaseBleConnectModule;
import com.lingsir.iblelib.read.IBleReadManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Socket连接的实现
 * Created by luoming on 2017/11/28.
 */

public class IBleSocketConnectModule extends BaseBleConnectModule {
    private static final String TAG = "IBleSocketConnectModule";
    private BluetoothSocket mBluetoothSocket; //蓝牙socket

    public IBleSocketConnectModule(String code) {
        super(code);
    }

    @Override
    public int getMode() {
        return SOCKET;
    }


    @Override
    public void startConnect(Context context, final String mac, UUID uuid) {

        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mac);
        if (device == null)
            return;

        //已经连接成功的就不需要继续连接了
        if (isConnected()) {
            if (mListener != null)
                mListener.onSucceed(mac, uuid, getMode());

            return;
        }


        try {
            mBluetoothSocket = null;
            //通过uuid连接
            mBluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);

            if (mBluetoothSocket == null) {
                if (mListener != null)
                    mListener.onFailed(mac, SOCKET);
                return;
            }

            //连接
            mBluetoothSocket.connect();


            //
            if (mListener != null)
                mListener.onSucceed(mac, uuid, getMode());

            Log.i(TAG, "startConnect: 连接成功 socket");

            while (true) {
                InputStream is = mBluetoothSocket.getInputStream();
                byte[] buffer = new byte[1024];  // 从is获取到的byte[]
                int bytes; //字节数

                bytes = is.read(buffer);

                String s = new String(buffer, 0, bytes);//说人话
                Log.i(TAG, "读取到InputStream : " + s);

                /**
                 * 广播通知
                 */
                Intent intent = new Intent();
                intent.setAction(IBleReadManager.READ_ACTION);

                intent.putExtra(IBleReadManager.MAC, mac);
                intent.putExtra(IBleReadManager.UUiD, uuid);
                intent.putExtra(IBleReadManager.CODE, getCode());
                intent.putExtra(IBleReadManager.BYTE, s.getBytes());

                if (context != null)
                    context.sendBroadcast(intent);
            }

        } catch (IOException e) {
            if (mBluetoothSocket != null) {
                try {
                    mBluetoothSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            if (mListener != null)
                mListener.onFailed(mac, SOCKET);

            Log.e(TAG, "startConnect: 连接失败 socket");
        }

    }

    @Override
    public void disConnect(Context context) {
        if (mBluetoothSocket != null) {
            try {
                mBluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isConnected() {
        return mBluetoothSocket != null && mBluetoothSocket.isConnected();
    }

}
