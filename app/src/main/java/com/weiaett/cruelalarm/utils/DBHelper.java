package com.weiaett.cruelalarm.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.util.Log;

import com.weiaett.cruelalarm.models.Alarm;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Weiss_A on 26.09.2016.
 * SQLite helper
 */

public class DBHelper extends SQLiteOpenHelper {

    private class DatabaseException extends Exception {
        DatabaseException (String message) {
            super(message);
        }
    }

    private static DBHelper dbHelper;

    private static final String DATABASE_NAME = "cruel_alarm.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "DBHelper";

    public static synchronized DBHelper getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new DBHelper(context.getApplicationContext());
        }
        return dbHelper;
    }

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String ALARM_TABLE = "alarm";
    private static final String PHOTO_ALARM_TABLE = "photo_alarm";
    private static final String DAY_ALARM_TABLE = "day_alarm";

    private static final String COLUMN_ALARM_IS_ACTIVE = "is_active";
    private static final String COLUMN_ALARM_TIME = "time";
    private static final String COLUMN_ALARM_TONE = "tone";
    private static final String COLUMN_ALARM_TONE_URI = "tone_uri";
    private static final String COLUMN_ALARM_HAS_VIBRATION = "has_vibration";
    private static final String COLUMN_ALARM_DESCRIPTION = "description";
    private static final String COLUMN_ALARM_ID = "alarm_id";
    private static final String COLUMN_PHOTO_ID = "photo_id";
    private static final String COLUMN_DAY = "day";

    private static final String CREATE_ALARM_TABLE = "CREATE TABLE " + ALARM_TABLE +
            " ( " +
            COLUMN_ALARM_IS_ACTIVE + " INTEGER DEFAULT 1, " +
            COLUMN_ALARM_TIME + " VARCHAR(10) NOT NULL, " +
            COLUMN_ALARM_TONE + " VARCHAR(255) NOT NULL, " +
            COLUMN_ALARM_TONE_URI + " VARCHAR(255) NOT NULL, " +
            COLUMN_ALARM_HAS_VIBRATION + " INTEGER DEFAULT 0, " +
            COLUMN_ALARM_DESCRIPTION + " TEXT, " +
            COLUMN_ALARM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT " +
            ");";

    private static final String CREATE_PHOTO_ALARM_TABLE = "CREATE TABLE " + PHOTO_ALARM_TABLE +
            " ( " +
            COLUMN_ALARM_ID + " INTEGER, " +
            COLUMN_PHOTO_ID + " VARCHAR(255) NOT NULL, " +
            "PRIMARY KEY (" + COLUMN_ALARM_ID + ", " + COLUMN_PHOTO_ID +") " +
            ");";

    private static final String CREATE_DAY_ALARM_TABLE = " CREATE TABLE " + DAY_ALARM_TABLE +
            " ( " +
            COLUMN_ALARM_ID + " INTEGER, " +
            COLUMN_DAY + " INTEGER, " +
            "FOREIGN KEY (" + COLUMN_DAY + ") " +
            "REFERENCES " + ALARM_TABLE + "(" + COLUMN_ALARM_ID + ") " +
            "ON DELETE CASCADE, " +
            "PRIMARY KEY (" + COLUMN_ALARM_ID + ", " + COLUMN_DAY +") " +
            ");";

    private int getLastID(String table, String idColumn) {
        String MAX_ID_SELECT_QUERY =
                String.format("SELECT MAX(%1$s) FROM %2$s;",
                        idColumn, table);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cur = db.rawQuery(MAX_ID_SELECT_QUERY, null);
        cur.moveToFirst();
        int id = cur.getInt(0);
        cur.close();
        return id;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ALARM_TABLE);
        db.execSQL(CREATE_PHOTO_ALARM_TABLE);
        db.execSQL(CREATE_DAY_ALARM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ALARM_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PHOTO_ALARM_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DAY_ALARM_TABLE);
        onCreate(db);
    }

    public List<Alarm> getAllAlarms(Context context) {
        List<Alarm> alarms = new ArrayList<>();
        String ALARMS_SELECT_QUERY =
                String.format("SELECT * FROM %1$s ORDER BY %1$s.%2$s ASC;",
                        ALARM_TABLE, COLUMN_ALARM_TIME);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(ALARMS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Alarm newAlarm = new Alarm(context);
                    bindAlarm(newAlarm, cursor);
                    alarms.add(newAlarm);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return alarms;
    }

    public Alarm getAlarm(Context context, int id) {
        Alarm alarm = new Alarm(context);
        String ALARMS_SELECT_QUERY =
                String.format(Locale.US, "SELECT * FROM %s WHERE alarm_id = %d;",
                        ALARM_TABLE, id);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(ALARMS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                bindAlarm(alarm, cursor);
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return alarm;
    }

    private void bindAlarm(Alarm alarm, Cursor cursor) {
        alarm.setId(cursor.getInt(cursor.getColumnIndex(String.
                format("%1$s", COLUMN_ALARM_ID))));
        alarm.setTime(cursor.getString(cursor.getColumnIndex(String.
                format("%1$s", COLUMN_ALARM_TIME))));
        alarm.setIsActive(cursor.getInt(cursor.getColumnIndex(String.
                format("%1$s", COLUMN_ALARM_IS_ACTIVE))) > 0);
        alarm.setTone(cursor.getString(cursor.getColumnIndex(String.
                format("%1$s", COLUMN_ALARM_TONE))));
        alarm.setToneUri(Uri.parse(cursor.getString(cursor.getColumnIndex(String.
                format("%1$s", COLUMN_ALARM_TONE_URI)))));
        alarm.setHasVibration(cursor.getInt(cursor.getColumnIndex(String.
                format("%1$s", COLUMN_ALARM_HAS_VIBRATION))) > 0);
        alarm.setDescription(cursor.getString(cursor.getColumnIndex(String.
                format("%1$s", COLUMN_ALARM_DESCRIPTION))));
        bindAlarmDays(alarm);
        bindAlarmPhotos(alarm);
    }

    private void bindAlarmDays(Alarm alarm) {
        String DAYS_SELECT_QUERY =
                String.format("SELECT * FROM %1$s WHERE %2$s = %3$s ORDER BY %4$s ASC;",
                        DAY_ALARM_TABLE, COLUMN_ALARM_ID, alarm.getId(), COLUMN_DAY);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(DAYS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    alarm.addDay(Weekday.values()[cursor.getInt(cursor.getColumnIndex(String.
                            format("%1$s", COLUMN_DAY)))]);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    private void bindAlarmPhotos(Alarm alarm) {
        List<String> images = new ArrayList<>();
        String PHOTOS_SELECT_QUERY =
                String.format("SELECT * FROM %1$s WHERE %2$s = %3$s;",
                        PHOTO_ALARM_TABLE, COLUMN_ALARM_ID, alarm.getId());
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(PHOTOS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    images.add(cursor.getString(cursor.getColumnIndex(String.format("%1$s", COLUMN_PHOTO_ID))));
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        alarm.setImages(images);
    }

    public void addAlarm(Alarm alarm) {
        SQLiteDatabase db = getWritableDatabase();
        String sql = String.format("INSERT INTO %1$s VALUES (?,?,?,?,?,?,?);",  ALARM_TABLE);
        db.beginTransaction();
        SQLiteStatement statement = db.compileStatement(sql);
        statement.clearBindings();
        statement.bindLong(1, alarm.getIsActive() ? 1 : 0);
        statement.bindString(2, alarm.getTime());
        statement.bindString(3, alarm.getTone());
        statement.bindString(4, alarm.getToneUri().toString());
        statement.bindLong(5, alarm.getHasVibration() ? 1 : 0);
        statement.bindString(6, alarm.getDescription());
        statement.execute();
        db.setTransactionSuccessful();
        db.endTransaction();
        alarm.setId(getLastID(ALARM_TABLE, COLUMN_ALARM_ID));
    }

    public void updateAlarm(Alarm alarm) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ALARM_IS_ACTIVE, alarm.getIsActive() ? 1 : 0);
        cv.put(COLUMN_ALARM_TIME, alarm.getTime());
        cv.put(COLUMN_ALARM_TONE, alarm.getTone());
        cv.put(COLUMN_ALARM_TONE_URI, alarm.getToneUri().toString());
        cv.put(COLUMN_ALARM_HAS_VIBRATION, alarm.getHasVibration() ? 1 : 0);
        cv.put(COLUMN_ALARM_DESCRIPTION, alarm.getDescription());
        try {
            if (db.update(ALARM_TABLE, cv, COLUMN_ALARM_ID + "=" + alarm.getId(), null) != 1) {
                throw new DatabaseException(String.format("Not updated id = %s", alarm.getId()));
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        String activeDays = "";
        for (Weekday day: alarm.getDays()) {
            String sql = "INSERT OR REPLACE INTO " + DAY_ALARM_TABLE +" VALUES (?,?);";
            SQLiteStatement statement = db.compileStatement(sql);
            statement.clearBindings();
            statement.bindLong(1, alarm.getId());
            statement.bindLong(2, day.ordinal());
            statement.execute();
            activeDays = activeDays.concat(String.valueOf(day.ordinal())).concat(",");
        }
        String sql;
        if (activeDays.isEmpty()) {
            sql = String.format(Locale.US,
                    "DELETE FROM %1$s WHERE %2$s = %3$d;",
                    DAY_ALARM_TABLE, COLUMN_ALARM_ID, alarm.getId());
        } else {
            sql = String.format(Locale.US,
                    "DELETE FROM %1$s WHERE %2$s = %3$d AND %4$s NOT IN (%5$s);",
                    DAY_ALARM_TABLE, COLUMN_ALARM_ID, alarm.getId(), COLUMN_DAY,
                    activeDays.substring(0, activeDays.length() - 1));
        }
        SQLiteStatement statement = db.compileStatement(sql);
        statement.execute();
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void deleteAlarm(Alarm alarm) {
//        String ALARM_DELETE_QUERY =
//                String.format("DELETE FROM %1$s WHERE %2$s = %3$d;",
//                        ALARM_TABLE, COLUMN_ALARM_ID, alarm.getId());
        SQLiteDatabase db = getWritableDatabase();
        try {
            if (db.delete(ALARM_TABLE, COLUMN_ALARM_ID + "=" + alarm.getId(), null) != 1) {
                throw new DatabaseException(String.format("Not deleted id = %s", alarm.getId()));
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
//        db.rawQuery(ALARM_DELETE_QUERY, null);
    }

    public void setAlarmPhotos(int id, List<String> photos) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        String sqlDelete;
        sqlDelete = String.format(Locale.US,
                "DELETE FROM %1$s WHERE %2$s = %3$d;",
                PHOTO_ALARM_TABLE, COLUMN_ALARM_ID, id);
        SQLiteStatement statementDelete = db.compileStatement(sqlDelete);
        statementDelete.execute();

        for (String photo: photos) {
            String sql = "INSERT OR REPLACE INTO " + PHOTO_ALARM_TABLE +" VALUES (?,?);";
            SQLiteStatement statement = db.compileStatement(sql);
            statement.clearBindings();
            statement.bindLong(1, id);
            statement.bindString(2, photo);
            statement.execute();
        }

        db.setTransactionSuccessful();
        db.endTransaction();
    }
}
