package com.example.talkypen.ui.activity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.talkypen.R;
import com.example.talkypen.framework.base.BlufiApp;
import com.example.talkypen.framework.ui.BaseActivity;
import com.example.talkypen.framework.utils.LogUtils;
import com.example.talkypen.ui.constants.BlufiConstants;
import com.example.talkypen.ui.constants.SettingsConstants;
import com.example.talkypen.widget.TopView;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;
import blufi.espressif.BlufiCallback;
import blufi.espressif.BlufiClient;
import blufi.espressif.params.BlufiConfigureParams;
import blufi.espressif.response.BlufiScanResult;
import blufi.espressif.response.BlufiStatusResponse;
import blufi.espressif.response.BlufiVersionResponse;
import butterknife.BindView;
import butterknife.OnClick;

public class ConnectActivity extends BaseActivity {

    @BindView(R.id.topView)
    TopView topView;
    @BindView(R.id.bt_connect)
    Button btConnect;
    @BindView(R.id.bt_config)
    Button btConfig;

    private static final int REQUEST_CONFIGURE = 0x20;
    private BluetoothDevice mDevice;
    private BluetoothGatt mGatt;
    private BlufiClient mBlufiClient;
    private volatile boolean mConnected;
    private List<Message> mMsgList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //topView.setTitle("点击以下设备进行连接");
        mDevice = getIntent().getParcelableExtra(BlufiConstants.KEY_BLE_DEVICE);
        String deviceName = mDevice.getName() == null ? getString(R.string.string_unknown) : mDevice.getName();
        topView.setTitle(deviceName);

        mMsgList = new LinkedList<>();
        btConfig.setEnabled(false);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_connect;
    }

    @OnClick({R.id.bt_connect,R.id.bt_config})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.bt_connect:
                connect();
                break;
            case R.id.bt_config:
                configureOptions();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBlufiClient != null) {
            mBlufiClient.close();
            mBlufiClient = null;
        }
        if (mGatt != null) {
            mGatt.close();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CONFIGURE) {
            if (!mConnected) {
                LogUtils.d("Huz disconnect");
                return;
            }
            if (resultCode == RESULT_OK) {
                BlufiConfigureParams params =
                        (BlufiConfigureParams) data.getSerializableExtra(BlufiConstants.KEY_CONFIGURE_PARAM);
                configure(params);
            }

            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateMessage(String message, boolean isNotificaiton) {
        runOnUiThread(() -> {
            Message msg = new Message();
            msg.text = message;
            LogUtils.d("Huz scanDeviceWifiList"+message);
            msg.isNotification = isNotificaiton;
            mMsgList.add(msg);
            LogUtils.d("Huz scanDeviceWifiList"+mMsgList);
        });
    }

    private void requestDeviceWifiScan() {

        mBlufiClient.requestDeviceWifiScan();
    }

    private void configureOptions() {
        Intent intent = new Intent(ConnectActivity.this, ConfigActivity.class);
        startActivityForResult(intent, REQUEST_CONFIGURE);
    }

    private void configure(BlufiConfigureParams params) {
        btConfig.setEnabled(false);

        LogUtils.d("Huz params"+params);
        mBlufiClient.configure(params);
        skipPage(ConfigSuccessActivity.class);
        LogUtils.d("Huz configure");
    }

    private void connect(){
        btConnect.setEnabled(false);

        if (mBlufiClient != null) {
            mBlufiClient.close();
            mBlufiClient = null;
        }
        if (mGatt != null) {
            mGatt.close();
        }
        GattCallback callback = new GattCallback();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mGatt = mDevice.connectGatt(this, false, callback, BluetoothDevice.TRANSPORT_LE);
            LogUtils.d("Huz connectGatt");
        } else {
            mGatt = mDevice.connectGatt(this, false, callback);
        }
    }


    private void onGattConnected() {
        mConnected = true;
        runOnUiThread(() -> {
            btConnect.setEnabled(false);

        });
    }

    private void onGattDisconnected() {
        mConnected = false;
        runOnUiThread(() -> {
            btConnect.setEnabled(true);
        });
    }

    /**
     * mBlufiClient call onCharacteristicWrite and onCharacteristicChanged is required
     */
    private class GattCallback extends BluetoothGattCallback {
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

                        onGattConnected();
                        btConfig.setEnabled(true);//连接设备成功
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        gatt.close();
                        onGattDisconnected();
                        break;
                }
            } else {
                gatt.close();
                onGattDisconnected();
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
                    //mLog.w("Discover service failed");
                    LogUtils.d("Discover service failed");
                    gatt.disconnect();
                    //updateMessage("Discover service failed", false);
                    return;
                }

                BluetoothGattCharacteristic writeCharact = service.getCharacteristic(BlufiConstants.UUID_WRITE_CHARACTERISTIC);
                if (writeCharact == null) {
                    //mLog.w("Get write characteristic failed");
                    LogUtils.d("Get write characteristic failed");
                    gatt.disconnect();
                    //updateMessage("Get write characteristic failed", false);
                    return;
                }

                BluetoothGattCharacteristic notifyCharact = service.getCharacteristic(BlufiConstants.UUID_NOTIFICATION_CHARACTERISTIC);
                if (notifyCharact == null) {
                    //mLog.w("Get notification characteristic failed");
                    LogUtils.d("Get notification characteristic failed");
                    gatt.disconnect();
                    //updateMessage("Get notification characteristic failed", false);
                    return;
                }

                //updateMessage("Discover service and characteristics success", false);

                if (mBlufiClient != null) {
                    mBlufiClient.close();
                }
                mBlufiClient = new BlufiClient(gatt, writeCharact, notifyCharact, new BlufiCallbackMain());
                if (mChangedMtu > 0) {
                    int blufiPkgLenLimit = mChangedMtu - 3;
                    LogUtils.d("BluFiClient setPostPackageLengthLimit " + blufiPkgLenLimit);
                    //mBlufiClient.setPostPackageLengthLimit(blufiPkgLenLimit);
                }
                requestDeviceWifiScan();


                gatt.setCharacteristicNotification(notifyCharact, true);

                //onGattServiceCharacteristicDiscovered();
            } else {
                gatt.disconnect();
                //updateMessage(String.format(Locale.ENGLISH, "Discover services error status %d", status), false);
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
            switch (status) {
                case STATUS_SUCCESS:
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onConfigureResult(BlufiClient client, int status) {
            switch (status) {
                case STATUS_SUCCESS:
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onDeviceStatusResponse(BlufiClient client, int status, BlufiStatusResponse response) {
            switch (status) {
                case STATUS_SUCCESS:
                    break;
                default:
                    break;
            }
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
                    updateMessage(msg.toString(), true);
                    break;
                default:
                    updateMessage("Device scan result error, code=" + status, false);
                    break;
            }

            //mBlufiDeviceScanBtn.setEnabled(mConnected);
        }

        @Override
        public void onDeviceVersionResponse(BlufiClient client, int status, BlufiVersionResponse response) {
            switch (status) {
                case STATUS_SUCCESS:
                    //updateMessage(String.format("Receive device version: %s", response.getVersionString()),true);
                    break;
                default:
                    //updateMessage("Device version error, code=" + status, false);
                    break;
            }

            //mBlufiVersionBtn.setEnabled(mConnected);
        }

        @Override
        public void onPostCustomDataResult(BlufiClient client, int status, byte[] data) {
            String dataStr = new String(data);
            String format = "Post data %s %s";
            switch (status) {
                case STATUS_SUCCESS:
                    //updateMessage(String.format(format, dataStr, "complete"), false);
                    break;
                default:
                    //updateMessage(String.format(format, dataStr, "failed"), false);
                    break;
            }
        }

        @Override
        public void onReceiveCustomData(BlufiClient client, int status, byte[] data) {
            switch (status) {
                case STATUS_SUCCESS:
                    String customStr = new String(data);
                    //updateMessage(String.format("Receive custom data:\n%s", customStr), true);
                    break;
                default:
                    //updateMessage("Receive custom data error, code=" + status, false);
                    break;
            }
        }

        @Override
        public void onError(BlufiClient client, int errCode) {
            //updateMessage(String.format(Locale.ENGLISH, "Receive error code %d", errCode), false);
        }
    }

    private class Message {
        String text;
        boolean isNotification;
    }

}
