package com.hanium.findplace.findplace_10.models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatModel {

    public int individualOrGroup;
    public String roomName;
    public Map<String, Boolean> users_uid = new HashMap<>();//대화방 참가자들의 UID 정보
    public Map<String, Comments> users_comments = new HashMap<>(); //대화방 참가자들의 코멘트들

    ///constructor
    public ChatModel(){

    }


    //comment innerClass
    public static class Comments{

        public String uid;
        public String message;
        public Object sendTime;
        public Map<String, Object> readUsers = new HashMap<>();

        public Comments(){

        }

        public Comments(String uid, String message, Date sendTime){
            this.uid = uid;
            this.message = message;
            this.sendTime = sendTime;
        }

        public Comments(String uid, String message){
            this.uid = uid;
            this.message = message;
        }


    }

}
