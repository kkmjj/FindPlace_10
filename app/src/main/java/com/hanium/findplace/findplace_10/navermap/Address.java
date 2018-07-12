package com.hanium.findplace.findplace_10.navermap;

public class Address {

    //member Variables
    private String address;
    private String country;
    private String sido;
    private String sigugun;
    private String dongmyun;
    private String ri;
    private String rest;

    private int x;
    private int y;

    //constructor
    public Address(){
        this.address = "주소정보없음";
        this.country = "국가정보없음";
        this.sido = "시도정보없음";
        this.sigugun = "시구군정보없음";
        this.dongmyun = "동면정보없음";
        this.ri = "리정보없음";
        this.rest = "기타주소정보없음";
    }

    public Address(String address, String country, String sido, String sigugun, String dongmyun, String ri, String rest){
        this();
        this.setAddress(address);
        this.setCountry(country);
        this.setSido(sido);
        this.setSigugun(sigugun);
        this.setDongmyun(dongmyun);
        this.setRi(ri);
        this.setRest(rest);
    }

    public Address(String address){
        this();
        this.setAddress(address);
    }

    //getter setter
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSido() {
        return sido;
    }

    public void setSido(String sido) {
        this.sido = sido;
    }

    public String getSigugun() {
        return sigugun;
    }

    public void setSigugun(String sigugun) {
        this.sigugun = sigugun;
    }

    public String getDongmyun() {
        return dongmyun;
    }

    public void setDongmyun(String dongmyun) {
        this.dongmyun = dongmyun;
    }

    public String getRi() {
        return ri;
    }

    public void setRi(String ri) {
        this.ri = ri;
    }

    public String getRest() {
        return rest;
    }

    public void setRest(String rest) {
        this.rest = rest;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
