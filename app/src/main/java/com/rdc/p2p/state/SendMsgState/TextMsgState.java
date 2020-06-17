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
        // 发消息时标识此消息是否是群聊消息
        dataOutputStream.writeBoolean(messageBean.isGroupMessage());
        // 如果是群聊消息，写入群聊名称
        if(messageBean.isGroupMessage()) {
            byte[] groupNameBytes = messageBean.getGroupName().getBytes();
            dataOutputStream.writeInt(groupNameBytes.length);
            dataOutputStream.write(groupNameBytes);
        }
        dataOutputStream.writeInt(textBytes.length);
        dataOutputStream.write(textBytes);
        StateUtil.setState(messageBean, mOnSocketSendCallback, position);
    }
}

