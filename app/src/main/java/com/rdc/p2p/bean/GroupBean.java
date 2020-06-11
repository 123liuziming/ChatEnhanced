package com.rdc.p2p.bean;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GroupBean {
    private int groupImageId;
    private String nickName;
    private String recentMessage;
    private String time;
    private int msgNum = 0; // 未读消息数量
    private List<PeerBean> peerBeanList;
    public GroupBean(){
        peerBeanList= new ArrayList<>();
    }
    @Override
    public String toString() {
        return "GroupBean{" +
                "groupImageId=" + groupImageId +
                ", nickName='" + nickName + '\'' +
                ", recentMessage='" + recentMessage + '\'' +
                ", time='" + time + '\'' +
                ", msgNum=" + msgNum +
                '}';
    }

    public int getGroupImageId() {
        return groupImageId;
    }

    public void setGroupImageId(int groupImageId) {
        this.groupImageId = groupImageId;
    }

    public String getNickName() {
        return nickName == null ? "" : nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName == null ? "" : nickName;
    }

    public String getRecentMessage() {
        return recentMessage == null ? "" : recentMessage;
    }

    public void setRecentMessage(String recentMessage) {
        this.recentMessage = recentMessage == null ? "" : recentMessage;
    }

    public String getTime() {
        if (time == null){
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date date = new Date();
            time = sdf.format(date);
        }
        return time;
    }

    public void setTime(String time) {
        this.time = time == null ? "" : time;
    }


    public int getMsgNum() {
        return msgNum;
    }

    public void setMsgNum(int msgNum) {
        this.msgNum = msgNum;
    }

    public List<PeerBean> getPeerBeanList() {
        return peerBeanList;
    }

    public void setPeerBeanList(List<PeerBean> peerBeanList) {
        this.peerBeanList = peerBeanList;
    }
}
