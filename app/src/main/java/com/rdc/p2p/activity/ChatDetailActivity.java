package com.rdc.p2p.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.MessagesBetweenQuery;
import com.example.SaveMessagesMutation;
import com.example.type.MessageInput;
import com.rdc.p2p.R;
import com.rdc.p2p.adapter.MsgRvAdapter;
import com.rdc.p2p.adapter.PeerListRvAdapter;
import com.rdc.p2p.app.App;
import com.rdc.p2p.base.BaseActivity;
import com.rdc.p2p.bean.MyDnsBean;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.config.FileState;
import com.rdc.p2p.event.LinkSocketRequestEvent;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.config.Constant;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.contract.ChatDetailContract;
import com.rdc.p2p.event.LinkSocketResponseEvent;
import com.rdc.p2p.event.RecentMsgEvent;
import com.rdc.p2p.listener.OnItemViewClickListener;
import com.rdc.p2p.manager.SocketManager;
import com.rdc.p2p.presenter.ChatDetailPresenter;
import com.rdc.p2p.util.AudioRecorderUtil;
import com.rdc.p2p.util.MediaPlayerUtil;
import com.rdc.p2p.util.MyDnsUtil;
import com.rdc.p2p.util.ProgressTextUtil;
import com.rdc.p2p.util.SDUtil;
import com.rdc.p2p.util.ChatDatabaseUtil;
import com.rdc.p2p.widget.PlayerSoundView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Logger;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class ChatDetailActivity extends BaseActivity<ChatDetailPresenter> implements ChatDetailContract.View {

    private static final String TAG = "ChatDetailActivity";
    private static final int CHOOSE_PHOTO = 2;
    private static final int TAKE_PHOTO = 3;
    private static final int FILE_MANAGER = 4;
    private static final int SCROLL = -1;//滑动到底部
    private static final int SCROLL_NOW = -3;//立即滚动到底部
    private static final int HIDE_SOFT_INPUT = -2;//隐藏软键盘
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.iv_photo_album_act_chat_detail)
    ImageView mIvPhotoAlbum;
    @BindView(R.id.iv_take_photo_act_chat_detail)
    ImageView mIvTakePhoto;
    @BindView(R.id.iv_record_voice_act_chat_detail)
    ImageView mIvRecordVoice;
    @BindView(R.id.rv_msg_list_act_chat_detail)
    RecyclerView mRvMsgList;
    @BindView(R.id.btn_send_chat_detail)
    Button mBtnSend;
    @BindView(R.id.et_input_act_chat_detail)
    EditText mEtInput;
    @BindView(R.id.layout_root_act_chat_detail)
    ConstraintLayout mRootLayout;
    @BindView(R.id.tv_pressed_start_record_act_chat_detail)
    TextView mTvPressedStartRecord;
    @BindView(R.id.iv_file_act_chat_detail)
    ImageView mIvFile;

    private static int mTargetpos;
    private MsgRvAdapter mMsgRvAdapter;
    private static String mTargetPeerName;
    private static String mTargetPeerIp;
    private static int mTargetPeerImageId;
    private Uri mTakePhotoUri;
    private File mTakePhotoFile;
    private AudioRecorderUtil mAudioRecorderUtil;
    private MediaPlayerUtil mMediaPlayerUtil;
    private ImageView mIvMicrophone;
    private TextView mTvRecordTime;
    private PopupWindow mPwMicrophone;
    private boolean isSendingFile;//是否正在发送文件
    private PlayerSoundView mPsvIsPlaying = null;//正在播放音频的view
    SQLiteOpenHelper openHelper;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case SCROLL:
                    int lastItemPosition = mMsgRvAdapter.getItemCount() - 1;
                    if (lastItemPosition >= 0 &&
                            mRvMsgList.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                        //如果当前用户没有拖动列表，则自动滚动到最后一个
                        mRvMsgList.smoothScrollToPosition(lastItemPosition);
                    }
                    break;
                case SCROLL_NOW:
                    if (mMsgRvAdapter.getItemCount() - 1 > 0) {
                        mRvMsgList.smoothScrollToPosition(mMsgRvAdapter.getItemCount() - 1);
                    }
                    break;
                case HIDE_SOFT_INPUT:
                    hideKeyboard();
                    break;
            }
            return false;
        }
    });

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    // 活动被销毁时保存信息
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("peerName", mTargetPeerName);
        outState.putString("peerIp", mTargetPeerIp);
        outState.putInt("peerImageId", mTargetPeerImageId);
    }

    // 活动被销毁后重新create时运行
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTargetPeerName = savedInstanceState.getString("peerName");
        mTargetPeerIp = savedInstanceState.getString("peerIp");
        mTargetPeerImageId = savedInstanceState.getInt("peerImageId");
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        mAudioRecorderUtil.stopRecord();
        mMediaPlayerUtil.stopPlayer();
        mPwMicrophone.dismiss();
        super.onDestroy();
    }

    public static void actionStart(BaseActivity context, String peerIp, String peerName, int peerImageId, int position) {
        mTargetPeerName = peerName;
        mTargetPeerIp = peerIp;
        mTargetPeerImageId = peerImageId;
        mTargetpos = position;
        context.startActivityForResult(new Intent(context, ChatDetailActivity.class), MainActivity.CHAT_DETAIL_ACTIVITY_CODE);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed(false);
                break;
            case R.id.delete_chat:
                DataSupport.deleteAll(MessageBean.class, "belongName = ?", mTargetPeerName);
                mMsgRvAdapter.clearData();
                onBackPressed(true);
                showToast("已删除");
                break;
            case R.id.get_chat:
                ApolloClient apolloClient = ApolloClient.builder().serverUrl("http://49.232.12.147:4000").build();
                //apolloClient.query(MessagesBetweenQuery.MessagesBetween);
                final MessagesBetweenQuery messagesBetween = MessagesBetweenQuery.builder()
                        .participantA(App.getUserBean().getNickName())
                        .participantB(mTargetPeerName)
                        .build();

                Log.d(TAG, "用户A：" + App.getUserBean().getNickName());
                Log.d(TAG, "用户B：" + mTargetPeerName);

                apolloClient.query(messagesBetween)
                        .enqueue(new ApolloCall.Callback<MessagesBetweenQuery.Data>() {
                            @Override
                            public void onResponse(@NotNull Response<MessagesBetweenQuery.Data> response) {
                                Log.d(TAG, response.getData().toString());
                                if (response.getData().MessagesBetween() != null) {
                                    try {
//                                    ArrayList<MessagesBetweenQuery.MessagesBetween> mock = new ArrayList<>();
//                                    mock.add(new MessagesBetweenQuery.MessagesBetween("typename1", "content1", "sender1", "receiver1", "time1", Protocol.TEXT));
//                                    mock.add(new MessagesBetweenQuery.MessagesBetween("typename2", "content2", "sender2", "receiver2", "time2", Protocol.TEXT));
//                                    mock.add(new MessagesBetweenQuery.MessagesBetween("typename3", "content3", "sender3", "receiver3", "time3", Protocol.TEXT));

                                        runOnUiThread(() -> {
                                            for (MessagesBetweenQuery.MessagesBetween message : response.getData().MessagesBetween()) { // mock
                                                MessageBean textMsg = MessageBean.getInstance(mTargetPeerIp);
                                                textMsg.setMine(message.sender().equals(App.getUserBean().getNickName())); // send by me
                                                textMsg.setMsgType(message.type()); // Protocol.TEXT
                                                textMsg.setText(message.content()); // getString(mEtInput)
                                                textMsg.setSendStatus(Constant.SEND_MSG_FINISH);
                                                textMsg.setGroupMsg(false);
                                                textMsg.getDate();
                                                textMsg.setTime(message.time());
                                                textMsg.setUserName(App.getUserBean().getNickName());
                                                if (textMsg.save()) {
                                                    //showToast("成功保存到数据库！");
                                                } else {
                                                    showToast("保存到数据库失败！");
                                                }
                                                mMsgRvAdapter.appendData(textMsg);
                                            }
                                        });
                                        Looper.prepare();
                                        showToast("拉取了" + response.getData().MessagesBetween().size() + "条信息！");
                                        Looper.loop();
                                    } catch (NullPointerException e) {
                                        Log.e(TAG, e.getLocalizedMessage(), e);
                                        Looper.prepare();
                                        showToast("拉取失败！");
                                        Looper.loop();
                                    }
                                } else {
                                    Looper.prepare();
                                    showToast("拉取失败！");
                                    Looper.loop();
                                }
                            }

                            @Override
                            public void onFailure(@NotNull ApolloException e) {
                                Log.e(TAG, e.getLocalizedMessage(), e);
                                Looper.prepare();
                                showToast("拉取失败！");
                                Looper.loop();
                            }
                        });


                break;
            //Response(operation=com.example.MessagesBetweenQuery@38b6340, data=Data{MessagesBetween=null}, errors=null, dependentKeys=[], fromCache=false, extensions={}, executionContext=com.apollographql.apollo.http.OkHttpExecutionContext@fb6c404)
            //D/EGL_emulation: eglMakeCurrent: 0xf5c82540: ver 3 0 (tinfo 0xf5ff8f90)
        }
        return true;
    }

    public void onBackPressed(boolean flag) {
        ApolloClient apolloClient = ApolloClient.builder().serverUrl("http://49.232.12.147:4000").build();
        List<MessageBean> msgList = mMsgRvAdapter.getDataList();
        final List<MessageInput> messageInputs = new ArrayList<MessageInput>();
        for (MessageBean msgBean : msgList) {
            if (msgList.size() != 0) {
                final MessageInput messageInput = MessageInput.builder()
                        .content(msgBean.getText())
                        .sender(msgBean.getUserName())
                        .receiver(msgBean.getBelongName())
                        .time(msgBean.getTime())
                        .type(msgBean.getMsgType())
                        .build();
                messageInputs.add(messageInput);
            }
        }
        if(msgList.size() != 0) {
            final SaveMessagesMutation SaveMessages = SaveMessagesMutation.builder()
                    .initiator(App.getUserBean().getNickName())
                    .messages(messageInputs)
                    .build();
            apolloClient.mutate(SaveMessages);
            showToast("向云端推送了" + mMsgRvAdapter.getDataList().size() + "条信息！");
            Log.d(TAG, "推送了" + mMsgRvAdapter.getDataList().size() + "条信息！");
        }

        /***
         .enqueue(new ApolloCall.Callback<SaveMessages.Data>(){
        @Override public void onResponse(@NotNull Response<SaveMessages.Data> response) {
        Log.d(TAG, response.toString());
        try {
        Looper.prepare();
        showToast("推送了" + mMsgRvAdapter.getDataList().size() + "条信息！");
        Looper.loop();
        } catch (NullPointerException e) {
        Log.e(TAG, e.getLocalizedMessage(), e);
        Looper.prepare();
        showToast("推送失败！");
        Looper.loop();
        }
        }
        @Override public void onFailure(@NotNull ApolloException e) {
        Log.e(TAG, e.getLocalizedMessage(), e);
        Looper.prepare();
        showToast("推送失败！");
        Looper.loop();
        }
        });***/


        //Log.d(TAG, "用户A：" + App.getUserBean().getNickName());
        //Log.d(TAG, "用户B：" + mTargetPeerName);

        if (flag) {
            Intent intent = new Intent();
            intent.putExtra("ip", mTargetPeerIp);
            setResult(RESULT_OK, intent);
            finish();
        } else
            super.onBackPressed();
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }


    @Override
    public ChatDetailPresenter getInstance() {
        return new ChatDetailPresenter(this, mTargetPeerIp);
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_chat_detail;
    }

    @Override
    protected void initData() {
        mAudioRecorderUtil = new AudioRecorderUtil();
        mMediaPlayerUtil = MediaPlayerUtil.getInstance();
        isSendingFile = false;
    }

    @Override
    protected void initView() {
        initToolbar();
        mTvTitle.setText(mTargetPeerName);
        mMsgRvAdapter = new MsgRvAdapter(mTargetPeerImageId);
        LinearLayoutManager mLLManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRvMsgList.setLayoutManager(mLLManager);
        mRvMsgList.setAdapter(mMsgRvAdapter);
        mRvMsgList.getItemAnimator().setChangeDuration(0);
        List<MessageBean> mRvMsgBeanList = DataSupport.where("belongName = ? and userName = ? and isGroupMsg = ?", mTargetPeerName, App.getUserBean().getNickName(), "0").find(MessageBean.class);
        mMsgRvAdapter.appendData(mRvMsgBeanList);
        mHandler.sendEmptyMessage(SCROLL_NOW);
        View view = View.inflate(this, R.layout.popupwindow_micorphone, null);
        mPwMicrophone = new PopupWindow(this);
        mPwMicrophone.setBackgroundDrawable(new ColorDrawable(0x00000000));
        mPwMicrophone.setContentView(view);
        mPwMicrophone.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPwMicrophone.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mIvMicrophone = view.findViewById(R.id.iv_microphone_popupWindow);
        mTvRecordTime = view.findViewById(R.id.tv_record_time_popupWindow);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initListener() {
        mMsgRvAdapter.setOnItemViewClickListener(new OnItemViewClickListener() {
            @Override
            public void onImageClick(int position) {
                List<String> list = new ArrayList<>();
                for (MessageBean messageBean : mMsgRvAdapter.getDataList()) {
                    if (messageBean.getMsgType() == Protocol.IMAGE) {
                        //获取所有的图片本地地址
                        list.add(messageBean.getImagePath());
                    }
                }
                String currentImagePath = mMsgRvAdapter.getDataList().get(position).getImagePath();
                PhotoActivity.actionStart(ChatDetailActivity.this, list, list.indexOf(currentImagePath));
            }

            @SuppressLint("ResourceType")
            @Override
            public void onTextLongClick(final int position, View view) {
                PopupMenu popupMenu = new PopupMenu(ChatDetailActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.copy_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_copy:
                                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData cd = ClipData.newPlainText("Label", mMsgRvAdapter.getDataList().get(position).getText());
                                cm.setPrimaryClip(cd);
                                showToast("已复制");
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }

            @Override
            public void onFileClick(int position) {
                MessageBean bean = mMsgRvAdapter.getDataList().get(position);
                if (bean.getFileState() == FileState.RECEIVE_FILE_FINISH
                        || bean.getFileState() == FileState.SEND_FILE_FINISH) {
                    String mineType = SDUtil.getMimeTypeFromFilePath(bean.getFilePath());
                    if (mineType != null) {
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setDataAndType(SDUtil.getFileUri(bean.getFilePath()), mineType);
                        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
                        if (list.size() == 0) {
                            showToast("无法打开此类型的文件!");
                        } else {
                            startActivity(intent);
                        }
                    } else {
                        showToast("无法打开此类型的文件!");
                    }
                }
            }

            @Override
            public void onAlterClick(int position) {
                if (SocketManager.getInstance().isClosedSocket(mTargetPeerIp)) {
                    linkSocket();
                    showToast("连接Socket中");
                } else {
                    MessageBean messageBean = mMsgRvAdapter.getDataList().get(position);
                    messageBean.setSendStatus(Constant.SEND_MSG_ING);
                    mMsgRvAdapter.notifyItemChanged(position, Constant.UPDATE_SEND_MSG_STATE);
                    presenter.sendMsg(messageBean, position);
                }
            }

            @Override
            public void onAudioClick(PlayerSoundView psvPlaySound, String audioUrl) {
                if (mPsvIsPlaying != null) {
                    //有正在播放的音频
                    mMediaPlayerUtil.stopPlayer();
                    mPsvIsPlaying.stopPlayer();
                    if (mPsvIsPlaying == psvPlaySound) {
                        //同一个Item，点击则停止播放
                        mPsvIsPlaying = null;
                        return;
                    }
                }
                mPsvIsPlaying = psvPlaySound;
                mPsvIsPlaying.startPlayer();
                mMediaPlayerUtil.startPlayer(audioUrl);
            }
        });
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(getString(mEtInput))) {
                    MessageBean textMsg = MessageBean.getInstance(mTargetPeerIp);
                    textMsg.setMine(true);
                    textMsg.setMsgType(Protocol.TEXT);
                    textMsg.setText(getString(mEtInput));
                    textMsg.setSendStatus(Constant.SEND_MSG_ING);
                    mMsgRvAdapter.appendData(textMsg);
                    mHandler.sendEmptyMessage(SCROLL_NOW);
                    Log.d(TAG, "现在开始产生消息:"+textMsg.getUserName());
                    presenter.sendMsg(textMsg, mMsgRvAdapter.getItemCount() - 1);
                    /***
                     try {
                     SQLiteDatabase db = openHelper.getReadableDatabase();
                     String userIp = textMsg.getUserIp();
                     String friendIp = textMsg.getBelongIp();
                     String content = textMsg.getText();
                     int type = textMsg.getMsgType();
                     Date date = textMsg.getDate();
                     ContentValues values = new ContentValues();
                     values.put("user_ip", userIp);
                     values.put("friend_ip", friendIp);
                     values.put("content", content);
                     values.put("type", type);
                     values.put("timedate", date.toString());
                     db.insert("chat", null, values);
                     db.close();
                     } catch (Exception e){
                     showToast("保存聊天记录出错");
                     }***/
                    EventBus.getDefault().post(new RecentMsgEvent(getString(mEtInput), mTargetPeerIp, false));
                    mEtInput.setText("");
                }
            }
        });
        mIvPhotoAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.setType("image/*");
                startActivityForResult(intent, CHOOSE_PHOTO);
            }
        });
        mIvTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(ChatDetailActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatDetailActivity.this,
                            new String[]{android.Manifest.permission.CAMERA}, 2);
                } else {
                    openCamera();
                }
            }
        });
        mIvRecordVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                //若有音频正在播放，则先停止音频
                if (mPsvIsPlaying != null) {
                    mPsvIsPlaying.stopPlayer();
                    mPsvIsPlaying = null;
                    mMediaPlayerUtil.stopPlayer();
                }
                //判断权限
                if (ContextCompat.checkSelfPermission(ChatDetailActivity.this,
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatDetailActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO}, 3);
                } else {
                    //有权限
                    if (mEtInput.getVisibility() == View.VISIBLE) {
                        //显示 按住录音 按钮，同时隐藏输入框
                        mEtInput.setVisibility(View.INVISIBLE);
                        mBtnSend.setClickable(false);
                        mTvPressedStartRecord.setVisibility(View.VISIBLE);
                    } else {
                        mEtInput.setVisibility(View.VISIBLE);
                        mBtnSend.setClickable(true);
                        mTvPressedStartRecord.setVisibility(View.INVISIBLE);
                        mAudioRecorderUtil.cancelRecord();
                    }
                }
            }
        });
        mIvFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, FILE_MANAGER);
            }
        });
        mAudioRecorderUtil.setOnAudioStatusUpdateListener(new AudioRecorderUtil.OnAudioStatusUpdateListener() {
            @Override
            public void onUpdate(double db, long time) {
                //根据分贝值来设置录音时话筒图标的上下波动，下面有讲解
                mIvMicrophone.getDrawable().setLevel((int) (3000 + 6000 * db / 100));
                mTvRecordTime.setText(ProgressTextUtil.getProgressText(time));
            }

            @Override
            public void onStop(String audioPath) {
                //录音结束，自动发送音频消息
                MessageBean audioMsg = MessageBean.getInstance(mTargetPeerIp);
                audioMsg.setMine(true);
                audioMsg.setMsgType(Protocol.AUDIO);
                audioMsg.setAudioPath(audioPath);
                audioMsg.setSendStatus(Constant.SEND_MSG_ING);
                mMsgRvAdapter.appendData(audioMsg);
                mHandler.sendEmptyMessage(SCROLL_NOW);
                EventBus.getDefault().post(new RecentMsgEvent("语音", mTargetPeerIp, false));
                presenter.sendMsg(audioMsg, mMsgRvAdapter.getItemCount() - 1);
            }
        });
        mTvPressedStartRecord.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mAudioRecorderUtil.cancelRecord();
                        mPwMicrophone.showAtLocation(mRootLayout, Gravity.CENTER, 0, 0);
                        mAudioRecorderUtil.startRecord();
                        break;
                    case MotionEvent.ACTION_UP:
                        mPwMicrophone.dismiss();
                        mAudioRecorderUtil.stopRecord();
                        break;
                }
                return false;
            }
        });
        mMediaPlayerUtil.setMediaPlayerListener(new MediaPlayerUtil.MediaPlayerListener() {
            @Override
            public void onCompletion() {
                mPsvIsPlaying.stopPlayer();
                mPsvIsPlaying = null;
            }

            @Override
            public void onError() {

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    showToast("拒绝授权，无法使用相机！");
                }
                break;
            case 3:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    showToast("拒绝授权，无法录音！");
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    String imagePath = SDUtil.getFilePathByUri(ChatDetailActivity.this, data.getData());
                    MessageBean imageMsg = MessageBean.getInstance(mTargetPeerIp);
                    imageMsg.setMine(true);
                    imageMsg.setMsgType(Protocol.IMAGE);
                    imageMsg.setImagePath(imagePath);
                    imageMsg.setSendStatus(Constant.SEND_MSG_ING);
                    mMsgRvAdapter.appendData(imageMsg);
                    mHandler.sendEmptyMessage(SCROLL_NOW);
                    EventBus.getDefault().post(new RecentMsgEvent("图片", mTargetPeerIp, false));
                    presenter.sendMsg(imageMsg, mMsgRvAdapter.getItemCount() - 1);
                }
                break;
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    MessageBean imageMsg = MessageBean.getInstance(mTargetPeerIp);
                    imageMsg.setMine(true);
                    imageMsg.setMsgType(Protocol.IMAGE);
                    imageMsg.setImagePath(mTakePhotoFile.getAbsolutePath());
                    imageMsg.setSendStatus(Constant.SEND_MSG_ING);
                    mMsgRvAdapter.appendData(imageMsg);
                    mHandler.sendEmptyMessage(SCROLL_NOW);
                    EventBus.getDefault().post(new RecentMsgEvent("图片", mTargetPeerIp, false));
                    presenter.sendMsg(imageMsg, mMsgRvAdapter.getItemCount() - 1);
                }
                break;
            case FILE_MANAGER:
                if (resultCode == RESULT_OK) {
                    isSendingFile = true;
                    MessageBean fileMsg = MessageBean.getInstance(mTargetPeerIp);
                    fileMsg.setMine(true);
                    fileMsg.setMsgType(Protocol.FILE);
                    fileMsg.setUserIp(mTargetPeerIp);//这里得设置为对方ip，否则在本窗口下 void receiveMessage(MessageBean messageBean) 会丢弃此消息
                    fileMsg.setFilePath(SDUtil.getFilePathByUri(ChatDetailActivity.this, data.getData()));
                    fileMsg.setFileName(SDUtil.checkFileName(mMsgRvAdapter.getFileNameList(), fileMsg.getFilePath()));
                    fileMsg.setFileSize(SDUtil.getFileByteSize(fileMsg.getFilePath()));
                    fileMsg.setTransmittedSize(0);
                    fileMsg.setFileState(FileState.SEND_FILE_ING);
                    mMsgRvAdapter.appendData(fileMsg);
                    mHandler.sendEmptyMessage(SCROLL_NOW);
                    EventBus.getDefault().post(new RecentMsgEvent("文件", mTargetPeerIp, false));
                    presenter.sendMsg(fileMsg, mMsgRvAdapter.getItemCount() - 1);
                }
                break;
        }
    }

    /**
     * 隐藏键盘
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * 打开相机
     */
    private void openCamera() {
        mTakePhotoFile = new File(getExternalCacheDir(), "take_photo.jpg");
        if (mTakePhotoFile.exists()) {
            mTakePhotoFile.delete();
            try {
                mTakePhotoFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (Build.VERSION.SDK_INT >= 24) {
            mTakePhotoUri = FileProvider.getUriForFile(ChatDetailActivity.this, "com.rdc.p2p.fileprovider", mTakePhotoFile);
        } else {
            mTakePhotoUri = Uri.fromFile(mTakePhotoFile);
        }
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mTakePhotoUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    @Override
    public void linkSocket() {
        LinkSocketRequestEvent linkSocketRequestEvent = new LinkSocketRequestEvent(mTargetPeerIp);
        EventBus.getDefault().post(linkSocketRequestEvent);
    }

    @Override
    public void sendMsgSuccess(int position) {
        //设置状态
        MessageBean messageBean = mMsgRvAdapter.getDataList().get(position);
        messageBean.setSendStatus(Constant.SEND_MSG_FINISH);
        mMsgRvAdapter.notifyItemChanged(position, Constant.UPDATE_SEND_MSG_STATE);
    }

    @Override
    public void sendMsgError(int position, String error) {
        //设置状态
        MessageBean messageBean = mMsgRvAdapter.getDataList().get(position);
        messageBean.setSendStatus(Constant.SEND_MSG_ERROR);
        mMsgRvAdapter.notifyItemChanged(position, Constant.UPDATE_SEND_MSG_STATE);
        showToast(error);
    }

    @Override
    public void fileSending(int position, MessageBean messageBean) {
        MessageBean dataMessage = mMsgRvAdapter.getDataList().get(position);
        dataMessage.updateFileState(messageBean);
        mMsgRvAdapter.notifyItemChanged(position, Constant.UPDATE_FILE_STATE);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View currentFocusView = getCurrentFocus();
            if (isShouldHideInput(currentFocusView, ev)) {
                mHandler.sendEmptyMessageDelayed(HIDE_SOFT_INPUT, 100);
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        return getWindow().superDispatchTouchEvent(ev) || onTouchEvent(ev);
    }

    /**
     * 点击EditText以外的地方，软键盘收起来
     *
     * @param v     获得焦点的View
     * @param event 点击事件
     * @return 是否隐藏键盘
     */
    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mToolbar.setTitle("");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveMessage(MessageBean messageBean) {
        // TODO 先用一个最简单粗暴的方法测试，后续需要一个新的事件
        if (messageBean.isGroupMessage()) {
            return;
        }
        if (messageBean.getUserIp().equals(mTargetPeerIp)) {
            if (messageBean.getMsgType() == Protocol.FILE) {
                if (messageBean.getFileState() == FileState.RECEIVE_FILE_START) {
                    //文件传输的第一次消息传到，要添加到列表并且自动滚动
                    mMsgRvAdapter.appendData(messageBean);
                    mHandler.sendEmptyMessage(SCROLL);
                    return;
                }
                int position = mMsgRvAdapter.getPositionByFileName(messageBean.getFileName());
                if (position >= 0) {
                    //更新Adapter中的数据源
                    mMsgRvAdapter.getDataList().get(position).updateFileState(messageBean);
                    //更新RecyclerView视图
                    mMsgRvAdapter.notifyItemChanged(position, Constant.UPDATE_FILE_STATE);
                }
            } else {
                //其他消息直接添加到数据源中并更新RecyclerView界面
                mMsgRvAdapter.appendData(messageBean);
                mHandler.sendEmptyMessage(SCROLL);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void linkSocketResponse(LinkSocketResponseEvent responseEvent) {
        PeerBean peerBean = responseEvent.getPeerBean();
        if (peerBean.getUserIp().equals(mTargetPeerIp)) {
            if (responseEvent.isState()) {
                mTargetPeerImageId = peerBean.getUserImageId();
                mTargetPeerName = peerBean.getNickName();
            }
            presenter.setLinkSocketState(responseEvent.isState());
        }
    }
}
