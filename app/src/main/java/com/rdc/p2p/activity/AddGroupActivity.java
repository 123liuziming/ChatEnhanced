package com.rdc.p2p.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rdc.p2p.R;
import com.rdc.p2p.adapter.AddGroupPeerRvAdapter;
import com.rdc.p2p.app.App;
import com.rdc.p2p.base.BaseActivity;
import com.rdc.p2p.base.BasePresenter;
import com.rdc.p2p.bean.GroupBean;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.bean.UserBean;
import com.rdc.p2p.model.PeerListModel;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;

public class AddGroupActivity extends BaseActivity {

    public static final String TAG = "AddGroup";

    @BindView(R.id.rv_peer_list_fragment_peer_list)
    RecyclerView mRvPeerList;
    @BindView(R.id.check_button)
    Button checkButton;
    @BindView(R.id.group_name)
    EditText groupName;

    private AddGroupPeerRvAdapter mPeerListRvAdapter;
    private List<PeerBean> peerBeans;
    private BaseActivity mBaseActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"num="+peerBeans.size());
        mRvPeerList.setVisibility(View.VISIBLE);
        mPeerListRvAdapter.updateData(peerBeans);
    }

    @Override
    public BasePresenter getInstance() {
        return null;
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_add_group;
    }

    @Override
    protected void initData() {
        mBaseActivity=this;
        peerBeans = PeerListModel.peerBeans;
    }

    @Override
    protected void initView() {
        mPeerListRvAdapter = new AddGroupPeerRvAdapter((AddGroupActivity)mBaseActivity);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mBaseActivity,DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(mBaseActivity, R.drawable.bg_divider)));
        mRvPeerList.addItemDecoration(dividerItemDecoration);
        mRvPeerList.setLayoutManager(new LinearLayoutManager(mBaseActivity,LinearLayoutManager.VERTICAL,false));
        mRvPeerList.setAdapter(mPeerListRvAdapter);
    }

    @Override
    protected void initListener() {
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPeerListRvAdapter.selectPeer.size()==0){
                    Toast.makeText(mBaseActivity,"选中人数为0",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(groupName.getText().toString().length()==0){
                    Toast.makeText(mBaseActivity,"群聊名字不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                UserBean userBean = App.getUserBean();
                PeerBean myself = new PeerBean();
                myself.setNickName(userBean.getNickName());
                myself.setUserIp(App.getMyIP());
                myself.setUserImageId(userBean.getUserImageId());
                mPeerListRvAdapter.selectPeer.add(myself);
                GroupBean groupBean = new GroupBean();
                groupBean.setNickName(groupName.getText().toString());
                groupBean.setPeerBeanList(mPeerListRvAdapter.selectPeer);
                Intent intent = new Intent();
                Log.d(TAG,new Gson().toJson(groupBean));
                intent.putExtra("groupBean",new Gson().toJson(groupBean));
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
    public void updateCheckBox(int size){
        if(size==0){
            checkButton.setBackground(getResources().getDrawable(R.drawable.buttonshapegraybg));
            checkButton.setText("确认");
        }
        else{
            checkButton.setBackground(getResources().getDrawable(R.drawable.buttonshapebluebg));
            checkButton.setText("确认("+size+")");
        }
    }
}
