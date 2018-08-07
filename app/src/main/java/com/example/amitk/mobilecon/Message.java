package com.example.amitk.mobilecon;

/**
 * Created by amitk on 26-Dec-17.
 */

public class Message {
    private String content,username,ctime;

    public Message(){

    }
    public Message(String content,String username,String ctime)
    {
        this.content=content;
        this.username=username;
        this.ctime=ctime;
    }

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
