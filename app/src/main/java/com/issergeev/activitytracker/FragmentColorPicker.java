package com.issergeev.activitytracker;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydoves.colorpickerpreference.ColorEnvelope;
import com.skydoves.colorpickerpreference.ColorListener;
import com.skydoves.colorpickerpreference.ColorPickerView;

public class FragmentColorPicker extends Fragment {
    private final String PREFERENCES_NAME = "Settings";
    private final String CHARTS_COLOR = "charts_color";

    private int color;

    private SharedPreferences settings;

    private ColorPickerView colorPicker;
    private RelativeLayout indicator;
    private Button save;
    private TextView colorText;

    private ImageView colorIndicator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parentViewGroup,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.color_picker_fragment, parentViewGroup, false);

        settings = getActivity().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

        colorPicker = rootView.findViewById(R.id.colorPickerView);
        indicator = rootView.findViewById(R.id.parentLayout);
        colorText = rootView.findViewById(R.id.colorText);
        save = rootView.findViewById(R.id.chouseButton);

        colorIndicator = getActivity().findViewById(R.id.chartsColor);

        colorPicker.setColorListener(new ColorListener() {
            @Override
            public void onColorSelected(ColorEnvelope colorEnvelope) {
                indicator.setBackgroundColor(colorEnvelope.getColor());
                colorText.setText("#" + colorEnvelope.getColorHtml());
                color = colorEnvelope.getColor();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(CHARTS_COLOR, color);
                editor.apply();

                getActivity().getFragmentManager().beginTransaction()
                        .remove(FragmentColorPicker.this)
                        .commit();
                getFragmentManager().popBackStack();

                colorIndicator.setBackgroundColor(color);
            }
        });
        return rootView;
    }
}