package com.example.financemanager.models;

public enum Category {
    FOOD("Food"),
    TRANSPORT("Transport"),
    HOUSING("Housing"),
    UTILITIES("Utilities"),
    HEALTH("Health"),
    ENTERTAINMENT("Entertainment"),
    EDUCATION("Education"),
    SHOPPING("Shopping"),
    TRAVEL("Travel"),
    SALARY("Salary"),
    INVESTMENT("Investment"),
    OTHER("Other");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
