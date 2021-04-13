/*******************************************************
 * Copyright (C) 2020 DataHop Labs Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/

package network.datahop.localfirst.net;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StatsHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "DataHopStats";

    // Contacts table name
    private static final String TABLE_CONTENT = "stats";

    // Contacts Table Columns names
    private static final String m_user = "user";
    private static final String m_userId = "userId";
    private static final String m_btStatus = "btstatus";
    private static final String m_btConnections = "btconnections";
    private static final String m_wStatus = "status";
    private static final String m_hsSSID = "ssid";
    private static final String m_hsClients = "clients";
    private static final String m_connections = "connections";
    private static final String m_failed = "failed";
    private static final String m_transferred = "transferred";

    public StatsHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTENT + "("
                + "id" + " INT PRIMARY KEY NOT NULL, "
                + m_btStatus + " TEXT,"
                + m_btConnections + " INT,"
                + m_wStatus + " TEXT,"
                + m_hsSSID + " TEXT, "
                + m_hsClients + " INT,"
                + m_connections + " INT, "
                + m_failed + " TEXT,"
                + m_user +" TEXT,"
                + m_userId +" TEXT,"
                + m_transferred+ " INT)";
        db.execSQL(CREATE_CONTACTS_TABLE);
        ContentValues values = new ContentValues();
        values.put("id", "1");
        values.put(m_user,"");
        db.insert(TABLE_CONTENT, null, values);

    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTENT);
        // Create tables again
        onCreate(db);
    }

    public int reset()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(m_btStatus, "");
        values.put(m_btConnections, 0);
        values.put(m_wStatus, "");
        values.put(m_hsSSID, "");
        values.put(m_hsClients, 0);
        values.put(m_connections, 0);
        values.put(m_failed, "");
        values.put(m_user, "");
        values.put(m_userId,"");
        values.put(m_transferred,0);

        if(db!=null)return  db.update(TABLE_CONTENT, values, "id=1",null);
        else return -1;
        // updating row
        //return result;
    }

    public String getBtStatus(){

        String m_btStatus="";
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT + " WHERE id=1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;
        if(db!=null){
            cursor = db.rawQuery(selectQuery, null);
            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    m_btStatus = cursor.getString(1);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }


        db.close(); // Closing database connection
        // return contact list
        return m_btStatus;
    }


    public int setBtStatus(String btStatus)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(m_btStatus, btStatus);
        if(db!=null)return db.update(TABLE_CONTENT, values, "id=1",null);
        else return -1;

    }

    public synchronized int getBtConnections(){

        int conn=0;
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT + " WHERE id=1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;
        if(db!=null) {
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    conn = cursor.getInt(2);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close(); // Closing database connection
        // return contact list
        return conn;
    }


    public int setBtConnections(int btConnections)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(m_btConnections, btConnections);
        if(db!=null)return db.update(TABLE_CONTENT, values, "id=1",null);
        else return -1;
    }

    public int getTransferred(){

        int conn=0;
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT + " WHERE id=1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;
        if(db!=null) {
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    conn = cursor.getInt(10);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close(); // Closing database connection
        // return contact list
        return conn;
    }


    public int setTransferred(int transferred)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(m_transferred, transferred);
        if(db!=null)return db.update(TABLE_CONTENT, values, "id=1",null);
        else return -1;
    }



    public String getWStatus(){
        String m_wtStatus="";
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT + " WHERE id=1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;
        if(db!=null) {
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    m_wtStatus = cursor.getString(3);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close(); // Closing database connection
        // return contact list
        return m_wtStatus;
    }

    public int setWStatus(String status)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(m_wStatus, status);
        if(db!=null)return db.update(TABLE_CONTENT, values, "id=1",null);
        else return -1;

    }


    public String getHsSSID(){
        String m_HsSSID="";
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT + " WHERE id=1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;
        if(db!=null) {
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    m_HsSSID = cursor.getString(4);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close(); // Closing database connection
        // return contact list
        return m_HsSSID;
    }

    public int setHsSSID(String SSID)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(m_hsSSID, SSID);
        if(db!=null)return db.update(TABLE_CONTENT, values, "id=1",null);
        else return -1;

    }


    public int getHsClients(){
        int conn=0;
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT + " WHERE id=1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                conn = cursor.getInt(5);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close(); // Closing database connection
        // return contact list
        return conn;
    }

    public int setHsClients(int clients)
    {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(m_hsClients, clients);
        if(db!=null)return db.update(TABLE_CONTENT, values, "id=1",null);
        else return -1;

    }

    public int getConnections(){
        int conn=0;
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT + " WHERE id=1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;
        if(db!=null) {
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    conn = cursor.getInt(6);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        //db.close(); // Closing database connection
        // return contact list
        return conn;
    }

    public int setConnections(int connections)
    {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(m_connections, connections);
        if(db!=null)return db.update(TABLE_CONTENT, values, "id=1",null);
        else return -1;
    }

    public int getConnectionsFailed(){
        int conn=0;
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT + " WHERE id=1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;
        if(db!=null) {
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    conn = cursor.getInt(7);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        //
        // return contact list
        return conn;
    }

    public int setConnectionsFailed(int failed)
    {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(m_failed, failed);
        if(db!=null)return db.update(TABLE_CONTENT, values, "id=1",null);
        else return -1;

    }

    public String getUserName(){
        String m_user="";
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT + " WHERE id=1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;

        if(db!=null) {
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    m_user = cursor.getString(8);

                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close(); // Closing database connection
        // return contact list
       // Log.d("STATS",m_user);
        return m_user;
    }

    public int setUserName(String username)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(m_user, username);
        if(db!=null)return db.update(TABLE_CONTENT, values, "id=1",null);
        else return -1;

    }

    public String getUserId(){
        String m_userId="";
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT + " WHERE id=1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;

        if(db!=null) {
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    m_userId = cursor.getString(9);

                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close(); // Closing database connection
        // return contact list
        // Log.d("STATS",m_user);
        return m_userId;
    }

    public int setUserId(String userId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(m_userId, userId);
        if(db!=null)return db.update(TABLE_CONTENT, values, "id=1",null);
        else return -1;

    }


    public void close()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.close();
    }





}
