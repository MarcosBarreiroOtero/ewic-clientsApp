package es.ewic.clients.utils;

public class BackEndEndpoints {

    public static String BASE_ENDPOINT = "http://192.168.1.44:8080/ewic";

    //Client
    public static String UPDATE_DELETE_CLIENT = BASE_ENDPOINT + "/client";
    public static String LOGIN_CLIENTS = BASE_ENDPOINT + "/client/login";

    //Shop
    public static String SHOP_BASE = BASE_ENDPOINT + "/shop";

}
