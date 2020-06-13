package com.rdc.p2p.Strategy;

import com.rdc.p2p.bean.MessageBean;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LatestMessageStrategy implements MessageStrategy {

    public MessageBean operation(List<MessageBean> allMsg){
        if (allMsg.size() != 0) {
            MessageBean msg = Collections.max(allMsg, new Comparator<MessageBean>() {
                @Override
                public int compare(MessageBean messageBean, MessageBean t1) {
                    return messageBean.getDate().compareTo(t1.getDate());
                }
            });
            return msg;
        }
        return null;
    }
}
