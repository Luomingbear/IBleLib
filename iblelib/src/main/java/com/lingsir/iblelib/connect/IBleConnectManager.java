package com.lingsir.iblelib.connect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.lingsir.iblelib.UUIDUtil;
import com.lingsir.iblelib.connect.gatt.IBleGattConnectModule;
import com.lingsir.iblelib.connect.socket.IBleSocketConnectModule;
import com.lingsir.iblelib.read.OnIBleReadListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 蓝牙配对连接管家
 * Created by luoming on 2017/11/28.
 */

public class IBleConnectManager {
    private static final String TAG = "IBleConnectManager";

    private static IBleConnectManager instance;
    private List<ConnectBleItem> mConnectItemList; //已经连接成功的

    private String mCurMac; //当前正在连接的mac地址
    private UUID mCurUUid; //当前正在连接的uuid
    private Context mContext;

    private OnBleConnectListener mListener; //蓝牙连接监听接口

    private BluetoothAdapter mBluetoothAdapter;

    public static IBleConnectManager getInstance() {
        if (instance == null) {
            synchronized (IBleConnectManager.class) {
                if (instance == null) {
                    instance = new IBleConnectManager();
                }
            }
        }
        return instance;

    }


    private IBleConnectManager() {
        mConnectItemList = new ArrayList<>();
        mConnectES = Executors.newSingleThreadExecutor();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * 获取已经连接成功的列表
     *
     * @return
     */
    public List<ConnectBleItem> getConnectBleItemList() {
        return mConnectItemList;
    }

    /**
     * 配对蓝牙
     *
     * @param mac
     */
    public void bond(final String mac, final OnBleBondListener listener) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mac);
        if (device == null) {
            if (listener != null)
                listener.noDevice(mac);
            return;
        }


        /**
         * 蓝牙设备的配对状态
         */
        switch (device.getBondState()) {
            case BluetoothDevice.BOND_BONDED: {
                //曾经配对了
                if (listener != null)
                    listener.onSucceed(mac);
                break;
            }

            case BluetoothDevice.BOND_NONE: {
                //没有配对过
                try {
                    Method createBond = BluetoothDevice.class.getMethod("createBond");
                    createBond.invoke(device);

                    //超时10s处理，检查用户是否已经配对成功，如果成功则开始连接
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (checkBonded(mac)) {
                                if (listener != null)
                                    listener.onSucceed(mac);
                            } else {
                                if (listener != null)
                                    listener.onFailed(mac);
                            }
                        }
                    }, 10 * 1000);

                } catch (Exception e) {
                    e.printStackTrace();
                    if (listener != null)
                        listener.onFailed(mac);
                }
                break;
            }
        }
    }

    /**
     * 检查是否已经配对成功
     *
     * @param mac
     * @return
     */
    public boolean checkBonded(String mac) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mac);

        return (device != null && device.getBondState() == BluetoothDevice.BOND_BONDED);
    }

    /**
     * 连接蓝牙
     *
     * @param context
     * @param mac
     */
    public void connect(Context context, String mac, UUID uuid, final OnBleConnectListener listener) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mac);
        if (device == null) {
            if (listener != null)
                listener.noDevice(mac);

            return;
        }


        this.mContext = context;
        this.mCurMac = mac;
        this.mCurUUid = uuid;

        this.mListener = listener;

        /**
         * 检查蓝牙配对情况
         */
        switch (device.getBondState()) {
            case BluetoothDevice.BOND_BONDED: {
                //曾经配对了
                startConnect();
                break;
            }

            case BluetoothDevice.BOND_NONE: {
                //没有配对过
                //执行配对
                bond(mac, new OnBleBondListener() {
                    @Override
                    public void noDevice(String mac) {
                        if (mListener != null)
                            mListener.noDevice(mac);
                    }

                    @Override
                    public void onSucceed(String mac) {
                        startConnect();
                    }

                    @Override
                    public void onFailed(String mac) {
                        //配对失败说明连接也会失败
                        if (mListener != null)
                            mListener.onFailed(mac);
                    }
                });
                break;
            }
        }
    }

    //当前的临时连模块列表
    private List<BaseBleConnectModule> mTmpConnectModuleList = new ArrayList<>();
    private int mConnectIndex = 0; //当前正在尝试连接的index
    private ExecutorService mConnectES;

    /**
     * 开始连接蓝牙
     */
    private void startConnect() {

        //连接模块
        mConnectItemList.clear();
        mTmpConnectModuleList.clear();

        mTmpConnectModuleList.add(new IBleGattConnectModule(mCurMac)); //gatt连接
        mTmpConnectModuleList.add(new IBleSocketConnectModule(mCurMac));


        for (final BaseBleConnectModule connectModule : mTmpConnectModuleList) {
            if (!mConnectES.isShutdown()) {
                mConnectES.execute(new Runnable() {
                    @Override
                    public void run() {
                        connectModule.setOnBleConnectListener(onBleConnectListener);
                        connectModule.startConnect(mContext, mCurMac, mCurUUid);
                    }
                });
            }
        }

    }

    /**
     * 蓝牙连接状态回调
     */
    private BaseBleConnectModule.OnBleConnectModuleListener onBleConnectListener =
            new BaseBleConnectModule.OnBleConnectModuleListener() {
                @Override
                public void onSucceed(String mac, UUID uuid, int mode) {
                    //保存连接的信息
                    ConnectBleItem connectItem = new ConnectBleItem(mac, uuid, mode, mTmpConnectModuleList);
                    mConnectItemList.add(connectItem);

                    if (mListener != null)
                        mListener.onSucceed(mac);

                    mListener = null;
                }

                @Override
                public void onFailed(String mac, int mode) {
                    for (ConnectBleItem item : mConnectItemList) {
                        if (item.mac.equals(mac)) {
                            return;
                        }
                    }

                    //尝试最后一个连接方案也失败了
                    if (mode == mTmpConnectModuleList.get(mTmpConnectModuleList.size() - 1).getMode()) {
                        if (mListener != null) {
                            mListener.onFailed(mac);
                        }
                    }
                }
            };


    private void nextConnect() {

    }

    /**
     * 断开连接
     *
     * @param mac
     */
    public void disConnect(String mac) {
        for (BaseBleConnectModule connectModule : mTmpConnectModuleList) {
            connectModule.disConnect(mContext);
        }

        //将连接信息从已经连接的列表里面移除
        for (ConnectBleItem item : mConnectItemList) {
            if (item.mac.equals(mac)) {
                mConnectItemList.remove(item);
            }
        }

    }

    /**
     * 断开所有的连接
     */
    public void disConnectAll() {
        for (ConnectBleItem connectBleItem : mConnectItemList) {
            connectBleItem.bleConnectModule.disConnect(mContext);
        }

        mConnectItemList.clear();
    }

    //配对蓝牙的回调
    public interface OnBleBondListener {
        //蓝牙设备不存在
        void noDevice(String mac);

        //成功
        void onSucceed(String mac);

        //失败
        void onFailed(String mac);
    }

    /**
     * 蓝牙连接监听
     */
    public interface OnBleConnectListener {
        //蓝牙设备不存在
        void noDevice(String mac);

        //成功
        void onSucceed(String mac);

        //失败
        void onFailed(String mac);
    }

    //连接的数据结构
    public class ConnectBleItem {
        public String mac;
        public UUID uuid;
        public int mode = BaseBleConnectModule.GATT; //使用的何种方式连接 gatt或者socket
        public BaseBleConnectModule bleConnectModule; //连接实现的模块

        public ConnectBleItem(String mac, UUID uuid, int mode, List<BaseBleConnectModule> bleConnectModuleList) {
            this.mac = mac;
            this.uuid = uuid;
            this.mode = mode;

            if (bleConnectModuleList == null)
                return;

            //只保留连接成功的模块
            List<BaseBleConnectModule> removeList = new ArrayList<>();
            for (BaseBleConnectModule connectModule : bleConnectModuleList) {
                if (connectModule.getMode() != mode)
                    removeList.add(connectModule);
            }
            bleConnectModuleList.remove(removeList);

            //保留一个就可以了
            if (bleConnectModuleList.size() > 0)
                this.bleConnectModule = bleConnectModuleList.get(0);
        }
    }

}
