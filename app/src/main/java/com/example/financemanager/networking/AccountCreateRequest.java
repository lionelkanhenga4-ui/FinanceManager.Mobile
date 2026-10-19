package com.example.financemanager.networking;

import com.google.gson.annotations.SerializedName;

public class AccountCreateRequest {
    @SerializedName("p_name")
    private final String name;

    @SerializedName("p_type")
    private final String type;

    @SerializedName("p_opening_balance")
    private final double openingBalance;

    public AccountCreateRequest(String name, String type, double openingBalance) {
        this.name = name;
        this.type = type;
        this.openingBalance = openingBalance;
    }
}
