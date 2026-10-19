package com.example.financemanager.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.financemanager.R;
import com.example.financemanager.TransactionViewModel;
import com.example.financemanager.models.Account;
import com.example.financemanager.models.Category;
import com.example.financemanager.models.Transaction;

import java.util.ArrayList;
import java.util.List;

public class EditTransactionDialogFragment extends DialogFragment {
    private static final String ARG_ID = "id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_AMOUNT = "amount";
    private static final String ARG_INCOME = "income";
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_ACCOUNT = "account";

    public static EditTransactionDialogFragment newInstance(Transaction transaction) {
        EditTransactionDialogFragment fragment = new EditTransactionDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, transaction.getId());
        args.putString(ARG_TITLE, transaction.getTitle());
        args.putDouble(ARG_AMOUNT, transaction.getAmount());
        args.putBoolean(ARG_INCOME, transaction.isIncome());
        args.putString(ARG_CATEGORY, transaction.getCategoryName());
        args.putString(ARG_ACCOUNT, transaction.getAccountId());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_FinanceManager);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_edit_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = requireArguments();
        TransactionViewModel viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        EditText titleInput = view.findViewById(R.id.et_edit_title);
        EditText amountInput = view.findViewById(R.id.et_edit_amount);
        Spinner typeSpinner = view.findViewById(R.id.sp_edit_type);
        Spinner categorySpinner = view.findViewById(R.id.sp_edit_category);
        Spinner accountSpinner = view.findViewById(R.id.sp_edit_account);
        Button saveButton = view.findViewById(R.id.btn_save_edit);

        titleInput.setText(args.getString(ARG_TITLE, ""));
        titleInput.setSingleLine(true);
        amountInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        amountInput.setText(String.valueOf(args.getDouble(ARG_AMOUNT)));

        String[] types = {"Expense", "Income"};
        typeSpinner.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.item_spinner, types));
        typeSpinner.setSelection(args.getBoolean(ARG_INCOME) ? 1 : 0);

        Category[] categories = Category.values();
        List<String> categoryNames = new ArrayList<>();
        int selectedCategory = 0;
        for (int i = 0; i < categories.length; i++) {
            categoryNames.add(categories[i].name());
            String savedCategory = args.getString(ARG_CATEGORY, "");
            if (categories[i].name().equalsIgnoreCase(savedCategory)) selectedCategory = i;
        }
        categorySpinner.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.item_spinner, categoryNames));
        categorySpinner.setSelection(selectedCategory);

        final List<Account> accounts = viewModel.getAccounts().getValue();
        List<String> accountNames = new ArrayList<>();
        accountNames.add("No account");
        int selectedAccount = 0;
        String savedAccountId = args.getString(ARG_ACCOUNT);
        if (accounts != null) {
            for (int i = 0; i < accounts.size(); i++) {
                accountNames.add(accounts.get(i).getName());
                if (accounts.get(i).getId().equals(savedAccountId)) selectedAccount = i + 1;
            }
        }
        accountSpinner.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.item_spinner, accountNames));
        accountSpinner.setSelection(selectedAccount);

        saveButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            if (title.isEmpty()) {
                titleInput.setError("Enter a title");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountInput.getText().toString().trim());
            } catch (NumberFormatException e) {
                amountInput.setError("Enter a valid amount");
                return;
            }
            if (amount <= 0) {
                amountInput.setError("Amount must be greater than zero");
                return;
            }

            boolean income = typeSpinner.getSelectedItemPosition() == 1;
            String category = categories[categorySpinner.getSelectedItemPosition()].name();
            int accountPosition = accountSpinner.getSelectedItemPosition();
            String accountId = accounts != null && accountPosition > 0 && accountPosition <= accounts.size()
                    ? accounts.get(accountPosition - 1).getId() : null;
            viewModel.updateTransaction(args.getString(ARG_ID), title, amount, income, category, accountId);
            Toast.makeText(requireContext(), "Transaction correction saved", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }
}
