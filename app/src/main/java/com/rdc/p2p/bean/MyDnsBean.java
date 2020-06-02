package com.rdc.p2p.bean;

import com.rdc.p2p.util.MyDnsUtil;

import org.litepal.crud.DataSupport;

import java.util.List;

public class MyDnsBean extends DataSupport {
    private String mTargetIp;
    private String mTargetName;
    private long id;

    public String getmTargetIp() {
        return mTargetIp;
    }

    public String getmTargetName() {
        return mTargetName;
    }

    public void setmTargetIp(String mTargetIp) {
        this.mTargetIp = mTargetIp;
    }

    public void setmTargetName(String mTargetName) {
        this.mTargetName = mTargetName;
    }

    public MyDnsBean(String mTargetIp, String mTargetName) {
        this.mTargetIp = mTargetIp;
        this.mTargetName = mTargetName;
    }


    @Override
    public synchronized boolean save() {
        DataSupport.deleteAll(MyDnsBean.class, "mTargetIp = ?", mTargetIp);
        MyDnsUtil.refresh(mTargetIp);
        return super.save();
    }
}
