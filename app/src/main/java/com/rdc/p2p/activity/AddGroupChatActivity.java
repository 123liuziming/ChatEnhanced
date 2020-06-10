package com.rdc.p2p.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rdc.p2p.R;
import com.rdc.p2p.adapter.AddGroupChatUserAdapter;
import com.rdc.p2p.adapter.PeerListRvAdapter;
import com.rdc.p2p.base.BaseActivity;
import com.rdc.p2p.base.BasePresenter;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.listener.OnClickRecyclerViewListener;
import com.rdc.p2p.manager.SocketManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;

public class AddGroupChatActivity extends BaseActivity {

    private static final String TAG = "AddGroupChatList";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.rv_add_groupChat_list)
    RecyclerView addUserListRecyclerView;
    @BindView(R.id.tv_tip_noneUser_GroupChat)
    TextView mTipNoneContract;

    AddGroupChatUserAdapter addGroupChatUserAdapter;

    List<PeerBean> allUserList;
    List<PeerBean> userCheckList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void initListener() {
//        if(addGroupChatUserAdapter != null) {
//            addGroupChatUserAdapter.setOnRecyclerViewListener(new OnClickRecyclerViewListener() {
//                @Override
//                public void onItemClick(int position) {
//                    PeerBean peerBean = addGroupChatUserAdapter.getDataList().get(position);
//                    if (userCheckList.contains(peerBean)) {
//                        userCheckList.remove(peerBean);
//                        showToast(peerBean.getNickName() + "被选中");
//                    } else {
//                        userCheckList.add(peerBean);
//                        showToast(peerBean.getNickName() + "被移除");
//                    }
//                }
//
//                @Override
//                public boolean onItemLongClick(int position) {
//                    return false;
//                }
//            });
//        }
    }

    @Override
    public BasePresenter getInstance() {
        return null;
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_add_group_chat;
    }

    @Override
    protected void initData() {
        // 用户已经通过Intent包装传输
        Intent intent = getIntent();
        allUserList = (List<PeerBean>) intent.getExtras().getSerializable("userList");
        showToast(allUserList.size()+"个联系人可用");
    }


    @Override
    protected void initView() {
        initToolbar();
        if(allUserList.size()==0){
            addUserListRecyclerView.setVisibility(View.GONE);
            mTipNoneContract.setVisibility(View.VISIBLE);
        }
        else {
            mTipNoneContract.setVisibility(View.GONE);
            addUserListRecyclerView.setVisibility(View.VISIBLE);
            // 设置recyclerview的adapter
            addGroupChatUserAdapter = new AddGroupChatUserAdapter(allUserList);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
            dividerItemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.bg_divider)));
            addUserListRecyclerView.addItemDecoration(dividerItemDecoration);
            addUserListRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            addUserListRecyclerView.setAdapter(addGroupChatUserAdapter);
        }
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }
        getSupportActionBar().setTitle("请选择群聊成员");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_group_chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.add_group_chat_menu_confirm:
                // 确认所选成员创建群聊
                SparseBooleanArray sparseBooleanArray = addGroupChatUserAdapter.getmSelectedPositions();
                for (int i = 0; i < sparseBooleanArray.size(); i++) {
                    if(sparseBooleanArray.get(i)){
                        userCheckList.add(allUserList.get(i));
                    }
                }
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("userList",(Serializable)userCheckList);
                intent.putExtras(bundle);
                setResult(RESULT_CANCELED,intent);
                // 开始发送Socket请求处理
                finish();
                break;
        }
        return true;
    }

}
