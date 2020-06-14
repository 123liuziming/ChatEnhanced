package com.rdc.p2p.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.rdc.p2p.R;
import com.rdc.p2p.activity.GroupChatDetailActivity;
import com.rdc.p2p.adapter.GroupListRvAdapter;
import com.rdc.p2p.app.App;
import com.rdc.p2p.base.BaseActivity;
import com.rdc.p2p.base.BaseFragment;
import com.rdc.p2p.bean.GroupBean;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.bean.UserBean;
import com.rdc.p2p.contract.GroupListContract;
import com.rdc.p2p.listener.OnClickRecyclerViewListener;
import com.rdc.p2p.presenter.GroupListPresenter;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;

public class GroupListFragment extends BaseFragment<GroupListPresenter> implements GroupListContract.View {
    private static final String TAG = "GroupListFragment";
    private static final int INIT_SERVER_SOCKET = 0;

    @BindView(R.id.rv_peer_list_fragment_peer_list)
    RecyclerView mRvPeerList;
    @BindView(R.id.tv_tip_nonePeer_fragment_peer_list)
    TextView mTvTipNonePeer;

    private static GroupListRvAdapter mGroupListRvAdapter = new GroupListRvAdapter();
    private List<GroupBean> mGroupList;
    private Handler mHandler =new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what){
                case INIT_SERVER_SOCKET:
                    mPresenter.initSocket();
                    break;
            }
            return true;
        }
    });
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

    }
    @Override
    protected int setLayoutResourceId() {
        return R.layout.fragment_group_chat;
    }
    @Override
    protected GroupListPresenter getInstance() {
        return new GroupListPresenter(mBaseActivity);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG,"size:"+mGroupList.size());
        updateGroupList(mGroupList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        if (mPresenter != null){
            mPresenter.disconnect();
        }
    }


    public boolean isServerSocketConnected(){
        return mPresenter.isServerSocketConnected();
    }


    @Override
    protected void initData(Bundle bundle) {
        mGroupList = new ArrayList<>();
        PeerBean testBean= new PeerBean();
        UserBean userBean= App.getUserBean();
        testBean.setUserImageId(userBean.getUserImageId());
        testBean.setNickName(userBean.getNickName());
        testBean.setUserIp(App.getMyIP());
        GroupBean groupBean= new GroupBean();
        groupBean.getPeerBeanList().add(testBean);
        groupBean.getPeerBeanList().add(testBean);
        groupBean.getPeerBeanList().add(testBean);
        groupBean.getPeerBeanList().add(testBean);
        groupBean.setNickName("testGroup");
        groupBean.setGroupImageId(userBean.getUserImageId());
        groupBean.setRecentMessage("这是测试群聊");
        mGroupList.add(groupBean);
    }

    public static GroupListRvAdapter getGroupListAdapter() {
        return mGroupListRvAdapter;
    }


    @Override
    protected void initView() {
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mBaseActivity,DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(mBaseActivity, R.drawable.bg_divider)));
        mRvPeerList.addItemDecoration(dividerItemDecoration);
        mRvPeerList.setLayoutManager(new LinearLayoutManager(mBaseActivity,LinearLayoutManager.VERTICAL,false));
        mRvPeerList.setAdapter(mGroupListRvAdapter);
    }

    @Override
    protected void setListener() {
        mGroupListRvAdapter.setOnRecyclerViewListener(new OnClickRecyclerViewListener() {
            @Override
            public void onItemClick(int position) {
                //这里进入群聊
                //传入需要的参数
                GroupBean groupBean= mGroupListRvAdapter.getDataList().get(position);
                GroupChatDetailActivity.actionStart((BaseActivity) getContext(),groupBean,position);
            }
            @Override
            public boolean onItemLongClick(int position) {
                return false;
            }

        });
    }

    @Override
    public void updateGroupList(List<GroupBean> list) {
        if (list.size() == 0){
            mRvPeerList.setVisibility(View.GONE);
            mTvTipNonePeer.setVisibility(View.VISIBLE);
        }else {
            mRvPeerList.setVisibility(View.VISIBLE);
            mTvTipNonePeer.setVisibility(View.GONE);
            mGroupListRvAdapter.updateData(list);
        }
    }

    @Override
    public void messageReceived(MessageBean messageBean) {
    }

    @Override
    public void fileReceiving(MessageBean messageBean) {
    }

    @Override
    public void addGroup(GroupBean groupBean) {
    }

    @Override
    public void removeGroup(String ip) {
    }

    @Override
    public void serverSocketError(String msg) {
        showToast(msg);
        mRvPeerList.setVisibility(View.GONE);
        mTvTipNonePeer.setVisibility(View.VISIBLE);
    }

    @Override
    public void linkGroupSuccess(String ip) {
        showToast("连接 Socket 成功！");
    }

    @Override
    public void linkGroupError(String message, String targetIp) {

    }


    @Override
    public void initServerSocketSuccess() {

    }
}
