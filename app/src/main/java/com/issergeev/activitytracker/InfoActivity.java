package com.issergeev.activitytracker;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Calendar;


public class InfoActivity extends AppCompatActivity implements View.OnClickListener {

    private ArrayList<String> mothers;
    private ArrayList<String> fathers;

    private SQLDataWorker worker;

    private RelativeLayout parentLayout;
    private Spinner spinner0, spinner1, spinner2, spinner3;
    private EditText cow_id;
    private Button age;
    private Button saveButton, closeButton, deleteButton;
    private GraphView milk, fat, weight;

    private AlertDialog.Builder alertDialog;
    private ArrayAdapter<String> typeAdapter, colorAdapter, motherAdapter, fatherAdapter;
    private Intent intent;
    private ProgressDialog PD;

    @Override
    protected void onResume() {
        super.onResume();
        try {
            DB db = new DB(this);
            SQLiteDatabase database = db.getReadableDatabase();

            this.milk.removeAllSeries();
            this.fat.removeAllSeries();
            this.weight.removeAllSeries();

            final String query = "SELECT * FROM charts_data WHERE cow_id = ? ORDER BY date ASC";
            Cursor cursor = database.rawQuery(query,
                    new String[]{intent.getStringExtra("id")});
            DataPoint[] datePoints = new DataPoint[cursor.getCount()];
            DataPoint[] milkPoints = new DataPoint[cursor.getCount()];
            DataPoint[] fatPoints = new DataPoint[cursor.getCount()];
            DataPoint[] weightPoints = new DataPoint[cursor.getCount()];

            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                String date = cursor.getString(cursor.getColumnIndex(DB.getDATE()));
                float milk = cursor.getFloat(cursor.getColumnIndex(DB.getMILK()));
                float fat = cursor.getFloat(cursor.getColumnIndex(DB.getFAT()));
                float weight = cursor.getFloat(cursor.getColumnIndex(DB.getWEIGHT()));
                milkPoints[i] = new DataPoint(i, milk);
                fatPoints[i] = new DataPoint(i, fat);
                weightPoints[i] = new DataPoint(i, weight);
            }

            LineGraphSeries<DataPoint> milkSeries = new LineGraphSeries<>(milkPoints);
            LineGraphSeries<DataPoint> fatSeries = new LineGraphSeries<>(fatPoints);
            LineGraphSeries<DataPoint> weightSeries = new LineGraphSeries<>(weightPoints);
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

        intent = getIntent();

        worker = new SQLDataWorker(this);

        spinner0 = findViewById(R.id.spinner0);
        spinner1 = findViewById(R.id.spinner1);
        spinner2 = findViewById(R.id.spinner2);
        spinner3 = findViewById(R.id.spinner3);
        cow_id = findViewById(R.id.number);
        age = findViewById(R.id.age);
        saveButton = findViewById(R.id.saveButton);
        closeButton = findViewById(R.id.closeButton);
        deleteButton = findViewById(R.id.deleteButton);
        parentLayout = findViewById(R.id.parentLayout);
        milk = findViewById(R.id.milkChart);
        fat = findViewById(R.id.fatChart);
        weight = findViewById(R.id.weightChart);

        mothers = new ArrayList<>();
        fathers = new ArrayList<>();

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

        age.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(age.getText().toString());
            }
        });

        try {
            motherAdapter.addAll(intent.getStringArrayListExtra("parents"));
            fatherAdapter.addAll(intent.getStringArrayListExtra("parents"));
        } catch (NullPointerException e) {
            Toast.makeText(getApplicationContext(), R.string.wrong_text, Toast.LENGTH_LONG).show();
        }

        if (intent.hasExtra("type")) {
            cow_id.setEnabled(false);
            deleteButton.setVisibility(View.VISIBLE);
            age.setEnabled(false);

            cow_id.setText(intent.getStringExtra("id"));
            spinner0.setSelection(typeAdapter.getPosition(intent.getStringExtra("type")));
            spinner1.setSelection(colorAdapter.getPosition(intent.getStringExtra("color")));
            spinner2.setSelection(motherAdapter.getPosition(intent.getStringExtra("mother")));
            spinner3.setSelection(fatherAdapter.getPosition(intent.getStringExtra("father")));
            age.setText(intent.getStringExtra("age"));

            mothers.remove(mothers.indexOf(intent.getStringExtra("id")));
            fathers.remove(fathers.indexOf(intent.getStringExtra("id")));
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

        saveButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);

        final Intent chartsIntent = new Intent(this, ChartsActivity.class);
        chartsIntent.putExtra("date", age.getText().toString());
        chartsIntent.putExtra("id", intent.getStringExtra("id"));
        milk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(chartsIntent);
            }
        });
        fat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(chartsIntent);
            }
        });
        weight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(chartsIntent);
            }
        });
    }

    @Override
    public void onBackPressed() {
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
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveButton:
                if (checkData() == 1) {
                    String id = cow_id.getText().toString().trim();
                    String type = spinner0.getSelectedItem().toString();
                    String color = spinner1.getSelectedItem().toString();
                    String age = this.age.getText().toString();
                    String mother = spinner2.getSelectedItem().toString();
                    String father = spinner3.getSelectedItem().toString();
                    
                    new AddData().execute(id, type, color, age, mother, father);
                    finish();
                }
                if (checkData() == 2) {
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
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                if(checkData() == 0) {
                    AlertDialog.Builder alertDialog;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        alertDialog = new AlertDialog.Builder(InfoActivity.this,
                                android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        alertDialog = new AlertDialog.Builder(InfoActivity.this);
                    }
                    alertDialog.setTitle(R.string.error)
                            .setMessage(R.string.input_fields)
                            .setPositiveButton(android.R.string.yes, null)
                            .setCancelable(true)
                            .setIcon(android.R.drawable.ic_dialog_alert)
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
                                new DeleteData().execute(cow_id.getText().toString());
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .setCancelable(true)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
        }
    }

    private int checkData() {
        if (new ArrayList<>(intent.getStringArrayListExtra("parents")).contains(cow_id.getText().toString()) &&
                !intent.hasExtra("id"))
            return 2;
        if (cow_id.getText().toString().trim().length() > 0 &&
                spinner0.getSelectedItemPosition() != 0 &&
                spinner1.getSelectedItemPosition() != 0 &&
                Character.isDigit(age.getText().charAt(0)))
            return 1;
        return 0;
    }

    private void showDatePickerDialog(final String currentDate) {
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
                    age.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                }
            };

            datePickerDialog = new DatePickerDialog(this,
                    dateSetListener, year, month, day);
        } else {
            dateSetListener = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    age.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                }
            };
            datePickerDialog = new DatePickerDialog(this, dateSetListener,
                    Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH);
        }
        datePickerDialog.show();
    }

    @SuppressLint("StaticFieldLeak")
    private class AddData extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            parentLayout.removeAllViews();

            PD = new ProgressDialog(InfoActivity.this);
            PD.setTitle(getString(R.string.wait));
            PD.setMessage(getString(R.string.refreshing_data));
            PD.setCancelable(false);
            PD.show();
        }

        @Override
        protected Void doInBackground(String... strings) {
            worker.open();
            worker.insertData(Integer.valueOf(strings[0]), strings[1], strings[2], strings[3], strings[4], strings[5]);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            PD.dismiss();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DeleteData extends AsyncTask<String, Void, Void> {

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
            worker.deleteData(Integer.valueOf(strings[0]));

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            PD.dismiss();

            finish();
        }
    }
}