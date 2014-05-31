package com.example.sqlite;

import com.example.newmp3player.R;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DateBaseHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "mp3_db";
	private static final int DATABASE_VERSION = 1;
	private Context context ;

	public DateBaseHelper(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
		if(this.context == null){
			this.context = context.getApplicationContext();
		}
	}

	public DateBaseHelper(Context context, int version) {
		super(context, DB_NAME, null, version);
		if(this.context == null){
			this.context = context.getApplicationContext();
		}
	}

	
	@Override
	public void onCreate(SQLiteDatabase db) {
		//创建本地所有Mp3的表
		String sql = "create table " + this.context.getString(R.string.tablelocalname) + "("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ this.context.getString(R.string.ID) + " number(5),"
				+ this.context.getString(R.string.TILTE) + " varchar(50),"
				+ this.context.getString(R.string.THUMB) + " varchar(100),"
				+ this.context.getString(R.string.ARTIST) + " varchar(20),"
				+ this.context.getString(R.string.ALBUM) + " varchar(50),"
				+ this.context.getString(R.string.ALBUM_ID) + " number(20),"
				+ this.context.getString(R.string.URL) + " varchar(100),"
				+ this.context.getString(R.string.DURATION) + " number(15),"
				+ this.context.getString(R.string.SIZE) + " number(15),"
				+ this.context.getString(R.string.LRCPATH) + " varchar(100),"
				+ this.context.getString(R.string.STATE) + " number(2) )";
		//创建所有播放列表的表
		String sql2 = "create table " + this.context.getString(R.string.alltablename) + "("
				+ this.context.getString(R.string.TABLE_NAME) + " varchar(50),"
				+ this.context.getString(R.string.MP3_ID) + " number )";
		db.execSQL(sql);
		db.execSQL(sql2);
	}

	
	//更新版本的时候重建表
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		try {
			String sql = "drop table if exists " +  this.context.getString(R.string.tablelocalname);
			String sq2 = "drop table if exists " + this.context.getString(R.string.alltablename);
			db.execSQL(sql);
			db.execSQL(sq2);
			onCreate(db);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
