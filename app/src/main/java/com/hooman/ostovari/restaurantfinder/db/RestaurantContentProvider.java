package com.hooman.ostovari.restaurantfinder.db;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.hooman.ostovari.restaurantfinder.db.tables.RestaurantTable;
import com.hooman.ostovari.restaurantfinder.utils.Logger;

/**
 * Created by hoomi on 28/01/2014.
 */
public class RestaurantContentProvider extends ContentProvider {

    public final static String AUTHORITY = "com.hooman.ostovari.restaurantfinder";
    private MySQLiteHelper mySQLiteHelper;

    static final UriMatcher URI_MATCHER = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, RestaurantTable.NAME, RestaurantTable.PATH_TOKEN);
        matcher.addURI(AUTHORITY, RestaurantTable.PATH_FOR_ID, RestaurantTable.PATH_FOR_ID_TOKEN);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mySQLiteHelper = new MySQLiteHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mySQLiteHelper.getReadableDatabase();
        if (db == null) {
            return null;
        }
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        Cursor cursor;
        int match = URI_MATCHER.match(uri);
        if (match != 1 && match != 100) {
            return null;
        }
        String name = RestaurantTable.NAME;
        String groupBy = null;
        String limit = null;
        builder.setTables(name);
        if (match >= 100) {
            builder.appendWhere(BaseColumns._ID + "=" + uri.getLastPathSegment());
        }

        cursor = builder.query(db, projection, selection, selectionArgs, groupBy, null, sortOrder, limit);
        Logger.d(this, "Function name: query/ selection: " + selection + "/ name: " + name);
        if (null != cursor) {
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
                Logger.d(this, "query: " + builder.buildQuery(projection, selection, groupBy, null, sortOrder, limit));
            }

            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = URI_MATCHER.match(uri);
        if (match != 1 && match != 100) {
            throw new UnsupportedOperationException("URI " + uri + " is not supported.");
        }
        String name = RestaurantTable.NAME;
        String basePath = "";

        if (match >= 100) {
            basePath = ContentResolver.CURSOR_ITEM_BASE_TYPE;
        } else {
            basePath = ContentResolver.CURSOR_DIR_BASE_TYPE;
        }
        Logger.d(this, "Function name: getType/ name: " + name);
        return basePath + "/" + AUTHORITY + "." + name;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mySQLiteHelper.getWritableDatabase();
        if (db == null) {
            return null;
        }
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        int match = URI_MATCHER.match(uri);
        if (match != 1 && match != 100) {
            return null;
        }
        long insertedId = db.insert(RestaurantTable.NAME, null, values);
        Logger.d(this, "Function name: insert/ name: " + RestaurantTable.NAME + " InsertedId: " + insertedId);

        return ContentUris.withAppendedId(RestaurantTable.CONTENT_URI, insertedId);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mySQLiteHelper.getWritableDatabase();
        if (db == null) {
            return 0;
        }
        int rowsDeleted;
        String whereClause = "";

        int match = URI_MATCHER.match(uri);
        if (match != 1 && match != 100) {
            throw new UnsupportedOperationException("URI " + uri + " is not supported.");
        }
        String name = RestaurantTable.NAME;
        // This condition is based on assumption that <ATable>.PATH_FOR_ID_TOKEN = <ATable>.PATH_TOKEN * Table.ITEM_PATH_MULTIPLIER
        if (match >= 100) {
            whereClause = appendId(uri, selection).toString();

        }
        if (!TextUtils.isEmpty(whereClause)) {
            rowsDeleted = db.delete(name, whereClause, selectionArgs);
        } else {
            rowsDeleted = db.delete(name, selection, selectionArgs);
        }
        Logger.d(this, "Function name: delete/ selection: " + selection + "/ whereClause: " + whereClause + "/ name: " + name + " rowsDeleted: " + rowsDeleted);

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mySQLiteHelper.getWritableDatabase();
        if (db == null) {
            return 0;
        }
        int rowsUpdated;
        String whereClause = "";

        int match = URI_MATCHER.match(uri);
        if (match != 1 && match != 100) {
            throw new UnsupportedOperationException("URI " + uri + " is not supported.");
        }
        String name = RestaurantTable.NAME;
        if (match >= 100) {
            whereClause = appendId(uri, selection).toString();
        }
        if (!TextUtils.isEmpty(whereClause)) {
            rowsUpdated = db.update(name, values, whereClause, selectionArgs);
        } else if (!TextUtils.isEmpty(selection)) {
            rowsUpdated = db.update(name, values, whereClause, selectionArgs);
        } else {
            rowsUpdated = 0;
        }
        Logger.d(this, "Function name: update/ selection: " + selection + "/ whereClause: " + whereClause + "/ name: " + name + " rowUpdated: " + rowsUpdated);
        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    public class MySQLiteHelper extends SQLiteOpenHelper {

        private static final String NAME = "restaurantfinder.db";
        private static final int VERSION = 1;

        public MySQLiteHelper(Context context) {
            super(context, NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(RestaurantTable.CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(RestaurantTable.DROP_TABLE);
            onCreate(db);
        }
    }

    private StringBuilder appendId(Uri uri, String selection) {
        String id = uri.getLastPathSegment();
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(id)) {
            sb.append(BaseColumns._ID).append("=").append(id);
            if (!TextUtils.isEmpty(selection)) {
                sb.append(" AND ");
            }
        }
        sb.append(selection);
        return sb;
    }
}
