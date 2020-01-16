package com.example.talkypen;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linksdk.cmp.core.base.AMessage;
import com.aliyun.alink.linksdk.cmp.core.base.ConnectState;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectNotifyListener;
import com.example.talkypen.framework.utils.LogUtils;
import com.example.talkypen.net.MqttUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class MyService extends Service {

    private static MyService mInstance;

    private  MyService() {}

    public static synchronized MyService getInstance() {
        if (mInstance == null) {
            mInstance = new MyService();
        }
        return mInstance;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * 首次创建服务时，系统将调用此方法来执行一次性设置程序（在调用 onStartCommand() 或onBind() 之前）。
     * 如果服务已在运行，则不会调用此方法，该方法只调用一次
     */
    @Override
    public void onCreate() {
        super.onCreate();

        LinkKit.getInstance().registerOnPushListener(onPushListener);

        MqttUtil.subscribe();
        String homeData = getDevicelistData();
        MqttUtil.publish(homeData);
    }

    /**
     * 当另一个组件（如 Activity）通过调用 startService() 请求启动服务时，
     * 系统将调用此方法。一旦执行此方法，服务即会启动并可在后台无限期运行
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
//        String homeData = getDevicelistData();
//        MqttUtil.publish(homeData);
        //LinkKit.getInstance().registerOnPushListener(onPushListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LinkKit.getInstance().unRegisterOnPushListener(onPushListener);
    }

    public String getDevicelistData(){
        String action = "getDeviceDetailList";
        String data = "";
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("action", action);
            jsonParam.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String DevicelistData = jsonParam.toString();
        return DevicelistData;
    }


    private IConnectNotifyListener onPushListener = new IConnectNotifyListener() {
        @Override
        public void onNotify(String connectId, String topic, AMessage aMessage) {
            // 下行数据通知
            LogUtils.d("Huz 收到下行数据 "+aMessage);
            LogUtils.d("Huz topic "+topic);
            String pushData = new String((byte[]) aMessage.data);
            LogUtils.d("Huz pushdata "+pushData);
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
