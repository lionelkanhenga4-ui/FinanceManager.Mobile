package com.example.financemanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financemanager.R;
import com.example.financemanager.models.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.AccountViewHolder> {
    private final List<Account> accounts = new ArrayList<>();

    public AccountsAdapter(List<Account> initialAccounts) {
        if (initialAccounts != null) {
            accounts.addAll(initialAccounts);
        }
    }

    public void setAccounts(List<Account> newAccounts) {
        accounts.clear();
        if (newAccounts != null) {
            accounts.addAll(newAccounts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = accounts.get(position);
        String accountType = account.getType();
        holder.nameText.setText(accountType == null || accountType.trim().isEmpty()
            ? account.getName() : account.getName() + " · " + accountType);
        holder.balanceText.setText(String.format(Locale.getDefault(), "$%.2f", account.getBalance()));
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    public static class AccountViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView balanceText;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.tv_account_name);
            balanceText = itemView.findViewById(R.id.tv_account_balance);
        }
    }
}
