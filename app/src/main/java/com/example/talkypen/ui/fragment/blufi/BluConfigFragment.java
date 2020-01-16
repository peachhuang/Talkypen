package com.example.talkypen.ui.fragment.blufi;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.talkypen.R;
import com.example.talkypen.framework.ui.BaseFragment;
import com.example.talkypen.framework.utils.CircularLoading;
import com.example.talkypen.framework.utils.LogUtils;
import com.example.talkypen.ui.activity.BlufiActivity;
import com.example.talkypen.ui.activity.ConfigActivity;
import com.example.talkypen.ui.activity.ConfigSuccessActivity;
import com.example.talkypen.ui.constants.BlufiConstants;
import com.example.talkypen.widget.TopView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import blufi.espressif.BlufiClient;
import blufi.espressif.params.BlufiConfigureParams;
import blufi.espressif.params.BlufiParameter;
import blufi.espressif.response.BlufiScanResult;
import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tools.xxj.phiman.net.XxjNetUtil;

import static android.content.Context.WIFI_SERVICE;

public class BluConfigFragment extends BaseFragment implements BlufiActivity.CallbackBlufiScanResult {

    @BindView(R.id.topView)
    TopView topView;
    @BindView(R.id.station_ssid)
    AutoCompleteTextView mStationSsidET;
    //    @BindView(R.id.station_wifi_scan)
//    ImageButton mBtStationWifiScan;
    @BindView(R.id.station_wifi_password)
    EditText mStationPasswordET;
    @BindView(R.id.confirm)
    Button mBtConfirm;

    private BlufiClient mBlufiClient;
    private WifiManager mWifiManager;
    private List<ScanResult> mWifiList;
    private Dialog mCircularLoading;
    private BluetoothDevice mDevice;

    //BlufiActivity blufiActivity = (BlufiActivity) getActivity();

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_blu_config;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        topView.setTitle("配网");

        Bundle bundle = getArguments();
        mDevice = bundle.getParcelable("bluDevice");
        String deviceName = mDevice.getName() == null ? getString(R.string.string_unknown) : mDevice.getName();
        LogUtils.d("Huz config devicename "+deviceName);

        mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(WIFI_SERVICE);
        if (!mWifiManager.isWifiEnabled() ){
            ((BlufiActivity) getActivity()).requestDeviceWifiScan();
            mCircularLoading = CircularLoading.showLoadDialog(getActivity(), "加载WiFi列表中...", true);
        }

        mWifiList = new ArrayList<>();

        mStationSsidET.setText(getConnectionSSID());
        WifiInfo info = ((WifiManager) getActivity().getApplicationContext().getSystemService(WIFI_SERVICE)).getConnectionInfo();
        if (info != null) {
            byte[] ssidBytes = XxjNetUtil.getOriginalSsidBytes(info);
            mStationSsidET.setTag(ssidBytes);
        }

        ((BlufiActivity)getActivity()).setCallbackBlufiScanResult(this);
    }

    @OnClick({R.id.station_wifi_scan,R.id.confirm})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.station_wifi_scan:
                BlufiActivity blufiActivity = (BlufiActivity) getActivity();
                if (blufiActivity != null){
                    blufiActivity.requestDeviceWifiScan();
                    mCircularLoading = CircularLoading.showLoadDialog(getActivity(), "加载WiFi列表中...", true);
                }
                break;
            case R.id.confirm:
                configure();
                break;
        }
    }

    private String getConnectionSSID() {
        if (!mWifiManager.isWifiEnabled()) {
            return null;
        }

        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            return null;
        }

        String ssid = wifiInfo.getSSID();
        if (ssid.startsWith("\"") && ssid.endsWith("\"") && ssid.length() >= 2) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }

        return ssid;
    }

    /**
     * 复写CallbackBlufiScanResult接口中的方法
     * @param results
     */
    @Override
    public void sendResult(List<BlufiScanResult> results) {
        CircularLoading.closeDialog(mCircularLoading);
        LogUtils.d("Huz BlufiScanResult"+results);
        int count = results.size();
        if (count == 0) {
            Toast.makeText(getActivity(), R.string.configure_station_wifi_scanning_nothing, Toast.LENGTH_SHORT).show();
            return;
        }
        int checkedItem = -1;
        String inputSsid = mStationSsidET.getText().toString();
        final String[] wifiSSIDs = new String[count];
        for (int i = 0; i < count; i++) {
            BlufiScanResult sr = results.get(i);
            wifiSSIDs[i] = sr.getSsid();
            if (inputSsid.equals(sr.getSsid())) {
                checkedItem = i;
            }
        }
        new AlertDialog.Builder(getActivity())
                .setSingleChoiceItems(wifiSSIDs, checkedItem, (dialog, which) -> {
                    mStationSsidET.setText(wifiSSIDs[which]);
                    BlufiScanResult result = results.get(which);
                    dialog.dismiss();
                })
                .show();
    }

    private void configure() {
        mStationSsidET.setError(null);

        final BlufiConfigureParams params = checkInfo();
        if (params == null) {
            LogUtils.d("Generate configure params null");
            return;
        }
        BlufiActivity blufiActivity = (BlufiActivity) getActivity();
        if (blufiActivity != null){
            blufiActivity.config(params);
        }
        Intent intent = new Intent(getActivity(),ConfigSuccessActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("bluDevice",mDevice);
        intent.putExtras(bundle);
        //intent.putExtra("device",mDevice);
        startActivity(intent);
        //skipPage(ConfigSuccessActivity.class);
    }

    private BlufiConfigureParams checkInfo() {
        BlufiConfigureParams params = new BlufiConfigureParams();
//        int deviceMode = BlufiParameter.OP_MODE_STA;
        params.setOpMode(BlufiParameter.OP_MODE_STA);
        if (checkSta(params)) {
            return params;
        } else {
            return null;
        }
    }

    private boolean checkSta(BlufiConfigureParams params) {
        String ssid = mStationSsidET.getText().toString();
        if (TextUtils.isEmpty(ssid)) {
            mStationSsidET.setError(getString(R.string.configure_station_ssid_error));
            return false;
        }
        byte[] ssidBytes = (byte[]) mStationSsidET.getTag();
        params.setStaSSIDBytes(ssidBytes != null ? ssidBytes : ssid.getBytes());
        String password = mStationPasswordET.getText().toString();
        params.setStaPassword(password);
        LogUtils.d("Huz checkSta"+params);

        int freq = -1;
        if (ssid.equals(getConnectionSSID())) {
            freq = getConnectionFrequncy();
        }
        if (freq == -1) {
            for (ScanResult sr : mWifiList) {
                if (ssid.equals(sr.SSID)) {
                    freq = sr.frequency;
                    break;
                }
            }
        }
        if (XxjNetUtil.is5GHz(freq)) {
            mStationSsidET.setError(getString(R.string.configure_station_wifi_5g_error));
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.configure_station_wifi_5g_dialog_message)
                    .setPositiveButton(R.string.configure_station_wifi_5g_dialog_continue, (dialog, which) -> {
                        //finishWithParams(params);
                    })
                    .setNegativeButton(R.string.configure_station_wifi_5g_dialog_cancel, null)
                    .show();
            return false;
        }

        return true;
    }

    /**
     * 获取连接wifi的频率
     * @return
     */
    private int getConnectionFrequncy() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return -1;
        }

        if (!mWifiManager.isWifiEnabled()) {
            return -1;
        }

        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            return -1;
        }

        return wifiInfo.getFrequency();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
