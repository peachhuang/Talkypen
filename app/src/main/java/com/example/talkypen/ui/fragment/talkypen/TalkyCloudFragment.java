package com.example.talkypen.ui.fragment.talkypen;

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
import com.example.talkypen.entity.TalkypenDevice;
import com.example.talkypen.framework.ui.BaseFragment;
import com.example.talkypen.framework.utils.LogUtils;
import com.example.talkypen.ui.adapter.TalkyCloudAdapter;
import com.example.talkypen.widget.TopView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;

public class TalkyCloudFragment extends BaseFragment {

    @BindView(R.id.topView)
    TopView topView;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private List<Cloud> cloudList = new ArrayList<>();
    private List<Cloud> list = new ArrayList<Cloud>();

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_talky_cloud;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LitePal.deleteAll(Cloud.class);

        Bundle bundle = getArguments();
        String id = bundle.getString("id");
        publish(id);

        topView.setTitle("云内容");
    }

    private void initRecycleView(){
        cloudList = LitePal.findAll(Cloud.class);
        for (Cloud cloud: cloudList){
            list.add(cloud);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        TalkyCloudAdapter adapter = new TalkyCloudAdapter(list, context);
        recyclerView.setAdapter(adapter);

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

    public void subscribe(){
        MqttSubscribeRequest subscribeRequest = new MqttSubscribeRequest();
        subscribeRequest.topic = "a1DYcm0MIqh/huzhao/user/cloudmsg";
        subscribeRequest.isSubscribe = true;
        LinkKit.getInstance().subscribe(subscribeRequest, new IConnectSubscribeListener() {
            @Override
            public void onSuccess() {
                // 订阅成功
                LogUtils.d("Huz getResourceList 订阅成功");
                LinkKit.getInstance().registerOnPushListener(onPushListener);
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
            LinkKit.getInstance().unRegisterOnPushListener(onPushListener);
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
                        initRecycleView();
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
// unSubTopic 替换成用户自己需要取消订阅的 topic
        unsubRequest.topic = "a1DYcm0MIqh/huzha0/user/cloudmsg";
        unsubRequest.isSubscribe = false;
        LinkKit.getInstance().unsubscribe(unsubRequest, new IConnectUnscribeListener() {
            @Override
            public void onSuccess() {
                LogUtils.d("Huz bing unSubscribe");
                // 取消订阅成功
            }
            @Override
            public void onFailure(AError aError) {
                // 取消订阅失败
            }
        });
    }
}
