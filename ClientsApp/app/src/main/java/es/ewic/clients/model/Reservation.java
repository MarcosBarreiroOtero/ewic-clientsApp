package es.ewic.clients.model;

import java.io.Serializable;
import java.util.Calendar;

public class Reservation implements Serializable {

    private int idReservation;
    private Calendar date;
    private String state;
    private String remarks;
    private int nClients;
    private String idGoogleLoginClient;
    private int idShop;
    private String shopName;

    public Reservation(int idReservation, Calendar date, String state, String remarks, int nClients, String idGoogleLoginClient, int idShop, String shopName) {
        this.idReservation = idReservation;
        this.date = date;
        this.state = state;
        this.remarks = remarks;
        this.nClients = nClients;
        this.idGoogleLoginClient = idGoogleLoginClient;
        this.idShop = idShop;
        this.shopName = shopName;
    }

    public Reservation(Calendar date, String remarks, int nClients, String idGoogleLoginClient, int idShop) {
        this.date = date;
        this.remarks = remarks;
        this.nClients = nClients;
        this.idGoogleLoginClient = idGoogleLoginClient;
        this.idShop = idShop;
    }

    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public int getnClients() {
        return nClients;
    }

    public void setnClients(int nClients) {
        this.nClients = nClients;
    }


    public String getIdGoogleLoginClient() {
        return idGoogleLoginClient;
    }

    public void setIdGoogleLoginClient(String idGoogleLoginClient) {
        this.idGoogleLoginClient = idGoogleLoginClient;
    }

    public int getIdShop() {
        return idShop;
    }

    public void setIdShop(int idShop) {
        this.idShop = idShop;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }
}
