package com.lingsir.bledemo;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.lingsir.iblelib.connect.IBleConnectManager;

import java.util.List;

/**
 * Created by luoming on 2017/12/28.
 */

public class BleAdapter extends RecyclerView.Adapter<BleAdapter.ViewHold> {
    private List<BleInfo> list;
    private OnBleConnectClickListener mListener;

    public BleAdapter(List<BleInfo> list) {
        this.list = list;
    }

    public void setOnBleConnectClickListener(OnBleConnectClickListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public ViewHold onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.ble_item, null);
        return new ViewHold(view);
    }

    @Override
    public void onBindViewHolder(ViewHold holder, final int position) {
        final BleInfo info = list.get(position);

        holder.nameTv.setText(info.name);
        holder.macTv.setText(info.mac);

        holder.connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null)
                    mListener.onClick(info, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class ViewHold extends RecyclerView.ViewHolder {
        protected TextView macTv;
        protected TextView nameTv;
        protected Button connectBtn;

        public ViewHold(View itemView) {
            super(itemView);

            macTv = (TextView) itemView.findViewById(R.id.mac);
            nameTv = (TextView) itemView.findViewById(R.id.name);
            connectBtn = (Button) itemView.findViewById(R.id.connect);
        }
    }

    public static class BleInfo {
        public String mac = "";
        public String name = "";

        public BleInfo() {
        }

        public BleInfo(String mac, String name) {
            this.mac = mac;
            this.name = name;
        }
    }

    public interface OnBleConnectClickListener {
        void onClick(BleInfo info, int position);
    }
}
