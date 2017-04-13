package com.aware.plugin.sensory_wristband;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.aware.Aware;
import com.aware.utils.DatabaseHelper;

import java.util.HashMap;

public class Provider extends ContentProvider {

    public static String AUTHORITY = "com.aware.plugin.sensory_wristband.provider.sensory_wristband";
    public static final int DATABASE_VERSION = 3;

    public static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String DATABASE_NAME = "plugin_sensory_wristband.db"; //the database filename, use plugin_xxx for plugins.

    //Add here your database table names, as many as you need
    public static final String DB_TBL_HEART_RATE = "table_heart_rate";
    public static final String DB_TBL_STEPS = "table_steps";

    //For each table, add two indexes: DIR and ITEM. The index needs to always increment. Next one is 3, and so on.
    private static final int TABLE_HEART_RATE_DIR = 1;
    private static final int TABLE_HEART_RATE_ITEM = 2;
    private static final int TABLE_STEPS_DIR = 3;
    private static final int TABLE_STEPS_ITEM = 4;

    //Put tables names in this array so AWARE knows what you have on the database
    public static final String[] DATABASE_TABLES = {
        DB_TBL_HEART_RATE,
        DB_TBL_STEPS
    };

    public interface AWAREColumns extends BaseColumns {
        String _ID = "_id";
        String TIMESTAMP = "timestamp";
        String DEVICE_ID = "device_id";
    }

    /**
     * Heart Rate Table definition
     */
    public static final class TableHeartRate_Data implements AWAREColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DB_TBL_HEART_RATE);
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.aware.plugin.sensory_wristband.provider.table_heart_rate";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.aware.plugin.sensory_wristband.provider.table_heart_rate";

        public static final String HEART_RATE = "heart_rate";
    }

    /**
     * Heart Rate Table fields
     */
    private static final String DB_TBL_HEART_RATE_FIELDS =
            TableHeartRate_Data._ID + " integer primary key autoincrement," +
            TableHeartRate_Data.TIMESTAMP + " real default 0," +
            TableHeartRate_Data.DEVICE_ID + " text default ''," +
            TableHeartRate_Data.HEART_RATE + " integer default 0";

    /**
     * Steps Table definition
     */
    public static final class TableSteps_Data implements AWAREColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DB_TBL_STEPS);
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.aware.plugin.sensory_wristband.provider.table_steps";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.aware.plugin.sensory_wristband.provider.table_steps";

        public static final String STEPS = "steps";
        public static final String DISTANCE = "distance";
        public static final String CALORIES = "calories";
    }

    /**
     * Steps Table fields
     */
    private static final String DB_TBL_STEPS_FIELDS =
            TableSteps_Data._ID + " integer primary key autoincrement," +
            TableSteps_Data.TIMESTAMP + " real default  0," +
            TableSteps_Data.DEVICE_ID + " text default ''," +
            TableSteps_Data.STEPS + " integer default 0," +
            TableSteps_Data.DISTANCE + " integer default 0," +
            TableSteps_Data.CALORIES + " integer default 0";

    /**
     * Share the fields with AWARE so we can replicate the table schema on the server
     */
    public static final String[] TABLES_FIELDS = {
            DB_TBL_HEART_RATE_FIELDS,
            DB_TBL_STEPS_FIELDS
    };

    //Helper variables for ContentProvider - don't change me
    private static UriMatcher sUriMatcher;
    private static DatabaseHelper dbHelper;
    private static SQLiteDatabase database;
    private void initialiseDatabase() {
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS);
        }
        if (database == null) {
            database = dbHelper.getWritableDatabase();
        }
    }

    //For each table, create a hashMap needed for database queries
    private static HashMap<String, String> tableHeartRateHashMap;
    private static HashMap<String, String> tableStepsHashMap;

    private static final String TAG = "DB: Sensory Wristband";

    @Override
    public boolean onCreate() {
        //This is a hack to allow providers to be reusable in any application/plugin by making the authority dynamic using the package name of the parent app
        AUTHORITY = getContext().getPackageName() + ".provider.sensory_wristband"; //make sure xxx matches the first string in this class

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], TABLE_HEART_RATE_DIR);
        sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0] + "/#", TABLE_HEART_RATE_ITEM);
        sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[1], TABLE_STEPS_DIR);
        sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[1] + "/#", TABLE_STEPS_ITEM);

        tableHeartRateHashMap = new HashMap<>();
        tableHeartRateHashMap.put(TableHeartRate_Data._ID, TableHeartRate_Data._ID);
        tableHeartRateHashMap.put(TableHeartRate_Data.TIMESTAMP, TableHeartRate_Data.TIMESTAMP);
        tableHeartRateHashMap.put(TableHeartRate_Data.DEVICE_ID, TableHeartRate_Data.DEVICE_ID);
        tableHeartRateHashMap.put(TableHeartRate_Data.HEART_RATE, TableHeartRate_Data.HEART_RATE);

        tableStepsHashMap = new HashMap<>();
        tableStepsHashMap.put(TableSteps_Data._ID, TableSteps_Data._ID);
        tableStepsHashMap.put(TableSteps_Data.TIMESTAMP, TableSteps_Data.TIMESTAMP);
        tableStepsHashMap.put(TableSteps_Data.DEVICE_ID, TableSteps_Data.DEVICE_ID);
        tableStepsHashMap.put(TableSteps_Data.STEPS, TableSteps_Data.STEPS);
        tableStepsHashMap.put(TableSteps_Data.DISTANCE, TableSteps_Data.DISTANCE);
        tableStepsHashMap.put(TableSteps_Data.CALORIES, TableSteps_Data.CALORIES);

        return true;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        initialiseDatabase();
        database.beginTransaction();
        int count;
        switch (sUriMatcher.match(uri)) {
            case TABLE_HEART_RATE_DIR:
                count = database.delete(DATABASE_TABLES[0], selection, selectionArgs);
                break;
            case TABLE_STEPS_DIR:
                count = database.delete(DATABASE_TABLES[1], selection, selectionArgs);
                break;
            default:
                database.endTransaction();
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues initialValues) {
        initialiseDatabase();
        ContentValues values = (initialValues != null) ? new ContentValues(initialValues) : new ContentValues();
        database.beginTransaction();
        long _id;
        switch (sUriMatcher.match(uri)) {
            case TABLE_HEART_RATE_DIR:
                _id = database.insert(DATABASE_TABLES[0], TableHeartRate_Data.DEVICE_ID, values);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (_id > 0) {
                    Uri dataUri = ContentUris.withAppendedId(TableHeartRate_Data.CONTENT_URI, _id);
                    getContext().getContentResolver().notifyChange(dataUri, null);
                    return dataUri;
                }
                database.endTransaction();
                throw new SQLException("Failed to insert row into " + uri);
            case TABLE_STEPS_DIR:
                _id = database.insert(DATABASE_TABLES[1], TableSteps_Data.DEVICE_ID, values);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (_id > 0) {
                    Uri dataUri = ContentUris.withAppendedId(TableSteps_Data.CONTENT_URI, _id);
                    getContext().getContentResolver().notifyChange(dataUri, null);
                    return dataUri;
                }
                database.endTransaction();
                throw new SQLException("Failed to insert row into " + uri);
            default:
                database.endTransaction();
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        initialiseDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
            case TABLE_HEART_RATE_DIR:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(tableHeartRateHashMap);
                break;
            case TABLE_STEPS_DIR:
                qb.setTables(DATABASE_TABLES[1]);
                qb.setProjectionMap(tableStepsHashMap);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Cursor c = qb.query(database, projection, selection, selectionArgs,
                    null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (IllegalStateException e) {
            if (Aware.DEBUG)
                Log.e(Aware.TAG, e.getMessage());
            return null;
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case TABLE_HEART_RATE_DIR:
                return TableHeartRate_Data.CONTENT_TYPE;
            case TABLE_HEART_RATE_ITEM:
                return TableHeartRate_Data.CONTENT_ITEM_TYPE;
            case TABLE_STEPS_DIR:
                return TableSteps_Data.CONTENT_TYPE;
            case TABLE_STEPS_ITEM:
                return TableSteps_Data.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        initialiseDatabase();
        database.beginTransaction();
        int count;
        switch (sUriMatcher.match(uri)) {
            case TABLE_HEART_RATE_DIR:
                count = database.update(DATABASE_TABLES[0], values, selection, selectionArgs);
                break;
            case TABLE_STEPS_DIR:
                count = database.update(DATABASE_TABLES[1], values, selection, selectionArgs);
                break;
            default:
                database.endTransaction();
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
