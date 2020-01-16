package com.example.talkypen.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.talkypen.R;
import com.example.talkypen.framework.ui.BaseActivity;
import com.example.talkypen.widget.TopView;

import butterknife.BindView;
import butterknife.OnClick;

public class PromptActivity extends BaseActivity {

    @BindView(R.id.topView)
    TopView topView;
    @BindView(R.id.bt_start)
    Button btStart;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_prompt;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        topView.setTitle("配网提示");
        //promptTopview.setLeftVisb(false);
//        Button btStart = findViewById(R.id.bt_start);
//        btStart.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                skipPage(SearchActivity.class);
//            }
//        });
    }

    @OnClick({R.id.bt_start})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.bt_start:
                skipPage(SearchActivity.class);
        }
    }


}
