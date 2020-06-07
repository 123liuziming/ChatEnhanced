package com.rdc.p2p.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.bumptech.glide.Glide;
import com.example.LoginQuery;
import com.example.MessagesBetweenQuery;
import com.rdc.p2p.app.App;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.bean.MyDnsBean;
import com.rdc.p2p.bean.UserBean;
import com.rdc.p2p.fragment.FragmentCommon;
import com.rdc.p2p.fragment.PeerListFragment;
import com.rdc.p2p.fragment.ScanDeviceFragment;
import com.rdc.p2p.fragment.SelectImageFragment;
import com.rdc.p2p.R;
import com.rdc.p2p.base.BaseActivity;
import com.rdc.p2p.base.BasePresenter;
import com.rdc.p2p.bean.ImageBean;
import com.rdc.p2p.util.ImageUtil;
import com.rdc.p2p.util.NetUtil;
import com.rdc.p2p.util.UserUtil;
import com.ycl.tabview.library.TabView;
import com.ycl.tabview.library.TabViewChild;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

public class LoginActivity extends BaseActivity {

    public static final String TAG = "Login";

    @BindView(R.id.civ_user_image_act_login)
    CircleImageView mCivUserImage;
    @BindView(R.id.et_nickname_act_login)
    EditText mEtNickname;
    @BindView(R.id.et_nickname_act_passwd)
    EditText password;
    @BindView(R.id.btn_login_act_login)
    Button mBtnLogin;
    @BindView(R.id.btn_login_act_register)
    Button mBtnRegister;


    private List<ImageBean> mImageList;
    private int mSelectedImageId;

    @Override
    public BasePresenter getInstance() {
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getPermission(this);
        // DataSupport.deleteAll(MessageBean.class);

    }

    /**
     * 获取储存权限
     *
     * @param activity
     * @return
     */

    public void getPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO
                }, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            finish();
                            showToast("拒绝授权，无法使用本应用！");
                        }
                    }
                } else {
                    showToast("拒绝授权，无法使用本应用！");
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_login;
    }

    @Override
    protected void initData() {
        mSelectedImageId = 0;
        mImageList = new ArrayList<>();
        for (int i = 0; i < 18; i++) {
            ImageBean imageBean = new ImageBean();
            imageBean.setImageId(i);
            mImageList.add(imageBean);
        }
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initListener() {
        mCivUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectImageFragment selectImageFragment = new SelectImageFragment();
                selectImageFragment.show(getSupportFragmentManager(), "DialogFragment");
            }
        });

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(mEtNickname.getText())) {
                    ApolloClient apolloClient = ApolloClient.builder().serverUrl("http://49.232.12.147:4000").build();
                    final LoginQuery login = LoginQuery.builder()
                            .username(getString(mEtNickname))
                            .password(getString(password))
                            .build();
                    apolloClient.query(login)
                            .enqueue(new ApolloCall.Callback<LoginQuery.Data>() {
                                @Override
                                public void onResponse(@NotNull Response<LoginQuery.Data> response) {
                                    Log.d(TAG, response.getData().toString());
                                    if (response.getData().Login() != null) {
                                        UserBean userBean = new UserBean();
                                        userBean.setNickName(getString(mEtNickname));
                                        userBean.setUserImageId(mSelectedImageId);
                                        UserUtil.saveUser(userBean);
                                        App.setUserBean(userBean);
                                        if (NetUtil.isWifi(LoginActivity.this)) {
                                            ScanDeviceFragment scanDeviceFragment = new ScanDeviceFragment();
                                            scanDeviceFragment.setCancelable(false);
                                            scanDeviceFragment.show(getSupportFragmentManager(), "progressFragment");
                                        } else {
                                            Looper.prepare();
                                            showToast("请连接WIFI！");
                                            Looper.loop();
                                        }
                                    } else {
                                        Looper.prepare();
                                        showToast("登录失败！");
                                        Looper.loop();
                                    }
                                }

                                @Override
                                public void onFailure(@NotNull ApolloException e) {
                                    Log.e(TAG, e.getLocalizedMessage(), e);
                                    showToast("登录失败！");
                                }
                            });


                } else {
                    showToast("昵称不能为空！");
                }
            }
        });

        // 点击按钮前往注册界面
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void ScanDeviceFinished(List<String> ipList) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putStringArrayListExtra("ipList", (ArrayList<String>) ipList);
        startActivity(intent);
        finish();
    }

    public void setImageId(int imageId) {
        mSelectedImageId = imageId;
        Glide.with(this).load(ImageUtil.getImageResId(imageId)).into(mCivUserImage);
    }

}
