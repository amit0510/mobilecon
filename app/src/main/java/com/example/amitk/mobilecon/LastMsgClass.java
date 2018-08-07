package com.example.amitk.mobilecon;

import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * Created by amitk on 22-Feb-18.
 */

public class LastMsgClass extends ArrayList<LastMsgClass> {
    public String content;
    public String ctime;
    public String receiver;
    public String username;

    public LastMsgClass(String content,String ctime,String receiver,String username){
        this.content=content;
        this.ctime=ctime;
        this.receiver=receiver;
        this.username=username;
    }

    public LastMsgClass(){

    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
