package com.rdc.p2p.event;

public class RecentGroupMsgEvent {
    private String text;
    private String groupName;
    public RecentGroupMsgEvent(String text, String groupName) {
        this.text = text;
        this.groupName = groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
