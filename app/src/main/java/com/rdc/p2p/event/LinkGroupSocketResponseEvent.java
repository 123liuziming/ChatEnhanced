package com.rdc.p2p.event;

import com.rdc.p2p.bean.GroupBean;

public class LinkGroupSocketResponseEvent {
    private boolean state;
    private GroupBean groupBean;

    public GroupBean getGroupBean() {
        return groupBean;
    }

    public void setPeerBean(GroupBean groupBean) {
        this.groupBean = groupBean;
    }

    public LinkGroupSocketResponseEvent(boolean state, GroupBean peerBean){
        this.state = state;
        this.groupBean = peerBean;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
