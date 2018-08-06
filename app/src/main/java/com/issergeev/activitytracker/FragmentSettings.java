package com.issergeev.activitytracker;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class FragmentSettings extends Fragment {
    private final String PREFERENCES_NAME = "Settings";
    private final String FIRST_START = "first_start";

    private SharedPreferences settings;

    private Button start;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parentViewGroup,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.settings_fragment, parentViewGroup, false);

        settings = getActivity().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

        start = rootView.findViewById(R.id.startButton);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(FIRST_START, false);
                editor.apply();

                startActivity(new Intent(getActivity(), ListActivity.class));

                getActivity().finish();
            }
        });
        return rootView;
    }
}