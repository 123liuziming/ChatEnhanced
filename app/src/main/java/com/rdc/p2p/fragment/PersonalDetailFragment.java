package com.rdc.p2p.fragment;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.rdc.p2p.R;
import com.rdc.p2p.base.BaseFragment;
import com.rdc.p2p.base.BasePresenter;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

public class PersonalDetailFragment extends BaseFragment {
    private static final String TAG ="PersonDetailFragment";
    @BindView(R.id.user_image_detail)
    CircleImageView mCivUserImage;
    @BindView(R.id.name_data)
    TextView textName;
    @BindView(R.id.password_data)
    EditText textPassWord;
    @BindView(R.id.btn_commit)
    Button btn_commit;
    @BindView(R.id.btn_return)
    Button btn_return;

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

    @Override
    protected void initView() {
    }

    @Override
    protected void setListener() {

    }

}
