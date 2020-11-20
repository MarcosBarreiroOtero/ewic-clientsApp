package es.ewic.clients.model;

import java.io.Serializable;

public class Client implements Serializable {

    private int idClient;
    private String idGoogleLogin;
    private String firstName;
    private String lastName;
    private String email;

    public Client(int idClient, String idGoogleLogin, String firstName, String lastName, String email) {
        this.idClient = idClient;
        this.idGoogleLogin = idGoogleLogin;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    public String getIdGoogleLogin() {
        return idGoogleLogin;
    }

    public void setIdGoogleLogin(String idGoogleLogin) {
        this.idGoogleLogin = idGoogleLogin;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Client{" +
                "idClient=" + idClient +
                ", idGoogleLogin='" + idGoogleLogin + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
