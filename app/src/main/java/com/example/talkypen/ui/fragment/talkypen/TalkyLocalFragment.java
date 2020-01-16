package com.example.talkypen.ui.fragment.talkypen;

import android.os.Bundle;

import com.example.talkypen.R;
import com.example.talkypen.framework.ui.BaseFragment;
import com.example.talkypen.widget.TopView;

import androidx.annotation.Nullable;
import butterknife.BindView;

public class TalkyLocalFragment extends BaseFragment {

    @BindView(R.id.topView)
    TopView topView;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_talky_local;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        topView.setTitle("本地内容");
    }
}
