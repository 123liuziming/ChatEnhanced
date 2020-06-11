package com.rdc.p2p.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.rdc.p2p.R;
import com.rdc.p2p.app.App;
import com.rdc.p2p.base.BaseActivity;
import com.rdc.p2p.base.BasePresenter;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.config.FileState;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.fragment.FragmentCommon;
import com.rdc.p2p.fragment.PeerListFragment;
import com.rdc.p2p.fragment.PersonalDetailFragment;
import com.rdc.p2p.fragment.ScanDeviceFragment;
import com.rdc.p2p.manager.SocketManager;
import com.rdc.p2p.thread.SocketThread;
import com.ycl.tabview.library.TabView;
import com.ycl.tabview.library.TabViewChild;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;

public class MainActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
//    @BindView(R.id.dl_drawer_act_main)
//    DrawerLayout mDrawerLayout;
//    @BindView(R.id.ll_bottom_left_layout_act_main)
//    LinearLayout mLlBottomLeft;
//    @BindView(R.id.iv_chat_act_main)
//    ImageView mIvChat;
//    @BindView(R.id.iv_peer_list_act_main)
//    ImageView mIvPeerList;
//    @BindView(R.id.tv_chat_act_main)
//    TextView mTvChat;
//    @BindView(R.id.tv_peer_list_act_main)
//    TextView mTvPeerList;
//    @BindView(R.id.ll_bottom_right_layout_act_main)
//    LinearLayout mLlBottomRight;

    private FragmentPagerAdapter mFragmentPagerAdapter;
    private boolean checking = true;// true 选中聊天列表 , false 选中 聊天室
    private static final String TAG = "LYT";
    private static final int BROADCAST_PORT = 3000;
    private static final String BROADCAST_IP = "239.0.0.3";
    private MulticastSocket mSocket;
    private InetAddress mAddress;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 0:
//                    mTvShow.append("\n"+message.obj.toString());
                    break;
            }
            return true;
        }
    });
    private PeerListFragment mPeerListFragment;
    public final int ADD_GROUPCHAT_ACTIVITY_REQUEST_CODE = 2;

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
    public BasePresenter getInstance() {
        return null;
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_main;
    }

    @Override
    protected void initData() {

    }


    @Override
    protected void initView() {
        initToolbar();
//        ActionBarDrawerToggle mDrawerToggle =
//                new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.open, R.string.close);
//        mDrawerToggle.syncState();
//        mDrawerLayout.addDrawerListener(mDrawerToggle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mFragmentPagerAdapter = new FragmentPagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return mPeerListFragment;
                }
                return null;
            }

            @Override
            public int getCount() {
                return 1;
            }

            @Override
            public void restoreState(Parcelable state, ClassLoader loader) {
                super.restoreState(state, loader);
            }

            @Override
            public Parcelable saveState() {
                return super.saveState();
            }
        };
        TabView tabView = (TabView) findViewById(R.id.tabView);
        //start add data
        mPeerListFragment = new PeerListFragment();
        List<TabViewChild> tabViewChildList = new ArrayList<>();
        TabViewChild tabViewChild01 = new TabViewChild(R.drawable.tab01_sel, R.drawable.tab01_unsel, "聊天", mPeerListFragment);
        TabViewChild tabViewChild02 = new TabViewChild(R.drawable.tab02_sel, R.drawable.tab02_unsel, "群聊", FragmentCommon.newInstance("群聊"));
        TabViewChild tabViewChild05 = new TabViewChild(R.drawable.tab05_sel, R.drawable.tab05_unsel, "我的", new PersonalDetailFragment());
        tabViewChildList.add(tabViewChild01);
        tabViewChildList.add(tabViewChild02);
        tabViewChildList.add(tabViewChild05);
        //end add data
        tabView.setTabViewDefaultPosition(0);
        tabView.setTabViewChild(tabViewChildList, getSupportFragmentManager());
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
//            actionBar.setDisplayShowTitleEnabled(false);
        }
        getSupportActionBar().setTitle("主界面");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_search:
                if (mPeerListFragment.isServerSocketConnected()) {
                    ScanDeviceFragment mScanDeviceFragment = new ScanDeviceFragment();
                    // 正在扫面中，设置为false，此时无法使用返回键返回
                    mScanDeviceFragment.setCancelable(false);
                    mScanDeviceFragment.show(getSupportFragmentManager(), "scanDevice");
                } else {
                    showToast("ServerSocket未连接，请检查WIFI！");
                }
                break;
            case R.id.menu_createGroupChat:
                List<PeerBean> userList = mPeerListFragment.getUserList();
                // 先模拟数据进行测试
//                List<PeerBean> userList = new ArrayList<>();
//                PeerBean peerBean = new PeerBean();
//                peerBean.setNickName("JOJO");
//                peerBean.setUserImageId(10);
//                userList.add(peerBean);
                Intent intent = new Intent(getApplicationContext(), AddGroupChatActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                Bundle bundle = new Bundle();
                bundle.putSerializable("userList",(Serializable)userList);
                intent.putExtras(bundle);
                startActivityForResult(intent, ADD_GROUPCHAT_ACTIVITY_REQUEST_CODE);
                break;
//            case android.R.id.home:
//                mDrawerLayout.openDrawer(GravityCompat.START);
//                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if(resultCode == RESULT_OK) {
                    mPeerListFragment.getmPeerListRvAdapter().updateItemText("", data.getStringExtra("ip"));
                }
                break;
            case ADD_GROUPCHAT_ACTIVITY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    // 群聊创建返回主界面，发送群聊请求
                    final List<PeerBean> checkedUserList = (List<PeerBean>) data.getExtras().getSerializable("returnUserList");
                    ThreadPoolExecutor mExecutor = new ThreadPoolExecutor(1, 255,
                            1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
                            255));
                    // 向所有IP地址发送一个
                    for (final PeerBean pb : checkedUserList) {
                        Log.d(TAG,"选取的用户为："+pb.getNickName());
                        mExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                SocketThread socketThread = SocketManager.getInstance().getSocketThreadByIp(pb.getUserIp());
                                if (socketThread != null) {
                                    socketThread.sendGroupChatRequest(checkedUserList);
                                } else {
                                    showToast("目标用户连接已断开");
                                }
                            }
                        });
                    }
                    showToast("邀请已发送");
                    mExecutor.shutdown();

                }
                else if(resultCode == RESULT_CANCELED){
                    // 返回界面，什么也不做
                }
                break;
        }
    }

    @Override
    protected void initListener() {
//        mLlBottomLeft.setOnClickListener(this);
//        mLlBottomRight.setOnClickListener(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void createGroupChatRoom(List<PeerBean> groupMembers){
        
    }
}
