package com.lanbitou.entities;

/**
 * 文件夹实体类
 * Created by Henvealf on 16-5-24.
 */
public class BillFolder {
    private int uid;
    private String name;
    private boolean inClouded;

    public BillFolder(){
        this.inClouded = true;
    }

    public BillFolder(int uid,String name) {
        this.name = name;
        this.uid = uid;
        this.inClouded = true;
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

    public boolean isInClouded() {
        return inClouded;
    }

    public void setInClouded(boolean inClouded) {
        this.inClouded = inClouded;
    }
}
