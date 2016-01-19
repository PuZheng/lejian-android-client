package com.puzheng.the_genuine.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by abc549825@163.com(https://github.com/abc549825) at 12-12.
 */
public class Favor {
    @SerializedName("create_time")
    private Date date;
    private int id;
    private SPU spu;
    @SerializedName("favor_cnt")
    private int favorCnt;
    private int distance;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getFavorCnt() {
        return favorCnt;
    }

    public void setFavorCnt(int favorCnt) {
        this.favorCnt = favorCnt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public SPU getSPU() {
        return spu;
    }

    public void setSpu(SPU spu) {
        this.spu = spu;
    }
}