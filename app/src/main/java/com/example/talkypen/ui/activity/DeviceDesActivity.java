package com.example.talkypen.ui.activity;

import butterknife.BindView;
import butterknife.OnClick;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.example.talkypen.framework.ui.BaseActivity;
import com.example.talkypen.framework.utils.LogUtils;
import com.example.talkypen.widget.TopView;

import org.json.JSONException;
import org.json.JSONObject;

public class DeviceDesActivity extends BaseActivity {

    @BindView(R.id.topView)
    TopView topView;

    @BindView(R.id.tv_device)
    TextView mTvDevice;
    @BindView(R.id.tv_status)
    TextView mTvStatus;
    @BindView(R.id.tv_mac)
    TextView mTvMac;
    @BindView(R.id.tv_version)
    TextView mTvVersion;
    @BindView(R.id.tv_usedsize)
    TextView mTvUsedsize;
    @BindView(R.id.tv_totalsize)
    TextView mTvTotalsize;

    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String id = intent.getStringExtra("devicedid");
        LogUtils.d("Huz DeviceDes id "+id);
        String mac = intent.getStringExtra("devicemac");
        String version = intent.getStringExtra("version");
        String status = intent.getStringExtra("status");
        String usedsize = intent.getStringExtra("usedsize");
        String totalsize = intent.getStringExtra("totalsize");
        this.id = id;
        topView.setTitle(id);
        mTvDevice.setText(id);
        mTvStatus.setText(status);
        mTvMac.setText(mac);
        mTvVersion.setText(version);
        mTvUsedsize.setText(usedsize+"kb");
        mTvTotalsize.setText(totalsize+"kb");


    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_device_des;
    }

    @OnClick({R.id.bt_local,R.id.bt_unbind})
    public void onclick(View view){
        switch (view.getId()){
            case R.id.bt_local:
                Intent intent = new Intent(DeviceDesActivity.this,LocalActivity.class);
                intent.putExtra("id",id);
                startActivity(intent);
                break;
            case R.id.bt_unbind:
                publish();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //LinkKit.getInstance().registerOnPushListener(onPushListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //LinkKit.getInstance().unRegisterOnPushListener(onPushListener);
    }

    private void publish(){
        String id = mTvDevice.getText().toString();

        JSONObject root = new JSONObject();

        try {
            root.put("action","unbind");
            JSONObject dataJson = new JSONObject();
            dataJson.put("did",id);
            root.put("data", dataJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LogUtils.d("Huz unbind json "+root);
        String bindData = root.toString();

        String topic = "/a1DYcm0MIqh/huzhao/user/appmsg";
        //String publishData ="";
//        MqttPublishRequest request = new MqttPublishRequest();
        MqttPublishRequest request = new MqttPublishRequest();
        request.isRPC = false;
        request.topic = topic;
        request.qos = 0;
        request.payloadObj = bindData;
        LinkKit.getInstance().publish(request, new IConnectSendListener() {
            @Override
            public void onResponse(ARequest aRequest, AResponse aResponse) {
                LogUtils.d("Huz unbind 发送成功");
                subscribe();

                //receive();
            }

            @Override
            public void onFailure(ARequest aRequest, AError aError) {
                LogUtils.d("Huz unbind 失败"+aError.getCode());
                LogUtils.d("Huz unbind 发送失败");
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
            LogUtils.d("Huz unbind 收到下行数据 "+aMessage);
            LogUtils.d("Huz unbind topic "+topic);
            String pushData = new String((byte[]) aMessage.data);
            LogUtils.d("Huz unbind pushdata "+pushData);
            unSubscribe();
            skipPage(MainActivity.class);
            try {
                JSONObject jsonObject = new JSONObject(pushData);
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
        unsubRequest.topic = "a1DYcm0MIqh/huzhao/user/cloudmsg";
        unsubRequest.isSubscribe = false;
        LinkKit.getInstance().unsubscribe(unsubRequest, new IConnectUnscribeListener() {
            @Override
            public void onSuccess() {
                LogUtils.d("Huz unbind unSubscribe");
                // 取消订阅成功
            }
            @Override
            public void onFailure(AError aError) {
                // 取消订阅失败
            }
        });
    }


}
