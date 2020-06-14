package com.rdc.p2p.state.ReceiveMsgState;

import com.rdc.p2p.base.BaseMsgState;
import com.rdc.p2p.bean.MyDnsBean;
import com.rdc.p2p.contract.PeerListContract;
import com.rdc.p2p.manager.SocketManager;
import com.rdc.p2p.util.MyDnsUtil;

import org.litepal.crud.DataSupport;

import java.io.IOException;

public class ReceiveMsgExceptionState extends BaseMsgState {

    private String mTargetIp;
    private boolean mKeepUser;
    private PeerListContract.Presenter mPresenter;

    public ReceiveMsgExceptionState(String mTargetIp, boolean mKeepUser, PeerListContract.Presenter mPresenter){
        this.mTargetIp = mTargetIp;
        this.mKeepUser = mKeepUser;
        this.mPresenter = mPresenter;
    }

    @Override
    public void handle(){
        DataSupport.deleteAll(MyDnsBean.class, "mTargetIp = ?", mTargetIp);
        MyDnsUtil.refresh(mTargetIp);
        if (!mKeepUser) {
            mPresenter.removePeer(mTargetIp);
        }
        SocketManager.getInstance().removeSocketByIp(mTargetIp);
        SocketManager.getInstance().removeSocketThreadByIp(mTargetIp);
    }
}
