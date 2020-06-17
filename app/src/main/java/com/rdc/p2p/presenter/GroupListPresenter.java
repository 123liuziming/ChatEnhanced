package com.rdc.p2p.presenter;

import android.app.Activity;
import android.widget.Toast;

import com.rdc.p2p.base.BasePresenter;
import com.rdc.p2p.bean.GroupBean;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.contract.GroupListContract;
import com.rdc.p2p.manager.SocketManager;
import com.rdc.p2p.model.GroupListModel;
import com.rdc.p2p.thread.SocketThread;

import java.util.List;

public class GroupListPresenter extends BasePresenter<GroupListContract.View> implements GroupListContract.Presenter{
    private GroupListContract.Model model;
    private Activity mActivity;

    public GroupListPresenter(Activity activity){
        model =new GroupListModel(this);
        mActivity = activity;
    }


    @Override
    public void disconnect() {
        model.disconnect();
    }

    @Override
    public void initServerSocketSuccess() {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().initServerSocketSuccess();
                }
            });
        }
    }

    @Override
    public void initSocket() {
        model.initServerSocket();
    }

    @Override
    public void linkGroups(List<String> list) {
        model.linkGroups(list);
    }

    @Override
    public void linkGroup(String targetIp) {
        model.linkGroup(targetIp);
    }


    @Override
    public void linkGroupSuccess(final String ip) {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().linkGroupSuccess(ip);
                }
            });
        }
    }

    @Override
    public void linkGroupError(final String message, final String targetIp) {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().linkGroupError(message,targetIp);
                }
            });
        }
    }


    @Override
    public void updateGroupList(final List<GroupBean> list) {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().updateGroupList(list);
                }
            });
        }
    }

    @Override
    public void addGroup(final GroupBean groupBean) {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().addGroup(groupBean);
                }
            });
        }
    }

    @Override
    public void messageReceived(final MessageBean messageBean) {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().messageReceived(messageBean);
                }
            });
        }
    }

    @Override
    public void removeGroup(final String ip) {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().removeGroup(ip);
                }
            });
        }
    }

    @Override
    public void serverSocketError(final String msg) {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().serverSocketError(msg);
                }
            });
        }
    }

    @Override
    public boolean isServerSocketConnected() {
        return model.isInitServerSocket();
    }

    @Override
    public void fileReceiving(final MessageBean messageBean) {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().fileReceiving(messageBean);
                }
            });
        }
    }

}
