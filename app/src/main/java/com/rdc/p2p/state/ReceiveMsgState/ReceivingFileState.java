package com.rdc.p2p.state.ReceiveMsgState;

import android.util.Log;

import com.rdc.p2p.app.App;
import com.rdc.p2p.base.BaseMsgState;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.config.FileState;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.contract.PeerListContract;

import java.io.FileOutputStream;
import java.io.IOException;

import static com.rdc.p2p.model.PeerListModel.TAG;

public class ReceivingFileState extends BaseMsgState {

    private MessageBean fileMsg;
    private PeerListContract.Presenter mPresenter;
    private int transLen;

    public ReceivingFileState(MessageBean fileMsg, PeerListContract.Presenter mPresenter, int transLen){
        this.fileMsg = fileMsg;
        this.mPresenter = mPresenter;
        this.transLen = transLen;
    }



    @Override
    public void handle() throws IOException {
        fileMsg = fileMsg.clone();
        fileMsg.setTransmittedSize(transLen);
        fileMsg.setFileState(FileState.RECEIVE_FILE_ING);
        fileMsg.saveOrUpdate("belongName = ? and filePath = ?", fileMsg.getBelongName(), fileMsg.getFilePath());
        mPresenter.fileReceiving(fileMsg);
    }
}
