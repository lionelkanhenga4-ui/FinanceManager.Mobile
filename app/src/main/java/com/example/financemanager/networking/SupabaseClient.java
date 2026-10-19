package com.example.financemanager.networking;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseClient {
    private static final String BASE_URL = "https://gsljgutbatlhsycpvsza.supabase.co/";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdzbGpndXRiYXRsaHN5Y3B2c3phIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc4Mjg5ODgsImV4cCI6MjEwMzQwNDk4OH0.X6U5PZbMW7LDhmIS_J49NtZ291fmrVA6osbOMXBuX5w";

    private static volatile Retrofit retrofit = null;
    private static volatile ApiService apiService = null;
    private static volatile String currentToken = null;

    public static void setToken(String token) {
        currentToken = token;
    }

    /**
     * Built once and reused. Every OkHttpClient owns its own thread pool and connection
     * pool, so creating a new one per request leaks resources. The interceptor reads the
     * token on each request, so login/logout take effect without rebuilding the client.
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            synchronized (SupabaseClient.class) {
                if (retrofit == null) {
                    OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(new Interceptor() {
                            @Override
                            public Response intercept(Chain chain) throws IOException {
                                String token = currentToken;
                                Request request = chain.request().newBuilder()
                                    .header("apikey", API_KEY)
                                    .header("Authorization", "Bearer " + (token != null ? token : API_KEY))
                                    .build();
                                return chain.proceed(request);
                            }
                        })
                        .build();

                    retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(client)
                        .build();
                }
            }
        }
        return retrofit;
    }

    public static ApiService getApi() {
        if (apiService == null) {
            synchronized (SupabaseClient.class) {
                if (apiService == null) {
                    apiService = getClient().create(ApiService.class);
                }
            }
        }
        return apiService;
    }
}
