package com.example.financemanager.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financemanager.R;
import com.example.financemanager.TransactionViewModel;
import com.example.financemanager.adapters.AccountsAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Locale;

public class AccountsFragment extends Fragment {
    private TransactionViewModel viewModel;
    private AccountsAdapter adapter;
    private TextView tvTotalBalance;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accounts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        tvTotalBalance = view.findViewById(R.id.tv_accounts_total_balance);
        RecyclerView rv = view.findViewById(R.id.rv_accounts);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AccountsAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        view.findViewById(R.id.btn_add_account).setOnClickListener(v -> showAddAccountDialog());
        view.findViewById(R.id.btn_transfer).setOnClickListener(v -> {
            DialogFragment dialog = new TransferDialogFragment();
            dialog.show(getParentFragmentManager(), "transfer");
        });

        viewModel.getAccounts().observe(getViewLifecycleOwner(), accounts -> {
            if (accounts != null) {
                adapter.setAccounts(accounts);
            }
            tvTotalBalance.setText(String.format(Locale.getDefault(), "$%.2f", viewModel.getBalance()));
        });
    }

    private void showAddAccountDialog() {
        LinearLayout form = new LinearLayout(requireContext());
        form.setOrientation(LinearLayout.VERTICAL);
        int padding = Math.round(20 * getResources().getDisplayMetrics().density);
        form.setPadding(padding, padding / 2, padding, 0);

        EditText nameInput = new EditText(requireContext());
        nameInput.setSingleLine(true);
        nameInput.setHint("Account name");
        form.addView(nameInput);

        Spinner typeSpinner = new Spinner(requireContext());
        String[] types = {"Bank", "Cash", "Mobile money", "Other"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, types);
        typeAdapter.setDropDownViewResource(R.layout.item_spinner);
        typeSpinner.setAdapter(typeAdapter);
        form.addView(typeSpinner);

        EditText balanceInput = new EditText(requireContext());
        balanceInput.setHint("Opening balance (optional)");
        balanceInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        form.addView(balanceInput);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add account")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    if (name.isEmpty()) {
                        nameInput.setError("Enter an account name");
                        return;
                    }

                    double balance = 0;
                    String balanceText = balanceInput.getText().toString().trim();
                    if (!balanceText.isEmpty()) {
                        try {
                            balance = Double.parseDouble(balanceText);
                        } catch (NumberFormatException e) {
                            balanceInput.setError("Enter a valid balance");
                            return;
                        }
                        if (balance < 0) {
                            balanceInput.setError("Opening balance cannot be negative");
                            return;
                        }
                    }
                    viewModel.createAccount(name, types[typeSpinner.getSelectedItemPosition()], balance);
                })
                .show();
    }
}
