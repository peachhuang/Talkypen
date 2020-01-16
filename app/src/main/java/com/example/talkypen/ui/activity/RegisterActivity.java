package com.example.talkypen.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.talkypen.R;
import com.example.talkypen.framework.ui.BaseActivity;
import com.example.talkypen.framework.utils.LogUtils;
import com.example.talkypen.net.HttpUtil;
import com.example.talkypen.widget.TopView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends BaseActivity {

    @BindView(R.id.topView)
    TopView topView;
    @BindView(R.id.et_input_username)
    EditText mEtInputUsername;
    @BindView(R.id.et_input_register_password)
    EditText mEtInputRegisterPassword;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected int getLayoutId() {
        return R.layout.activity_register;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        topView.setTitle("注册");
        topView.setBackground(R.color.light_blue);
    }

    @OnClick({R.id.btn_register})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_register:
                register();
                break;
        }
    }

    public void register(){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        });
        String username = mEtInputUsername.getText().toString().trim();
        String password = mEtInputRegisterPassword.getText().toString().trim();
        //String address = "http://zxw.free.idcfengye.com/api/app/v1/register";
        String address = "http://oid.szxcz.com/api/app/v1/register";
        String json = "";
        try {
            json = getJson(username,password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(username)||TextUtils.isEmpty(password)){
            Toast.makeText(this,"昵称和密码不能为空",Toast.LENGTH_SHORT).show();
        }else {
            OkHttpClient client = new OkHttpClient();
            LogUtils.d("Huz json"+json);
            RequestBody requestBody = RequestBody.create(JSON,json);
            Request request = new Request.Builder().post(requestBody).url(address).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    LogUtils.d("Huz:"+e.toString());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RegisterActivity.this,"注册请求失败",Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    LogUtils.d("Huz 注册请求成功");
                    if (response.isSuccessful()){
                        String responText = response.body().string();
                        LogUtils.d("Huz responseText "+responText);
                        try {
                            JSONObject jsonObject = new JSONObject(responText);
                            int code = jsonObject.getInt("code");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (code == 0){
                                        Toast.makeText(RegisterActivity.this,"注册成功",Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                                        intent.putExtra("username",username);
                                        startActivity(intent);
                                        //skipPage(LoginActivity.class);
                                    }else if(code == 1){
                                        Toast.makeText(RegisterActivity.this,"用户名已存在",Toast.LENGTH_SHORT).show();
                                    }else if(code == 2){
                                        Toast.makeText(RegisterActivity.this,"用户名不合法",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else {
                        LogUtils.d("Huz response "+response.code());
                    }
                }
            });
        }

    }

    //将提交到服务器数据转换为JSON格式数据字符串
    public String getJson(String name, String passward)throws Exception {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("username", name);
        jsonParam.put("password", passward);
        return jsonParam.toString();
    }

//    public void handleRegisterResponse(String reponse){
//        try {
//            JSONObject jsonObject = new JSONObject(reponse);
//            LogUtils.d("Huz "+jsonObject);
//            Register register = new Register();
//            register.setCode(jsonObject.getInt("code"));
//            register.setMessage(jsonObject.getString("message"));
//            register.setData(jsonObject.get("data"));
//            register.save();
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//    }
}
