package com.example.talkypen.ui.activity;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import com.aliyun.alink.linkkit.api.ILinkKitConnectListener;
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
import com.example.talkypen.entity.Device;
import com.example.talkypen.framework.ui.BaseActivity;
import com.example.talkypen.framework.utils.CircularLoading;
import com.example.talkypen.framework.utils.InitManager;
import com.example.talkypen.framework.utils.LogUtils;
import com.example.talkypen.widget.TopView;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class ConfigSuccessActivity extends BaseActivity {

    @BindView(R.id.topView)
    TopView topView;
    @BindView(R.id.bt_config_success)
    Button btConfigSuccess;

    private List<Device> deviceList = new ArrayList<>();
    private BluetoothDevice mDevice;
    private Dialog mCircularLoading;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        topView.setTitle("恭喜您、配网成功");

        Bundle bundle = getIntent().getExtras();
        mDevice = bundle.getParcelable("bluDevice");
        String deviceName = mDevice.getName() == null ? getString(R.string.string_unknown) : mDevice.getName();
        //bind();
        publish();
        //mHandler.postDelayed(runnable,5000);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_config_success;
    }

    @OnClick({R.id.bt_config_success})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.bt_config_success:
                skipPage(MainActivity.class);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LinkKit.getInstance().registerOnPushListener(onPushListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LinkKit.getInstance().unRegisterOnPushListener(onPushListener);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            CircularLoading.closeDialog(mCircularLoading);
            finish();
        }
    };

    private void publish(){
        //mCircularLoading = CircularLoading.showLoadDialog(this, "绑定设备中，请稍候……", true);
        String deviceName = mDevice.getName() == null ? getString(R.string.string_unknown) : mDevice.getName();
        int i = 3;
        String device = deviceName.substring(3);
        LogUtils.d("Huz bind devicename "+deviceName);
        LogUtils.d("Huz bind device "+device);
        //String device = "did:";
        String data = "";

        JSONObject root = new JSONObject();

        try {
            root.put("action","bind");
            JSONObject dataJson = new JSONObject();
            dataJson.put("did",device);
            root.put("data", dataJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LogUtils.d("Huz bind json "+root);
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
                LogUtils.d("Huz bind 发送成功");
                //mHandler.removeCallbacks(runnable);
                //CircularLoading.closeDialog(mCircularLoading);
                subscribe();

                //receive();
            }

            @Override
            public void onFailure(ARequest aRequest, AError aError) {
                LogUtils.d("Huz bind 失败"+aError.getCode());
                LogUtils.d("Huz bind 发送失败");
            }
        });
    }

    public String getJson(String device)throws Exception {
        JSONObject root = new JSONObject();

        try {
            root.put("action","bind");
            JSONObject dataJson = new JSONObject();
            dataJson.put("did",device);
            root.put("data", dataJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return root.toString();
    }

    public void subscribe(){
        MqttSubscribeRequest subscribeRequest = new MqttSubscribeRequest();
        subscribeRequest.topic = "a1DYcm0MIqh/huzha0/user/cloudmsg";
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
            //CircularLoading.closeDialog(mCircularLoading);
            LogUtils.d("Huz bind 收到下行数据 "+aMessage);
            LogUtils.d("Huz bind topic "+topic);
            String pushData = new String((byte[]) aMessage.data);
            unSubscribe();
            LogUtils.d("Huz bind pushdata "+pushData);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unSubscribe();
    }

    private void unSubscribe(){
        MqttSubscribeRequest unsubRequest = new MqttSubscribeRequest();
// unSubTopic 替换成用户自己需要取消订阅的 topic
        unsubRequest.topic = "a1DYcm0MIqh/huzha0/user/cloudmsg";
        unsubRequest.isSubscribe = false;
        LinkKit.getInstance().unsubscribe(unsubRequest, new IConnectUnscribeListener() {
            @Override
            public void onSuccess() {
                LogUtils.d("Huz bing unSubscribe");
                //CircularLoading.closeDialog(mCircularLoading);
                // 取消订阅成功
            }
            @Override
            public void onFailure(AError aError) {
                // 取消订阅失败
            }
        });
    }
}
