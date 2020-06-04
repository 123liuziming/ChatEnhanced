package com.rdc.p2p.state.SendMsgState;

import com.rdc.p2p.app.App;
import com.rdc.p2p.base.BaseMsgState;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.config.Constant;
import com.rdc.p2p.listener.OnSocketSendCallback;
import com.rdc.p2p.util.StateUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class TextMsgState extends BaseMsgState {

    public TextMsgState(MessageBean messageBean, OnSocketSendCallback mOnSocketSendCallback, int position, OutputStream outputStream) {
        super(messageBean, mOnSocketSendCallback, position, outputStream);
    }

    @Override
    public void handle() throws IOException {
        byte[] textBytes = messageBean.getText().getBytes();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeInt(textBytes.length);
        dataOutputStream.write(textBytes);
        StateUtil.setState(messageBean, mOnSocketSendCallback, position);
    }
}

