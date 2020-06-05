package com.rdc.p2p.state.ReceiveMsgState;

import com.rdc.p2p.base.BaseMsgState;

import java.io.IOException;

public class ReceiveMsgContext {
    private BaseMsgState state;

    public void setState(BaseMsgState state) {
        this.state = state;
    }

    public void request() throws IOException {
        state.handle();
    }

}

