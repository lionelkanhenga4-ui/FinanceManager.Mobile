package com.example.financemanager.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.financemanager.R;
import com.example.financemanager.TransactionViewModel;

public class LoginFragment extends Fragment {
    private TransactionViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        EditText email = view.findViewById(R.id.email);
        EditText password = view.findViewById(R.id.password);
        Button btnLogin = view.findViewById(R.id.btn_login);
        TextView tvSignup = view.findViewById(R.id.tv_signup);

        btnLogin.setOnClickListener(v -> {
            // Keyboards often append a trailing space to emails, which makes login fail.
            String emailText = email.getText().toString().trim();
            String passwordText = password.getText().toString();
            if (emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(getContext(), "Enter your email and password", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.login(emailText, passwordText);
        });

        tvSignup.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.navigation_signup);
        });

        viewModel.getIsLoggedIn().observe(getViewLifecycleOwner(), loggedIn -> {
            if (Boolean.TRUE.equals(loggedIn)) {
                // Pop login off the back stack. Otherwise Back from Home returns here, this
                // observer fires again, and the user is bounced straight back to Home.
                NavOptions options = new NavOptions.Builder()
                        .setPopUpTo(R.id.navigation_login, true)
                        .build();
                Navigation.findNavController(view).navigate(R.id.navigation_home, null, options);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
                btnLogin.setEnabled(!Boolean.TRUE.equals(loading)));

        viewModel.getAuthError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                viewModel.clearAuthError(); // otherwise it replays on every re-entry to this screen
            }
        });
    }
}
