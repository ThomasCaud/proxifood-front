package com.cdps.proxifood;

public class Globals{

    static String SERVER_ADDRESS = "http://proxifood.ddns.net:8080";
    static String TAG_ACTION = "[ACTION]";
    static String TAG_SUCCESS = "[SUCCESS]";
    static String TAG_FAILURE = "[FAILURE]";
    static String TAG_REQUEST = "[REQUEST]";
    static String TAG_REQUEST_DATA = "[REQUEST DATA]";
    static String TAG_SERVER_RESPONSE = "[SERVER RESPONSE]";
    static String TAG_SERVER_ERROR = "[SERVER RESPONSE]";
    static String TAG_TOKEN = "[TOKEN]";
    static String TAG_SERVER_INVALID_RESPONSE = "[INVALID RESPONSE FORMAT]";

    static String SESSION_TOKEN = "";
    static long USER_ID = -1;

    static void setSessionToken(String token) {
        SESSION_TOKEN = token;
    }

    static void setUserId(long id) {
        USER_ID = id;
    }

    static String getSessionToken() {
        return SESSION_TOKEN;
    }

    static long getUserId() { return USER_ID; }

}