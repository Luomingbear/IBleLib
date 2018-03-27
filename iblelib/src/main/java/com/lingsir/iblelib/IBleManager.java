package com.lingsir.iblelib;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import com.lingsir.iblelib.connect.IBleConnectManager;
import com.lingsir.iblelib.read.IBleReadManager;
import com.lingsir.iblelib.read.OnIBleReadListener;
import com.lingsir.iblelib.search.BleSearchModule;
import com.lingsir.iblelib.search.BroadcastSearchModule;
import com.lingsir.iblelib.search.IBleSearchManager;

import java.util.UUID;

/**
 * 蓝牙连接管家
 * Created by luoming on 2017/11/27.
 */

public class IBleManager {
    private static IBleManager instance;
    private IBleSearchManager iBleSearchManager; //搜索蓝牙的执行管家
    private Context mActivity; //当前的activity

    public static IBleManager getInstance() {
        if (instance == null) {
            synchronized (IBleManager.class) {
                instance = new IBleManager();
            }
        }
        return instance;
    }

    private IBleManager() {
        iBleSearchManager = new IBleSearchManager();
    }

    /**
     * 开始搜索
     */
    public void startSearch(Context activity, IBleSearchManager.OnIBleSearchListener listener) {
        //先停止搜索
        stopSearch();

        this.mActivity = activity;


        //搜索结果监听
        iBleSearchManager.setOnIBleSearchListener(listener);

        //添加搜索模块
        //添加的顺序决定了执行的顺序
        iBleSearchManager.addSearchModule(new BroadcastSearchModule()); //广播搜索
        iBleSearchManager.addSearchModule(new BleSearchModule()); // ble搜索

        //开始
        iBleSearchManager.startSearch(activity);
    }

    /**
     * 停止搜索
     */
    public void stopSearch() {
        iBleSearchManager.stopSearch();
    }

    /**
     * 配对蓝牙
     *
     * @param mac
     */
    public void bond(String mac) {
        IBleConnectManager.getInstance().bond(mac, null);

    }

    /**
     * 连接蓝牙
     */
    public void connect(Context context, String mac, UUID uuid, IBleConnectManager.OnBleConnectListener listener) {
        stopSearch();

        //
        IBleConnectManager.getInstance().connect(context, mac, uuid, listener);

    }

    /**
     * 断开连接
     */
    public boolean disConnect(String mac) {
        return IBleConnectManager.getInstance().disConnect(mac);
    }

    /**
     * 打开蓝牙
     * 需要蓝牙权限
     */
    public boolean openBle() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null)
            return false;

        return bluetoothAdapter.enable();
    }

    /**
     * 关闭蓝牙
     * 需要蓝牙权限
     */
    public boolean closeBle() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null)
            return false;

        return bluetoothAdapter.disable();
    }

    /**
     * 检查蓝牙是否打开
     *
     * @return
     */
    public boolean isOpen() {
        return BluetoothAdapter.getDefaultAdapter() != null && BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    /**
     * 是否已经连接
     *
     * @return
     */
    public boolean isConnect(String mac) {
        for (IBleConnectManager.ConnectBleItem connectBleItem : IBleConnectManager.getInstance().getConnectBleItemList()) {
            if (connectBleItem.mac.equals(mac))
                return true;
        }
        return false;
    }

    /**
     * 读取数据
     *
     * @param mac
     * @param uuid
     * @param listener
     */
    public void read(Context context, String mac, UUID uuid, OnIBleReadListener listener) {
        for (IBleConnectManager.ConnectBleItem connectItem : IBleConnectManager.getInstance().getConnectBleItemList()) {
            if (connectItem.mac.equals(mac) && connectItem.uuid.equals(uuid)) {
                //已经连接成功
                IBleReadManager.getInstance().read(context, connectItem, listener);
                return;
            }
        }

        //连接失败
        if (listener != null)
            listener.disConnect(mac);
    }

    /**
     * 停止数据读取的监听
     *
     * @param mac
     * @param uuid
     */
    public void stopRead(Context context, String mac, UUID uuid) {
        //断开所有的连接
        IBleConnectManager.getInstance().disConnectAll();
        IBleReadManager.getInstance().stopRead(context, mac, uuid);
    }

}
