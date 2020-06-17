package com.rdc.p2p.event;

/**
 * Created by Lin Yaotian on 2018/6/1.
 */
public class RecentMsgEvent {

    private String text;
    private String targetIp;
    // 是否是群聊消息
    private boolean isGroup;
    private String groupName;

    public RecentMsgEvent(String text,String targetIp, boolean isGroup){
        this.text = text;
        this.targetIp = targetIp;
        this.isGroup = isGroup;
    }

    public RecentMsgEvent(String text,String targetIp, boolean isGroup, String groupName){
        this.text = text;
        this.targetIp = targetIp;
        this.isGroup = isGroup;
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public String getTargetIp() {
        return targetIp == null ? "" : targetIp;
    }

    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp == null ? "" : targetIp;
    }

    public String getText() {
        return text == null ? "" : text;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
    }
}
