package com.lanbitou.net;

/**
 * BIll链接集合地
 * Created by Henvealf on 16-5-25.
 */
public class NoteUrl {

    public final static String ROOT_URL = "http://192.168.1.108:8082/lanbitou";

    /***笔记本***/
    public final static String NOTEBOOK_UPDATEONE = ROOT_URL + "/notebook/updateOne";
    public final static String NOTEBOOK_UPDATEALL = ROOT_URL + "/notebook/updateAll";
    public final static String NOTEBOOK_DELETEALL = ROOT_URL + "/notebook/deleteAll";
    public final static String NOTEBOOK_POSTALL = ROOT_URL + "/notebook/postAll";

    public final static String NOTEBOOK_POSTONE = ROOT_URL + "/notebook/postOne";
    public final static String NOTEBOOK_DELETEONE = ROOT_URL + "/notebook/deleteOne";
    public final static String NOTEBOOK_GETALL = ROOT_URL + "/notebook/getAll";

    /***笔记***/
    public final static String NOTE_UPDATEALL = ROOT_URL + "/note/updateAll";
    public final static String NOTE_DELETEALL = ROOT_URL + "/note/deleteAll";
    public final static String NOTE_POSTALL = ROOT_URL + "/note/postAll";

    public final static String NOTE_GETONE = ROOT_URL + "/note/getOne";
    public final static String NOTE_GETALL = ROOT_URL + "/note/getAll";

    public final static String NOTE_UPDATEONE = ROOT_URL + "/note/updateOne";
    public final static String NOTE_POSTONE = ROOT_URL + "/note/postOne";
    public final static String NOTE_DELETEONE = ROOT_URL + "/note/deleteOne";

    public final static String NOTE_GETSOME = ROOT_URL + "/note/getSome";



}
