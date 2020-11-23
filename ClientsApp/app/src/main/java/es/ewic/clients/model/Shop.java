package es.ewic.clients.model;

import java.io.Serializable;


public class Shop implements Serializable {

    private int idShop;
    private String name;
    private double latitude;
    private double longitude;
    private String location;
    private int maxCapacity;
    private int actualCapacity;
    private String type;
    private boolean allowEntries;
    private int idSeller;

    public Shop(int idShop, String name, double latitude, double longitude, String location, int maxCapacity, int actualCapacity, String type, boolean allowEntries, int idSeller) {
        this.idShop = idShop;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
        this.maxCapacity = maxCapacity;
        this.actualCapacity = actualCapacity;
        this.type = type;
        this.allowEntries = allowEntries;
        this.idSeller = idSeller;
    }

    public int getIdShop() {
        return idShop;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getLocation() {
        return location;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public int getActualCapacity() {
        return actualCapacity;
    }

    public String getType() {
        return type;
    }

    public boolean isAllowEntries() {
        return allowEntries;
    }

    public int getIdSeller() {
        return idSeller;
    }

    @Override
    public String toString() {
        return "Shop{" +
                "idShop=" + idShop +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", location='" + location + '\'' +
                ", maxCapacity=" + maxCapacity +
                ", actualCapacity=" + actualCapacity +
                ", type='" + type + '\'' +
                ", allowEntries=" + allowEntries +
                ", idSeller=" + idSeller +
                '}';
    }
}
