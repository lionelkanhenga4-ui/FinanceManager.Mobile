package com.example.financemanager.networking;

import com.google.gson.annotations.SerializedName;

public class TransactionUpdateRequest {
    @SerializedName("title")
    private final String title;

    @SerializedName("amount")
    private final double amount;

    @SerializedName("type")
    private final String type;

    @SerializedName("income")
    private final boolean income;

    @SerializedName("category_name")
    private final String categoryName;

    @SerializedName("account_id")
    private final String accountId;

    public TransactionUpdateRequest(String title, double amount, boolean income, String categoryName, String accountId) {
        this.title = title;
        this.amount = amount;
        this.type = income ? "income" : "expense";
        this.income = income;
        this.categoryName = categoryName;
        this.accountId = accountId;
    }
}
