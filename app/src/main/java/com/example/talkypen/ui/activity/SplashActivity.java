package com.example.talkypen.ui.activity;

import android.os.Bundle;
import android.os.Handler;

import com.example.talkypen.R;
import com.example.talkypen.entity.Device;
import com.example.talkypen.framework.ui.BaseActivity;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.util.List;

public class SplashActivity extends BaseActivity {

    private Handler mHandler = new Handler();
    private List<Device> deviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler.postDelayed(runnable,2000);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_splash;
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
//            deviceList = LitePal.findAll(Device.class);
//            for (Device device:deviceList){
//                String devicename = device.getName();
//                if (devicename == null){
//                    skipPage(LoginActivity.class);
//                }else {
//                    skipPage(MainActivity.class);
//                }
//            }
            skipPage(LoginActivity.class);
            finish();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(runnable);
    }
}
