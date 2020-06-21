package com.rdc.p2p.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class ChatDatabaseUtil extends SQLiteOpenHelper{
    //调用父类构造器
    public ChatDatabaseUtil(Context context, String name, CursorFactory factory,
                            int version) {
        super(context, name, factory, version);
    }

    /**
     * 当数据库首次创建时执行该方法，一般将创建表等初始化操作放在该方法中执行.
     * 重写onCreate方法，调用execSQL方法创建表
     * */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE chat (id INTEGER PRIMARY KEY,"+
                "user_ip varchar(64), "+
                "friend_ip varchar(64), "+
                "content varchar(255)," +
                "type INTEGER, "+
                "timedate varchar(255));");
    }

    //当打开数据库时传入的版本号与当前的版本号不同时会调用该方法
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}