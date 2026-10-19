package com.example.financemanager.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.financemanager.R;
import com.example.financemanager.TransactionViewModel;
import com.example.financemanager.models.Account;
import com.example.financemanager.models.Category;

import java.util.ArrayList;
import java.util.List;

public class AddTransactionDialogFragment extends DialogFragment {
    private TransactionViewModel viewModel;
    private boolean isIncome;

    private TextView dialogTitle;
    private TextView btnTypeExpense;
    private TextView btnTypeIncome;

    public static AddTransactionDialogFragment newInstance(boolean isIncome) {
        AddTransactionDialogFragment fragment = new AddTransactionDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean("isIncome", isIncome);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isIncome = getArguments().getBoolean("isIncome");
        }
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_FinanceManager);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        dialogTitle = view.findViewById(R.id.tv_dialog_title);
        btnTypeExpense = view.findViewById(R.id.btn_type_expense);
        btnTypeIncome = view.findViewById(R.id.btn_type_income);

        EditText etTitle = view.findViewById(R.id.et_title);
        EditText etAmount = view.findViewById(R.id.et_amount);
        Spinner spAccount = view.findViewById(R.id.sp_account);
        Spinner spCategory = view.findViewById(R.id.sp_category);
        Button btnAdd = view.findViewById(R.id.btn_add);

        updateTypeToggle();

        btnTypeExpense.setOnClickListener(v -> {
            isIncome = false;
            updateTypeToggle();
        });

        btnTypeIncome.setOnClickListener(v -> {
            isIncome = true;
            updateTypeToggle();
        });

        List<Account> accounts = viewModel.getAccounts().getValue();
        List<String> accountNames = new ArrayList<>();
        if (accounts == null || accounts.isEmpty()) {
            accountNames.add("No account");
        } else {
            for (Account account : accounts) accountNames.add(account.getName());
        }
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, accountNames);
        accountAdapter.setDropDownViewResource(R.layout.item_spinner);
        spAccount.setAdapter(accountAdapter);

        Category[] categories = Category.values();
        List<String> categoryNames = new ArrayList<>();
        for (Category c : categories) categoryNames.add(c.getDisplayName());
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, categoryNames);
        categoryAdapter.setDropDownViewResource(R.layout.item_spinner);
        spCategory.setAdapter(categoryAdapter);

        btnAdd.setOnClickListener(v -> {
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";

            if (title.isEmpty()) {
                etTitle.setError("Title is required");
                return;
            }

            if (amountStr.isEmpty()) {
                etAmount.setError("Amount is required");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                etAmount.setError("Enter a valid amount");
                return;
            }
            if (amount <= 0) {
                etAmount.setError("Amount must be greater than 0");
                return;
            }

            Category category = categories[spCategory.getSelectedItemPosition()];

                String accountId = accounts != null && spAccount.getSelectedItemPosition() >= 0
                    && spAccount.getSelectedItemPosition() < accounts.size()
                    ? accounts.get(spAccount.getSelectedItemPosition()).getId()
                    : null;
                viewModel.addTransaction(title, amount, isIncome, category, accountId);
            dismiss();
        });
    }

    private void updateTypeToggle() {
        if (getContext() == null) return;

        if (dialogTitle != null) {
            dialogTitle.setText(isIncome ? R.string.add_income : R.string.add_expense);
        }

        if (isIncome) {
            btnTypeIncome.setBackgroundResource(R.drawable.bg_chip_selected);
            btnTypeIncome.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.income_green));
            btnTypeIncome.setTextColor(ContextCompat.getColor(getContext(), R.color.on_primary));

            btnTypeExpense.setBackgroundResource(R.drawable.bg_chip_unselected);
            btnTypeExpense.setBackgroundTintList(null);
            btnTypeExpense.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
        } else {
            btnTypeExpense.setBackgroundResource(R.drawable.bg_chip_selected);
            btnTypeExpense.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.expense_red));
            btnTypeExpense.setTextColor(ContextCompat.getColor(getContext(), R.color.on_primary));

            btnTypeIncome.setBackgroundResource(R.drawable.bg_chip_unselected);
            btnTypeIncome.setBackgroundTintList(null);
            btnTypeIncome.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
        }
    }
}
