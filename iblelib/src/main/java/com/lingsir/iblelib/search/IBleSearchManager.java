package com.lingsir.iblelib.search;

import android.content.Context;

import com.lingsir.iblelib.info.IBleInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 蓝牙搜索的模块
 * Created by luoming on 2017/11/27.
 */

public class IBleSearchManager {
    private Context mContext; //
    private OnIBleSearchListener mListener;
    private List<BaseSearchModule> mSearchModuleList; //搜索蓝牙服务列表
    private ScheduledExecutorService mSearchES; //搜索服务线程池
    private int mSearchIndex = 0; //正在执行第几个搜索模块

    public IBleSearchManager() {
        mSearchModuleList = new ArrayList<>();
        mSearchES = Executors.newSingleThreadScheduledExecutor();
    }

    public void setOnIBleSearchListener(OnIBleSearchListener listener) {
        this.mListener = listener;
    }

    /**
     * 添加搜索模块
     *
     * @param baseSearchModule
     * @return
     */
    public IBleSearchManager addSearchModule(BaseSearchModule baseSearchModule) {
        mSearchModuleList.add(baseSearchModule);

        return this;
    }

    /**
     * 开始搜索
     */
    public void startSearch(Context activity) {
        mContext = activity;

        if (mSearchModuleList == null || mSearchModuleList.size() == 10)
            return;

        mSearchModuleList.get(0).startSearch(mContext, onBaseSearchListener);
    }

    //
    private BaseSearchModule.OnBaseSearchListener onBaseSearchListener =
            new BaseSearchModule.OnBaseSearchListener() {
                @Override
                public void onSearched(IBleInfo bleInfo) {
                    if (mListener != null)
                        mListener.onSearched(bleInfo);
                }

                @Override
                public void onComplete() {
                    mSearchIndex++;

                    nextSearchModule();
                }
            };

    /**
     * 执行下一个搜索模块
     */
    private void nextSearchModule() {
        if (mSearchModuleList == null || mSearchModuleList.size() == 0)
            return;

        if (mSearchIndex < mSearchModuleList.size())
            mSearchModuleList.get(mSearchIndex).startSearch(mContext, onBaseSearchListener);
    }

    /**
     * 停止搜索
     */
    public void stopSearch() {
        for (BaseSearchModule baseSearchModule : mSearchModuleList) {
            baseSearchModule.stopSearch();
        }

        mSearchModuleList.clear();
    }

    public interface OnIBleSearchListener {
        void onSearched(IBleInfo bleInfo);
    }

}
