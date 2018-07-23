package com.issergeev.activitytracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DB extends SQLiteOpenHelper {

    private static final String TABLE_NAME = "cows";
    private static final String COW_ID = "cow_id";
    private static final String TYPE = "type";
    private static final String MOTHER = "mother";
    private static final String FATHER = "father";
    private static final String COLOR = "color";
    private static final String AGE = "age";

    private static final String CHARTS_TABLE_NAME = "charts_data";
    private static final String ID = "id";
    private static final String DATE = "date";
    private static final String MILK = "milk";
    private static final String FAT = "fat";
    private static final String WEIGHT = "weight";

    private static final String DB_NAME = "ActivityTracker.DB";
    private static final int DB_VERSION = 1;

    private static final String CREATE_TABLE = "create table " + TABLE_NAME
            + "(" + COW_ID + " INTEGER PRIMARY KEY, "
            + TYPE + " TEXT NOT NULL," + COLOR
            + " TEXT NOT NULL," + AGE + " TEXT NOT NULL," + MOTHER + " TEXT," + FATHER + " TEXT);";
    private static final String CREATE_TABLE_CHARTS = "create table " + CHARTS_TABLE_NAME
            + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COW_ID + " INTEGER NOT NULL,"
            + DATE + " TEXT NOT NULL,"
            + MILK + " TEXT NOT NULL,"
            + FAT + " TEXT NOT NULL,"
            + WEIGHT + " TEXT NOT NULL);";

    public static String getMOTHER() {
        return MOTHER;
    }

    public static String getFATHER() {
        return FATHER;
    }

    public static String getCowId() {
        return COW_ID;
    }

    public static String getTYPE() {
        return TYPE;
    }

    public static String getCOLOR() {
        return COLOR;
    }

    public static String getAGE() {
        return AGE;
    }

    public static String getDATE() {
        return DATE;
    }

    public static String getChartsTableName() {
        return CHARTS_TABLE_NAME;
    }

    public static String getID() {
        return ID;
    }

    public static String getMILK() {
        return MILK;
    }

    public static String getFAT() {
        return FAT;
    }

    public static String getWEIGHT() {
        return WEIGHT;
    }

    public static String getTableName() {
        return TABLE_NAME;
    }

    public DB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        db.execSQL(CREATE_TABLE_CHARTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CHARTS_TABLE_NAME);

        onCreate(db);
    }
}