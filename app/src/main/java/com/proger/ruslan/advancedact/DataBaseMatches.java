package com.proger.ruslan.advancedact;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DataBaseMatches {
    private static final String DATABASE_NAME = "measuring_results.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "space_results";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_AREA = "Area";
    private static final String COLUMN_PERIMETER = "Perimeter";
    private static final String COLUMN_DOTS = "Dots";
    private static final String COLUMN_DOTS_RELATIVES = "DotsRelatives";
    private static final String COLUMN_DATE = "Date";
    private static final String COLUMN_TYPE = "Type";
    private static final String COLUMN_NOTE = "Note";
    private static final String COLUMN_PLACE = "Place";
    private static final String COLUMN_PERCENT = "Percent";
    private static final int NUM_COLUMN_ID = 0;
    private static final int NUM_COLUMN_AREA = 1;
    private static final int NUM_COLUMN_PERIMETER = 2;
    private static final int NUM_COLUMN_DOTS = 3;
    private static final int NUM_COLUMN_DOTS_RELATIVES = 4;
    private static final int NUM_COLUMN_DATE = 5;
    private static final int NUM_COLUMN_TYPE = 6;
    private static final int NUM_COLUMN_NOTE = 7;
    private static final int NUM_COLUMN_PLACE = 8;
    private static final int NUM_COLUMN_PERCENT = 9;
    private SQLiteDatabase mDataBase;
    private Context context;

    public DataBaseMatches(Context context) {
        this.context = context;
        OpenHelper mOpenHelper = new OpenHelper(context);
        mDataBase = mOpenHelper.getWritableDatabase();
    }
    public long insert(DataMatches md) {
        ContentValues cv=new ContentValues();
        cv.put(COLUMN_AREA, md.getArea());
        cv.put(COLUMN_PERIMETER, md.getPerimeter());
        cv.put(COLUMN_DOTS, md.getDots());
        cv.put(COLUMN_DOTS_RELATIVES, md.getDotsRelatives());
        cv.put(COLUMN_DATE, md.getDate());
        cv.put(COLUMN_TYPE, md.getType());
        cv.put(COLUMN_NOTE, md.getNote());
        cv.put(COLUMN_PLACE, md.getPlace());
        cv.put(COLUMN_PERCENT, md.getPercent_of_quality());
        return mDataBase.insert(TABLE_NAME, null, cv);
    }
    public long update(DataMatches md) {
        ContentValues cv=new ContentValues();
        cv.put(COLUMN_AREA, md.getArea());
        cv.put(COLUMN_PERIMETER, md.getPerimeter());
        cv.put(COLUMN_DOTS, md.getDots());
        cv.put(COLUMN_DOTS_RELATIVES, md.getDotsRelatives());
        cv.put(COLUMN_DATE, md.getDate());
        cv.put(COLUMN_TYPE, md.getType());
        cv.put(COLUMN_NOTE, md.getNote());
        cv.put(COLUMN_PLACE, md.getPlace());
        cv.put(COLUMN_PERCENT, md.getPercent_of_quality());
        return mDataBase.update(TABLE_NAME, cv, COLUMN_ID + " = ?",new String[] {
                String.valueOf(md.getId())});
    }
    public void deleteAll() {
        mDataBase.delete(TABLE_NAME, null, null);
        OpenHelper mOpenHelper = new OpenHelper(context);
        mOpenHelper.onUpgrade(mDataBase, DATABASE_VERSION, DATABASE_VERSION);
    }
    public void delete(long id) {
        mDataBase.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[] {
                String.valueOf(id) });
    }
    public DataMatches select(long id) {
        Cursor mCursor = mDataBase.query(TABLE_NAME, null, COLUMN_ID + " = ?", new
                String[]{String.valueOf(id)}, null, null, null);
        mCursor.moveToFirst();
        double area = mCursor.getDouble(NUM_COLUMN_AREA);
        double perimeter = mCursor.getDouble(NUM_COLUMN_PERIMETER);
        int dots = mCursor.getInt(NUM_COLUMN_DOTS);
        String dotsRelatives = mCursor.getString (NUM_COLUMN_DOTS_RELATIVES);
        String date = mCursor.getString (NUM_COLUMN_DATE);
        String type = mCursor.getString (NUM_COLUMN_TYPE);
        String note = mCursor.getString (NUM_COLUMN_NOTE);
        String place = mCursor.getString (NUM_COLUMN_PLACE);
        double percent = mCursor.getDouble (NUM_COLUMN_PERCENT);
        return new DataMatches(id, area, perimeter, dots, dotsRelatives, date, type, note, place, percent);
    }
    public ArrayList<DataMatches> selectAll() {
        Cursor mCursor = mDataBase.query(TABLE_NAME, null, null, null, null, null,
                null);
        ArrayList<DataMatches> arr = new ArrayList<DataMatches>();
        mCursor.moveToFirst();
        if (!mCursor.isAfterLast()) {
            do {
                long id = mCursor.getLong(NUM_COLUMN_ID);
                double area = mCursor.getDouble(NUM_COLUMN_AREA);
                double perimeter = mCursor.getDouble(NUM_COLUMN_PERIMETER);
                int dots = mCursor.getInt(NUM_COLUMN_DOTS);
                String dotsRelatives = mCursor.getString (NUM_COLUMN_DOTS_RELATIVES);
                String date = mCursor.getString (NUM_COLUMN_DATE);
                String type = mCursor.getString (NUM_COLUMN_TYPE);
                String note = mCursor.getString (NUM_COLUMN_NOTE);
                String place = mCursor.getString (NUM_COLUMN_PLACE);
                double percent = mCursor.getDouble (NUM_COLUMN_PERCENT);
                arr.add(new DataMatches(id, area, perimeter, dots, dotsRelatives, date, type, note, place, percent));
            } while (mCursor.moveToNext());
        }
        return arr;
    }
    private class OpenHelper extends SQLiteOpenHelper {
        OpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String query = "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_AREA+ " REAL, " +
                    COLUMN_PERIMETER + " REAL, " +
                    COLUMN_DOTS + " INT, " +
                    COLUMN_DOTS_RELATIVES + " TEXT, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_TYPE + " TEXT, " +
                    COLUMN_NOTE + " TEXT, " +
                    COLUMN_PLACE + " TEXT, " +
                    COLUMN_PERCENT + " INT);";
            db.execSQL(query);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
