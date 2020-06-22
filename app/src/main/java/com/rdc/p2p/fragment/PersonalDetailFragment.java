package com.rdc.p2p.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.rdc.p2p.R;
import com.rdc.p2p.activity.LoginActivity;
import com.rdc.p2p.app.App;
import com.rdc.p2p.base.BaseFragment;
import com.rdc.p2p.base.BasePresenter;
import com.rdc.p2p.bean.MyDnsBean;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.manager.SocketManager;
import com.rdc.p2p.thread.SocketThread;
import com.rdc.p2p.util.ImageUtil;
import com.rdc.p2p.util.MyDnsUtil;

import org.litepal.crud.DataSupport;

import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

    @BindView(R.id.mImage)
    ImageView mImage;

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
        Bitmap sampleImg = BitmapFactory.decodeResource(getResources(), ImageUtil.getImageResId(App.getUserBean().getUserImageId()));
        Glide.with(this).load(sampleImg).into(mCivUserImage);
        sampleImg = blur(sampleImg,14);
        mImage.setImageBitmap(sampleImg);
        //        Glide.with(this)
//                .load(ImageUtil.getImageResId(App.getUserBean().getUserImageId()))
//
//                .into(mImage);
    }


    @Override
    protected void setListener() {
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataSupport.deleteAll(MyDnsBean.class);
                MyDnsUtil.refreshAll();
                Collection<SocketThread> allSocketThreads = SocketManager.getInstance().getSocketThreads();
                for(final SocketThread s : allSocketThreads) {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            s.sendRequest(App.getUserBean(), Protocol.DISCONNECT);
                        }
                    });
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Intent intent_login = new Intent();
                intent_login.setClass(mBaseActivity, LoginActivity.class);
                intent_login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //关键的一句，将新的activity置为栈顶
                startActivity(intent_login);
                mBaseActivity.finish();
            }
        });
    }

    private Bitmap blur(Bitmap bitmap, float radius) {
        Bitmap output = Bitmap.createBitmap(bitmap); // 创建输出图片
        RenderScript rs = RenderScript.create(getContext()); // 构建一个RenderScript对象
        ScriptIntrinsicBlur gaussianBlue = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs)); //
        // 创建高斯模糊脚本
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap); // 开辟输入内存
        Allocation allOut = Allocation.createFromBitmap(rs, output); // 开辟输出内存
        gaussianBlue.setRadius(radius); // 设置模糊半径，范围0f<radius<=25f
        gaussianBlue.setInput(allIn); // 设置输入内存
        gaussianBlue.forEach(allOut); // 模糊编码，并将内存填入输出内存
        allOut.copyTo(output); // 将输出内存编码为Bitmap，图片大小必须注意
        rs.destroy(); // 关闭RenderScript对象，API>=23则使用rs.releaseAllContexts()
        return output;
    }


}
