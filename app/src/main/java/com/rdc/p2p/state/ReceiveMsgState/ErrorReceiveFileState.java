package com.rdc.p2p.state.ReceiveMsgState;

import android.util.Log;

import com.rdc.p2p.base.BaseMsgState;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.config.FileState;
import com.rdc.p2p.contract.PeerListContract;

import java.io.IOException;

import static com.rdc.p2p.model.PeerListModel.TAG;

public class ErrorReceiveFileState extends BaseMsgState {

    private MessageBean fileMsg;
    private PeerListContract.Presenter mPresenter;

    public ErrorReceiveFileState(MessageBean fileMsg, PeerListContract.Presenter mPresenter){
        this.fileMsg = fileMsg;
        this.mPresenter = mPresenter;
    }

    @Override
    public void handle() throws IOException {
        Log.d(TAG, "run: read=-1");
        //对方关闭
        fileMsg.setSendStatus(FileState.RECEIVE_FILE_ERROR);
        fileMsg.saveOrUpdate("belongName = ? and filePath = ?", fileMsg.getBelongName(), fileMsg.getFilePath());
        mPresenter.fileReceiving(fileMsg);
    }
}
