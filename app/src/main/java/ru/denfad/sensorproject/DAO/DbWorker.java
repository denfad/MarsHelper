package ru.denfad.sensorproject.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.denfad.sensorproject.DAO.model.Zone;

class DbWorker extends DbStructure {

    private SQLiteDatabase mDataBase;

    public DbWorker(Context context) {
        OpenHelper mOpenHelper = new OpenHelper(context);
        mDataBase = mOpenHelper.getWritableDatabase();
    }

    public long insertZone(float x, float y, int temperature) {
        ContentValues cv = new ContentValues();
        cv.put(ZONE_X, x);
        cv.put(ZONE_Y, y);
        cv.put(ZONE_TEMPERATURE, temperature);
        return mDataBase.insert(TABLE_ZONES, null, cv);
    }

    public Zone selectZone(int id) {
        Cursor mCursor = mDataBase.query(TABLE_ZONES, null, ZONE_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);

        int zoneId = mCursor.getInt(0); //column id
        float x = mCursor.getFloat(1); //column x
        float y = mCursor.getFloat(2); //column y
        int temperature = mCursor.getInt(3); //column temperature

        return new Zone(zoneId, temperature, x, y);
    }

    public List<Zone> selectAllZones() {
        Cursor mCursor = mDataBase.query(TABLE_ZONES, null, null, null, null, null, null);

        List<Zone> arr = new ArrayList<>();
        mCursor.moveToFirst();

        if (!mCursor.isAfterLast()) {
            do {
                int zoneId = mCursor.getInt(0); //column id
                float x = mCursor.getFloat(1); //column x
                float y = mCursor.getFloat(2); //column y
                int temperature = mCursor.getInt(3); //column temperature
                arr.add(new Zone(zoneId, temperature, x, y));
            } while (mCursor.moveToNext());
        }
        return arr;
    }

    private class OpenHelper extends SQLiteOpenHelper {

        OpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            //CREATE TABLE zones;
            String query_students = "CREATE TABLE " + TABLE_ZONES + " (" +
                    ZONE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ZONE_X + " REAL, " +
                    ZONE_Y + " REAL, " +
                    ZONE_TEMPERATURE + " INETEGER);";
            Log.d("CREATE TABLE", query_students);


            sqLiteDatabase.execSQL(query_students);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ZONES);
            onCreate(sqLiteDatabase);
        }
    }
}
