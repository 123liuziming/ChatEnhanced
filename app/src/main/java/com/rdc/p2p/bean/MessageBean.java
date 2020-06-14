package com.rdc.p2p.bean;

import android.annotation.SuppressLint;

import com.rdc.p2p.util.MyDnsUtil;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Lin Yaotian on 2018/5/16.
 */
public class MessageBean extends DataSupport implements Cloneable {
    private String groupName;
    private String userName;
    private String belongName;
    private String userIp;//消息发送方IP
    private String belongIp; //数据库存储标识，区分消息归属方(例如 我和A聊天，则我本地存储的我和A之间相互发送的 MessageBean 的 belongIp 都是 A 的IP)
    private String text;
    private String time;
    private String imagePath;
    private String audioPath;
    private String filePath;
    private String fileName;
    private Date mDate;
    private int fileSize;
    private int fileState;//传输状态
    private int transmittedSize;//已经传输的字节
    private int msgType;//消息类型 音频/图片/文字/文件
    private boolean isMine;//是否是本人发的消息
    private int sendStatus;//若为本人发的音频/图片/文字消息，发送状态有 SEND_FILE_ING ,SEND_FINISH,SEND_ERROR



    private MessageBean(String belongIp){
        this.belongIp = belongIp;
    }

    private MessageBean(String belongIp, String belongName) {
        this.belongIp = belongIp;
        this.belongName = belongName;
    }

    private MessageBean(String belongIp, String belongName, String groupName) {
        this.belongIp = belongIp;
        this.belongName = belongName;
        this.groupName = groupName;
    }

    public static MessageBean getInstance(String belongIp) {
        // 静态工厂模式联合享元模式
        String belongName = MyDnsUtil.convertUserIp(belongIp);
        return new MessageBean(belongIp, belongName);
    }

    public static MessageBean getInstance(String belongIp,String groupName) {
        // 静态工厂模式联合享元模式
        String belongName = MyDnsUtil.convertUserIp(belongIp);
        return new MessageBean(belongIp, belongName, groupName);
    }

    public Boolean isGroupMessage(){
        return groupName != null;
    }


    @Override
    public MessageBean clone() {
        MessageBean messageBean = null;
        try {
            messageBean = (MessageBean) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return messageBean;
    }

    public void updateFileState(MessageBean messageBean){
        this.transmittedSize = messageBean.getTransmittedSize();
        this.fileState = messageBean.getFileState();
        this.filePath = messageBean.getFilePath();
        this.fileName = messageBean.getFileName();
        this.fileSize = messageBean.getFileSize();
    }

    @Override
    public String toString() {
        return "MessageBean{" +
                "userIp='" + userIp + '\'' +
                ", belongIp='" + belongIp + '\'' +
                ", text='" + text + '\'' +
                ", time='" + time + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", audioPath='" + audioPath + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", fileState=" + fileState +
                ", transmittedSize=" + transmittedSize +
                ", msgType=" + msgType +
                ", isMine=" + isMine +
                ", sendStatus=" + sendStatus +
                '}';
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getBelongName() {
        return belongName;
    }

    public void setBelongName(String belongName) {
        this.belongName = belongName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public String getBelongIp() {
        return belongIp == null ? "" : belongIp;
    }

    public void setBelongIp(String belongIp) {
        this.belongIp = belongIp == null ? "" : belongIp;
    }

    public String getFilePath() {
        return filePath == null ? "" : filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath == null ? "" : filePath;
    }

    public String getFileName() {
        return fileName == null ? "" : fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? "" : fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getFileState() {
        return fileState;
    }

    public void setFileState(int fileState) {
        this.fileState = fileState;
    }

    public int getTransmittedSize() {
        return transmittedSize;
    }

    public void setTransmittedSize(int transmittedSize) {
        this.transmittedSize = transmittedSize;
    }

    public int getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(int sendStatus) {
        this.sendStatus = sendStatus;
    }

    public String getUserIp() {
        return userIp == null ? "" : userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp == null ? "" : userIp;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public String getText() {
        if (text == null || text.equals("")){
            if (audioPath != null){
                return "语音";
            }else if (imagePath != null){
                return "图片";
            }else if (filePath != null){
                return "文件";
            }
        }
        return text == null ? "" : text;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
    }

    public String getTime() {
        return time;
    }

    public Date getDate() {
        if (time == null){
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date date = new Date();
            time = sdf.format(date);
            mDate = date;
        }
        return mDate;
    }

    public void setTime(String time) {
        this.time = time == null ? "" : time;
    }

    public String getImagePath() {
        return imagePath == null ? "" : imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath == null ? "" : imagePath;
    }

    public String getAudioPath() {
        return audioPath == null ? "" : audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath == null ? "" : audioPath;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }
}
