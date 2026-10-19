package com.example.financemanager.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.financemanager.R;
import com.example.financemanager.TransactionViewModel;
import com.example.financemanager.models.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;

public class AnalyticsFragment extends Fragment {
    private TransactionViewModel viewModel;
    private LinearLayout container;
    private TextView emptyView;
    private TextView monthIncome;
    private TextView monthExpense;
    private TextView monthNet;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analytics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        container = view.findViewById(R.id.ll_category_breakdown_container);
        emptyView = view.findViewById(R.id.tv_analytics_empty);
        monthIncome = view.findViewById(R.id.tv_month_income);
        monthExpense = view.findViewById(R.id.tv_month_expense);
        monthNet = view.findViewById(R.id.tv_month_net);

        viewModel.getTransactions().observe(getViewLifecycleOwner(), this::updateAnalytics);
    }

    private void updateAnalytics(List<Transaction> transactions) {
        if (container == null) return;
        updateMonthlySummary(transactions);
        container.removeAllViews();

        if (transactions == null || transactions.isEmpty()) {
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            return;
        }

        Map<String, Double> categoryTotals = new HashMap<>();
        double totalExpense = 0;

        for (Transaction t : transactions) {
            if (t.isExpense()) {
                String cat = t.getCategoryName() != null ? t.getCategoryName() : "General";
                double amt = Math.abs(t.getAmount());
                categoryTotals.put(cat, categoryTotals.getOrDefault(cat, 0.0) + amt);
                totalExpense += amt;
            }
        }

        if (categoryTotals.isEmpty()) {
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            return;
        }

        if (emptyView != null) emptyView.setVisibility(View.GONE);

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            String category = entry.getKey();
            double amount = entry.getValue();
            int percentage = totalExpense > 0 ? (int) ((amount / totalExpense) * 100) : 0;

            View row = LayoutInflater.from(getContext()).inflate(R.layout.item_category_analytic, container, false);
            TextView tvName = row.findViewById(R.id.tv_cat_name);
            TextView tvAmount = row.findViewById(R.id.tv_cat_amount);
            ProgressBar progressBar = row.findViewById(R.id.progress_cat);

            tvName.setText(category);
            tvAmount.setText(String.format(Locale.getDefault(), "$%.2f (%d%%)", amount, percentage));
            progressBar.setProgress(percentage);

            container.addView(row);
        }
    }

    private void updateMonthlySummary(List<Transaction> transactions) {
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());
        double income = 0;
        double expense = 0;

        if (transactions != null) {
            for (Transaction transaction : transactions) {
                String transactionMonth = new SimpleDateFormat("yyyy-MM", Locale.US)
                        .format(new Date(transaction.getDate()));
                if (!currentMonth.equals(transactionMonth)) continue;
                if (transaction.isIncome()) income += transaction.getAmount();
                else if (transaction.isExpense()) expense += transaction.getAmount();
            }
        }

        if (monthIncome != null) monthIncome.setText(String.format(Locale.getDefault(), "$%.2f", income));
        if (monthExpense != null) monthExpense.setText(String.format(Locale.getDefault(), "$%.2f", expense));
        if (monthNet != null) monthNet.setText(String.format(Locale.getDefault(), "$%.2f", income - expense));
    }
}
