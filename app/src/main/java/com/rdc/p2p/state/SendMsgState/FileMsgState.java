package com.rdc.p2p.state.SendMsgState;

import com.rdc.p2p.app.App;
import com.rdc.p2p.base.BaseMsgState;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.config.FileState;
import com.rdc.p2p.listener.OnSocketSendCallback;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileMsgState extends BaseMsgState {
    public FileMsgState(MessageBean messageBean, OnSocketSendCallback mOnSocketSendCallback, int position, OutputStream outputStream) {
        super(messageBean, mOnSocketSendCallback, position, outputStream);
    }

    public FileMsgState(MessageBean messageBean, OnSocketSendCallback mOnSocketSendCallback, int position, OutputStream outputStream, InputStream inputStream) {
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
        int fileSize = messageBean.getFileSize();
        dos.writeInt(fileSize);
        dos.writeUTF(messageBean.getFileName());
        byte[] fileBytes = new byte[fileSize];
        inputStream.read(fileBytes);
        int offset = 0;//每次写入长度
        int count = 0;//次数
        int denominator;//将总文件分为多少份传输
        if (fileSize < (1024 * 300)) {
            denominator = 1;
        } else {
            denominator = fileSize / (1024 * 300);
        }
        messageBean.setFileState(FileState.SEND_FILE_ING);
        while (true) {
            count++;
            dos.write(fileBytes, offset, fileSize / denominator);
            offset += fileSize / denominator;
            if (count == denominator) {
                dos.write(fileBytes, offset, fileSize % denominator);
                dos.flush();
                messageBean.setTransmittedSize(fileSize);
                messageBean.setFileState(FileState.SEND_FILE_FINISH);
                messageBean.setUserName(App.getUserBean().getNickName());
                messageBean.setUserIp(App.getMyIP());
                messageBean.getDate();
                messageBean.save();
                if (mOnSocketSendCallback != null) {
                    mOnSocketSendCallback.fileSending(position, messageBean);
                }
                break;
            }
            messageBean.setUserName(App.getUserBean().getNickName());
            messageBean.setUserIp(App.getMyIP());
            messageBean.getDate();
            messageBean.setTransmittedSize(offset);
            messageBean.save();
            if (mOnSocketSendCallback != null) {
                mOnSocketSendCallback.fileSending(position, messageBean);
            }
        }
    }
}
