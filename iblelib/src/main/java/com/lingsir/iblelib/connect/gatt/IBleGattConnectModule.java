package com.lingsir.iblelib.connect.gatt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.lingsir.iblelib.connect.BaseBleConnectModule;
import com.lingsir.iblelib.read.IBleReadManager;

import java.util.UUID;

/**
 * 蓝牙gatt连接
 * Created by luoming on 2017/11/28.
 */

public class IBleGattConnectModule extends BaseBleConnectModule {
    private static final String TAG = "IBleGattConnectModule";
    private BluetoothGatt mBluetoothGatt;
    private String mac;

    public IBleGattConnectModule(String code) {
        super(code);
    }

    @Override
    public int getMode() {
        return GATT;
    }

    @Override
    public void startConnect(final Context context, final String mac, final UUID uuid) {
        this.mac = mac;
        final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mac);
        if (device == null || context == null) {
            if (mListener != null)
                mListener.onFailed(mac, GATT);
            return;
        }

        if (mBluetoothGatt != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
            }
            mBluetoothGatt = null;
        }

        //已经连接成功的就不需要继续连接了
        if (isConnected()) {
            if (mListener != null)
                mListener.onSucceed(mac, uuid, getMode());

            isConnect = true;
            return;
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            mBluetoothGatt = device.connectGatt(context, false, new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    switch (newState) {
                        case BluetoothProfile.STATE_CONNECTED: {
                            if (mListener != null)
                                mListener.onSucceed(mac, uuid, getMode());
                            Log.i(TAG, "onConnectionStateChange: 连接成功 gatt");

                            mListener = null;
                            isConnect = true;
                            //发现服务
                            mBluetoothGatt.discoverServices();
                            break;
                        }

                        case BluetoothProfile.STATE_DISCONNECTED: {
                            //断开连接
                            mBluetoothGatt.disconnect();
                            mBluetoothGatt.close();
                            isConnect = false;
                            if (mListener != null)
                                mListener.onFailed(mac, GATT);

                            Log.e(TAG, "onConnectionStateChange: 连接失败 gatt");
                            //失败则取消监听，因为连接会有重试
                            mListener = null;
                            break;
                        }

                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    super.onServicesDiscovered(gatt, status);
                    Log.i(TAG, "onServicesDiscovered: " + gatt);
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.i(TAG, "onServicesDiscovered: gatt" + gatt);
                    }
                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    Log.i(TAG, "onCharacteristicRead: " + gatt);
                    //处理特性读取返回的数据
                    if (status == BluetoothGatt.GATT_SUCCESS) {

                        /**
                         * 广播通知
                         */
                        sendBroadcast(context, mac, uuid, getCode(), characteristic.getValue());
                    }

                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    Log.i(TAG, "onCharacteristicChanged: ");
                    //处理通知返回的数据

                    /**
                     * 广播通知
                     */
                    sendBroadcast(context, mac, uuid, getCode(), characteristic.getValue());
                }

                @Override
                public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    Log.i(TAG, "onDescriptorRead: " + descriptor);

                    /**
                     * 广播通知
                     */
                    sendBroadcast(context, mac, uuid, getCode(), descriptor.getValue());
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    Log.i(TAG, "onCharacteristicWrite: ");

                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    Log.i(TAG, "onDescriptorWrite: ");

                }

            });

        }
    }


    @Override
    public boolean disConnect(Context context) {
        if (context == null)
            return false;

        //断开连接
        if (mBluetoothGatt != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
                isConnect = false;
                return true;
            }

        return false;
    }

    @Override
    public boolean isConnected() {
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mac);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return mBluetoothGatt != null && mBluetoothGatt.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED;
        }
        return false;
    }

    /**
     * 数据获取到了,广播通知
     */
    private void sendBroadcast(Context context, String mac, UUID uuid, String code, byte[] bytes) {
        if (bytes == null || bytes.length == 0)
            return;

        Intent intent = new Intent();
        intent.setAction(IBleReadManager.READ_ACTION);

        intent.putExtra(IBleReadManager.MAC, mac);
        intent.putExtra(IBleReadManager.UUiD, uuid);
        intent.putExtra(IBleReadManager.CODE, code);
        intent.putExtra(IBleReadManager.BYTE, bytes);
        if (context != null)
            context.sendBroadcast(intent);
    }
}
