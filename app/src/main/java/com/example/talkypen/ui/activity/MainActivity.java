package com.example.talkypen.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttPublishRequest;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttRrpcRegisterRequest;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttSubscribeRequest;
import com.aliyun.alink.linksdk.cmp.core.base.AMessage;
import com.aliyun.alink.linksdk.cmp.core.base.ARequest;
import com.aliyun.alink.linksdk.cmp.core.base.AResponse;
import com.aliyun.alink.linksdk.cmp.core.base.ConnectState;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectNotifyListener;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectRrpcHandle;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectRrpcListener;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSendListener;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSubscribeListener;
import com.aliyun.alink.linksdk.tools.AError;
import com.example.talkypen.R;
import com.example.talkypen.entity.TalkypenDevice;
import com.example.talkypen.framework.utils.LogUtils;
import com.example.talkypen.framework.utils.ToastUtil;
import com.example.talkypen.net.MqttUtil;
import com.example.talkypen.ui.fragment.HomeFragment;
import com.example.talkypen.ui.fragment.CloudFragment;
import com.example.talkypen.ui.fragment.SettingFragment;
import com.example.talkypen.framework.ui.BaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;

public class MainActivity extends BaseActivity {

    @BindView(R.id.ll_bottom)
    LinearLayout mLlBottom;

    @BindView(R.id.ll_home)
    LinearLayout mLlHome;
    @BindView(R.id.iv_home)
    ImageView ivHome;
    @BindView(R.id.tv_home)
    TextView tvHome;

    @BindView(R.id.ll_cloud)
    LinearLayout mLlCloud;
    @BindView(R.id.iv_cloud)
    ImageView ivCloud;
    @BindView(R.id.tv_cloud)
    TextView tvCloud;

    @BindView(R.id.ll_set)
    LinearLayout mLlSetting;
    @BindView(R.id.iv_set)
    ImageView ivSet;
    @BindView(R.id.tv_set)
    TextView tvSet;

    private HomeFragment homeFragment;
    private CloudFragment cloudFragment;
    private SettingFragment settingFragment;

    private Fragment currentFragment;

    private static Boolean isExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String devicename = intent.getStringExtra("devicename");
        String deviceSecret = intent.getStringExtra("devicesecret");
//        if (MqttUtil.connect = false){
//            MqttUtil.certificate(context,devicename,deviceSecret);
//        }
//        String productkey = "a1DYcm0MIqh";
//        String productSecret = "BMeb4ssB5AMaH3Dh";
//        LogUtils.d("Main devicename "+devicename);
//        LogUtils.d("Main devicesecret "+deviceSecret);
//
//        InitManager.init(this, productkey, devicename, deviceSecret, productSecret, new ILinkKitConnectListener() {
//            @Override
//            public void onError(AError aError) {
//                LogUtils.d("Huz init fail");
//                //ToastUtil.show(context,"设备认证失败");
//            }
//
//            @Override
//            public void onInitDone(Object o) {
//                LogUtils.d("Huz init success");
//                publish();
//                //ToastUtil.show(context,"设备认证成功");
//            }
//        });
        //publish();

        //send();

        initView();
        initFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }


    private void send(){
        String action = "getDeviceDetailList";
        String data = "";
        String json = "";
        try {
            json = getJson(action,data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MqttUtil.publish(json);
    }

    private void received(){
        //MqttUtil.
    }

    private void publish(){
        String action = "getDeviceDetailList";
        String data = "";
        String json = "";
        try {
            json = getJson(action,data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //String topic = "/a1DYcm0MIqh/huzhai1/user/appmsg";
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
                    LogUtils.d("Huz Litepal save two "+talkypenDevice.isSaved());
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

    public void receive(){
        MqttRrpcRegisterRequest registerRequest = new MqttRrpcRegisterRequest();
        registerRequest.topic = "a1DYcm0MIqh/huzhao/user/cloudmsg";
        LinkKit.getInstance().subscribeRRPC(registerRequest, new IConnectRrpcListener() {
            @Override
            public void onSubscribeSuccess(ARequest aRequest) {
                //订阅成功
                LogUtils.d("Huz 订阅成功");
            }

            @Override
            public void onSubscribeFailed(ARequest aRequest, AError aError) {
                LogUtils.d("Huz 订阅失败");
            }

            @Override
            public void onReceived(ARequest aRequest, IConnectRrpcHandle iConnectRrpcHandle) {
                LogUtils.d("Huz 收到下行数据 "+aRequest);
                if (iConnectRrpcHandle != null){
                    AResponse aResponse = new AResponse();
                    // 仅供参考，具体返回云端的数据用户根据实际场景添加到data结构体
                    aResponse.data = "{\"id\":\"" + 123 + "\", \"code\":\"200\"" + ",\"data\":{} }";
                    iConnectRrpcHandle.onRrpcResponse(registerRequest.replyTopic, aResponse);
                }
            }

            @Override
            public void onResponseSuccess(ARequest aRequest) {
                LogUtils.d("Huz RRPC响应成功");
            }

            @Override
            public void onResponseFailed(ARequest aRequest, AError aError) {
                LogUtils.d("Huz RRPC响应失败");
            }
        });

    }

    public String getJson(String action, String data)throws Exception {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("action", action);
        jsonParam.put("data", data);
        return jsonParam.toString();
    }

    private void initView(){
        mLlHome.setOnClickListener(listener);
        mLlCloud.setOnClickListener(listener);
        mLlSetting.setOnClickListener(listener);

    }


    //初始化fragment，并默认显示首页fragment
    private void initFragment(){
        homeFragment = new HomeFragment();
        cloudFragment = new CloudFragment();
        settingFragment = new SettingFragment();
        currentFragment = homeFragment;
        // 第一次添加Fragments
        FragmentManager fMgr = getSupportFragmentManager();
        FragmentTransaction ft = fMgr.beginTransaction();
        ft.replace(R.id.frame_content, homeFragment);
        ft.commit();
        setSelectedChange(tvHome);
        ivHome.setImageResource(R.mipmap.iv_home_press);
    }

    private boolean isWifiOpened() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            resetAll();
            switch (v.getId()){
                case R.id.ll_home:
                    //showFragment(homeFragment,HomeFragment.class);
                    switchContent(currentFragment, homeFragment);
                    currentFragment = homeFragment;
                    setSelectedChange(tvHome);
                    ivHome.setImageResource(R.mipmap.iv_home_press);
                    break;
                case R.id.ll_cloud:
                    //showFragment(cloudFragment,CloudFragment.class);
                    switchContent(currentFragment, cloudFragment);
                    currentFragment = cloudFragment;
                    setSelectedChange(tvCloud);
                    ivCloud.setImageResource(R.mipmap.iv_cloud_press);
                    break;
                case R.id.ll_set:
                    //showFragment(settingFragment,SettingFragment.class);
                    switchContent(currentFragment, settingFragment);
                    currentFragment = settingFragment;
                    setSelectedChange(tvSet);
                    ivSet.setImageResource(R.mipmap.iv_set_press);
                    break;
            }
        }
    };

    //重置底部的按钮和文字的状态
    private void resetAll() {
        tvHome.setTextColor(getResources().getColor(R.color.gray_888888));
        tvCloud.setTextColor(getResources().getColor(R.color.gray_888888));
        tvSet.setTextColor(getResources().getColor(R.color.gray_888888));

        ivHome.setImageResource(R.mipmap.iv_home);
        ivCloud.setImageResource(R.mipmap.iv_cloud);
        ivSet.setImageResource(R.mipmap.iv_set);
    }

    /**
     * 更改TextView字体颜色
     *
     * @param tv 要改变颜色的textView
     * @return void
     * @date 2019-01-05
     */
    private void setSelectedChange(TextView tv) {
        tv.setTextColor(getResources().getColor(R.color.blue_press));
    }

    /**
     * 切换fragment，先将fragment初始化的方式
     *
     * @param from 当前显示的fragment
     * @param to   要显示的fragment
     * @return void
     * @date 2019-06-28
     */
    public void switchContent(Fragment from, Fragment to) {
        if (from == to) {
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (!to.isAdded()) {
            transaction.hide(from).add(R.id.frame_content, to)
                    .commitAllowingStateLoss();
        } else {
            transaction.hide(from).show(to).commitAllowingStateLoss();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        //LitePal.deleteAll(TalkypenDevice.class);
        //LinkKit.getInstance().unRegisterOnPushListener(onPushListener);
        homeFragment = null;
        cloudFragment = null;
        settingFragment = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            exit();
            return true;

        }
        return super.onKeyDown(keyCode, event);
    }

    private Handler handler = new Handler();

    //连续按2次退出运用
    private void exit() {
        if (!isExit) {
            isExit = true;
            ToastUtil.show(context, "再按一次退出");
            handler.sendEmptyMessageDelayed(1, 2000);
        } else {
            finish();
        }
    }

    /*private void showFragment(Fragment fragment, Class<?> cl) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragments(transaction);
        if (fragment == null) {
            if (cl == HomeFragment.class) {
                if (homeFragment == null) {
                    homeFragment = new HomeFragment();
                    transaction.remove(homeFragment).
                            add(R.id.frame_content, homeFragment).show(homeFragment);
                }
            } else if (cl == CloudFragment.class) {
                if (cloudFragment == null) {
                    cloudFragment = new CloudFragment();
                    transaction.remove(cloudFragment).
                            add(R.id.frame_content, cloudFragment).show(cloudFragment);
                }
            } else if (cl == SettingFragment.class) {
                if (settingFragment == null) {
                    settingFragment = new SettingFragment();
                    transaction.remove(settingFragment).
                            add(R.id.frame_content, settingFragment).show(settingFragment);
                }
            }
        } else {
            if (cl == HomeFragment.class) {
                transaction.show(homeFragment);
            }
            if (cl == CloudFragment.class) {
                transaction.show(cloudFragment);
            }
            if (cl == SettingFragment.class) {
                transaction.show(settingFragment);
            }
        }
        transaction.addToBackStack(null);
        transaction.commit();
    }*/

    /**
     * 隐藏所有的fragment
     *
     * @param transaction .操作fragment的事务
     * @return void
     * @date 2019-01-17
     */
    /*private void hideFragments(FragmentTransaction transaction) {
        if (homeFragment != null) {
            transaction.hide(homeFragment);
        }
        if (cloudFragment != null) {
            transaction.hide(cloudFragment);
        }
        if (settingFragment != null) {
            transaction.hide(settingFragment);
        }
    }*/

    /*private void clickHome(){
        homeFragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_content,homeFragment);
        fragmentTransaction.commit();

        mLlHome.setSelected(true);
        mLlLocal.setSelected(false);
        mLlSetting.setSelected(false);
    }

    private void clickLocal(){
        cloudFragment = new CloudFragment();
        FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_content,cloudFragment);
        fragmentTransaction.commit();

        mLlHome.setSelected(false);
        mLlLocal.setSelected(true);
        mLlSetting.setSelected(false);

    }

    private void clickSetting(){
        settingFragment = new SettingFragment();
        FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_content,settingFragment);
        fragmentTransaction.commit();

    }*/

}
