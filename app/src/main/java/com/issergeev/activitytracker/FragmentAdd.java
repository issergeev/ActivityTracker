package com.issergeev.activitytracker;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class FragmentAdd extends Fragment {
    private Button next;

    private Fragment settingsFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parentViewGroup,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.add_fragment, parentViewGroup, false);

        settingsFragment = new FragmentSettings();

        next = rootView.findViewById(R.id.nextButton);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, settingsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        return rootView;
    }
}