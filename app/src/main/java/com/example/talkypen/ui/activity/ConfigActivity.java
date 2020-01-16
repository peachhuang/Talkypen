package com.example.talkypen.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.talkypen.R;
import com.example.talkypen.framework.ui.BaseActivity;
import com.example.talkypen.framework.utils.LogUtils;
import com.example.talkypen.ui.constants.BlufiConstants;
import com.example.talkypen.widget.TopView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import androidx.appcompat.app.AlertDialog;
import blufi.espressif.params.BlufiConfigureParams;
import blufi.espressif.params.BlufiParameter;
import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tools.xxj.phiman.net.XxjNetUtil;

public class ConfigActivity extends BaseActivity {

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

    private WifiManager mWifiManager;
    private List<ScanResult> mWifiList;
    private boolean mScanning = false;

    private HashMap<String, String> mApMap;
    private List<String> mAutoCompleteSSIDs;
    private ArrayAdapter<String> mAutoCompleteSSIDAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        topView.setTitle("配网");

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        mApMap = new HashMap<>();
        mAutoCompleteSSIDs = new LinkedList<>();
        mWifiList = new ArrayList<>();

        findViewById(R.id.station_wifi_scan).setOnClickListener(v -> scanWifi());
        mAutoCompleteSSIDAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, mAutoCompleteSSIDs);

        mStationSsidET.setText(getConnectionSSID());
        WifiInfo info = ((WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE)).getConnectionInfo();
        if (info != null) {
            byte[] ssidBytes = XxjNetUtil.getOriginalSsidBytes(info);
            mStationSsidET.setTag(ssidBytes);
        }

        findViewById(R.id.confirm).setOnClickListener(v -> configure());

        Observable.just(this)
                .subscribeOn(Schedulers.io())
                .doOnNext(ConfigActivity::updateWifi)
                .subscribe();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_config;
    }

    @OnClick({R.id.confirm})
    public void onClick(View view){
        switch (view.getId()){
//            case R.id.station_wifi_scan:
//                LogUtils.d("Huz scanWifi");
//                scanWifi();
//                break;
//            case R.id.confirm:
//                configure();
//                break;
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

    private void scanWifi() {
        LogUtils.d("Huz scanWifi");
        if (!mWifiManager.isWifiEnabled()) {
            Toast.makeText(this, R.string.configure_wifi_disable_msg, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mScanning) {
            return;
        }

        mScanning = true;

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage(getString(R.string.configure_station_wifi_scanning));
        dialog.show();

        Observable.just(mWifiManager)
                .subscribeOn(Schedulers.io())
                .doOnNext(wm -> {
                    wm.startScan();
                    try {
                        Thread.sleep(1500L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    updateWifi();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                    dialog.dismiss();
                    showWifiListDialog();
                    mScanning = false;
                })
                .subscribe();
    }

    /**
     * 扫描wifi,并将扫描到的WiFi名称添加道WiFi列表当中去
     */
    private void updateWifi() {
        final List<ScanResult> scans = new LinkedList<>();
        Observable.fromIterable(mWifiManager.getScanResults())
                .filter(scanResult -> {
                    if (TextUtils.isEmpty(scanResult.SSID)) {
                        return false;
                    }

                    boolean contain = false;
                    for (ScanResult srScaned : scans) {
                        if (srScaned.SSID.equals(scanResult.SSID)) {
                            contain = true;
                            break;
                        }
                    }
                    return !contain;
                })
                .doOnNext(scans::add)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                    mWifiList.clear();
                    mWifiList.addAll(scans);

                    mAutoCompleteSSIDs.clear();
                    Set<String> apDBSet = mApMap.keySet();
                    mAutoCompleteSSIDs.addAll(apDBSet);
                    Observable.fromIterable(mWifiList)
                            .filter(scanResult -> !apDBSet.contains(scanResult.SSID))
                            .doOnNext(scanResult -> mAutoCompleteSSIDs.add(scanResult.SSID))
                            .subscribe();
                    mAutoCompleteSSIDAdapter.notifyDataSetChanged();
                })
                .subscribe();
    }

    /**
     * 将新的WiFi列表展示到ui界面dialog上
     */
    private void showWifiListDialog() {
        int count = mWifiList.size();
        if (count == 0) {
            Toast.makeText(this, R.string.configure_station_wifi_scanning_nothing, Toast.LENGTH_SHORT).show();
            return;
        }

        int checkedItem = -1;
        String inputSsid = mStationSsidET.getText().toString();
        final String[] wifiSSIDs = new String[count];
        for (int i = 0; i < count; i++) {
            ScanResult sr = mWifiList.get(i);
            wifiSSIDs[i] = sr.SSID;
            if (inputSsid.equals(sr.SSID)) {
                checkedItem = i;
            }
        }
        new AlertDialog.Builder(this)
                .setSingleChoiceItems(wifiSSIDs, checkedItem, (dialog, which) -> {
                    mStationSsidET.setText(wifiSSIDs[which]);
                    ScanResult scanResult = mWifiList.get(which);
                    byte[] ssidBytes = XxjNetUtil.getOriginalSsidBytes(scanResult);
                    mStationSsidET.setTag(ssidBytes);
                    dialog.dismiss();
                })
                .show();
    }

    private void configure() {
//        mSoftAPSsidET.setError(null);
//        mSoftAPPAsswordET.setError(null);
        mStationSsidET.setError(null);

        final BlufiConfigureParams params = checkInfo();
        if (params == null) {
            LogUtils.d("Generate configure params null");
            return;
        }

        finishWithParams(params);
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
            new AlertDialog.Builder(this)
                    .setMessage(R.string.configure_station_wifi_5g_dialog_message)
                    .setPositiveButton(R.string.configure_station_wifi_5g_dialog_continue, (dialog, which) -> {
                        finishWithParams(params);
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

    private void finishWithParams(BlufiConfigureParams params) {
        Intent intent = new Intent();
        intent.putExtra(BlufiConstants.KEY_CONFIGURE_PARAM, params);

//        saveAP(params);

        setResult(RESULT_OK, intent);
        finish();
    }

}
