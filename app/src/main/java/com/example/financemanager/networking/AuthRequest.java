package com.example.financemanager.networking;

import com.google.gson.annotations.SerializedName;

public class AuthRequest {
    @SerializedName("email")
    private final String email;

    @SerializedName("password")
    private final String password;

    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
