package com.safewifi;

/**
 * Created by JiseokWoo
 * 공통 Command
 */
public class Command {
    // basic
    public final static String SUCCESS = "SUCCESS";
    public final static String FAIL = "FAIL";

    // operation
    public final static String GET = "GET";
    public final static String PUT = "PUT";

    // error
    public final static String WIFI_ENABLE_ERROR = "WIFI_ENABLE_ERROR";
    public final static String WIFI_UNAVAILABLE_ERROR = "WIFI_UNAVAILABLE_ERROR";
    public final static String NO_MAC_ERROR = "NO_MAC_ERROR";
    public final static String DB_INSERT_ERROR = "DB_INSERT_ERROR";
    public final static String DB_SELECT_ERROR = "DB_SELECT_ERROR";
    public final static String EMPTY = "EMPTY";
}
