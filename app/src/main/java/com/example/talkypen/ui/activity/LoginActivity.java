package com.example.talkypen.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.alink.linkkit.api.ILinkKitConnectListener;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttPublishRequest;
import com.aliyun.alink.linksdk.cmp.core.base.ARequest;
import com.aliyun.alink.linksdk.cmp.core.base.AResponse;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSendListener;
import com.aliyun.alink.linksdk.tools.AError;
import com.example.talkypen.R;
import com.example.talkypen.entity.Device;
import com.example.talkypen.framework.ui.BaseActivity;
import com.example.talkypen.framework.utils.InitManager;
import com.example.talkypen.framework.utils.LogUtils;
import com.example.talkypen.framework.utils.ToastUtil;
import com.example.talkypen.net.MqttUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.btn_login)
    Button mBtnLogin;
    @BindView(R.id.tv_register)
    TextView mTvRegister;
    @BindView(R.id.et_username)
    EditText mEtUsername;
    @BindView(R.id.et_password)
    EditText mEtPassword;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        //intent.getStringExtra("username");
        mEtUsername.setText(intent.getStringExtra("username"));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @OnClick({R.id.btn_login,R.id.tv_register})
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_login:
                login();
                //skipPage(MainActivity.class);
                break;
            case R.id.tv_register:
                skipPage(RegisterActivity.class);
                break;
        }
    }

    public void login() {
        String username = mEtUsername.getText().toString();
        String password = mEtPassword.getText().toString();
        //String address = "http://zxw.free.idcfengye.com/api/app/v1/login";
        String address = "https://oid.szxcz.com/api/app/v1/login";
        String json = "";
        try {
            json = getJson(username,password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password)){
            ToastUtil.show(this,"用户名和密码不能为空");
        }else {
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(JSON,json);
            Request request = new Request.Builder().post(requestBody).url(address).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.show(LoginActivity.this,"网络错误");
                        }
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseText = response.body().string();
                    LogUtils.d("Huz Login "+responseText);
                    try {
                        JSONObject jsonObject = new JSONObject(responseText);
                        LogUtils.d("Huz jsonObject"+jsonObject);
                        int code = jsonObject.getInt("code");
                        String userinfo = jsonObject.getString("data");
                        LogUtils.d("Huz userinfo "+userinfo);
                        JSONObject jsonObject1 = new JSONObject(userinfo);
                        Device device = new Device();
                        device.setName(jsonObject1.getString("iotDeviceName"));
                        device.setDes(jsonObject1.getString("iotDeviceSecret"));
                        device.save();
                        LogUtils.d("Huz Litepal save "+device.isSaved());
                        String devicename = jsonObject1.getString("iotDeviceName");
                        String deviceSecret = jsonObject1.getString("iotDeviceSecret");
                        String productkey = "a1DYcm0MIqh";
                        String productSecret = "BMeb4ssB5AMaH3Dh";

                        Bundle bundle = new Bundle();
                        bundle.putString("devicename",devicename);
                        bundle.putString("devicesecret",deviceSecret);
                        LogUtils.d("Huz devicename "+devicename);

                        InitManager.init(context, productkey, devicename, deviceSecret, productSecret, new ILinkKitConnectListener() {
                            @Override
                            public void onError(AError aError) {
                                LogUtils.d("Huz init fail");
                                //ToastUtil.show(context,"设备认证失败");
                            }

                            @Override
                            public void onInitDone(Object o) {
                                LogUtils.d("Huz init success");
                                //publish();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (code == 0){
                                            skipPage(MainActivity.class);
                                            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                            intent.putExtras(bundle);
                                            startActivity(intent);
                                        }else if(code == 1){
                                            ToastUtil.show(LoginActivity.this,"用户名或密码输入错误");
                                        }
                                    }
                                });
                                //publish();
                                //ToastUtil.show(context,"设备认证成功");
                            }
                        });
//                        Bundle bundle = new Bundle();
//                        bundle.putString("devicename",devicename);
//                        bundle.putString("devicesecret",deviceSecret);
//                        LogUtils.d("Huz devicename "+devicename);
                        //handDataResponse(userinfo);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (code == 0){
//                                    skipPage(MainActivity.class);
//                                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
//                                    intent.putExtras(bundle);
//                                    startActivity(intent);
//                                }else if(code == 1){
//                                    ToastUtil.show(LoginActivity.this,"用户名或密码输入错误");
//                                }
//                            }
//                        });
                        //String username = jsonObject.getString("iotDeviceName");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

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
                //subscribe();

                //receive();
            }

            @Override
            public void onFailure(ARequest aRequest, AError aError) {
                LogUtils.d("Huz 失败"+aError);
                LogUtils.d("Huz 发送失败");
            }
        });
    }

    public String getJson(String name, String passward)throws Exception {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("username", name);
        jsonParam.put("password", passward);
        return jsonParam.toString();
    }

    public void handDataResponse(String reponse){
        try {
            JSONObject jsonObject = new JSONObject(reponse);
            LogUtils.d("Huz iotDeviceName "+jsonObject.getString("iotDeviceName"));
            Device device = new Device();
            device.setName(jsonObject.getString("iotDeviceName"));
            device.setDes(jsonObject.getString("iotDeviceSecret"));
            device.save();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
