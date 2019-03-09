package com.cdps.proxifood;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Booking {
    private long id;
    private User user;
    private Post post;
    private Date askedAt;
    private int nbPlaces;
    private boolean helpCooking;
    private Status status;

    public Booking() {
        this.id = -1;
        this.user = new User();
        this.post = new Post();
        this.askedAt = new Date();
        this.nbPlaces = 0;
        this.helpCooking = false;
        this.status = Status.PENDING;
    }

    public Booking(long id, User user, Post post, Date askedAt, int nbPlaces, boolean helpCooking, Status status) {
        this.id = id;
        this.user = user;
        this.post = post;
        this.askedAt = askedAt;
        this.nbPlaces = nbPlaces;
        this.helpCooking = helpCooking;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Date getAskedAt() {
        return askedAt;
    }

    public void setAskedAt(Date askedAt) {
        this.askedAt = askedAt;
    }

    public int getNbPlaces() {
        return nbPlaces;
    }

    public void setNbPlaces(int nbPlaces) {
        this.nbPlaces = nbPlaces;
    }

    public boolean isHelpCooking() {
        return helpCooking;
    }

    public void setHelpCooking(boolean helpCooking) {
        this.helpCooking = helpCooking;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleDateFormat.format(post.getDate());
        return post.getCreator().getLogin() + " | " + post.getMeal() + "\n" + date + "\t - \t" + post.getLocation().getCity();
    }
}
