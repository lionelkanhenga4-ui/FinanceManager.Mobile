package com.example.financemanager;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.financemanager.fragments.AddTransactionDialogFragment;
import com.example.financemanager.fragments.TransferDialogFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

@SuppressWarnings("unused")
public class MainActivity extends AppCompatActivity {
    private static final int MENU_ADD_INCOME = 1;
    private static final int MENU_ADD_EXPENSE = 2;
    private static final int MENU_ADD_TRANSFER = 3;

    @Override
    @SuppressWarnings("unused")
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_FinanceManager);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        FloatingActionButton fab = findViewById(R.id.fab_add);
        
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            
            NavigationUI.setupWithNavController(navView, navController);

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                @SuppressWarnings("unused") Object unusedController = controller;
                @SuppressWarnings("unused") Object unusedArgs = arguments;
                if (destination.getId() == R.id.navigation_login || 
                    destination.getId() == R.id.navigation_signup ||
                    destination.getId() == R.id.navigation_splash) {
                    navView.setVisibility(View.GONE);
                    fab.setVisibility(View.GONE);
                } else {
                    navView.setVisibility(View.VISIBLE);
                    fab.setVisibility(View.VISIBLE);
                }
            });
        }

        fab.setOnClickListener(v -> {
            @SuppressWarnings("unused") View unusedV = v;
            Context wrapper = new ContextThemeWrapper(this, R.style.Theme_FinanceManager_PopupMenu);
            PopupMenu popup = new PopupMenu(wrapper, fab);

            SpannableString incomeTitle = new SpannableString("Add Income");
            incomeTitle.setSpan(new ForegroundColorSpan(Color.BLACK), 0, incomeTitle.length(), 0);

            SpannableString expenseTitle = new SpannableString("Add Expense");
            expenseTitle.setSpan(new ForegroundColorSpan(Color.BLACK), 0, expenseTitle.length(), 0);
            SpannableString transferTitle = new SpannableString("Transfer Funds");
            transferTitle.setSpan(new ForegroundColorSpan(Color.BLACK), 0, transferTitle.length(), 0);

            popup.getMenu().add(0, MENU_ADD_INCOME, 0, incomeTitle);
            popup.getMenu().add(0, MENU_ADD_EXPENSE, 1, expenseTitle);
            popup.getMenu().add(0, MENU_ADD_TRANSFER, 2, transferTitle);

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == MENU_ADD_TRANSFER) {
                    new TransferDialogFragment().show(getSupportFragmentManager(), "transfer");
                    return true;
                }
                boolean isIncome = item.getItemId() == MENU_ADD_INCOME;
                AddTransactionDialogFragment.newInstance(isIncome)
                        .show(getSupportFragmentManager(), "add_transaction");
                return true;
            });
            popup.show();
        });
    }
}
