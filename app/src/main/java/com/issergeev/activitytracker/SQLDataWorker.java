package com.issergeev.activitytracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SQLDataWorker {

    //Where clause constants
    private final String WHERE_CLAUSE = DB.getCowId() + " = ?",
                         WHERE_CLAUSE_DELETE = DB.getCowId() + " = ? AND " + DB.getDATE() + " = ?";

    //Classes to work with Database
    private DB dbhelper;
    private SQLiteDatabase database;

    private Context ourcontext;

    //Constructor
    public SQLDataWorker(Context c) {
        ourcontext = c;
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

    public SQLDataWorker open() throws SQLException {
        dbhelper = new DB(ourcontext);
        database = dbhelper.getWritableDatabase();
        return this;
    }

    //Function to INSERT new animal into Database
    public void insertAnimal(String cow_id, String type, String color, String age, String mother, String father) {
        ContentValues values = new ContentValues();

        values.put(DB.getCowId(), cow_id);
        values.put(DB.getTYPE(), type);
        values.put(DB.getCOLOR(), color);
        values.put(DB.getAGE(), age);
        values.put(DB.getMOTHER(), mother);
        values.put(DB.getFATHER(), father);

        database.insert(DB.getTableName(), null, values);
    }

    //Function to UPDATE animal in Database
    public void updateAnimal(String old_cow_id, String cow_id, String type, String color,
                             String age, String mother, String father) {
        ContentValues values = new ContentValues();

        values.put(DB.getCowId(), cow_id);
        values.put(DB.getTYPE(), type);
        values.put(DB.getCOLOR(), color);
        values.put(DB.getAGE(), age);
        values.put(DB.getMOTHER(), mother);
        values.put(DB.getFATHER(), father);

        database.update(DB.getTableName(), values, WHERE_CLAUSE, new String[]{old_cow_id});
        updateChartsData(old_cow_id, cow_id);
    }

    //Function to UPDATE CHARTS data in Database
    private void updateChartsData(String old_cow_id, String cow_id) {
        ContentValues values = new ContentValues();

        values.put(DB.getCowId(), cow_id);

        database.update(DB.getChartsTableName(), values, WHERE_CLAUSE, new String[]{old_cow_id});
    }

    //Function to DELETE an animal in Database
    public void deleteAnimal(String cow_id) {
        database.delete(DB.getTableName(), WHERE_CLAUSE,
                new String[]{cow_id});
        database.delete(DB.getChartsTableName(), WHERE_CLAUSE,
                new String[]{cow_id});
    }

    //Function to DELETE CHARTS data in Database
    public void deleteChartsData(String cow_id) {
        ContentValues values = new ContentValues();

        values.put(DB.getCowId(), cow_id);

        database.delete(DB.getChartsTableName(), WHERE_CLAUSE, new String[]{cow_id});
    }

    //Function to INSERT CHARTS data into Database
    public void insertChartsData(String cow_id, String date, String milk, String fat, String weight) {
        ContentValues values = new ContentValues();

        values.put(DB.getCowId(), cow_id);
        values.put(DB.getDATE(), date);
        values.put(DB.getMILK(), milk);
        values.put(DB.getFAT(), fat);
        values.put(DB.getWEIGHT(), weight);

        database.delete(DB.getChartsTableName(), WHERE_CLAUSE_DELETE, new String[]{cow_id, date});
        database.insert(DB.getChartsTableName(), null, values);
    }

    //Reading rows in a Table in Database
    public Cursor readEntry() {
        String[] allColumns = new String[] { DB.getCowId(), DB.getTYPE(),
                DB.getCOLOR(), DB.getAGE() };

        Cursor c = database.query(DB.getTableName(), allColumns, null, null, null,
                null, null);

        if (c != null) {
            c.moveToFirst();
        }

        return c;
    }

    public void close() {
        dbhelper.close();
    }
}