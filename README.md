# Personal Finance Manager

Personal Finance Manager is an Android app for recording day-to-day income and expenses, tracking account balances, planning monthly category budgets, and monitoring savings goals. It uses Supabase Auth for accounts and Supabase PostgREST for cloud data storage.

## Features

### Sign-in and account security

- Create an account with an email address and password.
- Sign in with the same email and password.
- Sign out and clear the locally stored session.
- Keep finance data scoped to the authenticated user through Supabase row-level security (RLS).

Email confirmation requirements depend on the Supabase project's Auth settings. The Android app's sign-up flow is email-based.

### Home dashboard

- View the combined balance across financial accounts and transactions without an account.
- See income and expense totals and total savings-goal contributions.
- Review recent transactions and open the full transaction history.

Transactions used to maintain account balances (opening balances, transfers, and savings-goal contributions) are not counted again as ordinary income or expenses in the dashboard totals.

### Accounts

- Create accounts with a name and type: Bank, Cash, Mobile money, or Other.
- Optionally enter an opening balance.
- View accounts and their current balances.
- Add accounts through the database function that creates the account and its opening-balance transaction together.

### Transactions

- Record income or expenses with a title, amount, category, and optional account.
- Browse transaction history and filter it to All, Income, or Expenses.
- Edit or delete ordinary income and expense records.
- Review transfer, opening-balance, and goal-contribution records; these special balance records are managed through their respective account or planning actions.

Categories include Food, Transport, Housing, Utilities, Health, Entertainment, Education, Shopping, Travel, Salary, Investment, and Other.

### Transfers

- Move money between two different accounts.
- Require a positive amount and sufficient funds in the source account.
- Record the transfer and update both account balances through a Supabase database function.

### Monthly budgets

- Create one category spending limit per month.
- Review the selected month's spending against its limit.
- See progress toward the limit and a warning when spending exceeds it.
- Spending is calculated from expense transactions in the same category and month.

### Savings goals

- Create a goal with a name, target amount, and optional target date.
- View the amount saved and progress toward the goal.
- Add contributions from an account.
- Validate contributions against the available account balance and update the goal and account through a Supabase database function.

### Reports

- Review expense totals grouped by category.
- View current-month income, expenses, and net amount.

All amounts are currently displayed in USD.

## Requirements

- Android Studio
- JDK 17
- Android SDK Platform 34
- A Supabase project configured for the app

## Build and install

In Android Studio, open this repository directory as a project, let Gradle sync, and run the `app` configuration on a device or emulator.

To build a debug APK on Windows from this directory:

```powershell
.\gradlew.bat :app:assembleDebug
```

The APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

To install it on a connected device with Android Debug Bridge:

```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Supabase setup

1. Create or select the Supabase project to use with the app.
2. In the Supabase Dashboard, open **SQL Editor** and run the project's Finance Manager schema. The schema file is maintained alongside the web client as `website/supabase-schema.sql`; that website folder is not included in this Android-only repository.
3. Configure the project URL and public anon/publishable key in `app/src/main/java/com/example/financemanager/networking/SupabaseClient.java` (`BASE_URL` and `API_KEY`). Use the same project and public key for any companion client.
4. Configure email confirmation and other authentication options in the Supabase Dashboard as needed.

The app uses Supabase Auth and the database REST API for accounts, transactions, budgets, and savings goals. Account setup, transfers, and goal contributions call database RPC functions provided by the schema. RLS policies should remain enabled so authenticated users can only read and change their own records.

Only use a Supabase public anon/publishable key in a client app. **Never place a `service_role` or other privileged secret key in the Android app.** A public key is not a replacement for RLS: keep the schema's user-scoped policies enabled.

## Project layout

- `app/src/main/java/.../fragments/` — login, sign-up, dashboard, transactions, accounts, plans, and reports screens.
- `app/src/main/java/.../models/` — account, transaction, budget, savings-goal, and category models.
- `app/src/main/java/.../networking/` — Supabase Auth and REST API requests, RPC request models, and session token handling.
- `app/src/main/java/.../TransactionViewModel.java` — shared screen state, loading, and finance operations.
- `app/src/main/res/` — Android layouts, navigation, colors, and launcher/logo resources.

## Data and project notes

- Supabase is the source of truth for signed-in users' finance records.
- The app sends the current user's access token with authenticated data requests.
- Transactions and account balances are synchronized through the configured Supabase schema.
- This Android repository excludes local SDK paths, generated build output, IDE metadata, and signing credentials.
