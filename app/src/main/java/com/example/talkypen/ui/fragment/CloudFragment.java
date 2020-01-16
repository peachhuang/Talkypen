package com.example.talkypen.ui.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.example.talkypen.R;
import com.example.talkypen.entity.Cloudcontent;
import com.example.talkypen.framework.ui.BaseFragment;
import com.example.talkypen.framework.utils.LogUtils;
import com.example.talkypen.net.HttpUtil;
import com.example.talkypen.ui.adapter.CloudAdapter;
import com.example.talkypen.widget.TopView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class CloudFragment extends BaseFragment {

    private List<Cloudcontent> list = new ArrayList<>();
    private List<Cloudcontent> cloudlist = new ArrayList<>();

    @BindView(R.id.topView)
    TopView topView;
    @BindView(R.id.recycler_view_cloud)
    RecyclerView recyclerViewCloud;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;

    private int page;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_cloud;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LitePal.deleteAll(Cloudcontent.class);
        topView.setTitle("云内容");
        topView.setLeftVisb(false);
        request();

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LitePal.deleteAll(Cloudcontent.class);
                list.clear();
                refreshCloud();
            }
        });
    }

    private void refreshCloud(){
//        for (int n=2; n<page; n++){
//
//        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int n=1; n<=page; n++){
                    String address = "https://oid.szxcz.com/api/app/v1/onlineResources?"+"page="+n+"&username=huzhao";
                    LogUtils.d("Huz cloud address "+address);
                    HttpUtil.sendOkHttpRequest(address, new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {

                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            String responseTest = response.body().string();
                            LogUtils.d("Huz cloud response page2"+responseTest);
                            try {
                                JSONObject jsonObject = new JSONObject(responseTest);
                                LogUtils.d("Huz cloud jsonObject page2"+jsonObject);
                                String message = jsonObject.getString("message");
                                LogUtils.d("Huz cloud message page2"+message);
                                String data = jsonObject.getString("data");
                                LogUtils.d("Huz cloud data page2"+data);
                                JSONObject object = new JSONObject(data);
                                int currentPage = object.getInt("current_page");
                                int lastPage = object.getInt("last_page");
                                String next_page_url = object.getString("next_page_url");
                                LogUtils.d("Huz lastPage+next_page_url "+lastPage +next_page_url);
                                String listData = object.getString("data");
                                LogUtils.d("Huz cloud listData "+listData);
                                JSONArray jsonArray = new JSONArray(listData);
                                for (int i = 0; i < jsonArray.length(); i++){
                                    JSONObject cloudResouce = jsonArray.getJSONObject(i);
                                    Cloudcontent cloudcontent = new Cloudcontent();
                                    cloudcontent.setName(cloudResouce.getString("name"));
                                    cloudcontent.setVersion(cloudResouce.getString("version"));
                                    cloudcontent.setOnline_flag(cloudResouce.getInt("online_flag"));
                                    cloudcontent.save();
                                    LogUtils.d("Huz Litepal save cloudcontent "+cloudcontent.isSaved());
                                    if (cloudcontent.isSaved() == true){
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                initRecyclerView();
                                                refreshLayout.setRefreshing(false);
                                                //LitePal.deleteAll(Cloudcontent.class);
                                            }
                                        });
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
//                String address = "https://oid.szxcz.com/api/app/v1/onlineResources?page=2&username=huzhao";
//                HttpUtil.sendOkHttpRequest(address, new Callback() {
//                    @Override
//                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
//
//                    }
//
//                    @Override
//                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                        String responseTest = response.body().string();
//                        LogUtils.d("Huz cloud response page2"+responseTest);
//                        try {
//                            JSONObject jsonObject = new JSONObject(responseTest);
//                            LogUtils.d("Huz cloud jsonObject page2"+jsonObject);
//                            String message = jsonObject.getString("message");
//                            LogUtils.d("Huz cloud message page2"+message);
//                            String data = jsonObject.getString("data");
//                            LogUtils.d("Huz cloud data page2"+data);
//                            JSONObject object = new JSONObject(data);
//                            int currentPage = object.getInt("current_page");
//                            int lastPage = object.getInt("last_page");
//                            String next_page_url = object.getString("next_page_url");
//                            LogUtils.d("Huz lastPage+next_page_url "+lastPage +next_page_url);
//                            String listData = object.getString("data");
//                            LogUtils.d("Huz cloud listData "+listData);
//                            JSONArray jsonArray = new JSONArray(listData);
//                            for (int i = 0; i < jsonArray.length(); i++){
//                                JSONObject cloudResouce = jsonArray.getJSONObject(i);
//                                Cloudcontent cloudcontent = new Cloudcontent();
//                                cloudcontent.setName(cloudResouce.getString("name"));
//                                cloudcontent.setVersion(cloudResouce.getString("version"));
//                                cloudcontent.setOnline_flag(cloudResouce.getInt("online_flag"));
//                                cloudcontent.save();
//                                LogUtils.d("Huz Litepal save cloudcontent "+cloudcontent.isSaved());
//                                if (cloudcontent.isSaved() == true){
//                                    getActivity().runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            initRecyclerView();
//                                            refreshLayout.setRefreshing(false);
//                                        }
//                                    });
//                                }
//                            }
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
            }
        }).start();
    }

    private void initRecyclerView(){
        cloudlist = LitePal.findAll(Cloudcontent.class);
        list.clear();
        for (Cloudcontent cloudcontent:cloudlist){
            list.add(cloudcontent);
            //LogUtils.d("Huz cloudcontent name "+cloudcontent.getName());
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerViewCloud.setLayoutManager(layoutManager);
        CloudAdapter adapter = new CloudAdapter(list);
        recyclerViewCloud.setAdapter(adapter);
    }

    private void request(){
        String address = "http://oid.szxcz.com/api/app/v1/onlineResources?username=huzhao";
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseTest = response.body().string();
                LogUtils.d("Huz cloud response "+responseTest);
                try {
                    JSONObject jsonObject = new JSONObject(responseTest);
                    LogUtils.d("Huz cloud jsonObject "+jsonObject);
                    String message = jsonObject.getString("message");
                    LogUtils.d("Huz cloud message "+message);
                    String data = jsonObject.getString("data");
                    LogUtils.d("Huz cloud data "+data);
                    JSONObject object = new JSONObject(data);
                    int currentPage = object.getInt("current_page");
                    int lastPage = object.getInt("last_page");
                    getpage(lastPage);
                    String next_page_url = object.getString("next_page_url");
                    LogUtils.d("Huz lastPage+next_page_url "+lastPage +next_page_url);
                    String listData = object.getString("data");
                    LogUtils.d("Huz cloud listData "+listData);
                    JSONArray jsonArray = new JSONArray(listData);
                    for (int i = 0; i < jsonArray.length(); i++){
                        JSONObject cloudResouce = jsonArray.getJSONObject(i);
                        Cloudcontent cloudcontent = new Cloudcontent();
                        cloudcontent.setName(cloudResouce.getString("name"));
                        cloudcontent.setVersion(cloudResouce.getString("version"));
                        cloudcontent.setOnline_flag(cloudResouce.getInt("online_flag"));
                        cloudcontent.save();
                        LogUtils.d("Huz Litepal save cloudcontent "+cloudcontent.isSaved());
                        if (cloudcontent.isSaved() == true){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    initRecyclerView();
                                }
                            });
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void getpage(int totalpage){
        this.page = totalpage;
    }
}
