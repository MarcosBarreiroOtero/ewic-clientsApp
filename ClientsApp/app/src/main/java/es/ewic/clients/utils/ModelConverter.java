package es.ewic.clients.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import es.ewic.clients.model.Client;
import es.ewic.clients.model.Shop;

public class ModelConverter {

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

    // [{"idShop":1,"name":"Tienda prueba 2","latitude":41.925205,"longitude":-7.438276,"location":"Queizas","maxCapacity":10,"actualCapacity":8,"type":"SUPERMARKET","allowEntries":true,"idSeller":1}


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
        ArrayList<Shop> shops = new ArrayList<Shop>();
        for (int i = 0; i < shopsData.length(); i++) {
            JSONObject shopData = shopsData.optJSONObject(i);
            shops.add(jsonObjectToShop(shopData));
        }
        return shops;
    }
}
