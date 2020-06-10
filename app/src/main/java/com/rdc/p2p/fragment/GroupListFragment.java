package com.rdc.p2p.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rdc.p2p.R;
import com.rdc.p2p.activity.AddGroupActivity;
import com.rdc.p2p.activity.ChatDetailActivity;
import com.rdc.p2p.adapter.GroupListRvAdapter;
import com.rdc.p2p.base.BaseFragment;
import com.rdc.p2p.bean.GroupBean;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.config.FileState;
import com.rdc.p2p.contract.GroupListContract;
import com.rdc.p2p.event.LinkGroupSocketResponseEvent;
import com.rdc.p2p.event.LinkSocketRequestEvent;
import com.rdc.p2p.event.LinkSocketResponseEvent;
import com.rdc.p2p.event.RecentMsgEvent;
import com.rdc.p2p.listener.OnClickRecyclerViewListener;
import com.rdc.p2p.manager.SocketManager;
import com.rdc.p2p.presenter.GroupListPresenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;

public class GroupListFragment extends BaseFragment<GroupListPresenter> implements GroupListContract.View {
    private static final String TAG = "GroupChatFragment";
    private static final int INIT_SERVER_SOCKET = 0;

    @BindView(R.id.rv_peer_list_fragment_peer_list)
    RecyclerView mRvPeerList;
    @BindView(R.id.ll_loadingPeersInfo_fragment_peer_list)
    LinearLayout mLlLoadingPeersInfo;
    @BindView(R.id.tv_tip_nonePeer_fragment_peer_list)
    TextView mTvTipNonePeer;

    private GroupListRvAdapter mGroupListRvAdapter;
    private WifiReceiver mWifiReceiver;
    private List<String> mGroupList;
    private boolean isFirstScanDeviceFinished;
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
        mPresenter.initSocket();
        EventBus.getDefault().register(this);
        mWifiReceiver = new WifiReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mBaseActivity.registerReceiver(mWifiReceiver, filter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        if (mBaseActivity != null){
            mBaseActivity.unregisterReceiver(mWifiReceiver);
        }else {
            Log.d(TAG, "onDestroyView：PeerListFragment Activity is null !");
        }
        if (mPresenter != null){
            mPresenter.disconnect();
        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void scanDeviceFinished(List<String> ipList){
        mGroupList.clear();
        mGroupList.addAll(ipList);
        if (isFirstScanDeviceFinished){
            //第一次扫描成功
            isFirstScanDeviceFinished = false;
        }else {
            if (mPresenter.isServerSocketConnected()){
                mPresenter.linkGroups(new ArrayList<>(mGroupList));
                mGroupList.clear();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void linkSocket(LinkSocketRequestEvent linkSocketRequestEvent){
        mPresenter.linkGroup(linkSocketRequestEvent.getTargetIp());
    }


    public boolean isServerSocketConnected(){
        return mPresenter.isServerSocketConnected();
    }


    @Override
    protected void initData(Bundle bundle) {
        isFirstScanDeviceFinished = true;
        mGroupList = new ArrayList<>();
    }


    @Override
    protected void initView() {
        mGroupListRvAdapter = new GroupListRvAdapter();
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
                Intent intent = new Intent();
                intent.setClass(getContext(), AddGroupActivity.class);
                startActivityForResult(intent,0);
//                GroupBean groupBean = mGroupListRvAdapter.getDataList().get(position);
//                mGroupListRvAdapter.clearItemBadge(position);
//                if (SocketManager.getInstance().isClosedSocket(groupBean.getUserIp())){
//                    showToast("正在建立Socket连接！");
//                    mPresenter.linkGroup(groupBean.getUserIp());
//                }else {
//                    ChatDetailActivity.actionStart(mBaseActivity,groupBean.getUserIp(),groupBean.getNickName(),groupBean.getGroupImageId(), position);
//                }
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
            mLlLoadingPeersInfo.setVisibility(View.GONE);
            mTvTipNonePeer.setVisibility(View.VISIBLE);
        }else {
            mRvPeerList.setVisibility(View.VISIBLE);
            mLlLoadingPeersInfo.setVisibility(View.GONE);
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
        mLlLoadingPeersInfo.setVisibility(View.GONE);
        mTvTipNonePeer.setVisibility(View.VISIBLE);
    }

    @Override
    public void linkGroupSuccess(String ip) {
        showToast("连接 Socket 成功！");
    }

    @Override
    public void linkGroupError(String message,String targetIp) {
        showToast(message);
        PeerBean peerBean = new PeerBean();
        peerBean.setUserIp(targetIp);
        EventBus.getDefault().post(new LinkSocketResponseEvent(false,peerBean));
    }

    @Override
    public void initServerSocketSuccess() {
        mPresenter.linkGroups(mGroupList);
    }

    public class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)) {
                case WifiManager.WIFI_STATE_ENABLED:
                    Log.d(TAG, "打开");
                    //已打开
                    if (!mPresenter.isServerSocketConnected()){
                        mHandler.sendEmptyMessage(INIT_SERVER_SOCKET);
                    }
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    //打开中
                    Log.d(TAG, "打开中");
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    //已关闭
                    Log.d(TAG, "已关闭");
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    //关闭中
                    Log.d(TAG, "关闭中");
                    mPresenter.disconnect();
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                    //未知
                    Log.d(TAG, "未知");
                    break;
            }
        }
    }
}
