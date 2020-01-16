package com.example.talkypen.ui.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.location.LocationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.talkypen.R;
import com.example.talkypen.framework.base.BlufiApp;
import com.example.talkypen.framework.ui.BaseActivity;
import com.example.talkypen.framework.utils.LogUtils;
import com.example.talkypen.ui.constants.BlufiConstants;
import com.example.talkypen.ui.constants.SettingsConstants;
import com.example.talkypen.widget.TopView;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import butterknife.BindView;
import butterknife.OnClick;
import tools.xxj.phiman.app.XxjPermissionHelper;
import tools.xxj.phiman.ble.XxjBleScanListener;
import tools.xxj.phiman.ble.XxjBleUtils;

public class SearchActivity extends BaseActivity {

    @BindView(R.id.topView)
    TopView topView;
//    @BindView(R.id.refresh_layout)
//    SwipeRefreshLayout mRefreshLayout;
//    @BindView(R.id.recycler_view)
//    RecyclerView mRecyclerView;

    private static final long TIMEOUT_SCAN = 4000L;
    private static final int REQUEST_BLUFI = 0x10;
    private static final int REQUEST_PERMISSION = 0x01;
    private static final int MENU_SETTINGS = 0x01;

    private XxjPermissionHelper mPermissionHelper;
    private SwipeRefreshLayout mRefreshLayout;

    private RecyclerView mRecyclerView;
    private List<BluetoothDevice> mBleList;
    private BleAdapter mBleAdapter;

    private Map<BluetoothDevice, Integer> mDeviceRssiMap;
    private ScanCallback mScanCallback;
    private String mBlufiFilter;
    private volatile long mScanStartTime;

    private ExecutorService mThreadPool;
    private Future mUpdateFuture;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_search;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mThreadPool = Executors.newSingleThreadExecutor();

        mRefreshLayout = findViewById(R.id.refresh_layout);
        mRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mRefreshLayout.setOnRefreshListener(this::scan);

        mRecyclerView = findViewById(R.id.recycler_view);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        mRecyclerView.setLayoutManager(layoutManager);
        mBleList = new LinkedList<>();
        mBleAdapter = new BleAdapter();
        mRecyclerView.setAdapter(mBleAdapter);

        mDeviceRssiMap = new HashMap<>();
        mScanCallback = new ScanCallback();

        mPermissionHelper = new XxjPermissionHelper(this, REQUEST_PERMISSION);
        mPermissionHelper.setOnPermissionsListener((permission, granted) -> {
            if (granted && permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                mRefreshLayout.setRefreshing(true);
                scan();
            }
        });
        mPermissionHelper.requestAuthorities(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});

        topView.setTitle("点击以下设备进行连接");
    }

//    @OnClick({R.id.refresh_layout})
//    public void onClick(View view){
//        switch (view.getId()){
//            case R.id.refresh_layout:
//                scan();
//                break;
//        }
//    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopScan();
        mThreadPool.shutdownNow();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_BLUFI) {
            mRefreshLayout.setRefreshing(true);
            scan();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_SETTINGS, 0, R.string.main_menu_settings);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == MENU_SETTINGS) {
//            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void scan() {
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Toast.makeText(this, R.string.main_bt_disable_msg, Toast.LENGTH_SHORT).show();
            mRefreshLayout.setRefreshing(false);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check location enable
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean locationEnable = locationManager != null && LocationManagerCompat.isLocationEnabled(locationManager);
            if (!locationEnable) {
                Toast.makeText(this, R.string.main_location_disable_msg, Toast.LENGTH_SHORT).show();
                mRefreshLayout.setRefreshing(false);
                return;
            }
        }

        mDeviceRssiMap.clear();
        mBleList.clear();
        mBleAdapter.notifyDataSetChanged();
        mBlufiFilter = (String) BlufiApp.getInstance().settingsGet(SettingsConstants.PREF_SETTINGS_KEY_BLE_PREFIX,
                BlufiConstants.BLUFI_PREFIX);
        mScanStartTime = SystemClock.elapsedRealtime();

        LogUtils.d("Start scan ble");
        XxjBleUtils.startScanBle(mScanCallback);//开始扫描蓝牙设备
        mUpdateFuture = mThreadPool.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

                long scanCost = SystemClock.elapsedRealtime() - mScanStartTime;
                if (scanCost > TIMEOUT_SCAN) {
                    break;
                }

                onIntervalScanUpdate(false);
            }

            XxjBleUtils.stopScanBle(mScanCallback);
            onIntervalScanUpdate(true);
            LogUtils.d("Scan ble thread is interrupted");
        });
    }

    /*private boolean isWifiOpened() {
        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }*/

    private void stopScan() {
        XxjBleUtils.stopScanBle(mScanCallback);
        if (mUpdateFuture != null) {
            mUpdateFuture.cancel(true);
        }
        LogUtils.d("Stop scan ble");
    }

    private void onIntervalScanUpdate(boolean over) {
        List<BluetoothDevice> devices = new LinkedList<>(mDeviceRssiMap.keySet());
        Collections.sort(devices, (dev1, dev2) -> {
            Integer rssi1 = mDeviceRssiMap.get(dev1);
            Integer rssi2 = mDeviceRssiMap.get(dev2);
            assert rssi1 != null && rssi2 != null;
            return rssi2.compareTo(rssi1);
        });
        Log.d("SerachActivity","HuZ ScanUpdate");
        runOnUiThread(() -> {
            mBleList.clear();
            mBleList.addAll(devices);
            Log.d("SerachActivity","HuZ BluetoothDevices"+devices);
            mBleAdapter.notifyDataSetChanged();

            if (over) {
                mRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void gotoDevice(BluetoothDevice device) {
        Intent intent = new Intent(SearchActivity.this, ConnectActivity.class);
        intent.putExtra(BlufiConstants.KEY_BLE_DEVICE, device);
        startActivityForResult(intent, REQUEST_BLUFI);
//        skipPage(ConnectActivity.class);

        mDeviceRssiMap.clear();
        mBleList.clear();
        mBleAdapter.notifyDataSetChanged();
    }

    private class BleHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        BluetoothDevice device;
        TextView text1;
        TextView text2;

        BleHolder(View itemView) {
            super(itemView);

            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            stopScan();
            gotoDevice(device);
        }
    }

    private class ScanCallback implements XxjBleScanListener {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord, ScanResult scanResult) {
            String name = device.getName();
            if (!TextUtils.isEmpty(mBlufiFilter)) {
                if (name == null || !name.startsWith(mBlufiFilter)) {
                    return;
                }
            }

            mDeviceRssiMap.put(device, rssi);
        }
    }

    private class BleAdapter extends RecyclerView.Adapter<BleHolder> {

        @NonNull
        @Override
        public BleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.main_ble_item, parent, false);
            return new BleHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BleHolder holder, int position) {
            BluetoothDevice device = mBleList.get(position);
            holder.device = device;

            String name = device.getName() == null ? getString(R.string.string_unknown) : device.getName();
            holder.text1.setText(name);

            SpannableStringBuilder info = new SpannableStringBuilder();
            info.append("Mac:").append(device.getAddress())
                    .append(" RSSI:").append(String.valueOf(mDeviceRssiMap.get(device)));
            info.setSpan(new ForegroundColorSpan(0xFF9E9E9E), 0, 21, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            info.setSpan(new ForegroundColorSpan(0xFF8D6E63), 21, info.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            holder.text2.setText(info);
        }

        @Override
        public int getItemCount() {
            return mBleList.size();
        }
    }

}
