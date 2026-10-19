package com.example.financemanager.models;

import com.google.gson.annotations.SerializedName;

public class Account {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("balance")
    private double balance;

    @SerializedName("type")
    private String type;

    @SerializedName("user_id")
    private String userId;

    public Account() {
    }

    public Account(String id, String name, double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
