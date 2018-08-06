package com.issergeev.activitytracker;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;

import com.xw.repo.BubbleSeekBar;

public class SettingsActivity extends AppCompatActivity {
    //SharedPreferences name
    private final String PREFERENCES_NAME = "Settings";
    private final String THICKNESS = "thickness",
                         SCROLLING = "scrolling",
                         EXIT = "exit",
                         CHARTS_COLOR = "charts_color";

    private SharedPreferences settings;

    private BubbleSeekBar lineThickness;
    private Switch enableScrolling, confirmExit;
    private ImageView chartsColor;

    private Fragment colorPickerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Initialize preferences
        settings = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

        colorPickerFragment = new FragmentColorPicker();

        //Initialize variables
        lineThickness = findViewById(R.id.thickness);
        chartsColor = findViewById(R.id.chartsColor);
        enableScrolling = findViewById(R.id.scrollGraph);
        confirmExit = findViewById(R.id.exit);

        //Set values from preferences
        lineThickness.setProgress(settings.getInt(THICKNESS, 8));
        enableScrolling.setChecked(settings.getBoolean(SCROLLING, false));
        confirmExit.setChecked(settings.getBoolean(EXIT, false));

        chartsColor.setBackgroundColor(settings.getInt(CHARTS_COLOR,
                getResources().getColor(android.R.color.holo_blue_dark)));

        chartsColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction()
                        .add(R.id.parentLayout, colorPickerFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Getting Editor and putting data
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(THICKNESS, lineThickness.getProgress());
        editor.putBoolean(SCROLLING, enableScrolling.isChecked());
        editor.putBoolean(EXIT, confirmExit.isChecked());
        editor.apply();
    }
}