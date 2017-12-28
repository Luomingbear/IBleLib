package com.lingsir.iblelib.read;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import com.lingsir.iblelib.connect.IBleConnectManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 数据读取的管家
 * Created by luoming on 2017/11/29.
 */

public class IBleReadManager {
    private static final String TAG = "IBleReadManager";
    private static IBleReadManager instance;
    private List<IBleReadItem> mReadItemList; //蓝牙数据获取的回调列表

    public static final String MAC = "mac";
    public static final String UUiD = "uuid";
    public static final String CODE = "code";
    public static final String BYTE = "byte";

    public static final String READ_ACTION = "com.lingsir.iblelib.READ";

    public static IBleReadManager getInstance() {
        if (instance == null) {
            synchronized (IBleReadManager.class) {
                if (instance == null) {
                    instance = new IBleReadManager();
                }
            }

        }

        return instance;

    }


    protected IBleReadManager() {
        mReadItemList = new ArrayList<>();
    }

    /**
     * 读取数据
     *
     * @param context
     * @param connectBleItem
     * @param listener
     */
    public void read(Context context, IBleConnectManager.ConnectBleItem connectBleItem, OnIBleReadListener listener) {
        IBleReadItem iBleReadItem = new IBleReadItem(connectBleItem.mac, connectBleItem.uuid, listener);
        mReadItemList.add(iBleReadItem);
//


        //注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(READ_ACTION);
        context.registerReceiver(bleReadBroadcast, intentFilter);
    }

    private BroadcastReceiver bleReadBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mac = intent.getStringExtra(IBleReadManager.MAC);
            String code = intent.getStringExtra(IBleReadManager.CODE);
            UUID uuid = (UUID) intent.getSerializableExtra(IBleReadManager.UUiD);
            byte[] bytes = intent.getByteArrayExtra(IBleReadManager.BYTE);

            Log.i(TAG, "onReceive: " + mac + "  数据是:" + new String(bytes));
//            Toast.makeText(context, "蓝牙数据是：" + new String(bytes), Toast.LENGTH_SHORT).show();

            for (IBleReadItem bleReadItem : mReadItemList) {
                if (bleReadItem.mac.equals(mac) && bleReadItem.uuid.equals(uuid)) {
                    bleReadItem.onIBleReadListener.onRead(code, bytes);
                }
            }
        }
    };

    /**
     * 停止数据读取
     *
     * @param context
     */
    public void stopRead(Context context, String mac, UUID uuid) {
        //取消广播接收
        try {
            if (context != null)
                context.unregisterReceiver(bleReadBroadcast);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //从读取数据的列表移除
        if (mReadItemList == null || mReadItemList.size() == 0)
            return;

        List<IBleReadItem> removeList = new ArrayList<>();
        for (IBleReadItem connectBleItem : mReadItemList) {
            if (connectBleItem.mac.equals(mac) && connectBleItem.uuid.equals(uuid)) {
                removeList.add(connectBleItem);
            }
        }
        mReadItemList.remove(removeList);
    }

    /**
     * 用来保存数据读取的回调
     */
    public class IBleReadItem {
        public String mac;
        public UUID uuid;
        OnIBleReadListener onIBleReadListener;

        public IBleReadItem(String mac, UUID uuid, OnIBleReadListener onIBleReadListener) {
            this.mac = mac;
            this.uuid = uuid;
            this.onIBleReadListener = onIBleReadListener;
        }
    }
}
