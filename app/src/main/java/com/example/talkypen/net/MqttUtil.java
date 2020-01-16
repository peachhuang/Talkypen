package com.example.talkypen.net;

import android.content.Context;

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
import com.aliyun.alink.linksdk.tools.AError;
import com.example.talkypen.framework.utils.InitManager;
import com.example.talkypen.framework.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class MqttUtil {

    public static boolean connect;
    public String message;

    public MqttUtil(String message) {
        this.message = message;
    }

    public static void certificate(Context context,String devicename,String deviceSecret){
        final String productkey = "a1DYcm0MIqh";
        final String productSecret = "BMeb4ssB5AMaH3Dh";
        InitManager.init(context, productkey, devicename, deviceSecret, productSecret, new ILinkKitConnectListener() {
            @Override
            public void onError(AError aError) {
                connect = false;
                LogUtils.d("Huz init fail");

            }

            @Override
            public void onInitDone(Object o) {
                connect = true;
                LogUtils.d("Huz init success");
            }
        });
    }

    public static void publish(String publishData){
        String topic = "/a1DYcm0MIqh/huzhai1/user/appmsg";
        MqttPublishRequest request = new MqttPublishRequest();
        request.isRPC = false;
        request.topic = topic;
        request.qos = 0;
        request.payloadObj = publishData;
        LinkKit.getInstance().publish(request, new IConnectSendListener() {
            @Override
            public void onResponse(ARequest aRequest, AResponse aResponse) {
                LogUtils.d("Huz 发送成功");
            }

            @Override
            public void onFailure(ARequest aRequest, AError aError) {
                LogUtils.d("Huz 失败"+aError);
                LogUtils.d("Huz 发送失败");
            }
        });
    }

    public static void subscribe(){
        MqttSubscribeRequest subscribeRequest = new MqttSubscribeRequest();
        subscribeRequest.topic = "a1DYcm0MIqh/huzhai1/user/cloudmsg";
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
        //return message;
    }

    // 下行数据监听
    IConnectNotifyListener onPushListener = new IConnectNotifyListener() {
        @Override
        public void onNotify(String connectId, String topic, AMessage aMessage) {
            // 下行数据通知
            LogUtils.d("Huz 收到下行数据 "+aMessage);
            LogUtils.d("Huz topic "+topic);
            String pushData = new String((byte[]) aMessage.data);
            LogUtils.d("Huz pushdata "+pushData);
            message = pushData;
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
}
