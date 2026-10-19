package com.example.financemanager.networking;

import com.example.financemanager.models.Account;
import com.example.financemanager.models.Budget;
import com.example.financemanager.models.SavingsGoal;
import com.example.financemanager.models.Transaction;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PATCH;
import retrofit2.http.Query;

public interface ApiService {
    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> login(@Body AuthRequest request);

    @POST("auth/v1/signup")
    Call<AuthResponse> signUp(@Body AuthRequest request);

    @GET("rest/v1/accounts")
    Call<List<Account>> getAccounts();

    @POST("rest/v1/rpc/create_account_with_opening_balance")
    Call<Void> createAccount(@Body AccountCreateRequest request);

    @GET("rest/v1/budgets")
    Call<List<Budget>> getBudgets(@Query("user_id") String userFilter);

    @POST("rest/v1/budgets")
    Call<Void> addBudget(@Body Budget budget);

    @GET("rest/v1/savings_goals")
    Call<List<SavingsGoal>> getSavingsGoals(@Query("user_id") String userFilter);

    @POST("rest/v1/savings_goals")
    Call<Void> addSavingsGoal(@Body SavingsGoal goal);

    @POST("rest/v1/rpc/contribute_to_savings_goal")
    Call<Void> contributeToSavingsGoal(@Body GoalContributionRequest request);

    @POST("rest/v1/rpc/transfer_between_accounts")
    Call<Void> transferBetweenAccounts(@Body TransferRequest request);

    @GET("rest/v1/transactions")
    Call<List<Transaction>> getTransactions();

    @POST("rest/v1/transactions")
    Call<Void> addTransaction(@Body Transaction transaction);

    @PATCH("rest/v1/transactions")
    Call<Void> updateTransaction(@Query("id") String idFilter, @Body TransactionUpdateRequest request);

    @DELETE("rest/v1/transactions")
    Call<Void> deleteTransaction(@Query("id") String filter);
}
