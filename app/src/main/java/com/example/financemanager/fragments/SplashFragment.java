package com.example.financemanager.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.financemanager.R;
import com.example.financemanager.TransactionViewModel;

public class SplashFragment extends Fragment {
    private static final int SPLASH_DELAY = 1800; // 1.8 seconds

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable navigateRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView ivLogo = view.findViewById(R.id.iv_splash_logo);
        if (ivLogo != null) {
            ivLogo.setScaleX(0.7f);
            ivLogo.setScaleY(0.7f);
            ivLogo.setAlpha(0f);
            ivLogo.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(700)
                    .start();
        }

        TransactionViewModel viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        navigateRunnable = () -> {
            if (!isAdded()) return;
            Boolean isLoggedIn = viewModel.getIsLoggedIn().getValue();
            int destination = (isLoggedIn != null && isLoggedIn)
                    ? R.id.navigation_home
                    : R.id.navigation_login;

            NavOptions options = new NavOptions.Builder()
                    .setPopUpTo(R.id.navigation_splash, true)
                    .build();
            Navigation.findNavController(view).navigate(destination, null, options);
        };
        handler.postDelayed(navigateRunnable, SPLASH_DELAY);
    }

    @Override
    public void onDestroyView() {
        if (navigateRunnable != null) handler.removeCallbacks(navigateRunnable);
        super.onDestroyView();
    }
}
