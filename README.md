# Finance Manager

Android application for managing accounts, transactions, monthly budgets, and savings goals with Supabase.

## Requirements

- Android Studio with Android SDK 34
- JDK 17

## Build

On Windows, from this directory run:

```powershell
.\gradlew.bat :app:assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Supabase

The app connects to the Supabase project configured in `app/src/main/java/com/example/financemanager/networking/SupabaseClient.java`. The bundled client key is a public anon key, not a service-role key. Apply the matching Supabase database schema in the Supabase SQL Editor before using the app.

Local SDK paths, build outputs, IDE metadata, signing keys, and local secrets are excluded from this repository.
