package com.rdc.p2p.base;

import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.listener.OnSocketSendCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// 状态模式的基类，使用的抽象类，没使用接口

public abstract class BaseMsgState {
    protected MessageBean messageBean;
    protected OnSocketSendCallback mOnSocketSendCallback;
    protected int position;
    protected OutputStream outputStream;
    protected InputStream inputStream;

    public BaseMsgState(MessageBean messageBean, OnSocketSendCallback mOnSocketSendCallback, int position, OutputStream outputStream) {
        this.messageBean = messageBean;
        this.mOnSocketSendCallback = mOnSocketSendCallback;
        this.position = position;
        this.outputStream = outputStream;
    }

    public BaseMsgState(MessageBean messageBean, OnSocketSendCallback mOnSocketSendCallback, int position, OutputStream outputStream, InputStream inputStream) {
        this.messageBean = messageBean;
        this.mOnSocketSendCallback = mOnSocketSendCallback;
        this.position = position;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
    }

    public abstract void handle() throws IOException;
}
