package com.example.financemanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financemanager.R;
import com.example.financemanager.models.Transaction;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
        void onTransactionDelete(Transaction transaction);
    }

    private final List<Transaction> transactions = new ArrayList<>();
    private final OnTransactionClickListener listener;

    public TransactionAdapter(List<Transaction> initialTransactions, OnTransactionClickListener listener) {
        if (initialTransactions != null) {
            transactions.addAll(initialTransactions);
        }
        this.listener = listener;
    }

    public void setTransactions(List<Transaction> newTransactions) {
        transactions.clear();
        if (newTransactions != null) {
            transactions.addAll(newTransactions);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction, listener);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView amountText;
        private final TextView metaText;
        private final Button deleteButton;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.tv_title);
            amountText = itemView.findViewById(R.id.tv_amount);
            metaText = itemView.findViewById(R.id.tv_meta);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(Transaction transaction, OnTransactionClickListener listener) {
            titleText.setText(transaction.getTitle());
            String amountPrefix = transaction.isTransfer() ? "↔ "
                    : transaction.isGoalContribution() ? "↗ "
                    : transaction.isIncome() || transaction.isOpeningBalance() ? "+" : "-";
            amountText.setText(String.format(Locale.getDefault(), "%s$%.2f", amountPrefix, Math.abs(transaction.getAmount())));
            amountText.setTextColor(transaction.isIncome()
                    ? itemView.getResources().getColor(android.R.color.holo_green_dark)
                    : transaction.isTransfer() || transaction.isOpeningBalance() || transaction.isGoalContribution()
                    ? itemView.getResources().getColor(android.R.color.darker_gray)
                    : itemView.getResources().getColor(android.R.color.holo_red_dark));

            String category = transaction.getCategoryName() == null ? "General" : transaction.getCategoryName();
            String dateText = DateFormat.getDateInstance(DateFormat.MEDIUM)
                    .format(new java.util.Date(transaction.getDate()));
            metaText.setText(category + " • " + dateText);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTransactionClick(transaction);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTransactionDelete(transaction);
                }
            });
        }
    }
}
