package com.example.financemanager.networking;

import com.google.gson.annotations.SerializedName;

public class GoalContributionRequest {
    @SerializedName("p_goal_id")
    private final String goalId;

    @SerializedName("p_account_id")
    private final String accountId;

    @SerializedName("p_amount")
    private final double amount;

    public GoalContributionRequest(String goalId, String accountId, double amount) {
        this.goalId = goalId;
        this.accountId = accountId;
        this.amount = amount;
    }
}
