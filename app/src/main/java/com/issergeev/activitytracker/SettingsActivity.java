package com.issergeev.activitytracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Switch;

import com.xw.repo.BubbleSeekBar;

public class SettingsActivity extends AppCompatActivity {
    //SharedPreferences name
    private final String PREFERENCES_NAME = "Settings";
    private final String THICKNESS = "thickness",
                         EXIT = "exit";

    private SharedPreferences settings;

    private BubbleSeekBar lineThickness;
    private Switch confirmExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Initialize preferences
        settings = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

        //Initialize variables
        lineThickness = findViewById(R.id.thickness);
        confirmExit = findViewById(R.id.exit);

        //Set values from preferences
        lineThickness.setProgress(settings.getInt(THICKNESS, 8));
        confirmExit.setChecked(settings.getBoolean(EXIT, false));
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(THICKNESS, lineThickness.getProgress());
        editor.putBoolean(EXIT, confirmExit.isChecked());
        editor.apply();
    }
}