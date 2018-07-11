package com.hanium.findplace.findplace_10.models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatModel {

    //대화방 참가자들의 UID 정보
    private Map<String, Boolean> users_uid = new HashMap<>();
    //대화방 참가자들의 코멘트들
    private Map<String, Comments> users_comments = new HashMap<>();

    ///constructor
    public ChatModel(){

    }

    //setter and getter
    public Map<String, Boolean> getUsers_uid() {
        return users_uid;
    }

    public void setUsers_uid(Map<String, Boolean> users_uid) {
        this.users_uid = users_uid;
    }

    public Map<String, Comments> getUsers_comments() {
        return users_comments;
    }

    public void setUsers_comments(Map<String, Comments> users_comments) {
        this.users_comments = users_comments;
    }

    //comment innerClass
    public static class Comments{

        public String uid;
        public String comments;
        public Date sendTime;

        public Comments(){

        }

        public Comments(String uid, String comments, Date sendTime){

            this.uid = uid;
            this.comments = comments;
            this.sendTime = sendTime;

        }

    }

}
