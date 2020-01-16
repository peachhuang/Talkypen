package com.example.talkypen.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.talkypen.R;
import com.example.talkypen.entity.Cloudcontent;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 云内容列表的adapter
 */
public class CloudAdapter extends RecyclerView.Adapter<CloudAdapter.ViewHolder> {

    private List<Cloudcontent> list;

    public CloudAdapter(List<Cloudcontent> list){
        this.list = list;

    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView mTvCloudName,mTvCloudVersion,mTvCloudOnlineflag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvCloudName = itemView.findViewById(R.id.tv_cloud_name);
            mTvCloudVersion = itemView.findViewById(R.id.tv_cloud_version);
            mTvCloudOnlineflag = itemView.findViewById(R.id.tv_cloud_onlineflag);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cloud_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cloudcontent cloudcontent = list.get(position);
        holder.mTvCloudName.setText(cloudcontent.getName());
        holder.mTvCloudVersion.setText(cloudcontent.getVersion());
        int state = cloudcontent.getOnline_flag();
        if ( state == 1){
            holder.mTvCloudOnlineflag.setText("云点读");
        } else if (state == 0){
            holder.mTvCloudOnlineflag.setText("");
        }

    }



    @Override
    public int getItemCount() {
        return list.size();
    }
}
