package com.example.talkypen.ui.activity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.example.talkypen.R;
import com.example.talkypen.framework.base.BlufiApp;
import com.example.talkypen.framework.ui.BaseActivity;
import com.example.talkypen.framework.utils.LogUtils;
import com.example.talkypen.ui.constants.BlufiConstants;
import com.example.talkypen.ui.constants.SettingsConstants;
import com.example.talkypen.ui.fragment.blufi.BluConfigFragment;
import com.example.talkypen.ui.fragment.blufi.BluConnectFragment;
import com.example.talkypen.ui.fragment.blufi.BluPromptFragment;
import com.example.talkypen.ui.fragment.blufi.BluetoothFragment;

import java.util.List;
import java.util.Locale;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import blufi.espressif.BlufiCallback;
import blufi.espressif.BlufiClient;
import blufi.espressif.params.BlufiConfigureParams;
import blufi.espressif.response.BlufiScanResult;
import blufi.espressif.response.BlufiStatusResponse;
import blufi.espressif.response.BlufiVersionResponse;
import butterknife.BindView;

public class BlufiActivity extends BaseActivity{

    @BindView(R.id.ll_blu_prompt)
    LinearLayout mLlBluPrompt;
    @BindView(R.id.ll_bluetooth)
    LinearLayout mLlBluetooth;
    @BindView(R.id.ll_blu_connect)
    LinearLayout mLlBluConnect;
    @BindView(R.id.ll_blu_config)
    LinearLayout mLlBluConfig;

    private BluPromptFragment bluPromptFragment;
    private BluetoothFragment bluetoothFragment;
    private BluConnectFragment bluConnectFragment;
    private BluConfigFragment bluConfigFragment;

    private Fragment currentFragment;

    private BluetoothGatt mGatt;
    private BluetoothDevice mDevice;
    private BlufiClient mBlufiClient;
    CallbackBlufiScanResult callbackBlufiScanResult;
    //CallbackGetDevice callbackGetDevice;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_blufi;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initFragment();
        //initView();
    }

    //初始化fragment，并默认显示首页fragment
    private void initFragment(){
        bluPromptFragment = new BluPromptFragment();
        bluetoothFragment = new BluetoothFragment();
        bluConnectFragment = new BluConnectFragment();
        bluConfigFragment = new BluConfigFragment();

        currentFragment = bluPromptFragment;
        // 第一次添加Fragments
        FragmentManager fMgr = getSupportFragmentManager();
        FragmentTransaction ft = fMgr.beginTransaction();
        ft.replace(R.id.blu_frame_content, bluPromptFragment);
        ft.commit();
    }

    /*private void initView(){
        mLlBluPrompt.setOnClickListener(listener);
        mLlBluetooth.setOnClickListener(listener);
        mLlBluConnect.setOnClickListener(listener);
        mLlBluConfig.setOnClickListener(listener);

    }*/

    /*private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.ll_blu_prompt:
                    switchContent(currentFragment, bluPromptFragment);
                    currentFragment = bluPromptFragment;
                    break;
                case R.id.ll_bluetooth:
                    switchContent(currentFragment, bluetoothFragment);
                    currentFragment = bluetoothFragment;
                    break;
                case R.id.ll_blu_connect:
                    switchContent(currentFragment, bluConnectFragment);
                    currentFragment = bluConnectFragment;
                    break;
                case R.id.ll_blu_config:
                    switchContent(currentFragment,bluConfigFragment);
                    currentFragment = bluConfigFragment;
                    break;
            }
        }
    };*/

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
            transaction.hide(from).add(R.id.blu_frame_content, to)
                    .commitAllowingStateLoss();
        } else {
            transaction.hide(from).show(to).commitAllowingStateLoss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        bluPromptFragment = null;
        bluetoothFragment = null;
        bluConnectFragment = null;
        bluConfigFragment = null;
        mBlufiClient = null;
    }

    public void config(BlufiConfigureParams params) {
        mBlufiClient.configure(params);
    }

    /**
     * mBlufiClient call onCharacteristicWrite and onCharacteristicChanged is required
     */
    public class GattCallback extends BluetoothGattCallback {
        private int mChangedMtu = -1;

        /**
         * 连接状态改变，连接成功
         * @param gatt
         * @param status
         * @param newState
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String devAddr = gatt.getDevice().getAddress();
            LogUtils.d(String.format(Locale.ENGLISH, "onConnectionStateChange addr=%s, status=%d, newState=%d",
                    devAddr, status, newState));
            mChangedMtu = -1;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
                            int mtu = (int) BlufiApp.getInstance().settingsGet(
                                    SettingsConstants.PREF_SETTINGS_KEY_MTU_LENGTH, BlufiConstants.DEFAULT_MTU_LENGTH);
                            boolean requestMtu = gatt.requestMtu(mtu);
                            if (!requestMtu) {
                                //mLog.w("Request mtu failed");
                                LogUtils.d("Request mtu failed");
                                //updateMessage(String.format(Locale.ENGLISH, "Request mtu %d failed", mtu), false);
                                gatt.discoverServices();
                            }
                        } else {
                            gatt.discoverServices();
                        }

                        LogUtils.d("设备连接成功");
                        //onGattConnected();
                       // btConfig.setEnabled(true);//连接设备成功
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        gatt.close();
                        //onGattDisconnected();
                        break;
                }
            } else {
                gatt.close();
                //onGattDisconnected();
            }
        }

        /**
         * 当给定连接的MTU更改时调用的回调
         * @param gatt
         * @param mtu
         * @param status
         */
        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            LogUtils.d(String.format(Locale.ENGLISH, "onMtuChanged status=%d, mtu=%d", status, mtu));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mChangedMtu = mtu;
                //updateMessage(String.format(Locale.ENGLISH, "Set mtu complete, mtu=%d ", mtu), false);
            } else {
                //updateMessage(String.format(Locale.ENGLISH, "Set mtu failed, mtu=%d, status=%d", mtu, status), false);
            }

            gatt.discoverServices();
        }

        /**
         * 远程搜索已经完成。内部对象结构现在应该反映状态远程设备数据库的。让应用程序知道这一点我们已经做完了。
         * @param gatt
         * @param status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            LogUtils.d(String.format(Locale.ENGLISH, "onServicesDiscovered status=%d", status));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(BlufiConstants.UUID_SERVICE);
                if (service == null) {
                    LogUtils.d("Discover service failed");
                    gatt.disconnect();
                    return;
                }

                BluetoothGattCharacteristic writeCharact = service.getCharacteristic(BlufiConstants.UUID_WRITE_CHARACTERISTIC);
                if (writeCharact == null) {
                    LogUtils.d("Get write characteristic failed");
                    gatt.disconnect();
                    return;
                }

                BluetoothGattCharacteristic notifyCharact = service.getCharacteristic(BlufiConstants.UUID_NOTIFICATION_CHARACTERISTIC);
                if (notifyCharact == null) {
                    LogUtils.d("Get notification characteristic failed");
                    gatt.disconnect();
                    return;
                }

                if (mBlufiClient != null) {
                    mBlufiClient.close();
                }
                mBlufiClient = new BlufiClient(gatt, writeCharact, notifyCharact, new BlufiCallbackMain());
                if (mChangedMtu > 0) {
                    int blufiPkgLenLimit = mChangedMtu - 3;
                    LogUtils.d("BluFiClient setPostPackageLengthLimit " + blufiPkgLenLimit);
                    //mBlufiClient.setPostPackageLengthLimit(blufiPkgLenLimit);
                }

                gatt.setCharacteristicNotification(notifyCharact, true);

            } else {
                gatt.disconnect();
            }
        }

        /**
         * 特征已写入远程设备。让应用程序知道我们做了什么……
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            LogUtils.d("onCharacteristicWrite " + status);
            // This is requirement
            mBlufiClient.onCharacteristicWrite(gatt, characteristic, status);
        }

        /**
         * 远程特性已更新。更新内部值。
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            LogUtils.d("onCharacteristicChanged");
            // This is requirement
            mBlufiClient.onCharacteristicChanged(gatt, characteristic);
        }
    }

    private class BlufiCallbackMain extends BlufiCallback {
        @Override
        public void onNegotiateSecurityResult(BlufiClient client, int status) {
        }

        @Override
        public void onConfigureResult(BlufiClient client, int status) {
        }

        @Override
        public void onDeviceStatusResponse(BlufiClient client, int status, BlufiStatusResponse response) {
        }

        @Override
        public void onDeviceScanResult(BlufiClient client, int status, List<BlufiScanResult> results) {
            switch (status) {
                case STATUS_SUCCESS:
                    StringBuilder msg = new StringBuilder();
                    msg.append("Receive device scan result:\n");
                    for (BlufiScanResult scanResult : results) {
                        msg.append(scanResult.toString()).append("\n");
                    }
                    if (callbackBlufiScanResult != null){
                        callbackBlufiScanResult.sendResult(results);
                    }
                    break;
                default:
                    break;
            }

            //mBlufiDeviceScanBtn.setEnabled(mConnected);
        }

        @Override
        public void onDeviceVersionResponse(BlufiClient client, int status, BlufiVersionResponse response) {
        }

        @Override
        public void onPostCustomDataResult(BlufiClient client, int status, byte[] data) {
        }

        @Override
        public void onReceiveCustomData(BlufiClient client, int status, byte[] data) {
        }

        @Override
        public void onError(BlufiClient client, int errCode) {
        }
    }
    public void setCallbackBlufiScanResult(CallbackBlufiScanResult callbackBlufiScanResult){
        this.callbackBlufiScanResult = callbackBlufiScanResult;
    }

    public interface CallbackBlufiScanResult{
        public void sendResult(List<BlufiScanResult> results);
    }

//    public void setCallbackGetDevice(CallbackGetDevice callbackGetDevice){
//        this.callbackGetDevice = callbackGetDevice;
//    }
//
//    public interface CallbackGetDevice{
//        public void getDevice(BluetoothDevice device);
//    }

    public void connect(){
        if (mBlufiClient != null) {
            mBlufiClient.close();
            mBlufiClient = null;
        }
        if (mGatt != null) {
            mGatt.close();
        }
        GattCallback callback = new GattCallback();
        //mDevice = getDevice();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mGatt = mDevice.connectGatt(this, false, callback, BluetoothDevice.TRANSPORT_LE);
            LogUtils.d("Huz connectGatt");
        } else {
            mGatt = mDevice.connectGatt(this, false, callback);
        }
    }

    public void requestDeviceWifiScan() {
        mBlufiClient.requestDeviceWifiScan();
    }

    public void saveDevice(BluetoothDevice device){
        mDevice = device;
    }
}
