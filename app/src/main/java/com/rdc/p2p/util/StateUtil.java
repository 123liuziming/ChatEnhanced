package com.rdc.p2p.util;

import com.rdc.p2p.app.App;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.config.Constant;
import com.rdc.p2p.listener.OnSocketSendCallback;

public class StateUtil {
    public static void setState(MessageBean messageBean, OnSocketSendCallback mOnSocketSendCallback, int position) {
        messageBean.setSendStatus(Constant.SEND_MSG_FINISH);
        messageBean.setUserName(App.getUserBean().getNickName());
        messageBean.setUserIp(App.getMyIP());
        messageBean.getDate();
        messageBean.save();
        mOnSocketSendCallback.sendMsgSuccess(position);
    }
}
