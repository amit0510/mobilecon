package com.example.amitk.mobilecon;

/**
 * Created by amitk on 19-Jan-18.
 */

public class CharModel {
    public String chatMessage;
    public boolean isSend;
    public Long times;
    public String uname;
    public CharModel(String chatMessage,boolean isSend,long times,String uname){
        this.chatMessage=chatMessage;
        this.isSend=isSend;
        this.times=times;
        this.uname=uname;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(String chatMessage) {
        this.chatMessage = chatMessage;
    }

    public boolean isSend() {
        return isSend;
    }

    public void setSend(boolean send) {
        isSend = send;
    }

    public Long getTimes() {
        return times;
    }

    public void setTimes(Long times) {
        this.times = times;
    }
}
