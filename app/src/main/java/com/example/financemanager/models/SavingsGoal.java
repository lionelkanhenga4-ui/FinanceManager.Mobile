package com.example.financemanager.models;

import com.google.gson.annotations.SerializedName;

public class SavingsGoal {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("target_amount")
    private double targetAmount;

    @SerializedName("current_amount")
    private double currentAmount;

    @SerializedName("deadline")
    private long deadline;

    @SerializedName("user_id")
    private String userId;

    public SavingsGoal() {
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

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
