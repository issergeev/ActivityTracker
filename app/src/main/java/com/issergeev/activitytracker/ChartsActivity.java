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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ChartsActivity extends AppCompatActivity {

    //Database variables
    private final String ID = "id",
                         DATE = "date";

    //Interface variables
    private Button saveButton, closeButton;
    private EditText milk, fat, weight;
    private Button date;

    //Classes
    private SQLDataWorker worker;
    private Listener listener;

    private Intent intent;

    private ProgressDialog PD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        intent = getIntent();

        worker = new SQLDataWorker(this);
        listener = new Listener();

        //Initializing interface variables
        saveButton = findViewById(R.id.saveButton);
        closeButton = findViewById(R.id.closeButton);
        milk = findViewById(R.id.milkInput);
        fat = findViewById(R.id.fatInput);
        date = findViewById(R.id.date);
        weight = findViewById(R.id.weightInput);
        saveButton.setOnClickListener(listener);
        closeButton.setOnClickListener(listener);

        //Adding Listeners
        milk.addTextChangedListener(listener);
        fat.addTextChangedListener(listener);
        weight.addTextChangedListener(listener);
        date.setOnClickListener(listener);

        date.setText(new StringBuilder().append(Calendar.getInstance().get(Calendar.YEAR))
                                        .append("-")
                                        .append(Calendar.getInstance().get(Calendar.MONTH))
                                        .append("-")
                                        .append(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));
    }

    //AsyncTask to add new data to Charts Table in Database
    @SuppressLint("StaticFieldLeak")
    private class AddChartsData extends AsyncTask<String, Void, Void> {

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
            worker.insertChartsData(strings[0], strings[1], strings[2], strings[3], strings[4]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            PD.dismiss();
        }
    }

    //Class to listen events
    private class Listener implements View.OnClickListener, TextWatcher {

        //TextWatcher
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            if (milk.getText().toString().trim().length() > 0 &&
                    fat.getText().toString().trim().length() > 0 &&
                    weight.getText().toString().trim().length() > 0 &&
                    !saveButton.isEnabled())
                saveButton.setEnabled(true);

            if ((milk.getText().toString().trim().length() == 0 ||
                    fat.getText().toString().trim().length() == 0 ||
                    weight.getText().toString().trim().length() == 0) &&
                    saveButton.isEnabled())
                saveButton.setEnabled(false);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.saveButton:
                    if (!checkData()){
                        AlertDialog.Builder alertDialog;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            alertDialog = new AlertDialog.Builder(ChartsActivity.this,
                                    android.R.style.Theme_Material_Dialog_Alert);
                        } else {
                            alertDialog = new AlertDialog.Builder(ChartsActivity.this);
                        }
                        alertDialog.setTitle(R.string.error)
                                .setMessage(getResources().getString(R.string.incorrect_date) + " "
                                        + intent.getStringExtra(DATE))
                                .setPositiveButton(android.R.string.yes, null)
                                .setCancelable(true)
                                .setIcon(R.drawable.ic_action_block)
                                .show();
                    } else {
                        String milkValue = milk.getText().toString().trim();
                        String fatValue = fat.getText().toString().trim();
                        String weightValue = weight.getText().toString().trim();

                        new AddChartsData().execute(intent.getStringExtra(ID), date.getText().toString(),
                                milkValue, fatValue, weightValue);

                        finish();
                    }
                    break;
                case R.id.closeButton:
                    finish();
                    break;
                case R.id.date:
                    showDatePickerDialog(date.getText().toString());
            }
        }
    }

    //Function to compare date
    private static boolean dateCompare(Date date1, Date date2) {
        return (!date1.before(date2) && !date1.equals(date2));
    }

    //Function to verify data
    private boolean checkData() {
        try {
            SimpleDateFormat format = new SimpleDateFormat(getString(R.string.date));
            Date current_date = format.parse(this.date.getText().toString());
            Date date = format.parse(intent.getStringExtra(DATE));

            if (dateCompare(current_date, date)) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    //Function to display DatePickerDialog
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
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    date.setText(new StringBuilder().append(year)
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
                    date.setText(new StringBuilder()
                            .append(year)
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