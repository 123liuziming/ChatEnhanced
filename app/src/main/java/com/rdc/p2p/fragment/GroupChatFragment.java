package com.rdc.p2p.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.rdc.p2p.R;
import com.rdc.p2p.activity.AddFriendToChat;
import com.rdc.p2p.activity.LoginActivity;
import com.rdc.p2p.app.App;
import com.rdc.p2p.base.BaseFragment;
import com.rdc.p2p.base.BasePresenter;
import com.rdc.p2p.bean.MyDnsBean;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.manager.SocketManager;
import com.rdc.p2p.thread.SocketThread;
import com.rdc.p2p.util.ImageUtil;
import com.rdc.p2p.util.MyDnsUtil;

import org.litepal.crud.DataSupport;

import java.util.Collection;

import butterknife.BindView;

public class GroupChatFragment extends BaseFragment {
    @Override
    protected int setLayoutResourceId() {
        return R.layout.fragment_group_chat;
    }
    @SuppressLint("CheckResult")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
    @Override
    protected BasePresenter getInstance() {
        return null;
    }

    @Override
    protected void initData(Bundle bundle) {

    }
    @SuppressLint("SetTextI18n")
    @Override
    protected void initView() {
        //TODO
        //这里要显示已经加入的群聊
    }
    @Override
    protected void setListener() {

    }
}
