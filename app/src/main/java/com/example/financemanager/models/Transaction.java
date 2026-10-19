package com.example.financemanager.models;

import com.google.gson.annotations.SerializedName;

public class Transaction {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("amount")
    private double amount;

    @SerializedName("type")
    private String type;

    @SerializedName("income")
    private boolean income;

    @SerializedName("date")
    private long date;

    @SerializedName("category_name")
    private String categoryName;

    @SerializedName("account_id")
    private String accountId;

    @SerializedName("destination_account_id")
    private String destinationAccountId;

    @SerializedName("user_id")
    private String userId;

    public Transaction() {
        this.date = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type != null ? type : (income ? "income" : "expense");
    }

    public void setType(String type) {
        this.type = type;
        this.income = "income".equalsIgnoreCase(type);
    }

    public boolean isIncome() {
        return type != null ? "income".equalsIgnoreCase(type) : income;
    }

    public void setIncome(boolean income) {
        this.income = income;
        this.type = income ? "income" : "expense";
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(String destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }

    public boolean isTransfer() {
        return "transfer".equalsIgnoreCase(getType());
    }

    public boolean isOpeningBalance() {
        return "opening".equalsIgnoreCase(getType());
    }

    public boolean isExpense() {
        return "expense".equalsIgnoreCase(getType());
    }

    public boolean isGoalContribution() {
        return "goal_contribution".equalsIgnoreCase(getType());
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
