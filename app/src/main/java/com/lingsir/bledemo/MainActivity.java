package com.lingsir.bledemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lingsir.iblelib.IBleManager;
import com.lingsir.iblelib.UUIDUtil;
import com.lingsir.iblelib.connect.IBleConnectManager;
import com.lingsir.iblelib.info.IBleInfo;
import com.lingsir.iblelib.read.OnIBleReadListener;
import com.lingsir.iblelib.search.IBleSearchManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private BleAdapter mAdapter; //蓝牙列表适配器
    private List<BleAdapter.BleInfo> mList; //蓝牙列表数据
    private RecyclerView mBleRv; //蓝牙列表显示的rv

    private TextView mConnectTv; //连接的蓝牙显示tv
    private TextView mDataTv; //数据显示tv

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        initEvent();
    }

    /**
     * 检查是否有定位权限
     *
     * @return
     */
    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED;
    }

    private void initView() {
        mBleRv = (RecyclerView) findViewById(R.id.recyclerView);

        initRv();

        mConnectTv = (TextView) findViewById(R.id.connectTv);
        mDataTv = (TextView) findViewById(R.id.dataTv);
    }

    private void initRv() {
        mList = new ArrayList<>();
        mAdapter = new BleAdapter(mList);

        mAdapter.setOnBleConnectClickListener(new BleAdapter.OnBleConnectClickListener() {
            @Override
            public void onClick(BleAdapter.BleInfo info, int position) {
                connectBle(info);
            }
        });
        mBleRv.setAdapter(mAdapter);
        mBleRv.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initEvent() {
        findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    mList.clear();
                    mAdapter.notifyDataSetChanged();
                    searchBle();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0x12);
                }
            }
        });

    }

    /**
     * 搜索蓝牙
     */
    private void searchBle() {
        IBleManager.getInstance().startSearch(this, new IBleSearchManager.OnIBleSearchListener() {
            @Override
            public void onSearched(IBleInfo bleInfo) {
                //不添加已经添加的蓝牙设备到列表
                for (BleAdapter.BleInfo info : mList) {
                    if (info.mac.equals(bleInfo.getMac()))
                        return;
                }

                Log.i(TAG, "onSearched: " + bleInfo);
                String name = "NULL";
                if (!TextUtils.isEmpty(bleInfo.getName()))
                    name = bleInfo.getName();
                mList.add(new BleAdapter.BleInfo(bleInfo.getMac(), name));

                mAdapter.notifyItemInserted(mList.size() - 1);
            }
        });
    }

    /**
     * 连接蓝牙
     *
     * @param info
     */
    private void connectBle(BleAdapter.BleInfo info) {
        showToast("开始连接");
        IBleManager.getInstance().connect(this, info.mac, UUIDUtil.SerialPortServiceClass_UUID,
                new IBleConnectManager.OnBleConnectListener() {
                    @Override
                    public void noDevice(String mac) {
                        showToast("该设备不存在");
                    }

                    @Override
                    public void onSucceed(String mac) {
                        showToast("连接成功");
                        for (final BleAdapter.BleInfo info : mList) {
                            if (info.mac.equals(mac)) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mConnectTv.setText("已连接：" + info.name);
                                    }
                                });

                                readData(mac);
                                return;
                            }
                        }
                    }

                    @Override
                    public void onFailed(String mac) {
                        showToast("连接失败");
                    }
                });
    }

    /**
     * 读取数据
     *
     * @param mac
     */
    private void readData(String mac) {
        IBleManager.getInstance().read(this, mac, UUIDUtil.SerialPortServiceClass_UUID, new OnIBleReadListener() {
            @Override
            public void onRead(String code, final byte[] bytes) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDataTv.setText("读取到了数据:" + new String(bytes));
                    }
                });
            }

            @Override
            public void disConnect(String mac) {
                showToast("设备未连接");
            }
        });
    }

    private void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
