package com.example.financemanager.fragments;

import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransferDialogFragment extends DialogFragment {
    private TransactionViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_FinanceManager);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_transfer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        List<Account> accounts = viewModel.getAccounts().getValue();
        if (accounts == null || accounts.size() < 2) {
            Toast.makeText(requireContext(), "Create at least two accounts before transferring", Toast.LENGTH_LONG).show();
            dismiss();
            return;
        }

        EditText titleInput = view.findViewById(R.id.et_transfer_title);
        EditText amountInput = view.findViewById(R.id.et_transfer_amount);
        Spinner sourceSpinner = view.findViewById(R.id.sp_transfer_source);
        Spinner destinationSpinner = view.findViewById(R.id.sp_transfer_destination);
        Button transferButton = view.findViewById(R.id.btn_transfer);

        List<String> labels = new ArrayList<>();
        for (Account account : accounts) {
            labels.add(String.format(Locale.getDefault(), "%s ($%.2f)", account.getName(), account.getBalance()));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, labels);
        adapter.setDropDownViewResource(R.layout.item_spinner);
        sourceSpinner.setAdapter(adapter);
        destinationSpinner.setAdapter(adapter);
        if (accounts.size() > 1) destinationSpinner.setSelection(1);

        transferButton.setOnClickListener(v -> {
            String amountText = amountInput.getText().toString().trim();
            double amount;
            try {
                amount = Double.parseDouble(amountText);
            } catch (NumberFormatException e) {
                amountInput.setError("Enter a valid amount");
                return;
            }
            if (amount <= 0) {
                amountInput.setError("Amount must be greater than zero");
                return;
            }

            int sourcePosition = sourceSpinner.getSelectedItemPosition();
            int destinationPosition = destinationSpinner.getSelectedItemPosition();
            if (sourcePosition == destinationPosition) {
                Toast.makeText(requireContext(), "Choose two different accounts", Toast.LENGTH_SHORT).show();
                return;
            }

            Account source = accounts.get(sourcePosition);
            if (amount > source.getBalance()) {
                amountInput.setError("Amount exceeds the available balance");
                return;
            }

            String title = titleInput.getText().toString().trim();
            if (title.isEmpty()) title = "Transfer";
            viewModel.transferBetweenAccounts(source.getId(), accounts.get(destinationPosition).getId(), amount, title);
            dismiss();
        });
    }
}
