package com.lanbitou.entities;


/**
 * Created by joyce on 16-5-11.
 */
public class UserEntity {
    private int uid;
    private String name;
    private String password;
    private String avatar;
    private String email;
    private String created_at;

    public UserEntity() {
        super();
    }

    public UserEntity(int uid, String name, String password, String avatar, String email, String created_at) {
        this.uid = uid;
        this.name = name;
        this.password = password;
        this.avatar = avatar;
        this.email = email;
        this.created_at = created_at;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
