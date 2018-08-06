package com.issergeev.activitytracker;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Calendar;

public class InfoActivity extends AppCompatActivity {

    //DataBase constants
    private final String ID = "id",
            TYPE = "type",
            PARENTS = "parents",
            DATE = "date",
            MOTHER = "mother",
            FATHER = "father",
            AGE = "age",
            COLOR = "color";

    //Preferences name
    private final String PREFERENCES_NAME = "Settings";
    private final String THICKNESS = "thickness",
            SCROLLING = "scrolling",
            CHARTS_COLOR = "charts_color";

    //Time to press the graphs
    private long milkTapTime = 0,
            fatTapTime = 0,
            weightTapTime = 0;
    private final long pressTime = 500;

    //List of parents
    private ArrayList<String> mothers;
    private ArrayList<String> fathers;

    private SQLDataWorker worker;
    private Listener listener;

    private SharedPreferences settings;

    private Spinner spinner0, spinner1, spinner2, spinner3;
    private EditText cow_id;
    private Button age;
    private Button saveButton, closeButton, deleteButton;
    private GraphView milk, fat, weight;

    private AlertDialog.Builder alertDialog;
    private ArrayAdapter<String> typeAdapter, colorAdapter, motherAdapter, fatherAdapter;
    private Intent intent;
    private ProgressDialog PD;

    //Creating Options Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }
    //Events on Options Menu buttons pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settingsButton:
                startActivity(new Intent(this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            milk.removeAllSeries();
            fat.removeAllSeries();
            weight.removeAllSeries();

            DB db = new DB(this);
            SQLiteDatabase database = db.getReadableDatabase();

            final String query = "SELECT * FROM " + DB.getChartsTableName() +
                    " WHERE " + DB.getCowId() + " = ? " +
                    "ORDER BY date(" + DB.getDATE() + ") ASC";

            Cursor cursor = database.rawQuery(query,
                    new String[]{intent.getStringExtra(ID)});

            //Creating DataPoints to draw through
            DataPoint[] milkPoints = new DataPoint[cursor.getCount()];
            DataPoint[] fatPoints = new DataPoint[cursor.getCount()];
            DataPoint[] weightPoints = new DataPoint[cursor.getCount()];

            //Filling DataPoints
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                float milk = cursor.getFloat(cursor.getColumnIndex(DB.getMILK()));
                float fat = cursor.getFloat(cursor.getColumnIndex(DB.getFAT()));
                float weight = cursor.getFloat(cursor.getColumnIndex(DB.getWEIGHT()));

                milkPoints[i] = new DataPoint(i, milk);
                fatPoints[i] = new DataPoint(i, fat);
                weightPoints[i] = new DataPoint(i, weight);
            }

            //Drawing Graphs
            LineGraphSeries<DataPoint> milkSeries = new LineGraphSeries<>(milkPoints);
            LineGraphSeries<DataPoint> fatSeries = new LineGraphSeries<>(fatPoints);
            LineGraphSeries<DataPoint> weightSeries = new LineGraphSeries<>(weightPoints);

            //Setting scrolling to Charts
            milk.getViewport().setScalable(settings.getBoolean(SCROLLING, false));
            fat.getViewport().setScalable(settings.getBoolean(SCROLLING, false));
            weight.getViewport().setScalable(settings.getBoolean(SCROLLING, false));

            final Paint paint = new Paint();
            paint.setColor(settings.getInt(CHARTS_COLOR, android.R.color.holo_blue_dark));
            paint.setStrokeWidth(settings.getInt(THICKNESS, 8));

            //Setting params on Charts
            milkSeries.setDrawDataPoints(true);
            milkSeries.setCustomPaint(paint);
            fatSeries.setDrawDataPoints(true);
            fatSeries.setCustomPaint(paint);
            weightSeries.setDrawDataPoints(true);
            weightSeries.setCustomPaint(paint);

            this.milk.addSeries(milkSeries);
            this.fat.addSeries(fatSeries);
            this.weight.addSeries(weightSeries);

            cursor.close();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        //Getting the Intent
        intent = getIntent();

        //Initializing classes
        worker = new SQLDataWorker(this);
        listener = new Listener();

        //Array of parents
        mothers = new ArrayList<>();
        fathers = new ArrayList<>();

        //Getting SharedPreferences
        settings = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

        //Initializing variables
        spinner0 = findViewById(R.id.spinner0);
        spinner1 = findViewById(R.id.spinner1);
        spinner2 = findViewById(R.id.spinner2);
        spinner3 = findViewById(R.id.spinner3);
        cow_id = findViewById(R.id.number);
        age = findViewById(R.id.age);
        saveButton = findViewById(R.id.saveButton);
        closeButton = findViewById(R.id.closeButton);
        deleteButton = findViewById(R.id.deleteButton);
        milk = findViewById(R.id.milkChart);
        fat = findViewById(R.id.fatChart);
        weight = findViewById(R.id.weightChart);

        //Setting adapters
        typeAdapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.spinner_text_layout, getResources().getStringArray(R.array.type_of_cows));
        colorAdapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.spinner_text_layout, getResources().getStringArray(R.array.color_of_cows));
        motherAdapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.spinner_text_layout, mothers);
        fatherAdapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.spinner_text_layout, fathers);
        typeAdapter.setDropDownViewResource(R.layout.spinner_adapter);
        colorAdapter.setDropDownViewResource(R.layout.spinner_adapter);
        motherAdapter.setDropDownViewResource(R.layout.spinner_adapter);
        fatherAdapter.setDropDownViewResource(R.layout.spinner_adapter);
        spinner0.setAdapter(typeAdapter);
        spinner1.setAdapter(colorAdapter);
        spinner2.setAdapter(motherAdapter);
        spinner3.setAdapter(fatherAdapter);

        //Filling parents list
        try {
            motherAdapter.addAll(intent.getStringArrayListExtra(PARENTS));
            fatherAdapter.addAll(intent.getStringArrayListExtra(PARENTS));
        } catch (NullPointerException e) {
            Toast.makeText(getApplicationContext(), R.string.wrong_text, Toast.LENGTH_LONG).show();
        }

        //Setting fields
        if (intent.hasExtra(ID)) {
            deleteButton.setVisibility(View.VISIBLE);

            cow_id.setText(intent.getStringExtra(ID));
            spinner0.setSelection(typeAdapter.getPosition(intent.getStringExtra(TYPE)));
            spinner1.setSelection(colorAdapter.getPosition(intent.getStringExtra(COLOR)));
            spinner2.setSelection(motherAdapter.getPosition(intent.getStringExtra(MOTHER)));
            spinner3.setSelection(fatherAdapter.getPosition(intent.getStringExtra(FATHER)));
            age.setText(intent.getStringExtra(AGE));

            mothers.remove(mothers.indexOf(intent.getStringExtra(ID)));
            fathers.remove(fathers.indexOf(intent.getStringExtra(ID)));
            motherAdapter.notifyDataSetChanged();
            fatherAdapter.notifyDataSetChanged();
        } else {
            LinearLayout milkLayout, fatLayout, weightLayout;
            milkLayout = findViewById(R.id.milk);
            fatLayout = findViewById(R.id.fat);
            weightLayout = findViewById(R.id.weight);
            milkLayout.setVisibility(View.GONE);
            fatLayout.setVisibility(View.GONE);
            weightLayout.setVisibility(View.GONE);
        }

        //Setting Listeners
        saveButton.setOnClickListener(listener);
        closeButton.setOnClickListener(listener);
        deleteButton.setOnClickListener(listener);
        age.setOnClickListener(listener);

        cow_id.addTextChangedListener(listener);
        spinner0.setOnItemSelectedListener(listener);
        spinner1.setOnItemSelectedListener(listener);
        age.addTextChangedListener(listener);

        //Creating an intent to start Charts Activity
        final Intent chartsIntent = new Intent(this, ChartsActivity.class);
        chartsIntent.putExtra(DATE, age.getText().toString());
        chartsIntent.putExtra(ID, intent.getStringExtra(ID));

        //Setting timeout to start Charts Activity
        milk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (System.currentTimeMillis() <= milkTapTime) {
                    startActivity(chartsIntent);
                } else
                    milkTapTime = System.currentTimeMillis() + pressTime;
            }
        });
        fat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (System.currentTimeMillis() <= fatTapTime) {
                    startActivity(chartsIntent);
                } else
                    fatTapTime = System.currentTimeMillis() + pressTime;
            }
        });
        weight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (System.currentTimeMillis() <= weightTapTime) {
                    startActivity(chartsIntent);
                } else
                    weightTapTime = System.currentTimeMillis() + pressTime;
            }
        });
    }

    @Override
    public void onBackPressed() {
        //Building AlertDialog
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alertDialog = new AlertDialog.Builder(InfoActivity.this,
                    android.R.style.Theme_Material_Dialog_Alert);
        } else {
            alertDialog = new AlertDialog.Builder(InfoActivity.this);
        }
        alertDialog.setTitle(R.string.quit)
                .setMessage(R.string.quit_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .setCancelable(true)
                .setIcon(R.drawable.ic_action_block);

        final AlertDialog dialog = alertDialog.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button deleteButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                deleteButton.setTextColor(getResources()
                        .getColor(android.R.color.holo_red_light));
                cancelButton.setTextColor(getResources()
                        .getColor(android.R.color.holo_green_light));
            }
        });
        dialog.show();
    }

    //AsyncTasks to perform functions
    @SuppressLint("StaticFieldLeak")
    protected class AddAnimal extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            PD = new ProgressDialog(InfoActivity.this);
            PD.setTitle(getString(R.string.wait));
            PD.setMessage(getString(R.string.refreshing_data));
            PD.setCancelable(false);
            PD.show();
        }

        @Override
        protected Void doInBackground(String... strings) {
            worker.open();
            worker.insertAnimal(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5]);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            PD.dismiss();

            finish();
        }
    }
    @SuppressLint("StaticFieldLeak")
    protected class UpdateAnimal extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            PD = new ProgressDialog(InfoActivity.this);
            PD.setTitle(getString(R.string.wait));
            PD.setMessage(getString(R.string.refreshing_data));
            PD.setCancelable(false);
            PD.show();
        }

        @Override
        protected Void doInBackground(String... strings) {
            worker.open();
            worker.updateAnimal(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6]);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            PD.dismiss();

            finish();
        }
    }
    @SuppressLint("StaticFieldLeak")
    protected class DeleteAnimal extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            PD = new ProgressDialog(InfoActivity.this);
            PD.setTitle(getString(R.string.wait));
            PD.setMessage(getString(R.string.refreshing_data));
            PD.setCancelable(false);
            PD.show();
        }

        @Override
        protected Void doInBackground(String... strings) {
            worker.open();
            worker.deleteAnimal(strings[0]);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            PD.dismiss();

            finish();
        }
    }
    @SuppressLint("StaticFieldLeak")
    protected class UpdateAnimalWithoutCharts extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... strings) {
            worker.open();
            worker.deleteChartsData(strings[0]);
            worker.updateAnimal(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6]);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            finish();
        }
    }

    //Class to listen events
    private class Listener implements View.OnClickListener, TextWatcher, AdapterView.OnItemSelectedListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.saveButton:
                    final String id = cow_id.getText().toString().trim();
                    final String type = spinner0.getSelectedItem().toString();
                    final String color = spinner1.getSelectedItem().toString();
                    final String animalAge = age.getText().toString();
                    final String mother = spinner2.getSelectedItem().toString();
                    final String father = spinner3.getSelectedItem().toString();

                    if (checkData()) {
                        if (intent.hasExtra(AGE) &&
                                intent.getStringExtra(AGE).compareTo(age.getText().toString()) != 0) {
                            final AlertDialog.Builder alertDialog;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                alertDialog = new AlertDialog.Builder(InfoActivity.this,
                                        android.R.style.Theme_Material_Dialog_Alert);
                            } else {
                                alertDialog = new AlertDialog.Builder(InfoActivity.this);
                            }
                            alertDialog.setTitle(R.string.attention)
                                    .setMessage(R.string.different_date)
                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            new UpdateAnimalWithoutCharts().execute(intent.getStringExtra(ID), id,
                                                    type, color, animalAge, mother, father);
                                        }
                                    })
                                    .setNegativeButton(R.string.no, null)
                                    .setCancelable(true)
                                    .setIcon(R.drawable.ic_action_block);

                            final AlertDialog dialog = alertDialog.create();
                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialogInterface) {
                                    Button deleteButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                    Button cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                                    deleteButton.setTextColor(getResources()
                                            .getColor(android.R.color.holo_red_light));
                                    cancelButton.setTextColor(getResources()
                                            .getColor(android.R.color.holo_green_light));
                                }
                            });
                            dialog.show();
                        } else if (intent.hasExtra(ID)) {
                            new UpdateAnimal().execute(intent.getStringExtra(ID), id, type, color, animalAge, mother, father);
                        } else
                            new AddAnimal().execute(id, type, color, animalAge, mother, father);
                    }

                    if (!checkData()) {
                        AlertDialog.Builder alertDialog;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            alertDialog = new AlertDialog.Builder(InfoActivity.this,
                                    android.R.style.Theme_Material_Dialog_Alert);
                        } else {
                            alertDialog = new AlertDialog.Builder(InfoActivity.this);
                        }
                        alertDialog.setTitle(R.string.error)
                                .setMessage(R.string.id_exists)
                                .setPositiveButton(android.R.string.yes, null)
                                .setCancelable(true)
                                .setIcon(R.drawable.ic_action_block)
                                .show();
                    }
                    break;
                case R.id.closeButton:
                    finish();
                    break;
                case R.id.deleteButton:
                    AlertDialog.Builder alertDialog;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        alertDialog = new AlertDialog.Builder(InfoActivity.this,
                                android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        alertDialog = new AlertDialog.Builder(InfoActivity.this);
                    }
                    alertDialog.setTitle(R.string.attention)
                            .setMessage(R.string.delete_text)
                            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new DeleteAnimal().execute(cow_id.getText().toString());
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .setCancelable(true)
                            .setIcon(R.drawable.ic_action_block);

                    final AlertDialog dialog = alertDialog.create();
                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {
                            Button deleteButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            Button cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                            deleteButton.setTextColor(getResources()
                                    .getColor(android.R.color.holo_red_light));
                            cancelButton.setTextColor(getResources()
                                    .getColor(android.R.color.holo_green_light));
                        }
                    });
                    dialog.show();
                    break;
                case R.id.age:
                    showDatePickerDialog(age.getText().toString());
            }
        }

        //Validating data on selected item change
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (cow_id.getText().toString().trim().length() > 0 &&
                    spinner0.getSelectedItemPosition() != 0 &&
                    spinner1.getSelectedItemPosition() != 0 &&
                    age.getText().toString().compareTo(getResources().getString(R.string.default_date)) != 0 &&
                    !saveButton.isEnabled())
                saveButton.setEnabled(true);

            if ((cow_id.getText().toString().trim().length() == 0 ||
                    spinner0.getSelectedItemPosition() == 0 ||
                    spinner1.getSelectedItemPosition() == 0 ||
                    age.getText().toString().compareTo(getResources().getString(R.string.default_date)) == 0) &&
                    saveButton.isEnabled())
                saveButton.setEnabled(false);
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
        @Override

        //Setting TextWatcher
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            if (cow_id.getText().toString().trim().length() > 0 &&
                    spinner0.getSelectedItemPosition() != 0 &&
                    spinner1.getSelectedItemPosition() != 0 &&
                    age.getText().toString().compareTo(getResources().getString(R.string.default_date)) != 0 &&
                    !saveButton.isEnabled())
                saveButton.setEnabled(true);

            if ((cow_id.getText().toString().trim().length() == 0 ||
                    spinner0.getSelectedItemPosition() == 0 ||
                    spinner1.getSelectedItemPosition() == 0 ||
                    age.getText().toString().compareTo(getResources().getString(R.string.default_date)) == 0) &&
                    saveButton.isEnabled())
                saveButton.setEnabled(false);
        }
    }

    //Function to check data
    private boolean checkData() {
        if (new ArrayList<>(intent.getStringArrayListExtra(PARENTS)).contains(cow_id.getText().toString()) &&
                intent.hasExtra(TYPE) && intent.getStringExtra(ID).compareTo(cow_id.getText().toString()) != 0)
            return false;

        return true;
    }

    //Display a DatePickerDialog
    protected void showDatePickerDialog(final String currentDate) {
        DatePickerDialog datePickerDialog;
        DatePickerDialog.OnDateSetListener dateSetListener;

        if (Character.isDigit(currentDate.charAt(0))) {
            String[] split = currentDate.split("-");
            int day = Integer.valueOf(split[2]);
            final int month = Integer.valueOf(split[1]) - 1;
            int year = Integer.valueOf(split[0]);

            dateSetListener = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    age.setText(new StringBuilder().append(year)
                            .append("-")
                            .append(monthOfYear + 1)
                            .append("-")
                            .append(dayOfMonth)
                            .toString());
                }
            };

            datePickerDialog = new DatePickerDialog(this,
                    dateSetListener, year, month, day);
        } else {
            dateSetListener = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    age.setText(new StringBuilder().append(year)
                            .append("-")
                            .append(monthOfYear + 1)
                            .append("-")
                            .append(dayOfMonth)
                            .toString());
                }
            };
            datePickerDialog = new DatePickerDialog(this, dateSetListener,
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH),
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        }

        datePickerDialog.show();
    }
}