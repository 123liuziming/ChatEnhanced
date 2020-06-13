package com.rdc.p2p.Strategy;

import com.rdc.p2p.bean.MessageBean;

import java.util.List;

public class MessageContext {
    private MessageStrategy strategy;

    public MessageContext(MessageStrategy strategy){
        this.strategy = strategy;
    }

    public MessageBean executeStrategy(List<MessageBean> allMsg){
        return strategy.operation(allMsg);
    }
}
