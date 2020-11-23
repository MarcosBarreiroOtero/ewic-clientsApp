package es.ewic.clients.model;

import java.util.Calendar;

public class Reservation {

    private int idReservation;
    private Calendar date;
    private String state;
    private String remarks;
    private String idGoogleLoginClient;
    private int idShop;

    public Reservation(int idReservation, Calendar date, String state, String remarks, String idGoogleLoginClient, int idShop) {
        this.idReservation = idReservation;
        this.date = date;
        this.state = state;
        this.remarks = remarks;
        this.idGoogleLoginClient = idGoogleLoginClient;
        this.idShop = idShop;
    }

    public Reservation(Calendar date, String remarks, String idGoogleLoginClient, int idShop) {
        this.date = date;
        this.remarks = remarks;
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

    @Override
    public String toString() {
        return "Reservation{" +
                "idReservation=" + idReservation +
                ", date=" + date.getTime().toString() +
                ", state='" + state + '\'' +
                ", remarks='" + remarks + '\'' +
                ", idGoogleLoginClient='" + idGoogleLoginClient + '\'' +
                ", idShop=" + idShop +
                '}';
    }
}
