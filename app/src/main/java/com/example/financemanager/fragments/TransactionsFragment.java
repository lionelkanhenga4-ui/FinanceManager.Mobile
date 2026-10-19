package com.example.financemanager.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financemanager.R;
import com.example.financemanager.TransactionViewModel;
import com.example.financemanager.adapters.TransactionAdapter;
import com.example.financemanager.models.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TransactionsFragment extends Fragment implements TransactionAdapter.OnTransactionClickListener {
    private enum FilterType { ALL, INCOME, EXPENSE }

    private TransactionViewModel viewModel;
    private TransactionAdapter adapter;
    private FilterType currentFilter = FilterType.ALL;

    private TextView chipAll;
    private TextView chipIncome;
    private TextView chipExpense;
    private LinearLayout llEmptyState;
    private RecyclerView rvTransactions;

    private List<Transaction> allTransactions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        chipAll = view.findViewById(R.id.chip_filter_all);
        chipIncome = view.findViewById(R.id.chip_filter_income);
        chipExpense = view.findViewById(R.id.chip_filter_expense);
        llEmptyState = view.findViewById(R.id.ll_empty_state);
        rvTransactions = view.findViewById(R.id.rv_transactions);

        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TransactionAdapter(new ArrayList<>(), this);
        rvTransactions.setAdapter(adapter);

        chipAll.setOnClickListener(v -> setFilter(FilterType.ALL));
        chipIncome.setOnClickListener(v -> setFilter(FilterType.INCOME));
        chipExpense.setOnClickListener(v -> setFilter(FilterType.EXPENSE));

        viewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                allTransactions = transactions;
                applyCurrentFilter();
            }
        });

        viewModel.getDataError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                viewModel.clearDataError();
            }
        });
    }

    private void setFilter(FilterType filterType) {
        currentFilter = filterType;
        updateChipStyles();
        applyCurrentFilter();
    }

    private void updateChipStyles() {
        if (getContext() == null) return;

        chipAll.setBackgroundResource(currentFilter == FilterType.ALL ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipAll.setTextColor(ContextCompat.getColor(getContext(), currentFilter == FilterType.ALL ? R.color.on_primary : R.color.text_secondary));

        chipIncome.setBackgroundResource(currentFilter == FilterType.INCOME ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipIncome.setTextColor(ContextCompat.getColor(getContext(), currentFilter == FilterType.INCOME ? R.color.on_primary : R.color.text_secondary));

        chipExpense.setBackgroundResource(currentFilter == FilterType.EXPENSE ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipExpense.setTextColor(ContextCompat.getColor(getContext(), currentFilter == FilterType.EXPENSE ? R.color.on_primary : R.color.text_secondary));
    }

    private void applyCurrentFilter() {
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction t : allTransactions) {
            if (currentFilter == FilterType.ALL) {
                filtered.add(t);
            } else if (currentFilter == FilterType.INCOME && t.isIncome()) {
                filtered.add(t);
            } else if (currentFilter == FilterType.EXPENSE && t.isExpense()) {
                filtered.add(t);
            }
        }

        if (filtered.isEmpty()) {
            if (llEmptyState != null) llEmptyState.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
        } else {
            if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);
        }

        adapter.setTransactions(filtered);
    }

    @Override
    public void onTransactionClick(Transaction transaction) {
        if ("income".equalsIgnoreCase(transaction.getType())
                || "expense".equalsIgnoreCase(transaction.getType())) {
            EditTransactionDialogFragment.newInstance(transaction)
                    .show(getParentFragmentManager(), "edit_transaction");
        }
    }

    @Override
    public void onTransactionDelete(Transaction transaction) {
        viewModel.deleteTransaction(transaction);
    }
}
