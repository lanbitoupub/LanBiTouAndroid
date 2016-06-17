package com.lanbitou.net;

/**
 * BIll链接集合地
 * Created by Henvealf on 16-5-25.
 */
public class BillUrl {

    public final static String ROOT_URL = "http://10.0.2.2:8082/lanbitou";

    public final static String ADD_ONE_BILL_URL =
            ROOT_URL + "/bill/addOne";
    public final static String ADD_SOME_BILLS_URL =
            ROOT_URL + "/bill/addSome";          //添加一些


    public final static String DELETE_ONE_BILL_URL =
            ROOT_URL + "/bill/deleteById";
    public final static String DELETE_SOME_BILLS_URL =
            ROOT_URL + "/bill/deleteSome";

    public final static String UPDATE_ONE_BILL_URL =
            ROOT_URL + "/bill/updateOneBill";
    public final static String UPDATE_SOME_BILLS_URL =
            ROOT_URL + "/bill/updateSomeBill";

    public final static String GET_ONE_BY_ID =
            ROOT_URL + "/bill/getOne";
    public final static String GET_SOME_BILLS_URL =
            ROOT_URL + "/bill/getSomeByFolder";
    public final static  String GET_SOME_BILLS_BY_UID =
            ROOT_URL + "/bill/getSomeByUid/";

    public static final String ADD_ONE_FOLDER =
                            BillUrl.ROOT_URL  + "/bill/addOneFolder";

    public static final String UPDATE_FOLDER =
            BillUrl.ROOT_URL  + "/bill/updateOneFolder";

    public static final String ADD_SOME_BILLS_FOLDER_URL =
                            BillUrl.ROOT_URL  + "/bill/addSomeFolder";

    public static final String DELETE_SOME_BILLS_FOLDER_URL =
                            BillUrl.ROOT_URL + "/bill/deleteByFolder";
}
