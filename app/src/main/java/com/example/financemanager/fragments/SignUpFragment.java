package com.example.financemanager.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.financemanager.R;
import com.example.financemanager.TransactionViewModel;

public class SignUpFragment extends Fragment {
    private static final int MIN_PASSWORD_LENGTH = 6; // Supabase's default minimum

    private TransactionViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        EditText email = view.findViewById(R.id.email);
        EditText password = view.findViewById(R.id.password);
        EditText confirmPassword = view.findViewById(R.id.confirm_password);
        Button btnSignup = view.findViewById(R.id.btn_signup);

        btnSignup.setOnClickListener(v -> {
            String emailText = email.getText().toString().trim();
            String passwordText = password.getText().toString();

            if (emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(getContext(), "Enter your email and password", Toast.LENGTH_SHORT).show();
            } else if (passwordText.length() < MIN_PASSWORD_LENGTH) {
                Toast.makeText(getContext(), "Password must be at least " + MIN_PASSWORD_LENGTH + " characters", Toast.LENGTH_SHORT).show();
            } else if (!passwordText.equals(confirmPassword.getText().toString())) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.signUp(emailText, passwordText);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
                btnSignup.setEnabled(!Boolean.TRUE.equals(loading)));

        viewModel.getAuthError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                viewModel.clearAuthError(); // otherwise it replays on every re-entry to this screen
            }
        });
    }
}
