package com.rdc.p2p.contract;

import com.rdc.p2p.bean.GroupBean;
import com.rdc.p2p.bean.MessageBean;

import java.util.List;

public interface GroupListContract {
    interface View{
        void updateGroupList(List<GroupBean> list);
        void messageReceived(MessageBean messageBean);
        void fileReceiving(MessageBean messageBean);
        void addGroup(GroupBean groupBean);
        void removeGroup(String ip);
        void serverSocketError(String msg);
        void linkGroupSuccess(String ip);
        void linkGroupError(String message,String targetIp);
        void initServerSocketSuccess();
    }

    interface Model{
        void initServerSocket();
        void linkGroups(List<String> list);
        void linkGroup(String targetIp);
        void disconnect();
        boolean isInitServerSocket();
    }

    interface Presenter{
        void disconnect();
        void initServerSocketSuccess();
        void initSocket();
        void linkGroups(List<String> list);
        void linkGroup(String targetIp);
        void linkGroupSuccess(String ip);
        void linkGroupError(String message,String targetIp);
        void updateGroupList(List<GroupBean> list);
        void addGroup(GroupBean groupBean);
        void messageReceived(MessageBean messageBean);
        void removeGroup(String ip);
        void serverSocketError(String msg);
        boolean isServerSocketConnected();
        void fileReceiving(MessageBean messageBean);
    }
}
