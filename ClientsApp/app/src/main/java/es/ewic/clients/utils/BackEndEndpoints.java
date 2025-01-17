package es.ewic.clients.utils;

public class BackEndEndpoints {

    //Coruña
    public static String BASE_ENDPOINT = "http://192.168.1.44:8080/ewic";

    //Client
    public static String UPDATE_DELETE_CLIENT = BASE_ENDPOINT + "/client";
    public static String LOGIN_CLIENTS = BASE_ENDPOINT + "/client/login";

    //Shop
    public static String SHOP_BASE = BASE_ENDPOINT + "/shop";
    public static String SHOP_NAMES = SHOP_BASE + "/names";
    public static String SHOP_TYPES = SHOP_BASE + "/types";
    public static String SHOP_TIMETABLE = SHOP_BASE + "/timetable";

    // Reservation
    public static String RESERVATION_BASE = BASE_ENDPOINT + "/reservation/client";

    //Configuration
    public static String CONFIGURATION_BASE = BASE_ENDPOINT + "/configuration";

    public static String CONFIGURATION_RESERVATION(int idShop) {
        return CONFIGURATION_BASE + "/" + idShop + "/reservation";
    }

    public static String CONFIGURATION_IMAGE(int idShop) {
        return CONFIGURATION_BASE + "/" + idShop + "/image";
    }

}
