package com.issergeev.activitytracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SQLDataWorker {

    private DB dbhelper;
    private SQLiteDatabase database;

    private Context ourcontext;

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

    public void insertData(int cow_id, String type, String color, String age, String mother, String father) {
        ContentValues cv = new ContentValues();

        cv.put(DB.getCowId(), cow_id);
        cv.put(DB.getTYPE(), type);
        cv.put(DB.getCOLOR(), color);
        cv.put(DB.getAGE(), age);
        cv.put(DB.getMOTHER(), mother);
        cv.put(DB.getFATHER(), father);

        database.delete(DB.getTableName(), DB.getCowId() + " = ?",
                new String[]{String.valueOf(cow_id)});
        database.insert(DB.getTableName(), null, cv);
    }

    public void insertData(int cow_id, String date, String milk, String fat, String weight) {
        ContentValues cv = new ContentValues();

        cv.put(DB.getCowId(), cow_id);
        cv.put(DB.getDATE(), date);
        cv.put(DB.getMILK(), milk);
        cv.put(DB.getFAT(), fat);
        cv.put(DB.getWEIGHT(), weight);

        database.delete(DB.getChartsTableName(), DB.getCowId() + " = ? AND " + DB.getDATE() + " = ?",
                new String[]{String.valueOf(cow_id), date});
        database.insert(DB.getChartsTableName(), null, cv);
    }

    public void deleteData(int cow_id) {
        database.delete(DB.getTableName(), DB.getCowId() + " = ?",
                new String[]{String.valueOf(cow_id)});
        database.delete(DB.getChartsTableName(), DB.getCowId() + " = ?",
                new String[]{String.valueOf(cow_id)});
    }

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