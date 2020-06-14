package com.rdc.p2p.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rdc.p2p.R;
import com.rdc.p2p.app.App;
import com.rdc.p2p.base.BaseActivity;
import com.rdc.p2p.base.BasePresenter;
import com.rdc.p2p.bean.GroupBean;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.event.LinkGroupSocketResponseEvent;
import com.rdc.p2p.fragment.GroupListFragment;
import com.rdc.p2p.fragment.PeerListFragment;
import com.rdc.p2p.fragment.PersonalDetailFragment;
import com.rdc.p2p.fragment.ScanDeviceFragment;
import com.rdc.p2p.manager.SocketManager;
import com.rdc.p2p.thread.SocketThread;
import com.ycl.tabview.library.TabView;
import com.ycl.tabview.library.TabViewChild;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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

    public static final int ADD_GROUP_CHAT_ACTIVITY_CODE = 3;
    public static final int GROUP_CHAT_DETAIL_ACTIVITY_CODE = 4;
    public static final int CHAT_DETAIL_ACTIVITY_CODE = 1;

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
    private int currentPosition;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
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
        EventBus.getDefault().register(this);
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

        TabViewChild tabViewChild02 = new TabViewChild(R.drawable.tab02_sel, R.drawable.tab02_unsel, "群聊", new GroupListFragment());
        TabViewChild tabViewChild05 = new TabViewChild(R.drawable.tab05_sel, R.drawable.tab05_unsel, "我的", new PersonalDetailFragment());
        tabViewChildList.add(tabViewChild01);
        tabViewChildList.add(tabViewChild02);
        tabViewChildList.add(tabViewChild05);
        //end add data
        tabView.setTabViewDefaultPosition(0);
        tabView.setTabViewChild(tabViewChildList, getSupportFragmentManager());
        TabView.OnTabChildClickListener onTabChildClickListener = new TabView.OnTabChildClickListener() {
            @Override
            public void onTabChildClick(int position, ImageView imageView, TextView textView) {
                currentPosition = position;
                invalidateOptionsMenu();
            }
        };
        tabView.setOnTabChildClickListener(onTabChildClickListener);
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mToolbar.setTitle("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // 动态设置ToolBar状态
        switch (currentPosition) {
            case 0:
                menu.findItem(R.id.menu_search).setVisible(true);
                menu.findItem(R.id.menu_add).setVisible(false);
                break;
            case 1:
                menu.findItem(R.id.menu_search).setVisible(true);
                menu.findItem(R.id.menu_add).setVisible(true);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG,"click menu");
        switch (item.getItemId()) {
            case R.id.menu_search:
                if (mPeerListFragment.isServerSocketConnected()) {
                    ScanDeviceFragment mScanDeviceFragment = new ScanDeviceFragment();
                    mScanDeviceFragment.setCancelable(false);
                    mScanDeviceFragment.show(getSupportFragmentManager(), "scanDevice");
                } else {
                    showToast("ServerSocket未连接，请检查WIFI！");
                }
                break;
            case R.id.menu_add:
                Intent intent = new Intent();
                intent.setClass(mContext,AddGroupActivity.class);
                startActivityForResult(intent,3);
                break;
//            case android.R.id.home:
//                mDrawerLayout.openDrawer(GravityCompat.START);
//                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(TAG,"return from activity"+requestCode);
        if(data == null)
            return;
        switch (requestCode) {
            case CHAT_DETAIL_ACTIVITY_CODE:
                if (resultCode == RESULT_OK) {
                    mPeerListFragment.getmPeerListRvAdapter().updateItemText("", data.getStringExtra("ip"));
                }
                break;
            case ADD_GROUP_CHAT_ACTIVITY_CODE:
                String groupJson = data.getStringExtra("groupBean");
                GroupBean groupBean = new Gson().fromJson(groupJson,GroupBean.class);
                // 更新视图层
                GroupListFragment.getGroupListAdapter().addItem(groupBean);
                ThreadPoolExecutor mExecutor = new ThreadPoolExecutor(1, 255,
                        1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
                        255));
                // 向所有IP地址发送一个
                for (final PeerBean pb : groupBean.getPeerBeanList()) {
                    Log.d(TAG,"选取的用户为："+pb.getNickName());
                    if(pb.getNickName().equals(App.getUserBean().getNickName()))
                        continue;
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            SocketThread socketThread = SocketManager.getInstance().getSocketThreadByIp(pb.getUserIp());
                            if (socketThread != null) {
                                socketThread.sendGroupChatRequest(groupBean);
                            } else {
                                Looper.prepare();
                                showToast("目标用户连接已断开");
                                Looper.loop();
                            }
                        }
                    });
                }
                showToast("邀请已发送");
                mExecutor.shutdown();
                break;
            case GROUP_CHAT_DETAIL_ACTIVITY_CODE:
                if (resultCode == RESULT_OK) {
                    // 这里是接收返回的最近消息，在前端进行更改
                    GroupListFragment.getGroupListAdapter().updateItemText("",data.getStringExtra("groupName"));
                }
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveGroupChatInvitation(LinkGroupSocketResponseEvent linkGroupSocketResponseEvent){
        // TODO 有个问题，在没有点击群聊之前，GroupListFragment似乎还没有初始化，所以发送的群聊邀请也无法接收
        Log.d(TAG,"对群聊邀请作出反应：");
        GroupListFragment.getGroupListAdapter().addItem(linkGroupSocketResponseEvent.getGroupBean());
    }

    @Override
    protected void initListener() {
//        mLlBottomLeft.setOnClickListener(this);
//        mLlBottomRight.setOnClickListener(this);
    }

//    @Override
//    public void onClick(View view) {
//        switch (view.getId()){
//            case R.id.ll_bottom_right_layout_act_main:
//                mIvChat.setImageResource(R.drawable.iv_chat_pressed);
//                mTvChat.setTextColor(getResources().getColor(R.color.colorPrimary));
//                mIvPeerList.setImageResource(R.drawable.iv_peer_list_normal);
//                mTvPeerList.setTextColor(getResources().getColor(R.color.grey_text_or_bg));
//                break;
//            case R.id.ll_bottom_left_layout_act_main:
//                mIvChat.setImageResource(R.drawable.iv_chat_normal);
//                mTvChat.setTextColor(getResources().getColor(R.color.grey_text_or_bg));
//                mIvPeerList.setImageResource(R.drawable.iv_peer_list_pressed);
//                mTvPeerList.setTextColor(getResources().getColor(R.color.colorPrimary));
//                Log.d(TAG, "SocketManager:"+ SocketManager.getInstance().toString());
//                break;
//        }
//    }


//    private void initServerSocket() {
//        try {
//            mSocket = new MulticastSocket(BROADCAST_PORT);
//            mAddress = InetAddress.getByName(BROADCAST_IP);
//            mSocket.setTimeToLive(2);
//            mSocket.joinGroup(mAddress);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    private void receiveBroadcast(){
//        byte buf[] = new byte[1024];
//        DatagramPacket packet = new DatagramPacket(buf,buf.length,mAddress,BROADCAST_PORT);
//        try {
//            mSocket.receive(packet);
//            String content = new String(buf,0,packet.getLength());
//            Log.d(TAG, "receiveBroadcast: "+content);
//            MessageEntity message = new MessageEntity();
//            message.what = 0;
//            message.obj = content;
//            mHandler.sendMessage(message);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    private void sendMultiBroadcast(){
//        DatagramPacket packet;
//        String msg = mEtInput.getText().toString();
//        if (!msg.equals("")){
//            byte[] bytes = msg.getBytes();
//            packet = new DatagramPacket(bytes,bytes.length,mAddress,BROADCAST_PORT);
//            try {
//                mSocket.send(packet);
//                Log.d(TAG, "sendMultiBroadcast: success");
//            } catch (IOException e) {
//                e.printStackTrace();
//                Log.d(TAG, "sendMultiBroadcast: error");
//            }
//        }else {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(MainActivity.this, "输入不能为空！", Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    }
}
