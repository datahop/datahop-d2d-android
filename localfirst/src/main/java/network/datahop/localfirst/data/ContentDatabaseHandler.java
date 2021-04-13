/*******************************************************
 * Copyright (C) 2020 DataHop Labs Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/

package network.datahop.localfirst.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class ContentDatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "contentManager";

    // table name
    private static final String TABLE_CONTENT = "content";
    private static final String TABLE_GROUPS = "groups";


    // Contacts Table Columns names
    private static final String KEY_GROUP_ID = "group_id";
    private static final String KEY_GROUP = "group_name";
    private static final String KEY_GROUP_DESC = "group_desc";
    private static final String KEY_GROUP_IMG = "group_img";
    private static final String KEY_GROUP_TIME = "group_time_created";
    private static final String KEY_GROUP_PENDING = "group_pending";


    private static final String KEY_NAME = "name";
    private static final String KEY_DESC = "desc";
    private static final String KEY_URL = "url";
    private static final String KEY_DOWN = "downloaded";
    private static final String KEY_JOIN = "joined";
    private static final String KEY_CHUNK_ID= "chunk_id";
    private static final String KEY_CHUNKS_TOTAL = "chunk_total";
    private static final String KEY_CRC = "CRC";
    private static final String KEY_URI = "uri";

    public ContentDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //Log.d("DB","ContentDatabaseHandler");
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        //Log.d("DB","onCreate");
        String CREATE_CONTENT_TABLE = "CREATE TABLE " + TABLE_CONTENT + "("
                + KEY_GROUP + " TEXT NOT NULL,"
                + KEY_URI + " TEXT NOT NULL,"
                + KEY_NAME + " TEXT NOT NULL,"
                + KEY_DESC + " TEXT,"
                + KEY_URL + " TEXT NOT NULL, "
                + KEY_CHUNK_ID + " INT NOT NULL,"
                + KEY_CHUNKS_TOTAL + " INT NOT NULL, "
                + KEY_CRC + " TEXT ,"
                + KEY_JOIN + " INT NOT NULL,"
                + KEY_DOWN + " INT NOT NULL,  PRIMARY KEY ("+KEY_URI+", "+KEY_CHUNK_ID+", "+KEY_GROUP+"))";

        String CREATE_GROUPS_TABLE = "CREATE TABLE " + TABLE_GROUPS + "("
                + KEY_GROUP_ID + " INTEGER PRIMARY KEY  AUTOINCREMENT,"
                + KEY_GROUP + " TEXT NOT NULL,"
                + KEY_GROUP_PENDING + " INT NOT NULL,"
                + KEY_GROUP_TIME + " BIGINT NOT NULL,"
                + KEY_GROUP_DESC + " TEXT,"
                + KEY_GROUP_IMG + " TEXT)";
        db.execSQL(CREATE_CONTENT_TABLE);
        db.execSQL(CREATE_GROUPS_TABLE);

    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new contact
    public synchronized boolean addContent(Content content) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT COUNT(*) FROM " + TABLE_CONTENT + " WHERE "+KEY_NAME+"='" +content.getName()+"' AND "+KEY_CHUNK_ID+"="+content.getId()+" AND "+KEY_GROUP+"='"+content.getGroup()+"'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        int val=0;
        if (cursor.moveToFirst()) {
            do {
                val=cursor.getInt(0);

            } while (cursor.moveToNext());
        }
        if(val>0) return false;
        ContentValues values = new ContentValues();
        values.put(KEY_GROUP, content.getGroup());
        values.put(KEY_URI, content.getUri());
        values.put(KEY_NAME, content.getName());
        values.put(KEY_DESC, content.getText());
        values.put(KEY_URL, content.getUrl());
        values.put(KEY_CHUNK_ID,content.getId());
        values.put(KEY_CHUNKS_TOTAL,content.getTotal());
        values.put(KEY_CRC,content.getCrc());
        values.put(KEY_JOIN, 0);
        values.put(KEY_DOWN, 0);
        // Inserting Row
        db.insert(TABLE_CONTENT, null, values);
        db.close(); // Closing database connection

        return true;
    }

    public void rmContent(String uri,String group) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTENT, KEY_URI + " = ? AND " + KEY_GROUP + "= ?",
                new String[] {uri,group});

        db.close();
    }

    public void rmGroup(String group) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GROUPS, KEY_GROUP + " = ?",
                new String[] {group});

        db.close();
    }


    public List<Content> getContent(String uri){
        List<Content> contentList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT + " WHERE "+KEY_DOWN+"=1 AND "+KEY_NAME+"='"+uri+"'";
        //Log.d("DB","get content ");

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Content content = new Content();
                // content.setID(Integer.parseInt(cursor.getString(0)));
                content.setGroup(cursor.getString(0));
                content.setUri(cursor.getString(1));
                content.setName(cursor.getString(2));
                content.setText(cursor.getString(3));
                content.setUrl(cursor.getString(4));
                content.setId(Integer.parseInt(cursor.getString(5)));
                content.setTotal(Integer.parseInt(cursor.getString(6)));
                content.setCrc(cursor.getString(7));
                //Log.d("DB","get content "+ content.getName()+" "+content.getId());

                contentList.add(content);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close(); // Closing database connection
        // return contact list
        return contentList;
    }


    public synchronized int setContentDownloaded(String uri, String group, int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_DOWN, 1);

        // updating row
        return db.update(TABLE_CONTENT, values, KEY_URI + " = ? AND " + KEY_CHUNK_ID + "= ? AND " + KEY_GROUP + "= ?",
                new String[] {uri,String.valueOf(id),group});
    }

    public synchronized int setContentJoined(String uri, String group)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_JOIN, 1);

        // updating row
        return db.update(TABLE_CONTENT, values, KEY_URI + " = ? AND " + KEY_GROUP + "= ?",
                new String[] {uri,group});
    }

    public boolean isContentJoined(String name, String group){
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT + " WHERE "+KEY_DOWN+"=1 AND "+KEY_GROUP+"='"+group+"' AND "+KEY_NAME+"='"+name+"'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            int joined = cursor.getInt(8);
            db.close();
            if(joined==1)return true;
            else return false;
        }
        db.close();
        return false;
    }


    public List<Group> getGroups()
    {
        //Log.d("DB","get group");
        List<Group> groupList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_GROUPS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
               // Log.d("DB","get group2");
                Group group = new Group();
                group.setGroupId(cursor.getInt(0));
                // content.setID(Integer.parseInt(cursor.getString(0)));
                group.setName(cursor.getString(1));
                group.setPending(cursor.getInt(2));
                group.setIconURL(cursor.getString(4));
                group.setTimestamp(cursor.getLong(5));
                groupList.add(group);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close(); // Closing database connection
        // return contact list
        return groupList;
    }

    // Adding new contact
    public synchronized boolean addGroup(Group group) {
        SQLiteDatabase db = this.getWritableDatabase();
        //Log.d("DB","add group");

        String selectQuery = "SELECT COUNT(*) FROM " + TABLE_GROUPS + " WHERE "+KEY_GROUP_ID+"='" +group.getGroupId()+"'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        int val=0;
        if (cursor.moveToFirst()) {
            do {
                val=cursor.getInt(0);
                //Log.d("DB","add group2 "+val);
            } while (cursor.moveToNext());
        }
        if(val>0) return false;
        ContentValues values = new ContentValues();
        //values.put(KEY_GROUP_ID, group.getGroupId());
        values.put(KEY_GROUP, group.getName());
        values.put(KEY_GROUP_DESC,"");
        values.put(KEY_GROUP_PENDING,0);
        values.put(KEY_GROUP_IMG, group.getIconURL());
        // Inserting Row
        values.put(KEY_GROUP_TIME,group.getCreatedOn());
        db.insert(TABLE_GROUPS, null, values);
        db.close(); // Closing database connection

        return true;
    }

    public String getGroupName(int id)
    {
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_GROUPS + " WHERE "+KEY_GROUP_ID+"="+id;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                return cursor.getString(1);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close(); // Closing database connection
        // return contact list
        return "";
    }

    public Group getGroup(String name)
    {
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_GROUPS + " WHERE "+KEY_GROUP+"='"+name+"'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        Group group = new Group();

        if (cursor.moveToFirst()) {
            do {
                // Log.d("DB","get group2");
                group.setGroupId(cursor.getInt(0));
                // content.setID(Integer.parseInt(cursor.getString(0)));
                group.setName(cursor.getString(1));
                group.setPending(cursor.getInt(2));
                group.setIconURL(cursor.getString(4));
                group.setTimestamp(cursor.getLong(5));
                //groupList.add(group);
            } while (cursor.moveToNext());
            cursor.close();
            db.close(); // Closing database connection
            // return contact list
            return group;
        } else {
            cursor.close();
            db.close(); // Closing database connection
            return null;
        }

    }


    public int getGroupPending(String name)
    {
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_GROUPS + " WHERE "+KEY_GROUP+"='"+name+"'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                return cursor.getInt(2);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close(); // Closing database connection
        // return contact list
        return 0;
    }

    public synchronized int groupIncreasePending(String name)
    {
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_GROUPS + " WHERE "+KEY_GROUP+"='"+name+"'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        int pending=0;
        if (cursor.moveToFirst()) {
            do {
                pending =  cursor.getInt(2);
            } while (cursor.moveToNext());
        }
        cursor.close();
        pending++;

        ContentValues values = new ContentValues();

        values.put(KEY_GROUP_PENDING, pending);

        // updating row
        return db.update(TABLE_GROUPS, values, KEY_GROUP + " = ?",
                new String[] {name});
        // return contact list
    }

    public synchronized int  groupClearPending(String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_GROUP_PENDING, 0);

        // updating row
        return db.update(TABLE_GROUPS, values, KEY_GROUP + " = ?",
                new String[] {name});
    }

    public int getGroupMaxId()
    {
        // Select All Query
        String selectQuery = "SELECT MAX("+KEY_GROUP_ID+") FROM " + TABLE_GROUPS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                return cursor.getInt(0);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close(); // Closing database connection
        // return contact list
        return -1;
    }

    public boolean checkContent(String name, int id){
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT + " WHERE "/*+KEY_DOWN+"=1 AND "*/+KEY_CHUNK_ID+"="+id+" AND "+KEY_NAME+"='"+name+"'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            db.close();
            return true;
        }
        db.close();
        return false;
    }

    public List<Content> getPendingContent(String group)
    {
        List<Content> contentList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT + " WHERE "+KEY_DOWN+"=0 AND "+KEY_CHUNK_ID+"=0 AND "+KEY_GROUP+"='"+group+"'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Content content = new Content();
                content.setGroup(cursor.getString(0));
                // content.setID(Integer.parseInt(cursor.getString(0)));
                content.setUri(cursor.getString(1));
                content.setName(cursor.getString(2));
                content.setText(cursor.getString(3));
                content.setUrl(cursor.getString(4));
                content.setId(Integer.parseInt(cursor.getString(5)));
                content.setTotal(Integer.parseInt(cursor.getString(6)));
                content.setCrc(cursor.getString(7));
                contentList.add(content);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close(); // Closing database connection
        // return contact list
        return contentList;
    }

    public List<Content> getContentDownloaded(String group)
    {
        List<Content> contentList = new ArrayList<Content>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT + " WHERE "+KEY_DOWN+"=1 AND "+KEY_GROUP+"='"+group+"' AND "+KEY_CHUNK_ID+"!=0";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Content content = new Content();
                content.setGroup(cursor.getString(0));
                content.setUri(cursor.getString(1));
                content.setName(cursor.getString(2));
                content.setText(cursor.getString(3));
                content.setUrl(cursor.getString(4));
                content.setId(Integer.parseInt(cursor.getString(5)));
                content.setTotal(Integer.parseInt(cursor.getString(6)));
                content.setCrc(cursor.getString(7));
                //Log.d("DB","get content "+ content.getName()+" "+content.getId());

                contentList.add(content);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close(); // Closing database connection
        // return contact list
        return contentList;
    }

    public List<Content> getContentDownloaded()
    {
        List<Content> contentList = new ArrayList<Content>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT + " WHERE "+KEY_DOWN+"=1 AND "+KEY_CHUNK_ID+"!=0";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Content content = new Content();
                content.setGroup(cursor.getString(0));
                content.setUri(cursor.getString(1));
                content.setName(cursor.getString(2));
                content.setText(cursor.getString(3));
                content.setUrl(cursor.getString(4));
                content.setId(Integer.parseInt(cursor.getString(5)));
                content.setTotal(Integer.parseInt(cursor.getString(6)));
                content.setCrc(cursor.getString(7));
                contentList.add(content);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close(); // Closing database connection
        // return contact list
        return contentList;
    }

    /*public List<Content> getContentDownloaded()
    {
        List<Content> contentList = new ArrayList<Content>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT + " WHERE "+KEY_DOWN+"=1 AND "+KEY_CHUNK_ID+"!=0";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Content content = new Content();
                // content.setID(Integer.parseInt(cursor.getString(0)));
                content.setUri(cursor.getString(0));
                content.setName(cursor.getString(1));
                content.setText(cursor.getString(2));
                content.setUrl(cursor.getString(3));
                content.setId(Integer.parseInt(cursor.getString(4)));
                content.setTotal(Integer.parseInt(cursor.getString(5)));
                content.setCrc(cursor.getString(6));
                contentList.add(content);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close(); // Closing database connection
        // return contact list
        return contentList;
    }

    public List<Content> getMainContentDownloaded()
    {
        List<Content> contentList = new ArrayList<Content>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT + " WHERE "+KEY_DOWN+"=1 AND "+KEY_CHUNK_ID+"=0";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                //G.Log("Content "+cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2)+" "+cursor.getString(3)+" "+cursor.getString(4)+" "+cursor.getString(5));
                Content content = new Content();
                content.setUri(cursor.getString(0));
                content.setName(cursor.getString(1));
                content.setText(cursor.getString(2));
                content.setUrl(cursor.getString(3));
                content.setId(Integer.parseInt(cursor.getString(4)));
                content.setTotal(Integer.parseInt(cursor.getString(5)));
                content.setCrc(cursor.getString(6));
                contentList.add(content);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close(); // Closing database connection
        // return contact list
        return contentList;
    }*/

    public int getPendingCount(String group)
    {
        // Select All Query
        String countQuery = "SELECT  * FROM " + TABLE_CONTENT + " WHERE "+KEY_DOWN+"=0 AND "+KEY_GROUP+"='"+group+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int value = cursor.getCount();
        cursor.close();
        db.close(); // Closing database connection

        // return count
        return value;
    }

    // Getting All content
    public List<Content> getGroupContent(String group) {
        List<Content> contentList = new ArrayList<Content>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT+ " WHERE "+KEY_GROUP+"='"+group+"'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Content content = new Content();
                content.setGroup(cursor.getString(0));
                content.setUri(cursor.getString(1));
                content.setName(cursor.getString(2));
                content.setText(cursor.getString(3));
                content.setUrl(cursor.getString(4));
                content.setId(Integer.parseInt(cursor.getString(5)));
                content.setTotal(Integer.parseInt(cursor.getString(6)));
                content.setCrc(cursor.getString(7));
                // Adding content to list
                contentList.add(content);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close(); // Closing database connection
        // return contact list
        return contentList;
    }


    // Getting All Contacts
    public List<Content> getContent(String name, String group) {
        List<Content> contentList = new ArrayList<Content>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT+ " WHERE "+KEY_NAME+"='"+name+"' AND "+KEY_GROUP+"='"+group+"'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Content content = new Content();
                content.setGroup(cursor.getString(0));
                content.setUri(cursor.getString(1));
                content.setName(cursor.getString(2));
                content.setText(cursor.getString(3));
                content.setUrl(cursor.getString(4));
                content.setId(Integer.parseInt(cursor.getString(5)));
                content.setTotal(Integer.parseInt(cursor.getString(6)));
                content.setCrc(cursor.getString(7));
                // Adding content to list
                contentList.add(content);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close(); // Closing database connection
        return contentList;
    }

    public int getReceived(String name,String group)
    {
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT+ " WHERE "+KEY_NAME+"='"+name+"' AND "+KEY_GROUP+"='"+group+"' AND "+KEY_DOWN+"=1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        /*if (cursor.moveToFirst()) {
            do {
            Log.d("DB","Result "+cursor.getString(0)+" "+cursor.getString(4));
            } while (cursor.moveToNext());
        }*/
        int value = cursor.getCount();
        cursor.close();
        db.close(); // Closing database connection

        return value;
        // ret
    }

    // Getting contacts Count
    public int getContentCount(String group) {
        String countQuery = "SELECT  * FROM " + TABLE_CONTENT +" WHERE "+KEY_GROUP+"='"+group+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int value = cursor.getCount();
        cursor.close();
        db.close(); // Closing database connection

        // return count
        return value;
    }

}
