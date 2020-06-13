package com.rdc.p2p.state.ReceiveMsgState;

import com.rdc.p2p.base.BaseMsgState;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.config.FileState;

import com.rdc.p2p.contract.PeerListContract;

import java.io.IOException;

public class EndingReceiveFileState extends BaseMsgState {

    private MessageBean fileMsg;
    private int transLen;
    private PeerListContract.Presenter mPresenter;

    public EndingReceiveFileState(MessageBean fileMsg, int transLen, PeerListContract.Presenter mPresenter){
        this.fileMsg = fileMsg;
        this.transLen = transLen;
        this.mPresenter = mPresenter;
    }

    @Override
    public void handle() throws IOException {
        fileMsg = fileMsg.clone();
        fileMsg.setTransmittedSize(transLen);
        fileMsg.setFileState(FileState.RECEIVE_FILE_FINISH);
        fileMsg.saveOrUpdate("belongName = ? and filePath = ?", fileMsg.getBelongName(), fileMsg.getFilePath());
        mPresenter.fileReceiving(fileMsg);
    }
}
