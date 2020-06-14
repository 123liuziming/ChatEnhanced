package com.rdc.p2p.state.ReceiveMsgState;


import com.rdc.p2p.app.App;
import com.rdc.p2p.base.BaseMsgState;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.config.FileState;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.contract.PeerListContract;

import java.io.File;
import java.io.IOException;


public class StartReceiveFileState extends BaseMsgState {

    private File file;
    private String mTargetIp;
    private PeerListContract.Presenter mPresenter;
    private int fileSize;
    private MessageBean fileMsg;

    public StartReceiveFileState(File file, String mTargetIp, PeerListContract.Presenter mPresenter, int fileSize ){
        this.file = file;
        this.mTargetIp = mTargetIp;
        this.mPresenter = mPresenter;
        this.fileSize = fileSize;
    }

    @Override
    public void handle() throws IOException {
        fileMsg = MessageBean.getInstance(mTargetIp);
        fileMsg.setUserName(App.getUserBean().getNickName());
        fileMsg.setFilePath(file.getAbsolutePath());
        fileMsg.setFileName(file.getName());
        fileMsg.setFileSize(fileSize);
        fileMsg.setFileState(FileState.RECEIVE_FILE_START);
        fileMsg.setTransmittedSize(0);
        fileMsg.setUserIp(mTargetIp);
        fileMsg.setMine(false);
        fileMsg.setMsgType(Protocol.FILE);
        fileMsg.getDate();
        fileMsg.save();
        mPresenter.fileReceiving(fileMsg);
    }

    public MessageBean getFileMsg(){
        return fileMsg;
    }
}
