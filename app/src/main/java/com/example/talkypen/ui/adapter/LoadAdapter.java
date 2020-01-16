package com.example.talkypen.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.talkypen.R;
import com.example.talkypen.entity.Load;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 本地内容界面下载进度列表的adapter
 */
public class LoadAdapter extends RecyclerView.Adapter<LoadAdapter.ViewHolder> {

    private List<Load> loadList;

    public LoadAdapter(List<Load> loadList){
        this.loadList = loadList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView mTvDid,mTvResouceName,mTvLoadState,mTvCurrent,mTvTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvDid = itemView.findViewById(R.id.tv_did);
            mTvResouceName = itemView.findViewById(R.id.tv_resouce_name);
            mTvLoadState = itemView.findViewById(R.id.tv_load_state);
            mTvCurrent = itemView.findViewById(R.id.tv_current);
            mTvTotal = itemView.findViewById(R.id.tv_total);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Load load = loadList.get(position);
        holder.mTvDid.setText(load.getDid());
        holder.mTvResouceName.setText(load.getName());
        holder.mTvLoadState.setText(load.getState());
        holder.mTvCurrent.setText(load.getCurrent_size());
        holder.mTvTotal.setText(load.getTotal_size());

    }

    @Override
    public int getItemCount() {
        return loadList.size();
    }
}
