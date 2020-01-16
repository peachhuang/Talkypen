package com.example.talkypen.ui.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.View;
import android.widget.ImageView;

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
import com.example.talkypen.entity.TalkypenDevice;
import com.example.talkypen.framework.ui.BaseFragment;
import com.example.talkypen.framework.utils.LogUtils;
import com.example.talkypen.ui.activity.BlufiActivity;
import com.example.talkypen.ui.activity.PromptActivity;
import com.example.talkypen.ui.adapter.DeciceAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.OnClick;

public class HomeFragment extends BaseFragment {

    @BindView(R.id.add_equipment)
    ImageView addEquipment;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;

    //private DeciceAdapter deciceAdapter;
    private List<TalkypenDevice> deviceList = new ArrayList<>();
    private List<TalkypenDevice> deviceList2 = new ArrayList<>();
    private List<String> dataList = new ArrayList<>();
    //private SwipeRefreshLayout mRefreshLayout;
    //private List<Device> deviceList;
    //private List<String> dataList = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LitePal.deleteAll(TalkypenDevice.class);

        //mRefreshLayout = getActivity().findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LitePal.deleteAll(TalkypenDevice.class);
                publish();
            }
        });

        //subscribe();
        publish();
        //initRecycleViewDate();
//        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
//        recyclerView.setLayoutManager(layoutManager);
//        DeciceAdapter adapter = new DeciceAdapter(deviceList2,context);
//        recyclerView.setAdapter(adapter);

    }

    @OnClick({R.id.add_equipment})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.add_equipment:
                skipPage(BlufiActivity.class);
                //skipPage(PromptActivity.class);
                break;
//            case R.id.refresh_layout:
//                publish();
//                break;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        LinkKit.getInstance().registerOnPushListener(onPushListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        //LitePal.deleteAll(TalkypenDevice.class);
    }

    @Override
    public void onStop() {
        super.onStop();
        LinkKit.getInstance().unRegisterOnPushListener(onPushListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //LitePal.deleteAll(TalkypenDevice.class);
    }

    private void initRecycleViewDate() {
        LogUtils.d("Huz initRecycleViewDate");
        deviceList = LitePal.findAll(TalkypenDevice.class);
        deviceList2.clear();
        for (TalkypenDevice talkypenDevice: deviceList){
            deviceList2.add(talkypenDevice);
            LogUtils.d("Huz Device "+talkypenDevice.getMac());
            LogUtils.d("Huz Device "+deviceList2);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        DeciceAdapter adapter = new DeciceAdapter(deviceList2,context);
        recyclerView.setAdapter(adapter);
        refreshLayout.setRefreshing(false);
    }

    private void publish(){
        String action = "getDeviceDetailList";
        String data = "";
        String json = "";
//        JSONObject jsonParam = new JSONObject();
//        try {
//            jsonParam.put("action", action);
//            jsonParam.put("data", data);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        String DevicelistData = jsonParam.toString();
        try {
            json = getJson(action,data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String topic = "/a1DYcm0MIqh/huzhao/user/appmsg";
        MqttPublishRequest request = new MqttPublishRequest();
        request.isRPC = false;
        request.topic = topic;
        request.qos = 0;
        request.payloadObj = json;
        LinkKit.getInstance().publish(request, new IConnectSendListener() {
            @Override
            public void onResponse(ARequest aRequest, AResponse aResponse) {
                LogUtils.d("Huz 发送成功");
                subscribe();

                //receive();
            }

            @Override
            public void onFailure(ARequest aRequest, AError aError) {
                LogUtils.d("Huz 失败"+aError);
                LogUtils.d("Huz 发送失败");
            }
        });
    }

    public void subscribe(){
        MqttSubscribeRequest subscribeRequest = new MqttSubscribeRequest();
        subscribeRequest.topic = "a1DYcm0MIqh/huzhao/user/cloudmsg";
        subscribeRequest.isSubscribe = true;
        LinkKit.getInstance().subscribe(subscribeRequest, new IConnectSubscribeListener() {
            @Override
            public void onSuccess() {
                // 订阅成功
                LogUtils.d("Huz 订阅成功");
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
            unSubscribe();
            LogUtils.d("Huz 收到下行数据 "+aMessage);
            LogUtils.d("Huz topic "+topic);
            String pushData = new String((byte[]) aMessage.data);
            LogUtils.d("Huz pushdata "+pushData);
            try {
                JSONObject jsonObject = new JSONObject(pushData);
                LogUtils.d("Huz receive json "+jsonObject);
                String userinfo = jsonObject.getString("data");
                LogUtils.d("Huz userinfo data "+userinfo);
                JSONArray jsonArray = new JSONArray(userinfo);
                for (int m = 0; m < jsonArray.length(); m++ ){
                    JSONObject jsonObject1 = jsonArray.getJSONObject(m);
                    if (jsonObject1 != null){

                        LogUtils.d("Huz receive data"+jsonObject1);
                        LogUtils.d("Huz receive did "+jsonObject1.getString("did"));
                        TalkypenDevice talkypenDevice = new TalkypenDevice();
                        talkypenDevice.setDid(jsonObject1.getString("did"));
                        talkypenDevice.setMac(jsonObject1.getString("mac"));
                        talkypenDevice.setVersion(jsonObject1.getString("version"));
                        talkypenDevice.setStatus(jsonObject1.getString("status"));
                        talkypenDevice.setUsed_size(jsonObject1.getString("used_size"));
                        talkypenDevice.setTotal_size(jsonObject1.getString("total_size"));
                        talkypenDevice.save();
                        if (talkypenDevice.isSaved() == true){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    initRecycleViewDate();
                                }
                            });
                            //initRecycleViewDate();
                        }
                        //initRecycleViewDate();
                        LogUtils.d("Huz Litepal save two "+talkypenDevice.isSaved());
                    }
                }
                //JSONObject jsonObject1 = new JSONObject(userinfo);
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

    public String getJson(String action, String data)throws Exception {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("action", action);
        jsonParam.put("data", data);
        return jsonParam.toString();
    }

    private void unSubscribe(){
        MqttSubscribeRequest unsubRequest = new MqttSubscribeRequest();
// unSubTopic 替换成用户自己需要取消订阅的 topic
        unsubRequest.topic = "a1DYcm0MIqh/huzhao/user/cloudmsg";
        unsubRequest.isSubscribe = false;
        LinkKit.getInstance().unsubscribe(unsubRequest, new IConnectUnscribeListener() {
            @Override
            public void onSuccess() {
                // 取消订阅成功
            }
            @Override
            public void onFailure(AError aError) {
                // 取消订阅失败
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //unSubscribe();
    }
}
