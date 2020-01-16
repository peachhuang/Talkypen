package com.example.talkypen.ui.fragment.blufi;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.talkypen.R;
import com.example.talkypen.framework.ui.BaseFragment;
import com.example.talkypen.framework.utils.LogUtils;
import com.example.talkypen.ui.activity.BlufiActivity;
import com.example.talkypen.ui.activity.ConnectActivity;
import com.example.talkypen.widget.TopView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import blufi.espressif.BlufiClient;
import butterknife.BindView;
import butterknife.OnClick;

public class BluConnectFragment extends BaseFragment {

    @BindView(R.id.topView)
    TopView topView;
    @BindView(R.id.bt_connect)
    Button mBtConnect;
//    @BindView(R.id.bt_config)
//    Button mBtConfig;

    private BluConnectFragment bluConnectFragment;
    private BluConfigFragment bluConfigFragment;

    private BluetoothDevice mDevice;
    private BlufiClient mBlufiClient;
    private BluetoothGatt mGatt;
    private BlufiActivity.GattCallback callback;

    //BlufiActivity blufiActivity = (BlufiActivity)getActivity();

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_blu_connect;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        bluConnectFragment = new BluConnectFragment();
        bluConfigFragment = new BluConfigFragment();

        Bundle bundle = getArguments();
        mDevice = bundle.getParcelable("bluDevice");
        String deviceName = mDevice.getName() == null ? getString(R.string.string_unknown) : mDevice.getName();
        topView.setTitle(deviceName);

    }

    @OnClick({R.id.bt_connect,R.id.bt_config})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.bt_connect:
                ((BlufiActivity)getActivity()).connect();
                break;
            case R.id.bt_config:
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                Bundle bundle = new Bundle();
                bundle.putParcelable("bluDevice",mDevice);
                //transaction.hide(bluConnectFragment).add(R.id.blu_frame_content,bluConfigFragment).commitAllowingStateLoss();
                transaction.replace(R.id.blu_frame_content,bluConfigFragment);
                bluConfigFragment.setArguments(bundle);
                transaction.commit();
                break;
        }
    }

//    public BluetoothDevice getDevice(){
//        BluetoothDevice Device = mDevice;
//        return Device;
//    }

    /*private void connect(){
        mBtConnect.setEnabled(false);
        //((BlufiActivity)getActivity()).connectCallback();

        if (mBlufiClient != null) {
            mBlufiClient.close();
            mBlufiClient = null;
        }
        if (mGatt != null) {
            mGatt.close();
        }
        callback = ((BlufiActivity)getActivity()).connectCallback();
        BlufiActivity.GattCallback callback = new BlufiActivity.GattCallback();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mGatt = mDevice.connectGatt(getActivity(), false, callback, BluetoothDevice.TRANSPORT_LE);
            LogUtils.d("Huz connectGatt");
        } else {
            mGatt = mDevice.connectGatt(getActivity(), false, callback);
        }
    }*/
}
