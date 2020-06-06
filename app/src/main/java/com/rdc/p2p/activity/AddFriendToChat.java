package com.rdc.p2p.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.rdc.p2p.R;

import butterknife.BindView;


public class AddFriendToChat extends AppCompatActivity {
    Button btn_confirm;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_friend_to_group);
//        btn_confirm.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //TODO
//                //添加的操作
//
//            }
//        });
        btn_confirm = (Button)findViewById(R.id.button33);
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

}
