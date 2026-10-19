package com.example.financemanager.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financemanager.R;
import com.example.financemanager.TransactionViewModel;
import com.example.financemanager.adapters.TransactionAdapter;
import com.example.financemanager.models.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements TransactionAdapter.OnTransactionClickListener {
    private TransactionViewModel viewModel;
    private TransactionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        TextView tvBalance = view.findViewById(R.id.tv_balance);
        TextView tvTotalIncome = view.findViewById(R.id.tv_total_income);
        TextView tvTotalExpense = view.findViewById(R.id.tv_total_expense);
        TextView tvTotalSavings = view.findViewById(R.id.tv_total_savings);
        TextView btnSeeAll = view.findViewById(R.id.btn_see_all);
        ImageView btnLogout = view.findViewById(R.id.btn_logout);
        LinearLayout llEmptyState = view.findViewById(R.id.ll_empty_state);

        RecyclerView rv = view.findViewById(R.id.rv_recent_transactions);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TransactionAdapter(new ArrayList<>(), this);
        rv.setAdapter(adapter);

        if (btnSeeAll != null) {
            btnSeeAll.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.navigation_transactions));
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                viewModel.logout();
                Navigation.findNavController(view).navigate(R.id.navigation_login);
            });
        }

        viewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
            double totalIncome = 0;
            double totalExpense = 0;

            if (transactions != null && !transactions.isEmpty()) {
                for (Transaction t : transactions) {
                    if (t.isTransfer() || t.isOpeningBalance() || t.isGoalContribution()) continue;
                    if (t.isIncome()) {
                        totalIncome += t.getAmount();
                    } else {
                        totalExpense += t.getAmount();
                    }
                }

                if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);

                List<Transaction> recent = transactions.size() > 5 ? transactions.subList(0, 5) : transactions;
                adapter.setTransactions(recent);
            } else {
                if (llEmptyState != null) llEmptyState.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
                adapter.setTransactions(new ArrayList<>());
            }

            if (tvTotalIncome != null) {
                tvTotalIncome.setText(String.format(Locale.getDefault(), "+$%.2f", totalIncome));
            }
            if (tvTotalExpense != null) {
                tvTotalExpense.setText(String.format(Locale.getDefault(), "-$%.2f", totalExpense));
            }
            if (tvTotalSavings != null) {
                tvTotalSavings.setText(String.format(Locale.getDefault(), "$%.2f", viewModel.getSavingsTotal()));
            }
            tvBalance.setText(String.format(Locale.getDefault(), "$%.2f", viewModel.getBalance()));
        });

        viewModel.getAccounts().observe(getViewLifecycleOwner(), accounts -> {
            tvBalance.setText(String.format(Locale.getDefault(), "$%.2f", viewModel.getBalance()));
        });

        viewModel.getDataError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                viewModel.clearDataError();
            }
        });
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
