package com.rdc.p2p.thread;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.rdc.p2p.app.App;
import com.rdc.p2p.base.BaseMsgState;
import com.rdc.p2p.bean.GroupBean;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.bean.MyDnsBean;
import com.rdc.p2p.bean.UserBean;
import com.rdc.p2p.config.Constant;
import com.rdc.p2p.config.FileState;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.contract.GroupListContract;
import com.rdc.p2p.listener.OnSocketSendCallback;
import com.rdc.p2p.manager.SocketManager;
import com.rdc.p2p.state.SendMsgState.FileMsgState;
import com.rdc.p2p.state.SendMsgState.ImageMsgState;
import com.rdc.p2p.state.SendMsgState.SendMsgContext;
import com.rdc.p2p.state.SendMsgState.TextMsgState;
import com.rdc.p2p.util.GsonUtil;
import com.rdc.p2p.util.MyDnsUtil;
import com.rdc.p2p.util.SDUtil;

import org.litepal.crud.DataSupport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GroupSocketThread extends Thread{

    private static final int DESTROY = 0;
    private static final int DELAY_DESTROY = 1;
    private static final String TAG = "SocketThread";
    private static final int DELAY_MILLIS = 1000 * 60 * 60;
    private Socket mSocket;
    private GroupListContract.Presenter mPresenter;
    private String mTargetIp;
    private boolean mTimeOutNeedDestroy;//超过 DELAY_MILLIS 时间没有进行通信，则把Socket连接关闭，但界面上仍然显示
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private AtomicBoolean mIsFileReceived;
    private OnSocketSendCallback mOnSocketSendCallback;
    private boolean mKeepUser;
    private SendMsgContext sendMsgContext = new SendMsgContext();
    private BaseMsgState state;


    public GroupSocketThread(Socket mSocket, GroupListContract.Presenter mPresenter) {
        mTargetIp = mSocket.getInetAddress().getHostAddress();
        mTimeOutNeedDestroy = true;
        mKeepUser = false;
        this.mSocket = mSocket;
        this.mPresenter = mPresenter;
        mIsFileReceived = new AtomicBoolean(true);
        mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case DESTROY:
                        Log.d(TAG, "handleMessage: 销毁" + mTargetIp);
                        if (mTimeOutNeedDestroy) {
                            sendRequest(App.getUserBean(), Protocol.KEEP_USER);
                        } else {
                            mTimeOutNeedDestroy = true;
                            mHandler.sendEmptyMessageDelayed(DESTROY, DELAY_MILLIS);
                        }
                        break;
                    case DELAY_DESTROY:
                        Log.d(TAG, "handleMessage: 延迟销毁" + mTargetIp);
                        mTimeOutNeedDestroy = false;
                        break;
                }
            }
        };
    }

    public void setOnSocketSendCallback(OnSocketSendCallback onSocketSendCallback) {
        this.mOnSocketSendCallback = onSocketSendCallback;
    }

    /**
     * 发送消息
     *
     * @param messageBean
     * @return
     */
    public void sendMsg(MessageBean messageBean, int position) {
        while (!mIsFileReceived.get()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                messageBean.setFileState(FileState.SEND_FILE_ERROR);
                messageBean.setSendStatus(Constant.SEND_MSG_ERROR);
                messageBean.save();
                if (mOnSocketSendCallback != null) {
                    if (messageBean.getMsgType() == Protocol.FILE) {
                        mOnSocketSendCallback.fileSending(position, messageBean);
                    } else {
                        mOnSocketSendCallback.sendMsgError(position);
                    }
                }
            }
        }
        mHandler.sendEmptyMessage(DELAY_DESTROY);
        try {
            DataOutputStream dos = new DataOutputStream(mSocket.getOutputStream());
            dos.writeInt(messageBean.getMsgType());
            switch (messageBean.getMsgType()) {
                case Protocol.TEXT:
                    state = new TextMsgState(messageBean, mOnSocketSendCallback, position, dos);
                    sendMsgContext.setState(state);
                    sendMsgContext.request();
                    break;
                case Protocol.IMAGE:
                    FileInputStream imageInputStream = new FileInputStream(messageBean.getImagePath());
                    state = new ImageMsgState(messageBean, mOnSocketSendCallback, position, dos, imageInputStream);
                    sendMsgContext.setState(state);
                    sendMsgContext.request();
                    break;
                case Protocol.AUDIO:
                    FileInputStream audioInputStream = new FileInputStream(messageBean.getAudioPath());
                    state = new ImageMsgState(messageBean, mOnSocketSendCallback, position, dos, audioInputStream);
                    sendMsgContext.setState(state);
                    sendMsgContext.request();
                    break;
                case Protocol.FILE:
                    mIsFileReceived.set(false);
                    FileInputStream fileInputStream = new FileInputStream(messageBean.getFilePath());
                    state = new FileMsgState(messageBean, mOnSocketSendCallback, position, dos, fileInputStream);
                    sendMsgContext.setState(state);
                    sendMsgContext.request();
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            mIsFileReceived.set(true);
            messageBean.setUserName(App.getUserBean().getNickName());
            messageBean.setUserIp(App.getMyIP());
            messageBean.setFileState(FileState.SEND_FILE_ERROR);
            messageBean.setSendStatus(Constant.SEND_MSG_ERROR);
            messageBean.getDate();
            messageBean.save();
            if (mOnSocketSendCallback != null) {
                if (messageBean.getMsgType() == Protocol.FILE) {
                    mOnSocketSendCallback.fileSending(position, messageBean);
                } else {
                    mOnSocketSendCallback.sendMsgError(position);
                }
            }
        }
    }

    /**
     * 发送连接或连接响应请求
     *
     * @param userBean
     * @param msgType
     * @return
     */
    public boolean sendRequest(UserBean userBean, int msgType) {
        mHandler.sendEmptyMessage(DELAY_DESTROY);
        try {
            DataOutputStream dos = new DataOutputStream(mSocket.getOutputStream());
            dos.writeInt(msgType);
            switch (msgType) {
                case Protocol.CONNECT:
                    dos.writeUTF(GsonUtil.gsonToJson(userBean));
                    break;
                case Protocol.CONNECT_RESPONSE:
                    dos.writeUTF(GsonUtil.gsonToJson(userBean));
                    break;
                case Protocol.FILE_RECEIVED:
                    break;
                case Protocol.KEEP_USER:
                    break;
                case Protocol.DISCONNECT:
                    break;

            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    @Override
    public void run() {
        mHandler.sendEmptyMessageDelayed(DESTROY, DELAY_MILLIS);
        MyDnsBean myDnsBean;
        try {
            DataInputStream dis = new DataInputStream(mSocket.getInputStream());
            //循环读取消息
            while (true) {
                int type = dis.readInt();
                mHandler.sendEmptyMessage(DELAY_DESTROY);
                switch (type) {
                    case Protocol.DISCONNECT:
                        Log.d(TAG, "Protocol disconnect ! ip=" + mTargetIp);
                        mKeepUser = false;
                        DataSupport.deleteAll(MyDnsBean.class, "mTargetIp = ?", mTargetIp);
                        System.out.println("已删除" + mTargetIp);
                        MyDnsUtil.refresh(mTargetIp);
                        mSocket.close();
                        break;
                    case Protocol.CONNECT:
                        Log.d(TAG, "连接到" + mTargetIp);
                        String u1 = dis.readUTF();
                        GroupBean groupInfo = getGroup(u1);
                        Log.d(TAG, "DNS is\n");
                        for(MyDnsBean m : DataSupport.where("id is not null").find(MyDnsBean.class)) {
                            Log.d(TAG, m.getmTargetIp() + " " + m.getmTargetName());
                        }
                        List<MessageBean> allMessage = DataSupport.where("belongName = ? and userName = ?",groupInfo.getNickName(), App.getUserBean().getNickName()).find(MessageBean.class);
                        setLatestMsg(allMessage, groupInfo);
                        //回复连接响应
                        sendRequest(App.getUserBean(), Protocol.CONNECT_RESPONSE);
                        break;
                    case Protocol.CONNECT_RESPONSE:
                        Log.d(TAG, "连接到" + mTargetIp);
                        String u2 = dis.readUTF();
                        groupInfo = getGroup(u2);

                        Log.d(TAG, "DNS is\n");
                        for(MyDnsBean m : DataSupport.where("id is not null").find(MyDnsBean.class)) {
                            Log.d(TAG, m.getmTargetIp() + " " + m.getmTargetName());
                        }
                        allMessage = DataSupport.where("belongName = ? and userName = ?",groupInfo.getNickName(), App.getUserBean().getNickName()).find(MessageBean.class);
                        setLatestMsg(allMessage, groupInfo);
                        break;
                    case Protocol.KEEP_USER_RESPONSE:
                        mKeepUser = true;
                        mSocket.close();
                        break;
                    case Protocol.KEEP_USER:
                        sendRequest(App.getUserBean(), Protocol.KEEP_USER_RESPONSE);
                        mKeepUser = true;
                        break;
                    case Protocol.FILE_RECEIVED:
                        mIsFileReceived.set(true);
                        break;
                    case Protocol.TEXT:
                        Log.d(TAG, "DNS is\n");
                        for(MyDnsBean m : DataSupport.where("id is not null").find(MyDnsBean.class)) {
                            Log.d(TAG, m.getmTargetIp() + " " + m.getmTargetName());
                        }
                        int textBytesLength = dis.readInt();
                        byte[] textBytes = new byte[textBytesLength];
                        dis.readFully(textBytes);
                        String text = new String(textBytes, "utf-8");
                        Log.d(TAG, "run: receive text:" + text);
                        Log.d(TAG, "ip and name is" + mTargetIp + MyDnsUtil.convertUserIp(mTargetIp));
                        MessageBean textMsg = MessageBean.getInstance(mTargetIp);
                        textMsg.setUserName(App.getUserBean().getNickName());
                        textMsg.setUserIp(mTargetIp);
                        textMsg.setMsgType(Protocol.TEXT);
                        textMsg.setMine(false);
                        textMsg.setText(text);
                        textMsg.getDate();
                        textMsg.save();
                        mPresenter.messageReceived(textMsg);
                        break;
                    case Protocol.IMAGE:
                        int size = dis.readInt();
                        byte[] bytes = new byte[size];
                        dis.readFully(bytes);
                        Log.d(TAG, "run: image size = " + size);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, size);
                        MessageBean imageMsg = MessageBean.getInstance(mTargetIp);
                        imageMsg.setUserName(App.getUserBean().getNickName());
                        imageMsg.setUserIp(mTargetIp);
                        imageMsg.setMine(false);
                        imageMsg.setMsgType(Protocol.IMAGE);
                        imageMsg.setImagePath(SDUtil.saveBitmap(bitmap, System.currentTimeMillis() + "", ".jpg"));
                        imageMsg.getDate();
                        imageMsg.save();
                        mPresenter.messageReceived(imageMsg);
                        break;
                    case Protocol.AUDIO:
                        int audioSize = dis.readInt();
                        byte[] audioBytes = new byte[audioSize];
                        dis.readFully(audioBytes);
                        MessageBean audioMsg = MessageBean.getInstance(mTargetIp);
                        audioMsg.setUserName(App.getUserBean().getNickName());
                        audioMsg.setUserIp(mTargetIp);
                        audioMsg.setMine(false);
                        audioMsg.setMsgType(Protocol.AUDIO);
                        audioMsg.setAudioPath(SDUtil.saveAudio(audioBytes, System.currentTimeMillis() + ""));
                        audioMsg.getDate();
                        audioMsg.save();
                        mPresenter.messageReceived(audioMsg);
                        break;
                    case Protocol.FILE:
                        int fileSize = dis.readInt();
                        String fileName = dis.readUTF();//文件名，包括了文件类型(e.g: pic.jpg)
                        Log.d(TAG, "run: 接收到一个文件=" + fileName);
                        byte[] fileBytes = new byte[1024 * 1024];
                        //解析出文件名和类型
                        int dotIndex = fileName.lastIndexOf(".");
                        String fileType;//文件类型(e.g: .jpg)
                        String name;//文件名(e.g: pic)
                        if (dotIndex != -1) {
                            fileType = fileName.substring(dotIndex, fileName.length());
                            name = fileName.substring(0, dotIndex);
                        } else {
                            //解析不到文件类型
                            fileType = "";
                            name = fileName;
                        }
                        File file = SDUtil.getMyAppFile(name, fileType);
                        if (file != null) {
                            MessageBean fileMsg = MessageBean.getInstance(mTargetIp);
                            fileMsg.setUserName(App.getUserBean().getNickName());
                            fileMsg.setFilePath(file.getAbsolutePath());
                            fileMsg.setFileName(file.getName());
                            fileMsg.setFileSize(fileSize);
                            fileMsg.setFileState(FileState.RECEIVE_FILE_START);
                            fileMsg.setTransmittedSize(0);
                            fileMsg.setUserIp(mTargetIp);
                            fileMsg.setMine(false);
                            fileMsg.setMsgType(Protocol.FILE);
                            fileMsg.getDate();
                            fileMsg.save();
                            mPresenter.fileReceiving(fileMsg);
                            FileOutputStream fos = new FileOutputStream(file);
                            int transLen = 0;
                            int countBytes = 0;//计算传输的字节，达到一定数量才更新界面
                            int read;
                            while (true) {
                                read = dis.read(fileBytes);
                                transLen += read;
                                countBytes += read;
                                if (read == -1) {
                                    Log.d(TAG, "run: read=-1");
                                    //对方关闭
                                    fileMsg.setSendStatus(FileState.RECEIVE_FILE_ERROR);
                                    fileMsg.saveOrUpdate("belongName = ? and filePath = ?", fileMsg.getBelongName(), fileMsg.getFilePath());
                                    file.delete();
                                    mPresenter.fileReceiving(fileMsg);
                                    sendRequest(App.getUserBean(), Protocol.FILE_RECEIVED);
                                    break;
                                }
                                fos.write(fileBytes, 0, read);
                                if (transLen == fileSize) {
                                    fos.flush();
                                    fos.close();
                                    fileMsg = fileMsg.clone();
                                    fileMsg.setTransmittedSize(transLen);
                                    fileMsg.setFileState(FileState.RECEIVE_FILE_FINISH);
                                    fileMsg.saveOrUpdate("belongName = ? and filePath = ?", fileMsg.getBelongName(), fileMsg.getFilePath());
                                    mPresenter.fileReceiving(fileMsg);
                                    sendRequest(App.getUserBean(), Protocol.FILE_RECEIVED);
                                    break;
                                }
                                if (countBytes >= 1024 * 300) {
                                    //每接收到300KB数据就更新一次界面
                                    fileMsg = fileMsg.clone();
                                    fileMsg.setTransmittedSize(transLen);
                                    fileMsg.setFileState(FileState.RECEIVE_FILE_ING);
                                    fileMsg.saveOrUpdate("belongName = ? and filePath = ?", fileMsg.getBelongName(), fileMsg.getFilePath());
                                    mPresenter.fileReceiving(fileMsg);
                                    countBytes = 0;
                                }
                            }
                        } else {
                            mSocket.close();
                        }
                        break;
                }
            }
        } catch (IOException e) {
            DataSupport.deleteAll(MyDnsBean.class, "mTargetIp = ?", mTargetIp);
            MyDnsUtil.refresh(mTargetIp);
            e.printStackTrace();
            if (!mKeepUser) {
                mPresenter.removeGroup(mTargetIp);
            }
            SocketManager.getInstance().removeSocketByIp(mTargetIp);
            SocketManager.getInstance().removeSocketThreadByIp(mTargetIp);
            mHandlerThread.quitSafely();
        }
    }

    /**
     * 根据UserGson构造PeerBean
     *
     * @param userGson User实体类对应的gson
     * @return PeerBean
     */
    @NonNull
    private GroupBean getGroup(String userGson) {
        UserBean userBean = GsonUtil.gsonToBean(userGson, UserBean.class);
        GroupBean peer = new GroupBean();
        peer.setGroupImageId(userBean.getUserImageId());
        peer.setNickName(userBean.getNickName());
        return peer;
    }

    private void setLatestMsg(List<MessageBean> allMsg, GroupBean groupInfo) {
        if (allMsg.size() != 0) {
            MessageBean tmp = Collections.max(allMsg, new Comparator<MessageBean>() {
                @Override
                public int compare(MessageBean messageBean, MessageBean t1) {
                    return messageBean.getDate().compareTo(t1.getDate());
                }
            });
            groupInfo.setRecentMessage(tmp.getText());
        }
        mPresenter.addGroup(groupInfo);

    }

}
