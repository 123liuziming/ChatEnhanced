package com.rdc.p2p.bean;

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

    public static MyDnsBean findPersonByPersonId(String personId) {
        List<MyDnsBean> persons = DataSupport.where("mTargetIp = ?", personId).find(MyDnsBean.class);
        if (persons == null || persons.size() == 0) {
            return null;
        } else {
            for (int i = 1; i < persons.size(); i++) {
                persons.get(i).delete();
            }
        }
        return persons.get(0);
    }

    @Override
    public synchronized boolean save() {
        MyDnsBean p = findPersonByPersonId(mTargetIp);
        if (p == null || p.id == 0) {
            return super.save();
        } else {
            this.id = p.id;
            return super.save();
        }
    }
}
