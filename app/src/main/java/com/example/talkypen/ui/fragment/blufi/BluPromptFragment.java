package com.example.talkypen.ui.fragment.blufi;

import android.os.Bundle;
import android.view.View;

import com.example.talkypen.R;
import com.example.talkypen.framework.ui.BaseFragment;
import com.example.talkypen.ui.activity.BlufiActivity;
import com.example.talkypen.widget.TopView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import butterknife.BindView;
import butterknife.OnClick;

public class BluPromptFragment extends BaseFragment {


    @BindView(R.id.topView)
    TopView topView;

    private BluPromptFragment bluPromptFragment;
    private BluetoothFragment bluetoothFragment;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_blu_prompt;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        topView.setTitle("配网提示");

        bluPromptFragment = new BluPromptFragment();
        bluetoothFragment = new BluetoothFragment();
    }
    @OnClick({R.id.bt_start})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.bt_start:
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.blu_frame_content,bluetoothFragment).commit();
                break;
        }
    }
}
