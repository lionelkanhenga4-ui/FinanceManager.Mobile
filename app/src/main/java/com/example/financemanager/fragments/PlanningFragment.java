package com.example.financemanager.fragments;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.financemanager.R;
import com.example.financemanager.TransactionViewModel;
import com.example.financemanager.models.Account;
import com.example.financemanager.models.Budget;
import com.example.financemanager.models.Category;
import com.example.financemanager.models.SavingsGoal;
import com.example.financemanager.models.Transaction;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlanningFragment extends Fragment {
    private TransactionViewModel viewModel;
    private LinearLayout budgetsContainer;
    private LinearLayout goalsContainer;
    private List<Transaction> transactions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_planning, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        budgetsContainer = view.findViewById(R.id.ll_budgets);
        goalsContainer = view.findViewById(R.id.ll_goals);

        view.findViewById(R.id.btn_add_budget).setOnClickListener(v -> showAddBudgetDialog());
        view.findViewById(R.id.btn_add_goal).setOnClickListener(v -> showAddGoalDialog());

        viewModel.getTransactions().observe(getViewLifecycleOwner(), items -> {
            transactions = items == null ? new ArrayList<>() : items;
            renderBudgets(viewModel.getBudgets().getValue());
        });
        viewModel.getBudgets().observe(getViewLifecycleOwner(), this::renderBudgets);
        viewModel.getSavingsGoals().observe(getViewLifecycleOwner(), this::renderGoals);
        viewModel.getDataError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                viewModel.clearDataError();
            }
        });
    }

    private void showAddBudgetDialog() {
        LinearLayout form = createForm();
        Spinner categorySpinner = new Spinner(requireContext());
        Category[] categories = Category.values();
        List<String> names = new ArrayList<>();
        for (Category category : categories) names.add(category.name());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, names);
        adapter.setDropDownViewResource(R.layout.item_spinner);
        categorySpinner.setAdapter(adapter);
        form.addView(categorySpinner);

        EditText amountInput = createNumberInput("Monthly limit");
        form.addView(amountInput);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add monthly budget")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    Double amount = parsePositiveAmount(amountInput);
                    if (amount != null) {
                        viewModel.addBudget(categories[categorySpinner.getSelectedItemPosition()].name(), amount, currentMonth());
                    }
                })
                .show();
    }

    private void showAddGoalDialog() {
        LinearLayout form = createForm();
        EditText nameInput = new EditText(requireContext());
        nameInput.setHint("Goal name");
        nameInput.setSingleLine(true);
        form.addView(nameInput);

        EditText targetInput = createNumberInput("Target amount");
        form.addView(targetInput);

        TextView deadlineLabel = new TextView(requireContext());
        deadlineLabel.setText("No deadline set");
        deadlineLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
        deadlineLabel.setPadding(0, 16, 0, 8);
        form.addView(deadlineLabel);
        long[] deadline = {0};
        deadlineLabel.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
                calendar.set(year, month, day, 0, 0, 0);
                deadline[0] = calendar.getTimeInMillis();
                deadlineLabel.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add savings goal")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    Double target = parsePositiveAmount(targetInput);
                    if (name.isEmpty()) {
                        nameInput.setError("Enter a goal name");
                    } else if (target != null) {
                        viewModel.addSavingsGoal(name, target, deadline[0]);
                    }
                })
                .show();
    }

    private void showContributionDialog(SavingsGoal goal) {
        List<Account> accounts = viewModel.getAccounts().getValue();
        if (accounts == null || accounts.isEmpty()) {
            Toast.makeText(requireContext(), "Create an account before adding a contribution", Toast.LENGTH_LONG).show();
            return;
        }

        LinearLayout form = createForm();
        EditText amountInput = createNumberInput("Contribution amount");
        form.addView(amountInput);
        Spinner accountSpinner = new Spinner(requireContext());
        List<String> labels = new ArrayList<>();
        for (Account account : accounts) {
            labels.add(String.format(Locale.getDefault(), "%s ($%.2f)", account.getName(), account.getBalance()));
        }
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, labels);
        accountAdapter.setDropDownViewResource(R.layout.item_spinner);
        accountSpinner.setAdapter(accountAdapter);
        form.addView(accountSpinner);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add to " + goal.getName())
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add", (dialog, which) -> {
                    Double amount = parsePositiveAmount(amountInput);
                    if (amount != null) {
                        Account account = accounts.get(accountSpinner.getSelectedItemPosition());
                        if (amount > account.getBalance()) {
                            amountInput.setError("Amount exceeds the available balance");
                            return;
                        }
                        viewModel.contributeToSavingsGoal(goal, account.getId(), amount);
                    }
                })
                .show();
    }

    private void renderBudgets(List<Budget> budgets) {
        if (budgetsContainer == null) return;
        budgetsContainer.removeAllViews();
        String month = currentMonth();
        boolean found = false;

        if (budgets != null) {
            for (Budget budget : budgets) {
                if (budget.getMonth() != null && !month.equals(budget.getMonth())) continue;
                found = true;
                double spent = 0;
                for (Transaction transaction : transactions) {
                    if (transaction.isIncome() || !sameCategory(budget.getCategoryName(), transaction.getCategoryName())) continue;
                    Date date = new Date(transaction.getDate());
                    if (month.equals(new SimpleDateFormat("yyyy-MM", Locale.US).format(date))) {
                        spent += Math.abs(transaction.getAmount());
                    }
                }
                budgetsContainer.addView(createBudgetCard(budget, spent));
            }
        }

        if (!found) addEmptyMessage(budgetsContainer, "No budget this month. Add a category limit to start tracking.");
    }

    private View createBudgetCard(Budget budget, double spent) {
        MaterialCardView card = createCard();
        LinearLayout content = createForm();
        TextView title = createText(budget.getCategoryName() == null ? budget.getName() : budget.getCategoryName(), 16, true);
        content.addView(title);

        double limit = budget.getLimitAmount();
        int percent = limit > 0 ? (int) Math.min(100, Math.round(spent / limit * 100)) : 0;
        TextView totals = createText(String.format(Locale.getDefault(), "$%.2f of $%.2f used", spent, limit), 14, false);
        content.addView(totals);
        ProgressBar progress = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgress(percent);
        if (spent > limit) {
            progress.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.expense_red)));
            totals.setTextColor(ContextCompat.getColor(requireContext(), R.color.expense_red));
        }
        content.addView(progress);
        card.addView(content);
        return card;
    }

    private void renderGoals(List<SavingsGoal> goals) {
        if (goalsContainer == null) return;
        goalsContainer.removeAllViews();
        if (goals == null || goals.isEmpty()) {
            addEmptyMessage(goalsContainer, "No savings goals yet. Add a target and track your progress.");
            return;
        }

        for (SavingsGoal goal : goals) goalsContainer.addView(createGoalCard(goal));
    }

    private View createGoalCard(SavingsGoal goal) {
        MaterialCardView card = createCard();
        LinearLayout content = createForm();
        content.addView(createText(goal.getName(), 16, true));
        double target = goal.getTargetAmount();
        double saved = goal.getCurrentAmount();
        int percent = target > 0 ? (int) Math.min(100, Math.round(saved / target * 100)) : 0;
        content.addView(createText(String.format(Locale.getDefault(), "$%.2f saved of $%.2f (%d%%)", saved, target, percent), 14, false));

        ProgressBar progress = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgress(percent);
        content.addView(progress);
        if (goal.getDeadline() > 0) {
            content.addView(createText("Target date: " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(goal.getDeadline())), 13, false));
        }

        MaterialButton contribute = new MaterialButton(requireContext());
        contribute.setText("Add contribution");
        contribute.setOnClickListener(v -> showContributionDialog(goal));
        content.addView(contribute);
        card.addView(content);
        return card;
    }

    private void addEmptyMessage(LinearLayout container, String message) {
        TextView empty = createText(message, 14, false);
        empty.setPadding(4, 12, 4, 8);
        container.addView(empty);
    }

    private MaterialCardView createCard() {
        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(10);
        card.setLayoutParams(params);
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface));
        card.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.card_stroke));
        card.setStrokeWidth(dp(1));
        card.setRadius(dp(12));
        return card;
    }

    private LinearLayout createForm() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(16), dp(12), dp(16), dp(12));
        return layout;
    }

    private EditText createNumberInput(String hint) {
        EditText input = new EditText(requireContext());
        input.setHint(hint);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        return input;
    }

    private Double parsePositiveAmount(EditText input) {
        try {
            double amount = Double.parseDouble(input.getText().toString().trim());
            if (amount > 0) return amount;
        } catch (NumberFormatException ignored) {
        }
        input.setError("Enter an amount greater than zero");
        return null;
    }

    private TextView createText(String text, int size, boolean bold) {
        TextView view = new TextView(requireContext());
        view.setText(text == null ? "" : text);
        view.setTextSize(size);
        view.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        if (bold) view.setTypeface(view.getTypeface(), android.graphics.Typeface.BOLD);
        return view;
    }

    private boolean sameCategory(String first, String second) {
        return first != null && second != null && first.trim().equalsIgnoreCase(second.trim());
    }

    private String currentMonth() {
        return new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
