package com.panxiong.instant.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.panxiong.instant.R;

/**
 * Created by panxi on 2016/7/11.
 * <p>
 * 数据库管理
 */
public class DataBaseHelper extends SQLiteOpenHelper {
    private Context context;

    private DataBaseHelper(Context context) {
        super(context, "instant.db", null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(context.getString(R.string.t_msg));  // 创建聊天消息表
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /*数据库*/
    private static SQLiteDatabase sqLiteDatabase = null;

    /*拿到数据库实例*/
    public static SQLiteDatabase getDataBase(Context context) {
        if (sqLiteDatabase == null) {
            synchronized (DataBaseHelper.class) {
                if (sqLiteDatabase == null) {
                    sqLiteDatabase = new DataBaseHelper(context).getWritableDatabase();
                }
            }
        }
        return sqLiteDatabase;
    }
}
