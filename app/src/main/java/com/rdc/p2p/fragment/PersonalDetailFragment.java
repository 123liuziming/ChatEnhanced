package com.rdc.p2p.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.rdc.p2p.R;
import com.rdc.p2p.activity.LoginActivity;
import com.rdc.p2p.app.App;
import com.rdc.p2p.base.BaseFragment;
import com.rdc.p2p.base.BasePresenter;
import com.rdc.p2p.bean.MyDnsBean;
import com.rdc.p2p.util.ImageUtil;
import com.rdc.p2p.util.MyDnsUtil;

import org.litepal.crud.DataSupport;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

public class PersonalDetailFragment extends BaseFragment {
    private static final String TAG ="PersonDetailFragment";

    @BindView(R.id.user_image_detail)
    CircleImageView mCivUserImage;

    @BindView(R.id.name_data)
    TextView nameData;

    @BindView(R.id.btn_return)
    Button btn_return;

    @BindView(R.id.ip_data)
    TextView ipData;

    @Override
    protected int setLayoutResourceId() {
        return R.layout.fragment_personal_detail;
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
        nameData.setText("用户名: " + App.getUserBean().getNickName());
        ipData.setText("本次登录IP: " + App.getMyIP());
        Glide.with(this).load(ImageUtil.getImageResId(App.getUserBean().getUserImageId())).into(mCivUserImage);
    }

    @Override
    protected void setListener() {
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataSupport.deleteAll(MyDnsBean.class);
                Intent intent_login = new Intent();
                intent_login.setClass(mBaseActivity, LoginActivity.class);
                intent_login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //关键的一句，将新的activity置为栈顶
                startActivity(intent_login);
                mBaseActivity.finish();
            }
        });
    }

}
