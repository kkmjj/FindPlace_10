package com.hanium.findplace.findplace_10.models;

import java.util.HashMap;
import java.util.Map;

public class UserModel {

    //member variables
    private String uid;
    private String profileURL;
    private String email;
    private String password;
    private String nickName;
    private String phoneNumber;
    private String address;
    private String pushToken;

    private Map<String, Boolean> friendUidList = new HashMap<>();

    //constructor
    public UserModel(){

    }

    public UserModel(String uid, String email, String password, String nickName, String phoneNumber){
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.nickName = nickName;
        this.phoneNumber = phoneNumber;
        this.address = "주소정보없음";
        this.profileURL = "사진정보없음";
    }

    public UserModel(String uid, String email, String password, String nickName, String phoneNumber, String address){
        this(uid, email, password, nickName, phoneNumber);
        this.address = address;
    }

    //getter and setter
    public Map<String, Boolean> getFriendUidList() {
        return friendUidList;
    }

    public void setFriendUidList(Map<String, Boolean> friendUidList) {
        this.friendUidList = friendUidList;
    }

    public String getPushToken(){ return pushToken;}

    public void setPushToken(String pushToken){ this.pushToken = pushToken; }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getProfileURL() {
        return profileURL;
    }

    public void setProfileURL(String profileURL) {
        this.profileURL = profileURL;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
