package com.lanbitou.entities;

/**
 * Created by joyce on 16-5-11.
 */
public class NoteEntity {
    private int nid;
    private int uid;
    private int bid;
    private String title;
    private String content;
    private Boolean mark;
    private String created_at;

    public NoteEntity() {
        super();
    }

    public NoteEntity(String title) {
        this(0, 0, 0, title, null, null, null);
    }

    public NoteEntity(int nid, int uid, int bid, String title, String content, Boolean mark, String created_at) {
        this.nid = nid;
        this.uid = uid;
        this.bid = bid;
        this.title = title;
        this.content = content;
        this.mark = mark;
        this.created_at = created_at;
    }

    public int getNid() {
        return nid;
    }

    public void setNid(int nid) {
        this.nid = nid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getMark() {
        return mark;
    }

    public void setMark(Boolean mark) {
        this.mark = mark;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

}
