package com.rdc.p2p.state.ReceiveMsgState;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.rdc.p2p.app.App;
import com.rdc.p2p.base.BaseMsgState;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.bean.MyDnsBean;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.contract.GroupListContract;
import com.rdc.p2p.contract.PeerListContract;
import com.rdc.p2p.util.MyDnsUtil;
import com.rdc.p2p.util.SDUtil;

import org.litepal.crud.DataSupport;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.rdc.p2p.model.PeerListModel.TAG;

public class ReceiveMsgNormalState extends BaseMsgState {

    private String mTargetIp;
    private PeerListContract.Presenter mPresenter;
    private GroupListContract.Presenter mGroupPresenter;
    private int type;

    public ReceiveMsgNormalState(InputStream dis, String mTargetIp, PeerListContract.Presenter mPresenter, GroupListContract.Presenter mGroupPresenter, int type){
        super(dis);
        this.mPresenter = mPresenter;
        this.mTargetIp = mTargetIp;
        this.type = type;
        this.mGroupPresenter = mGroupPresenter;
    }

    @Override
    public void handle() throws IOException {
        Log.d(TAG, "DNS is\n");
        for(MyDnsBean m : DataSupport.where("id is not null").find(MyDnsBean.class)) {
            Log.d(TAG, m.getmTargetIp() + " " + m.getmTargetName());
        }
        DataInputStream dis = new DataInputStream(inputStream);
        boolean isGroup =dis.readBoolean();
        String groupName = "";
        int groupNameLen = 0;
        if(isGroup) {
            groupNameLen = dis.readInt();
            byte[] groupNameBytes = new byte[groupNameLen];
            dis.readFully(groupNameBytes);
            groupName = new String(groupNameBytes, "utf-8");
        }
        int bytesLength = dis.readInt();
        byte[] dataBytes = new byte[bytesLength];
        dis.readFully(dataBytes);
        Log.d(TAG, "ip and name is" + mTargetIp + MyDnsUtil.convertUserIp(mTargetIp));
        MessageBean msgBean = MessageBean.getInstance(mTargetIp);
        if(isGroup)
            msgBean.setGroupName(groupName);
        msgBean.setUserName(App.getUserBean().getNickName());
        msgBean.setUserIp(mTargetIp);
        msgBean.setMine(false);
        switch (type){
            case Protocol.TEXT:
                String msg = new String(dataBytes, "utf-8");
                Log.d(TAG, "run: receive text:" + msg);
                msgBean.setMsgType(Protocol.TEXT);
                msgBean.setText(msg);
                msgBean.setGroupMsg(isGroup);
                break;
            case Protocol.AUDIO:
                msgBean.setMsgType(Protocol.AUDIO);
                Log.d(TAG, "run: audio size = " + bytesLength);
                msgBean.setGroupMsg(isGroup);
                msgBean.setAudioPath(SDUtil.saveAudio(dataBytes, System.currentTimeMillis() + ""));
                break;
            case Protocol.IMAGE:
                Bitmap bitmap = BitmapFactory.decodeByteArray(dataBytes, 0, bytesLength);
                Log.d(TAG, "run: image size = " + bytesLength);
                msgBean.setMsgType(Protocol.IMAGE);
                msgBean.setGroupMsg(isGroup);
                msgBean.setImagePath(SDUtil.saveBitmap(bitmap, System.currentTimeMillis() + "", ".jpg"));
                break;
        }
        msgBean.getDate();
        msgBean.save();
        if(!msgBean.isGroupMessage())
            mPresenter.messageReceived(msgBean);
        else
            mGroupPresenter.messageReceived(msgBean);

    }
}
