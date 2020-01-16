package com.example.talkypen.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttPublishRequest;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttSubscribeRequest;
import com.aliyun.alink.linksdk.cmp.core.base.AMessage;
import com.aliyun.alink.linksdk.cmp.core.base.ARequest;
import com.aliyun.alink.linksdk.cmp.core.base.AResponse;
import com.aliyun.alink.linksdk.cmp.core.base.ConnectState;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectNotifyListener;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSendListener;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSubscribeListener;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectUnscribeListener;
import com.aliyun.alink.linksdk.tools.AError;
import com.example.talkypen.R;
import com.example.talkypen.entity.Cloud;
import com.example.talkypen.entity.Load;
import com.example.talkypen.framework.ui.BaseActivity;
import com.example.talkypen.framework.utils.LogUtils;
import com.example.talkypen.ui.adapter.LoadAdapter;
import com.example.talkypen.ui.adapter.TalkyCloudAdapter;
import com.example.talkypen.widget.TopView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;

public class LocalActivity extends BaseActivity {

    @BindView(R.id.topView)
    TopView topView;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.recycler_view_load)
    RecyclerView recyclerViewLoad;

    private List<Cloud> cloudList = new ArrayList<>();
    private List<Cloud> list = new ArrayList<Cloud>();
    private List<Load> loadList = new ArrayList<>();
    private List<Load> listLoad = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        publish(id);
        publishLoad(id);

        LitePal.deleteAll(Cloud.class);
        LitePal.deleteAll(Load.class);

        topView.setTitle("本地内容");

//        LinkKit.getInstance().registerOnPushListener(onPushListenerLoad);
//        LinkKit.getInstance().registerOnPushListener(onPushListener);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_local;
    }

    @Override
    protected void onStart() {
        super.onStart();
//        LinkKit.getInstance().registerOnPushListener(onPushListenerLoad);
//        LinkKit.getInstance().registerOnPushListener(onPushListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LinkKit.getInstance().registerOnPushListener(onPushListenerLoad);
        LinkKit.getInstance().registerOnPushListener(onPushListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        LinkKit.getInstance().unRegisterOnPushListener(onPushListenerLoad);
        LinkKit.getInstance().unRegisterOnPushListener(onPushListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

//        LinkKit.getInstance().unRegisterOnPushListener(onPushListenerLoad);
//        LinkKit.getInstance().unRegisterOnPushListener(onPushListener);
        //LitePal.deleteAll(Cloud.class);
    }

    private void initRecycleView(){
        cloudList = LitePal.findAll(Cloud.class);
        list.clear();
        for (Cloud cloud: cloudList){
            list.add(cloud);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        TalkyCloudAdapter adapter = new TalkyCloudAdapter(list, context);
        recyclerView.setAdapter(adapter);

    }

    private void initRecycleViewLoad(){
        loadList = LitePal.findAll(Load.class);
        listLoad.clear();
        for (Load load: loadList){
            listLoad.add(load);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerViewLoad.setLayoutManager(layoutManager);
        LoadAdapter adapter = new LoadAdapter(listLoad);
        recyclerViewLoad.setAdapter(adapter);
    }

    private void publish(String id){
        //String device = "did:";
        String data = "";

        JSONObject root = new JSONObject();

        try {
            root.put("action","getResourceList");
            JSONObject dataJson = new JSONObject();
            dataJson.put("did",id);
            root.put("data", dataJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LogUtils.d("Huz getResourceList json "+root);
        String bindData = root.toString();

        String topic = "/a1DYcm0MIqh/huzhao/user/appmsg";
        MqttPublishRequest request = new MqttPublishRequest();
        request.isRPC = false;
        request.topic = topic;
        request.qos = 0;
        request.payloadObj = bindData;
        LinkKit.getInstance().publish(request, new IConnectSendListener() {
            @Override
            public void onResponse(ARequest aRequest, AResponse aResponse) {
                LogUtils.d("Huz getResourceList 发送成功");
                subscribe();

                //receive();
            }

            @Override
            public void onFailure(ARequest aRequest, AError aError) {
                LogUtils.d("Huz getResourceList 失败"+aError.getCode());
                LogUtils.d("Huz getResourceList 发送失败");
            }
        });
    }

    private void publishLoad(String id){
        JSONObject root = new JSONObject();

        try {
            root.put("action","reportDownloadInfo");
            JSONObject dataJson = new JSONObject();
            dataJson.put("did",id);
            root.put("data", dataJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LogUtils.d("Huz reportDownloadInfo json "+root);
        String bindData = root.toString();

        String topic = "/a1DYcm0MIqh/huzhao/user/appmsg";
        MqttPublishRequest request = new MqttPublishRequest();
        request.isRPC = false;
        request.topic = topic;
        request.qos = 0;
        request.payloadObj = bindData;
        LinkKit.getInstance().publish(request, new IConnectSendListener() {
            @Override
            public void onResponse(ARequest aRequest, AResponse aResponse) {
                LogUtils.d("Huz reportDownloadInfo 发送成功");
                subscribeLoad();

                //receive();
            }

            @Override
            public void onFailure(ARequest aRequest, AError aError) {
                LogUtils.d("Huz reportDownloadInfo 失败"+aError.getCode());
            }
        });
    }

    public void subscribeLoad(){
        MqttSubscribeRequest subscribeRequest = new MqttSubscribeRequest();
        subscribeRequest.topic = "a1DYcm0MIqh/huzhao/user/cloudmsg";
        subscribeRequest.isSubscribe = true;
        LinkKit.getInstance().subscribe(subscribeRequest, new IConnectSubscribeListener() {
            @Override
            public void onSuccess() {
                // 订阅成功
                LogUtils.d("Huz reportDownloadInfo 订阅成功");
                //LinkKit.getInstance().registerOnPushListener(onPushListenerLoad);
                //receive();
            }
            @Override
            public void onFailure(AError aError) {
                // 订阅失败
            }
        });
    }

    // 下行数据监听
    IConnectNotifyListener onPushListenerLoad = new IConnectNotifyListener() {
        @Override
        public void onNotify(String connectId, String topic, AMessage aMessage) {
            // 下行数据通知
            //LinkKit.getInstance().unRegisterOnPushListener(onPushListenerLoad);
            LogUtils.d("Huz reportDownloadInfo 收到下行数据 "+aMessage);
            LogUtils.d("Huz reportDownloadInfo topic "+topic);
            String pushData = new String((byte[]) aMessage.data);
            unSubscribe();
            LogUtils.d("Huz reportDownloadInfo pushdata "+pushData);
            try {
                JSONObject jsonObject = new JSONObject(pushData);
                String loadData = jsonObject.getString("data");
                LogUtils.d("Huz reportDownloadInfo data "+loadData);
                JSONArray jsonArray = new JSONArray(loadData);
                for (int i = 0; i < jsonArray.length(); i++){
                    JSONObject loadjson = jsonArray.getJSONObject(i);
                    Load load = new Load();
                    load.setDid(loadjson.getString("did"));
                    load.setName(loadjson.getString("name"));
                    load.setState(loadjson.getString("state"));
                    load.setCurrent_size(loadjson.getString("current_size"));
                    load.setTotal_size(loadjson.getString("total_size"));
                    load.save();
                    LogUtils.d("Huz Litepal save load "+load.isSaved());
                    if (load.isSaved() == true){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initRecycleViewLoad();
                            }
                        });
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //JSONObject jsonObject = JSONObject.p(pushData);
        }
        @Override
        public boolean shouldHandle(String connectId, String topic) {
            return true; // 是否需要处理 该 topic
        }
        @Override
        public void onConnectStateChange(String connectId, ConnectState connectState) {
            // 连接状态变化
        }
    };

    public void subscribe(){
        MqttSubscribeRequest subscribeRequest = new MqttSubscribeRequest();
        subscribeRequest.topic = "a1DYcm0MIqh/huzhao/user/cloudmsg";
        subscribeRequest.isSubscribe = true;
        LinkKit.getInstance().subscribe(subscribeRequest, new IConnectSubscribeListener() {
            @Override
            public void onSuccess() {
                // 订阅成功
                LogUtils.d("Huz getResourceList 订阅成功");
                //LinkKit.getInstance().registerOnPushListener(onPushListener);
                //receive();
            }
            @Override
            public void onFailure(AError aError) {
                // 订阅失败
            }
        });
    }

    // 下行数据监听
    IConnectNotifyListener onPushListener = new IConnectNotifyListener() {
        @Override
        public void onNotify(String connectId, String topic, AMessage aMessage) {
            // 下行数据通知
            //LinkKit.getInstance().unRegisterOnPushListener(onPushListener);
            LogUtils.d("Huz getResourceList 收到下行数据 "+aMessage);
            LogUtils.d("Huz getResourceList topic "+topic);
            String pushData = new String((byte[]) aMessage.data);
            unSubscribe();
            LogUtils.d("Huz getResourceList pushdata "+pushData);
            try {
                JSONObject jsonObject = new JSONObject(pushData);
                String userinfo = jsonObject.getString("data");
                LogUtils.d("Huz cloud userinfo data "+userinfo);
                JSONObject jsonObject1 = new JSONObject(userinfo);
                String list = jsonObject1.getString("list");
                LogUtils.d("Huz cloud list "+list);
                JSONArray jsonArray = new JSONArray(list);
                for (int i = 0; i < jsonArray.length(); i++){
                    JSONObject cloudjson = jsonArray.getJSONObject(i);
                    Cloud cloud = new Cloud();
                    cloud.setName(cloudjson.getString("name"));
                    cloud.setVersion(cloudjson.getString("version"));
                    cloud.save();
                    LogUtils.d("Huz Litepal save cloud "+cloud.isSaved());
                    if (cloud.isSaved() == true){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initRecycleView();
                            }
                        });
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //JSONObject jsonObject = JSONObject.p(pushData);
        }
        @Override
        public boolean shouldHandle(String connectId, String topic) {
            return true; // 是否需要处理 该 topic
        }
        @Override
        public void onConnectStateChange(String connectId, ConnectState connectState) {
            // 连接状态变化
        }
    };

    private void unSubscribe(){
        MqttSubscribeRequest unsubRequest = new MqttSubscribeRequest();
        unsubRequest.topic = "a1DYcm0MIqh/huzha0/user/cloudmsg";
        unsubRequest.isSubscribe = false;
        LinkKit.getInstance().unsubscribe(unsubRequest, new IConnectUnscribeListener() {
            @Override
            public void onSuccess() {
                LogUtils.d("Huz local unSubscribe");
                // 取消订阅成功
            }
            @Override
            public void onFailure(AError aError) {
                // 取消订阅失败
            }
        });
    }
}
