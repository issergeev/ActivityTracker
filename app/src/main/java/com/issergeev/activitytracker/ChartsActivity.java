package com.issergeev.activitytracker;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

public class ChartsActivity extends AppCompatActivity implements View.OnClickListener {
    private final String ID = "id",
                         DATE = "date";

    private Button saveButton, closeButton;
    private EditText milk, fat, weight;
    private Button date;

    private ProgressDialog PD;

    private SQLDataWorker worker;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        intent = getIntent();

        worker = new SQLDataWorker(this);

        saveButton = findViewById(R.id.saveButton);
        closeButton = findViewById(R.id.closeButton);
        milk = findViewById(R.id.milkInput);
        fat = findViewById(R.id.fatInput);
        date = findViewById(R.id.date);
        weight = findViewById(R.id.weightInput);
        saveButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(date.getText().toString());
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveButton:
                if (checkData() == 0){
                    AlertDialog.Builder alertDialog;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        alertDialog = new AlertDialog.Builder(ChartsActivity.this,
                                android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        alertDialog = new AlertDialog.Builder(ChartsActivity.this);
                    }
                    alertDialog.setTitle(R.string.error)
                            .setMessage(R.string.input_fields)
                            .setPositiveButton(android.R.string.yes, null)
                            .setCancelable(true)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                if (checkData() == 1) {
                    String milk = this.milk.getText().toString().trim();
                    String fat = this.fat.getText().toString().trim();
                    String weight = this.weight.getText().toString().trim();
                    new AddData().execute(intent.getStringExtra(ID), date.getText().toString(),
                            milk, fat, weight);
                    finish();
                }
                if (checkData() == 2) {
                    AlertDialog.Builder alertDialog;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        alertDialog = new AlertDialog.Builder(ChartsActivity.this,
                                android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        alertDialog = new AlertDialog.Builder(ChartsActivity.this);
                    }
                    alertDialog.setTitle(R.string.error)
                            .setMessage(getResources().getString(R.string.incorrect_date) +
                                    " " + intent.getStringExtra(DATE))
                            .setPositiveButton(android.R.string.yes, null)
                            .setCancelable(true)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                break;
            case R.id.closeButton:
                finish();
        }
    }

    private int checkData() {
        if (date.getText().toString().compareTo(intent.getStringExtra(DATE)) <= 0)
            return 2;
        if (Character.isDigit(date.getText().toString().charAt(0)) &&
            milk.getText().toString().trim().length() > 0 &&
            fat.getText().toString().trim().length() > 0 &&
            weight.getText().toString().trim().length() > 0)
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
                    date.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                }
            };

            datePickerDialog = new DatePickerDialog(this,
                    dateSetListener, year, month, day);
        } else {
            dateSetListener = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    date.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
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

            PD = new ProgressDialog(ChartsActivity.this);
            PD.setTitle(getResources().getString(R.string.wait));
            PD.setMessage(getResources().getString(R.string.refreshing_data));
            PD.setCancelable(false);
            PD.show();
        }

        @Override
        protected Void doInBackground(String... strings) {

            worker.open();
            worker.insertData(Integer.valueOf(strings[0]), strings[1], strings[2], strings[3], strings[4]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            PD.dismiss();
        }
    }
}