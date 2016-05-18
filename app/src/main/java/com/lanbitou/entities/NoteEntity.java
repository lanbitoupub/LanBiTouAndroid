package com.lanbitou.entities;

import java.util.Date;

/**
 * Created by joyce on 16-5-11.
 */
public class NoteEntity {
    private int nid;
    private int uid;
    private String title;
    private String content;
    private String notebook;
    private Date created_at;

    public NoteEntity() {

    }

    public NoteEntity(String title) {
        this(0, 0, title, null, null, null);
    }

    public NoteEntity(int nid, int uid, String title, String content, String notebook, Date created_at) {
        this.nid = nid;
        this.uid = uid;
        this.title = title;
        this.content = content;
        this.notebook = notebook;
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

    public String getNotebook() {
        return notebook;
    }

    public void setNotebook(String notebook) {
        this.notebook = notebook;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

}
