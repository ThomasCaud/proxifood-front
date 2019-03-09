package com.cdps.proxifood;

public class Address {
    private String zipcode;
    private String city;
    private String country;
    private String street;
    private String number;
    private String formatted;
    private double longitude;
    private double latitude;

    public Address() {
        this.zipcode = "";
        this.city = "";
        this.country = "";
        this.street = "";
        this.number = "";
        this.formatted = "";
    }

    public Address(String zipcode, String city, String country, String street, String number, String formatted, double longitude, double latitude) {
        this.zipcode=zipcode;
        this.city=city;
        this.country = country;
        this.street = street;
        this.number = number;
        this.formatted = formatted;
        this.longitude = longitude;
        this.latitude = latitude;

    }

    public String getZipcode() {
        return zipcode;
    }
    public String getCity() {
        return city;
    }
    public String getCountry() {
        return country;
    }
    public String getStreet() {
        return street;
    }

    public String getNumber() {
        return number;
    }

    public String getFormatted() {
        return formatted;
    }

    public double getLongitude() {
        return longitude;
    }
    public double getLatitude() {
        return latitude;
    }

}
