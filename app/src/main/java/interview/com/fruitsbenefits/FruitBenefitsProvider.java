package interview.com.fruitsbenefits;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class FruitBenefitsProvider extends ContentProvider {
    public static final String AUTHORITY = "interview.com.fruitsbenefits";
    public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/fruits");
    public static final String FRUIT_TABLE = "FRUITLIST";
    public static final String ID = "_id";
    public static final String NAME = "_name";
    public static final String DESC = "_desc";
    public static final String TAG = "FruitBenefitsProvider";

    private DatabaseOpenHelper mDbHelper;

    public FruitBenefitsProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "Insert data into DB");
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Uri resUri = null;
        long rowId = -1;
        try {
            rowId = db.insertOrThrow(FRUIT_TABLE, null, values);
        } catch (SQLiteFullException sf) {
            Log.e(TAG, "Memory full"+sf.toString());
        } catch (Exception e) {
            Log.e(TAG, "Exception while inserting data into DB" +e.toString());
        }
        resUri = ContentUris.withAppendedId(uri, rowId);
        getContext().getContentResolver().notifyChange(CONTENT_URI, null);
        return resUri;
    }

    @Override
    public boolean onCreate() {
        boolean ret = true;
        try {
            Log.d(TAG, "Open db helper");
            mDbHelper = new DatabaseOpenHelper(getContext());
        } catch (Exception e) {
            ret = false;
        } finally {
            return ret;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query DB");
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(FRUIT_TABLE, projection, selection, selectionArgs, null,null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.d(TAG, "update DB");
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rows = db.update(FRUIT_TABLE, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return  rows;
    }

    private static class DatabaseOpenHelper extends SQLiteOpenHelper {
        private Context mContext;
        private static final String DBNAME = "fruitstorage.dat";
        private static final int DBVERSION = 1;

        public DatabaseOpenHelper(Context context) {
            super(context, DBNAME, null, DBVERSION);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            Log.d(TAG,"create db");
            try {
                sqLiteDatabase.execSQL("create table if not exists "+ FRUIT_TABLE + "("
                        + ID + " integer primary key,"
                        + NAME + " text,"
                        + DESC + " text" + ")"
                );
            } catch (SQLException e) {
                Log.e(TAG, "Exception while creating DB "+e.toString());
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }
    }
}
