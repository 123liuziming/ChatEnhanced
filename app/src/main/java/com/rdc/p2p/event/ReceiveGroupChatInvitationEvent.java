package com.rdc.p2p.event;

import com.rdc.p2p.bean.PeerBean;

import java.util.List;

public class ReceiveGroupChatInvitationEvent {
    private List<PeerBean> groupMembers;

    public ReceiveGroupChatInvitationEvent(List<PeerBean> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public List<PeerBean> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(List<PeerBean> groupMembers) {
        this.groupMembers = groupMembers;
    }
}
