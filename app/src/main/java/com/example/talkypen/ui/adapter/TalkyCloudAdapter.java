package com.example.talkypen.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.talkypen.R;
import com.example.talkypen.entity.Cloud;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 本地内容列表recyclerView的适配器
 */
public class TalkyCloudAdapter extends RecyclerView.Adapter<TalkyCloudAdapter.ViewHolder> {

    private List<Cloud> list;

    private Context context;

    public TalkyCloudAdapter(List<Cloud> list, Context context){
        this.list = list;
        this.context = context;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView mTvLocalName,mTvLocalVersion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvLocalName = itemView.findViewById(R.id.tv_local_name);
            mTvLocalVersion = itemView.findViewById(R.id.tv_local_version);
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.local_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cloud cloud = list.get(position);
        holder.mTvLocalName.setText(cloud.getName());
        holder.mTvLocalVersion.setText(cloud.getVersion());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
