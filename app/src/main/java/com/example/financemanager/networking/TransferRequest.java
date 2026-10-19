package com.example.financemanager.networking;

import com.google.gson.annotations.SerializedName;

public class TransferRequest {
    @SerializedName("p_source_account_id")
    private final String sourceAccountId;

    @SerializedName("p_destination_account_id")
    private final String destinationAccountId;

    @SerializedName("p_amount")
    private final double amount;

    @SerializedName("p_title")
    private final String title;

    public TransferRequest(String sourceAccountId, String destinationAccountId, double amount, String title) {
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.title = title;
    }
}
