package es.ewic.clients.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import es.ewic.clients.model.Client;
import es.ewic.clients.model.Reservation;
import es.ewic.clients.model.Shop;

public class ModelConverter {


    //CLIENT

    public static JSONObject clientToJsonObject(Client client) {
        try {
            return new JSONObject().put("idClient", client.getIdClient())
                    .put("idGoogleLogin", client.getIdGoogleLogin())
                    .put("firstName", client.getFirstName())
                    .put("lastName", client.getLastName())
                    .put("email", client.getEmail());
        } catch (JSONException e) {
            return null;
        }
    }

    public static Client jsonObjectToClient(JSONObject clientData) {
        try {
            return new Client(clientData.getInt("idClient"),
                    clientData.getString("idGoogleLogin"),
                    clientData.getString("firstName"),
                    clientData.getString("lastName"),
                    clientData.getString("email"));
        } catch (JSONException e) {
            return null;
        }
    }

    //SHOP

    public static Shop jsonObjectToShop(JSONObject shopData) {
        try {
            return new Shop(shopData.getInt("idShop"),
                    shopData.getString("name"),
                    shopData.getDouble("latitude"),
                    shopData.getDouble("longitude"),
                    shopData.getString("location"),
                    shopData.getInt("maxCapacity"),
                    shopData.getInt("actualCapacity"),
                    shopData.getString("type"),
                    shopData.getBoolean("allowEntries"),
                    shopData.getInt("idSeller"));
        } catch (JSONException e) {
            return null;
        }
    }

    public static List<Shop> jsonArrayToShopList(JSONArray shopsData) {
        ArrayList<Shop> shops = new ArrayList<>();
        for (int i = 0; i < shopsData.length(); i++) {
            JSONObject shopData = shopsData.optJSONObject(i);
            shops.add(jsonObjectToShop(shopData));
        }
        return shops;
    }

    //RESERVATION

    public static JSONObject reservationToJsonObject(Reservation reservation) {
        Calendar reservationDate = reservation.getDate();
        try {
            return new JSONObject().put("date", DateUtils.formatDateLong(reservationDate))
                    .put("remarks", reservation.getRemarks())
                    .put("idGoogleLogin", reservation.getIdGoogleLoginClient())
                    .put("idShop", reservation.getIdShop());
        } catch (JSONException e) {
            return null;
        }
    }

    public static Reservation jsonObjectToReservation(JSONObject reservationData) {
        try {
            Calendar reservationDate = DateUtils.parseDateLong(reservationData.getString("date"));
            return new Reservation(reservationData.getInt("idReservation"),
                    reservationDate,
                    reservationData.getString("state"),
                    reservationData.getString("remarks"),
                    reservationData.getString("idGoogleLoginClient"),
                    reservationData.getInt("idShop"));
        } catch (JSONException e) {
            return null;
        }

    }
}
