package com.example.talkypen.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.talkypen.R;
import com.example.talkypen.entity.TalkypenDevice;
import com.example.talkypen.ui.activity.DeviceDesActivity;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 主界面设备列表的adapter
 */
public class DeciceAdapter extends RecyclerView.Adapter<DeciceAdapter.ViewHolder> {

    private List<TalkypenDevice> mDeviceList;
    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView mTvName,mTvDes,mTvVersion,mTvStatus,mTvUsedsize,mTvTotalsize;
        //TextView mTvDes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvName = itemView.findViewById(R.id.tv_name);
            mTvStatus = itemView.findViewById(R.id.tv_status);

        }
    }

    public DeciceAdapter(List<TalkypenDevice> deviceList,Context context){
        mDeviceList = deviceList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TalkypenDevice talkypenDevice = mDeviceList.get(position);
        holder.mTvName.setText("设备id:"+talkypenDevice.getDid());
        holder.mTvStatus.setText("设备状态"+talkypenDevice.getStatus());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DeviceDesActivity.class);
                intent.putExtra("devicedid",talkypenDevice.getDid());
                intent.putExtra("devicemac",talkypenDevice.getDid());
                intent.putExtra("version",talkypenDevice.getVersion());
                intent.putExtra("status",talkypenDevice.getStatus());
                intent.putExtra("usedsize",talkypenDevice.getUsed_size());
                intent.putExtra("totalsize",talkypenDevice.getTotal_size());
                mContext.startActivity(intent);
            }
        });

    }


    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }
}
