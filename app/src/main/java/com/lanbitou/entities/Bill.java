package com.lanbitou.entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Henvealf on 16-5-15.
 */
public class Bill implements Serializable{

    private int id;
    private int uid;                 //用户Id
    private String type;             //收支类型
    private double money;            //收或支多少钱.
    private String remark;          //备注
    private String folder;           //所属的文件夹
    private Date billDate;           //账单时间,并非创建时间,由用户指定

    public Bill(){

    }

    public Bill(int id, int uid, String type, double money, String remark, String folder, Date billDate) {
        this.id = id;
        this.uid = uid;
        this.type = type;
        this.money = money;
        this.remark = remark;
        this.folder = folder;
        this.billDate = billDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRemark() {
        return remark;
    }

    public void setBillDate(Date billDate) {
        this.billDate = billDate;
    }

    public Date getBillDate() {
        return billDate;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

}
