package com.example.sqlite;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DateBaseHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "mp3_db";
	private static final int DATABASE_VERSION = 1;
	private static final  String tablelocalname = "local_mp3_list_all";
	private static final String alltablename = "mp3_list_name";

	public DateBaseHelper(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
	}

	public DateBaseHelper(Context context, int version) {
		super(context, DB_NAME, null, version);
	}
	
	public static String getTablelocalname() {
		return tablelocalname;
	}

	public static String getAlltablename() {
		return alltablename;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//创建本地所有Mp3的表
		String sql = "create table " + getTablelocalname() + "("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ PlayMp3ListTable.getMp3name() + " varchar(20),"
				+ PlayMp3ListTable.getMp3size() + " number(15),"
				+ PlayMp3ListTable.getMp3path() + " varchar(100),"
				+ PlayMp3ListTable.getLrcpath() + " varchar(100),"
				+ PlayMp3ListTable.getState() + " number(2) )";
		//创建所有播放列表的表
		String sql2 = "create table " + getAlltablename() + "("
				+ PlayListTable.getTableName() + " varchar(50),"
				+ PlayListTable.getMp3Id() + " number )";
		db.execSQL(sql);
		db.execSQL(sql2);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		try {
			String sql = "drop table if exists " + getTablelocalname();
			String sq2 = "drop table if exists " + getAlltablename();
			db.execSQL(sql);
			db.execSQL(sq2);
			onCreate(db);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
