package com.issergeev.activitytracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.format.Time;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ListActivity extends Activity {

    //Database variables
    private final String ID = "id",
                         TYPE = "type",
                         PARENTS = "parents",
                         MOTHER = "mother",
                         FATHER = "father",
                         AGE = "age",
                         COLOR = "color";

    //SharedPreferences constants
    private final String PREFERENCES_NAME = "Settings";
    private final String EXIT = "exit";

    //Timeout
    private long exitTime = 0l;
    private final long pressTime = 1500l;

    //List of parents
    private ArrayList<String> parents;

    private SQLDataWorker worker;

    private SharedPreferences settings;

    //Interface variables
    private TableLayout table_layout;
    private FloatingActionButton floatingActionButton;
    private TextView noAnimalsText;

    //Intents
    private Intent startInfoActivityIntent, startInfoActivityIntent1;

    @Override
    protected void onResume() {
        super.onResume();

        table_layout.removeAllViews();
        CreateTable();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        settings = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

        parents = new ArrayList<>();

        worker = new SQLDataWorker(this);

        startInfoActivityIntent = new Intent(this, InfoActivity.class);
        startInfoActivityIntent1 = new Intent(this, InfoActivity.class);

        //Initializing interface variables
        table_layout = findViewById(R.id.tableLayout1);
        floatingActionButton = findViewById(R.id.fab);
        noAnimalsText = findViewById(R.id.noAnimalsText);

        floatingActionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(startInfoActivityIntent1.putExtra(PARENTS, parents));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        worker.close();
    }

    //Action to do when "Back" button pressed
    @Override
    public void onBackPressed() {
        if (settings.getBoolean(EXIT, false)) {
            if (exitTime >= System.currentTimeMillis()) {
                super.onBackPressed();
            } else {
                exitTime = System.currentTimeMillis() + pressTime;
                Toast.makeText(getApplicationContext(), R.string.exit_confirmation,
                        Toast.LENGTH_SHORT)
                        .show();
            }
        } else
            super.onBackPressed();
    }

    //Creating table with data from Database
    private void CreateTable() {

        worker.open();
        final Cursor c = worker.readEntry();

        final int rows = c.getCount();
        final int cols = c.getColumnCount();
        parents.clear();
        parents.add(getString(R.string.select_parent));

        c.moveToFirst();

        if (rows == 0 || cols == 0) {
            noAnimalsText.setVisibility(View.VISIBLE);
        } else {
            noAnimalsText.setVisibility(View.GONE);
        }

        //Creating rows
        for (int i = 0; i < rows; i++) {
            final TableRow row = new TableRow(this);
            final TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 10, 0, 10);
            row.setLayoutParams(layoutParams);
            if (i % 2 == 0) {
                row.setBackgroundResource(R.drawable.cell_shape);
            } else {
                row.setBackgroundResource(R.drawable.cell_shape_new);
            }
            row.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String query = "SELECT age, mother, father FROM " + DB.getTableName()
                            + " WHERE " + DB.getCowId() + " = ?";
                    String id = ((TextView) ((TableRow) table_layout.getChildAt(table_layout.indexOfChild(v)))
                            .getChildAt(0)).getText().toString();
                    String type = ((TextView) ((TableRow) table_layout.getChildAt(table_layout.indexOfChild(v)))
                            .getChildAt(1)).getText().toString();
                    String color = ((TextView) ((TableRow) table_layout.getChildAt(table_layout.indexOfChild(v)))
                            .getChildAt(2)).getText().toString();

                    Cursor cursor = worker.getDatabase().rawQuery(query, new String[] {id});
                    cursor.moveToFirst();
                    String mother = cursor.getString(cursor.getColumnIndex(DB.getMOTHER()));
                    String father = cursor.getString(cursor.getColumnIndex(DB.getFATHER()));
                    String age = cursor.getString(cursor.getColumnIndex(DB.getAGE()));
                    cursor.close();

                    //Starting Info Activity with Extras
                    startActivity(startInfoActivityIntent
                            .putExtra(ID, id)
                            .putExtra(TYPE, type)
                            .putExtra(COLOR, color)
                            .putExtra(AGE, age)
                            .putExtra(MOTHER, mother)
                            .putExtra(FATHER, father)
                            .putExtra(PARENTS, parents));
                }
            });

            //Creating columns
            for (int j = 0; j < cols; j++) {
                float weight = 0f;
                switch (j) {
                    case 0:
                        weight = 0.2f;
                        parents.add(c.getString(j));
                        break;
                    case 1:
                        weight = 0.3f;
                        break;
                    case 2:
                        weight = 0.2f;
                        break;
                    case 3:
                        weight = 0.3f;
                }

                TextView tv = new TextView(this);
                tv.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, weight));

                tv.setGravity(Gravity.CENTER);

                //Setting different text size for columns
                switch (j) {
                    case 1:
                        tv.setTextSize(10);
                        break;
                    case 3:
                        tv.setTextSize(10);
                        break;
                    default:
                        tv.setTextSize(20);
                }

                tv.setTextSize(20);

                //Setting different color for first column to highlight it
                if (j == 0) {
                    tv.setTextColor(Color.RED);
                    tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
                } else {
                    tv.setTextColor(Color.BLACK);
                }

                tv.setPadding(0, 10, 0, 10);
                tv.setGravity(Gravity.CENTER);

                Time time = new Time(Time.getCurrentTimezone());
                time.setToNow();

                if (j == 3) {
                    String[] date;
                    int month, year;

                    date = c.getString(j).split("-");

                    month = time.month + 1 - Integer.valueOf(date[1]);
                    year = time.year - Integer.valueOf(date[0]);

                    if (month < 0) {
                        year--;
                        month += 13;
                    }
                    if (year < 0) {
                        Toast.makeText(getApplicationContext(), R.string.incorrect_data, Toast.LENGTH_LONG).show();
                    }

                    tv.setText(year + " лет\n" + month + " мес");
                } else {
                    tv.setText(c.getString(j));
                }

                row.addView(tv);
            }

            c.moveToNext();

            table_layout.addView(row);
        }
    }
}