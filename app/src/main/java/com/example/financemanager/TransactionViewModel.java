package com.example.financemanager;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.financemanager.models.Account;
import com.example.financemanager.models.Budget;
import com.example.financemanager.models.Category;
import com.example.financemanager.models.SavingsGoal;
import com.example.financemanager.models.Transaction;
import com.example.financemanager.networking.ApiService;
import com.example.financemanager.networking.AccountCreateRequest;
import com.example.financemanager.networking.AuthRequest;
import com.example.financemanager.networking.AuthResponse;
import com.example.financemanager.networking.GoalContributionRequest;
import com.example.financemanager.networking.SupabaseClient;
import com.example.financemanager.networking.TransferRequest;
import com.example.financemanager.networking.TransactionUpdateRequest;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionViewModel extends AndroidViewModel {
    private static final String PREF_NAME = "finance_manager_session";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_USER_ID = "user_id";

    private final MutableLiveData<List<Account>> accounts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Transaction>> transactions = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Budget>> budgets = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<SavingsGoal>> goals = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> authError = new MutableLiveData<>(null);
    private final MutableLiveData<String> dataError = new MutableLiveData<>(null);
    private final MutableLiveData<String> userId = new MutableLiveData<>(null);

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        restoreSession();
    }

    private void restoreSession() {
        SharedPreferences prefs = getApplication().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String savedToken = prefs.getString(KEY_TOKEN, null);
        String savedUserId = prefs.getString(KEY_USER_ID, null);

        if (savedToken != null && !savedToken.isEmpty() && savedUserId != null && !savedUserId.isEmpty()) {
            SupabaseClient.setToken(savedToken);
            userId.setValue(savedUserId);
            isLoggedIn.setValue(true);
            refreshData();
        }
    }

    private void saveSession(String token, String uId) {
        SharedPreferences prefs = getApplication().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_ID, uId)
                .apply();
    }

    private void clearSession() {
        SharedPreferences prefs = getApplication().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    public LiveData<List<Account>> getAccounts() { return accounts; }
    public LiveData<List<Transaction>> getTransactions() { return transactions; }
    public LiveData<List<Budget>> getBudgets() { return budgets; }
    public LiveData<List<SavingsGoal>> getSavingsGoals() { return goals; }
    public LiveData<Boolean> getIsLoggedIn() { return isLoggedIn; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getAuthError() { return authError; }
    public LiveData<String> getDataError() { return dataError; }

    /** LiveData replays its last value to new observers, so consumers clear a message once shown. */
    public void clearAuthError() { authError.setValue(null); }
    public void clearDataError() { dataError.setValue(null); }

    /** Builds a readable message from an HTTP error, preferring the server's own explanation. */
    private static String describeError(String action, Response<?> response) {
        String detail = "";
        try {
            ResponseBody body = response.errorBody();
            if (body != null) {
                String raw = body.string();
                detail = raw;
                try {
                    JsonObject obj = JsonParser.parseString(raw).getAsJsonObject();
                    for (String key : new String[]{"msg", "message", "error_description", "error"}) {
                        if (obj.has(key) && !obj.get(key).isJsonNull()) {
                            detail = obj.get(key).getAsString();
                            break;
                        }
                    }
                } catch (Exception ignored) {
                    // Not a JSON object; keep the raw text.
                }
            }
        } catch (IOException ignored) {
        }
        if (detail.length() > 140) detail = detail.substring(0, 140) + "...";
        return detail.isEmpty()
                ? action + " (HTTP " + response.code() + ")"
                : action + ": " + detail;
    }

    private void reportNetworkError(Throwable t) {
        dataError.setValue("Network error: " + t.getLocalizedMessage());
    }

    public void refreshData() {
        ApiService api = SupabaseClient.getApi();

        api.getAccounts().enqueue(new Callback<List<Account>>() {
            @Override
            public void onResponse(Call<List<Account>> call, Response<List<Account>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) accounts.setValue(response.body());
                } else {
                    dataError.setValue(describeError("Could not load accounts", response));
                }
            }
            @Override
            public void onFailure(Call<List<Account>> call, Throwable t) {
                reportNetworkError(t);
            }
        });

        api.getTransactions().enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        List<Transaction> list = response.body();
                        Collections.sort(list, (a, b) -> Long.compare(b.getDate(), a.getDate()));
                        transactions.setValue(list);
                    }
                } else {
                    dataError.setValue(describeError("Could not load transactions", response));
                }
            }
            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                reportNetworkError(t);
            }
        });

        String userFilter = "eq." + userId.getValue();
        api.getBudgets(userFilter).enqueue(new Callback<List<Budget>>() {
            @Override
            public void onResponse(Call<List<Budget>> call, Response<List<Budget>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    budgets.setValue(response.body());
                } else if (!response.isSuccessful()) {
                    dataError.setValue(describeError("Could not load budgets", response));
                }
            }

            @Override
            public void onFailure(Call<List<Budget>> call, Throwable t) {
                reportNetworkError(t);
            }
        });

        api.getSavingsGoals(userFilter).enqueue(new Callback<List<SavingsGoal>>() {
            @Override
            public void onResponse(Call<List<SavingsGoal>> call, Response<List<SavingsGoal>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    goals.setValue(response.body());
                } else if (!response.isSuccessful()) {
                    dataError.setValue(describeError("Could not load savings goals", response));
                }
            }

            @Override
            public void onFailure(Call<List<SavingsGoal>> call, Throwable t) {
                reportNetworkError(t);
            }
        });
    }

    public void addBudget(String categoryName, double limitAmount, String month) {
        String currentUserId = userId.getValue();
        if (currentUserId == null) return;

        Budget budget = new Budget();
        budget.setName(categoryName);
        budget.setCategoryName(categoryName);
        budget.setLimitAmount(limitAmount);
        budget.setMonth(month);
        budget.setUserId(currentUserId);

        SupabaseClient.getApi().addBudget(budget).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) refreshData();
                else dataError.setValue(describeError("Could not save budget", response));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                reportNetworkError(t);
            }
        });
    }

    public void createAccount(String name, String type, double openingBalance) {
        SupabaseClient.getApi().createAccount(
                new AccountCreateRequest(name, type, openingBalance))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) refreshData();
                        else dataError.setValue(describeError("Could not create account", response));
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        reportNetworkError(t);
                    }
                });
    }

    public void addSavingsGoal(String name, double targetAmount, long deadline) {
        String currentUserId = userId.getValue();
        if (currentUserId == null) return;

        SavingsGoal goal = new SavingsGoal();
        goal.setName(name);
        goal.setTargetAmount(targetAmount);
        goal.setCurrentAmount(0);
        goal.setDeadline(deadline);
        goal.setUserId(currentUserId);

        SupabaseClient.getApi().addSavingsGoal(goal).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) refreshData();
                else dataError.setValue(describeError("Could not save savings goal", response));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                reportNetworkError(t);
            }
        });
    }

    public void contributeToSavingsGoal(SavingsGoal goal, String accountId, double amount) {
        SupabaseClient.getApi().contributeToSavingsGoal(
                new GoalContributionRequest(goal.getId(), accountId, amount))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) refreshData();
                        else dataError.setValue(describeError("Could not update savings goal", response));
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        reportNetworkError(t);
                    }
                });
    }

    public void transferBetweenAccounts(String sourceId, String destinationId, double amount, String title) {
        SupabaseClient.getApi().transferBetweenAccounts(
                new TransferRequest(sourceId, destinationId, amount, title))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) refreshData();
                        else dataError.setValue(describeError("Could not transfer funds", response));
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        reportNetworkError(t);
                    }
                });
    }

    public void login(String email, String pass) {
        if (Boolean.TRUE.equals(isLoading.getValue())) return; // ignore double taps
        isLoading.setValue(true);
        authError.setValue(null);
        SupabaseClient.getApi().login(new AuthRequest(email, pass)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                isLoading.setValue(false);
                AuthResponse body = response.body();
                if (response.isSuccessful() && body != null
                        && body.getAccessToken() != null && body.getUser() != null) {
                    String token = body.getAccessToken();
                    String uId = body.getUser().getId();
                    SupabaseClient.setToken(token);
                    userId.setValue(uId);
                    isLoggedIn.setValue(true);
                    saveSession(token, uId);
                    refreshData();
                } else if (!response.isSuccessful()) {
                    authError.setValue(describeError("Login failed", response));
                } else {
                    authError.setValue("Login failed: unexpected response from server");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                isLoading.setValue(false);
                authError.setValue(t.getLocalizedMessage());
            }
        });
    }

    public void signUp(String email, String pass) {
        if (Boolean.TRUE.equals(isLoading.getValue())) return; // ignore double taps
        isLoading.setValue(true);
        authError.setValue(null);
        SupabaseClient.getApi().signUp(new AuthRequest(email, pass)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    authError.setValue("Success! Please confirm your email.");
                } else {
                    authError.setValue(describeError("Sign up failed", response));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                isLoading.setValue(false);
                authError.setValue(t.getLocalizedMessage());
            }
        });
    }

    public void logout() {
        clearSession();
        SupabaseClient.setToken(null);
        userId.setValue(null);
        isLoggedIn.setValue(false);
        accounts.setValue(new ArrayList<>());
        transactions.setValue(new ArrayList<>());
    }

    public void addTransaction(String title, double amount, boolean isIncome, Category category, String accountId) {
        String currentUserId = userId.getValue();
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            dataError.setValue("User is not logged in. Please log in first.");
            return;
        }

        Transaction t = new Transaction();
        t.setTitle(title);
        t.setAmount(amount);
        t.setIncome(isIncome);
        t.setType(isIncome ? "income" : "expense");
        t.setCategoryName(category != null ? category.name() : Category.OTHER.name());
        t.setAccountId(accountId);
        t.setUserId(currentUserId);

        SupabaseClient.getApi().addTransaction(t).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    refreshData();
                } else {
                    dataError.setValue(describeError("Could not add transaction", response));
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                reportNetworkError(t);
            }
        });
    }

    public void deleteTransaction(Transaction transaction) {
        // PostgREST filters use the form column=operator.value, so a bare id is rejected.
        SupabaseClient.getApi().deleteTransaction("eq." + transaction.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    refreshData();
                } else {
                    dataError.setValue(describeError("Could not delete transaction", response));
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                reportNetworkError(t);
            }
        });
    }

    public void updateTransaction(String id, String title, double amount, boolean income,
                                  String categoryName, String accountId) {
        if (id == null || title == null || title.trim().isEmpty() || amount <= 0) {
            dataError.setValue("Enter a title and an amount greater than zero.");
            return;
        }

        TransactionUpdateRequest request = new TransactionUpdateRequest(
                title.trim(), amount, income, categoryName, accountId);
        SupabaseClient.getApi().updateTransaction("eq." + id, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) refreshData();
                else dataError.setValue(describeError("Could not correct transaction", response));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                reportNetworkError(t);
            }
        });
    }

    public double getBalance() {
        double balance = 0;
        if (accounts.getValue() != null && !accounts.getValue().isEmpty()) {
            for (Account account : accounts.getValue()) balance += account.getBalance();
            if (transactions.getValue() != null) {
                for (Transaction transaction : transactions.getValue()) {
                    if (transaction.getAccountId() != null || transaction.isTransfer() || transaction.isOpeningBalance()) continue;
                    balance += transaction.isIncome() ? transaction.getAmount() : -transaction.getAmount();
                }
            }
        } else if (transactions.getValue() != null) {
            for (Transaction t : transactions.getValue()) {
                if (t.isTransfer()) continue;
                if (t.isOpeningBalance()) {
                    balance += t.getAmount();
                } else if (t.isIncome()) {
                    balance += t.getAmount();
                } else {
                    balance -= t.getAmount();
                }
            }
        }
        return balance + getSavingsTotal();
    }

    public double getSavingsTotal() {
        double total = 0;
        if (goals.getValue() != null) {
            for (SavingsGoal goal : goals.getValue()) total += goal.getCurrentAmount();
        }
        return total;
    }
}
