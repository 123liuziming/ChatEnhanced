package com.rdc.p2p.state.SendMsgState;

import com.rdc.p2p.base.BaseMsgState;

import java.io.IOException;

public class SendMsgContext {
    private BaseMsgState state;

    public void setState(BaseMsgState state) {
        this.state = state;
    }

    public void request() throws IOException {
        state.handle();
    }

}
