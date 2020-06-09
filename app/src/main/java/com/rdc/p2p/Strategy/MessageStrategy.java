package com.rdc.p2p.Strategy;

import com.rdc.p2p.bean.MessageBean;

import java.util.List;

public interface MessageStrategy{
    MessageBean operation(List<MessageBean> list);
}
