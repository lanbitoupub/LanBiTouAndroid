package com.lanbitou.entities;

/**
 * Created by joyce on 16-5-24.
 */
public class NoteBookEntity {

    private int bid;
    private int uid;
    private String name;


    public NoteBookEntity(int bid, int uid, String name) {
        this.bid = bid;
        this.uid = uid;
        this.name = name;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
