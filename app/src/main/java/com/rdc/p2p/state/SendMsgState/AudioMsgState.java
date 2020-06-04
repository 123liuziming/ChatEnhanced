package com.rdc.p2p.state.SendMsgState;

import com.rdc.p2p.app.App;
import com.rdc.p2p.base.BaseMsgState;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.config.Constant;
import com.rdc.p2p.listener.OnSocketSendCallback;
import com.rdc.p2p.util.StateUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AudioMsgState extends BaseMsgState {
    public AudioMsgState(MessageBean messageBean, OnSocketSendCallback mOnSocketSendCallback, int position, OutputStream outputStream) {
        super(messageBean, mOnSocketSendCallback, position, outputStream);
    }

    public AudioMsgState(MessageBean messageBean, OnSocketSendCallback mOnSocketSendCallback, int position, OutputStream outputStream, InputStream inputStream) {
        super(messageBean, mOnSocketSendCallback, position, outputStream, inputStream);
    }

    @Override
    public void handle() throws IOException {
        DataOutputStream dos = new DataOutputStream(outputStream);
        int audioSize = inputStream.available();
        dos.writeInt(audioSize);
        byte[] audioBytes = new byte[audioSize];
        inputStream.read(audioBytes);
        dos.write(audioBytes);
        dos.flush();
        StateUtil.setState(messageBean, mOnSocketSendCallback, position);
    }
}
