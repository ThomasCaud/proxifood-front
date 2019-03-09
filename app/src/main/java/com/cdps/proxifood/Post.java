package com.cdps.proxifood;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Post {
    private long id;
    private User creator;
    private boolean allowHelpCooking;
    private Date date;
    private Date endOfInscription;
    private Date createdAt;
    private String meal;
    private String description;
    private int nbPlaces;
    private float price;
    private Address location;
    private int nbBookings;

    public Post() {
        this.id = -1;
        this.creator = new User();
        this.allowHelpCooking = false;
        this.date = new Date();
        this.endOfInscription = new Date();
        this.createdAt = new Date();
        this.meal = "Meal name";
        this.description = "Meal description";
        this.nbPlaces = 0;
        this.price = 0;
        this.location = new Address();
        this.nbBookings = 0;
    }

    public Post(long id, User creator, boolean allowHelpCooking, Date date, Date endOfInscription, Date createdAt, String meal, String description, int nbPlaces, float price, Address location) {
        this.id = id;
        this.creator = creator;
        this.allowHelpCooking = allowHelpCooking;
        this.date = date;
        this.endOfInscription = endOfInscription;
        this.createdAt = createdAt;
        this.meal = meal;
        this.description = description;
        this.nbPlaces = nbPlaces;
        this.price = price;
        this.location = location;
        this.nbBookings = 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public boolean isAllowHelpCooking() {
        return allowHelpCooking;
    }

    public void setAllowHelpCooking(boolean allowHelpCooking) {
        this.allowHelpCooking = allowHelpCooking;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getEndOfInscription() {
        return endOfInscription;
    }

    public void setEndOfInscription(Date endOfInscription) {
        this.endOfInscription = endOfInscription;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getMeal() {
        return meal;
    }

    public void setMeal(String meal) {
        this.meal = meal;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNbPlaces() {
        return nbPlaces;
    }

    public void setNbPlaces(int nbPlaces) {
        this.nbPlaces = nbPlaces;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public Address getLocation() {
        return location;
    }

    public void setLocation(Address location) {
        this.location = location;
    }

    public int getNbBookings() {
        return nbBookings;
    }

    public void setNbBookings(int nbBookings) {
        this.nbBookings = nbBookings;
    }

    @Override
    public String toString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String mealDate = simpleDateFormat.format(date);
        return meal + '\n' + mealDate + "\t|\t" + getNbBookings() + '/' + nbPlaces;
    }
}
