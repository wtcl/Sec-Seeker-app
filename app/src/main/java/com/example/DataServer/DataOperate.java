package com.example.DataServer;


//管理用户登录注册数据的本地数据库


import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Vector;

public class DataOperate {

    @Entity(tableName = "user_table")
    public class User {
        @PrimaryKey
        @NonNull
        @ColumnInfo(name = "name")
        final String name;

        @NonNull
        @ColumnInfo(name = "password")
        final String password;

        @ColumnInfo(name = "uuid")
        final String session;

        public User(@NonNull String name, @NonNull String password, String session) {
            this.name = name;
            this.password = password;
            this.session = session;
        }

        //获取用户名
        @NonNull
        public String getName(){return this.name;}
        //获取密码
        @NonNull
        public String getPassword(){return this.password;}
        //获取session
        public String getSession(){return this.session;}

    }

    Context testContext;
    public class DBHelper extends SQLiteOpenHelper {
        //本类构造方法
        public DBHelper(Context context, String name,
                        SQLiteDatabase.CursorFactory factory, int version) {

            super(context, name, factory, version);
            testContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //数据库创建测试
            Toast.makeText(testContext, "创建了数据库", Toast.LENGTH_SHORT).show();
            db.execSQL(T_UserInfo0);
            db.execSQL(T_tracks);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        //使用者登录信息
        private static final String T_UserInfo0 =
                "create table T_UserInfo("
                        + "name varchar,"//id
                        + "password varchar,"//密码
                        + "uuid varchar,"   //用户的uuid
                        + "session varchar)";//用户的session

        private static final String T_tracks =
                "create table T_tracks("
                        + "session varchar,"//id
                        + "tracks varchar)";//pwd
    }



    //检查用户名是否存在（注册）
    public boolean CheckRepeat(String SQLName,String TableName,String UserName,Context ActivityContext)
    {
        //新建数据库管理对象
        DataOperate.DBHelper test = this.new DBHelper(ActivityContext,SQLName,
                null,1                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              );
        //获取写入数据库的权限
        SQLiteDatabase db = test.getWritableDatabase();
        //获取Table光标
        Cursor MyCursor = db.query(TableName,null,null,null,
                null,null,null);

        //遍历Table对象
        if(MyCursor.moveToFirst())//将光标移动到表头
        {
            //遍历每一行用户名
            do {
                @SuppressLint("Range") String CurrentUserName =
                        MyCursor.getString(MyCursor.getColumnIndex("name"));

                //判断是否为无效名字
                if(CurrentUserName == null)
                    continue;
                //有重名返回真
                if(CurrentUserName.equals(UserName))
                    return true;
            }while(MyCursor.moveToNext());
        }
        //没有重名则返回假
        return false;
    }

    //检查用户名信息是否匹配
    public boolean CheckUserImformation(String SQLName,String TableName,String UserName,
                                        String UserCode,Context ActivityContext)
    {
        //新建数据库管理对象
        DataOperate.DBHelper test = this.new DBHelper(ActivityContext,SQLName,
                null,1                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              );
        //获取写入数据库的权限
        SQLiteDatabase db = test.getWritableDatabase();
        //获取Table光标
        Cursor MyCursor = db.query(TableName,null,null,null,
                null,null,null);

        //遍历Table对象
        if(MyCursor.moveToFirst())//将光标移动到表头
        {
            //遍历每一行用户名
            do {
                @SuppressLint("Range") String CurrentUserName =
                        MyCursor.getString(MyCursor.getColumnIndex("name"));
                @SuppressLint("Range") String CurrentUserCode =
                        MyCursor.getString(MyCursor.getColumnIndex("password"));

                //判断是否为无效名字
                if(CurrentUserName == null || CurrentUserCode == null)
                    continue;

                //密码和用户名匹配则登录成功
                if(CurrentUserName.equals(UserName) && CurrentUserCode.equals(UserCode))
                    return true;
            }while(MyCursor.moveToNext());
        }
        return false;
    }


    //获取数据库全部的用户名
    public Vector<String> GetTotalName(String SQLName,String TableName,Context ActivityContext)
    {
        Vector<String> TableTotalName = new Vector<String>();
        //新建数据库管理对象
        DataOperate.DBHelper test = this.new DBHelper(ActivityContext,SQLName,
                null,1                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              );
        //获取写入数据库的权限
        SQLiteDatabase db = test.getWritableDatabase();
        //获取Table光标
        Cursor MyCursor = db.query(TableName,null,null,null,
                null,null,null);

        //遍历Table对象
        if(MyCursor.moveToFirst())//将光标移动到表头
        {
            //遍历每一行用户名
            do {
                @SuppressLint("Range") String CurrentUserName =
                        MyCursor.getString(MyCursor.getColumnIndex("name"));
                if(CurrentUserName == null)
                    continue;

                TableTotalName.add(CurrentUserName);
            }while(MyCursor.moveToNext());
        }

        return TableTotalName;
    }


    //插入对应用户的session
    public void WriteSession(String SQLName,String TableName,String UserName,
                             Context ActivityContext,String session)
    {
        //新建数据库管理对象
        DataOperate.DBHelper test = this.new DBHelper(ActivityContext,SQLName,
                null,1);
        //获取写入数据库的权限
        SQLiteDatabase db = test.getWritableDatabase();
        //获取Table光标
        Cursor MyCursor = db.query(TableName,null,null,null,
                null,null,null);

        //遍历Table对象
        if(MyCursor.moveToFirst())//将光标移动到表头
        {
            //遍历每一行用户名
            do {
                @SuppressLint("Range") String CurrentUserName =
                        MyCursor.getString(MyCursor.getColumnIndex("name"));

                if(CurrentUserName == null)
                {
                    continue;
                }
                    //密码和用户名匹配则登录成功
                if(CurrentUserName.equals(UserName))
                {
                    ContentValues WritePen = new ContentValues();
                    WritePen.put("session",session);
                    //数据库插入用户信息
                    db.update(TableName,WritePen, "name = ?",
                            new String[]{CurrentUserName});
                    WritePen.clear();

                    //找到用户并且插入之后直接返回
                    return ;
                }
            }while(MyCursor.moveToNext());
        }
    }

}
