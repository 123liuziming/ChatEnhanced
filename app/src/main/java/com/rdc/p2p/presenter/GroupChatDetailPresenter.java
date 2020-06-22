package com.rdc.p2p.presenter;

import android.app.Activity;

import com.rdc.p2p.base.BasePresenter;
import com.rdc.p2p.bean.GroupBean;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.contract.ChatDetailContract;
import com.rdc.p2p.contract.GroupChatDetailContract;
import com.rdc.p2p.model.ChatDetailModel;
import com.rdc.p2p.model.GroupChatDetailModel;
import com.rdc.p2p.util.ImageUtil;
import com.zxy.tiny.callback.FileCallback;

/**
 *
 */
public class GroupChatDetailPresenter extends BasePresenter<GroupChatDetailContract.View> implements GroupChatDetailContract.Presenter {

    private GroupChatDetailContract.Model mModel;
    private Activity mActivity;

    public GroupChatDetailPresenter(Activity activity, GroupBean groupBean){
        mModel = new GroupChatDetailModel(this,groupBean);
        mActivity = activity;
    }

    @Override
    public void linkSocket() {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().linkSocket();
                }
            });
        }
    }

    @Override
    public void setLinkSocketState(boolean state) {
        mModel.setLinkSocketState(state);
    }

    @Override
    public void sendMsg(final MessageBean msg, final int position) {
        if (msg.getMsgType() == Protocol.IMAGE){
            //压缩图片
            ImageUtil.compressImage(msg.getImagePath(), new FileCallback() {
                @Override
                public void callback(boolean isSuccess, String outfile, Throwable t) {
                    if (isSuccess){
                        msg.setImagePath(outfile);
                    }
                    mModel.sendMessage(msg,position);
                    msg.save();
                }
            });
        }else {
            mModel.sendMessage(msg,position);
            msg.save();
        }
    }

    @Override
    public void sendMsgSuccess(final int position) {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().sendMsgSuccess(position);
                }
            });
        }
    }

    @Override
    public void sendMsgError(final int position, final String error) {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().sendMsgError(position,error);
                }
            });
        }
    }

    @Override
    public void fileSending(final int position, final MessageBean messageBean) {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().fileSending(position, messageBean);
                }
            });
        }
    }

    @Override
    public void exit() {
        mModel.exit();
    }
}
