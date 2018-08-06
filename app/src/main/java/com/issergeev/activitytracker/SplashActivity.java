package com.issergeev.activitytracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private final String PREFERENCES_NAME = "Settings";
    private final String FIRST_START = "first_start";

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

        if (!settings.getBoolean(FIRST_START, true)) {
            startActivity(new Intent(this, ListActivity.class));
        } else
            startActivity(new Intent(this, StudyActivity.class));

        finish();
    }
}