package com.rdc.p2p.state.SendMsgState;

import com.rdc.p2p.app.App;
import com.rdc.p2p.base.BaseMsgState;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.config.Constant;
import com.rdc.p2p.listener.OnSocketSendCallback;
import com.rdc.p2p.util.StateUtil;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageMsgState extends BaseMsgState {
    public ImageMsgState(MessageBean messageBean, OnSocketSendCallback mOnSocketSendCallback, int position, OutputStream outputStream) {
        super(messageBean, mOnSocketSendCallback, position, outputStream);
    }

    public ImageMsgState(MessageBean messageBean, OnSocketSendCallback mOnSocketSendCallback, int position, OutputStream outputStream, InputStream inputStream) {
        super(messageBean, mOnSocketSendCallback, position, outputStream, inputStream);
    }

    @Override
    public void handle() throws IOException {
        DataOutputStream dos = new DataOutputStream(outputStream);
        // 发消息时标识此消息是否是群聊消息
        dos.writeBoolean(messageBean.isGroupMessage());
        // 如果是群聊消息，写入群聊名称
        if(messageBean.isGroupMessage()) {
            byte[] groupNameBytes = messageBean.getGroupName().getBytes();
            dos.writeInt(groupNameBytes.length);
            dos.write(groupNameBytes);
        }
        int size = inputStream.available();
        dos.writeInt(size);
        byte[] bytes = new byte[size];
        inputStream.read(bytes);
        dos.write(bytes);
        StateUtil.setState(messageBean, mOnSocketSendCallback, position);
    }
}
