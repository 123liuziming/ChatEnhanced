package com.rdc.p2p.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.rdc.p2p.R;

public class GroupChatActivity extends Activity{
    String TAG = "GroupChatActivity";
    private static String groupName;
    private Intent it;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_detail);
        it = getIntent();
        Button bt = findViewById(R.id.rt_button);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        initTitle();
    }

    protected void initTitle(){
        //设置标题
        Bundle bd = it.getExtras();
        groupName = bd.getString("title");
        System.out.println(groupName);
        TextView title = findViewById(R.id.tv_title_group);
        title.setText(groupName);
    }


}
