package com.example.sqlite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.download.FileUtil;
import com.example.xmlmodel.Mp3Info;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DProvider {
	private static DProvider sInstance;
	private DateBaseHelper dhelper;
	private SQLiteDatabase db;
	private FileUtil util = new FileUtil();

	public static DProvider getInstance(Context context) {
		if (sInstance == null) {
			System.out.println("���뵥��ģʽ");
			sInstance = new DProvider(context.getApplicationContext());
		}
		return sInstance;
	}

	public DProvider(Context context) {
		System.out.println("���빹����");
		dhelper = new DateBaseHelper(context);
		db = dhelper.getWritableDatabase();
	}

	// ÿ�εĳ�ʼ��ȫ�����������б��������
	public void addDate(String tablename, List<Mp3Info> locallist) {
		// �����ﲻ���ڵ�ʱ������һ��mp3��¼
		String sql = "insert into " + tablename + " values(?,?,?,?,?,?) ";
		// ��ǰ�����Ƿ��������MP3��¼(״̬������2Ҳ������3)
		String sql1 = "select * from " + tablename + " where "
				+ PlayMp3ListTable.getMp3name() + " = ? ";
		// �����ǰ��������¼������״̬�Ǽ������ó�3����ʾMp3�ڱ�����
		String sql2 = "update " + tablename + " set state = '3' where "
				+ PlayMp3ListTable.getMp3name() + " = ? ";
		// ������mp3�ó�״̬2,��ʾ�������ڹ�������û��
		String sql3 = "update " + tablename + " set state = '2' ";
		;
		// ��ʼ����
		db.beginTransaction();
		db.execSQL(sql3);
		for (Mp3Info mp3Info : locallist) {
			Cursor result = db.rawQuery(sql1,
					new String[] { mp3Info.getMp3name() });
			if (result.getCount() == 0) {
				String mp3path = util.getMp3Path(mp3Info.getMp3name(), "mp3");
				String lrcpath = util.getMp3Path(
						mp3Info.getMp3name().replace(".mp3", ".lrc"), "lrc");
				db.execSQL(sql, new Object[] { null, mp3Info.getMp3name(),
						mp3Info.getMp3size(), mp3path, lrcpath, 3 });
			} else if ((result.getCount() == 1)) {
				db.execSQL(sql2, new String[] { mp3Info.getMp3name() });
			}
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		// dbClose();
	}

	// �½������б�
	public void insertList(String listname, List<Integer> chooselist) {
		String sql = "insert into " + DateBaseHelper.getAlltablename()
				+ " values( ?,?)";
		db.beginTransaction();
		for (Integer integer : chooselist) {
			db.execSQL(sql, new Object[] { listname, integer });
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	// ��ԃȫ�������б�
	public List<String> queryList(String tablename) {
		List<String> list = new ArrayList<String>();
		list.add("ȫ������");
		String sql = "select distinct " + PlayListTable.getTableName()
				+ " from " + tablename;
		Cursor result = db.rawQuery(sql, null);
		while (result.moveToNext()) {
			int index_name = result
					.getColumnIndex(PlayListTable.getTableName());
			String name = result.getString(index_name);
			list.add(name);
		}
		list.add("���������б�");
		result.close();
		return list;
	}

	public Cursor querydate(String tablename) {
		String sql = "select * from " + tablename
				+ " where state = '3' order by _id";
		Cursor result = db.rawQuery(sql, null);
		// db.close();
		return result;
	}

	public Cursor querydate(String tablename, String selection) {

		String sql = "select * from " + DateBaseHelper.getTablelocalname()
				+ " a where state = '3' and exists (select 1 from " + tablename
				+ " b where a._id = b." + PlayListTable.getMp3Id() + " and b."
				+ PlayListTable.getTableName() + " = '" + selection
				+ "' ) order by a._id";
		Cursor result = db.rawQuery(sql, null);
		// db.close();
		return result;
	}

	public void deleteTable(String tablename) {
		db.execSQL("delete from " + tablename);
		// ���������_id��ʼ��Ϊ0
		db.execSQL("update sqlite_sequence set seq = 0 where name = '"
				+ tablename + "' and seq <> 0");

	}

	public void deleteDate(String listname) {
		db.execSQL("delete from " + DateBaseHelper.getAlltablename()
				+ " where " + PlayListTable.getTableName() + "= '" + listname
				+ "' ");
	}

	public void initAllList() {
		String localpath = "mp3";
		FileUtil fileutil = new FileUtil();
		List<Mp3Info> locallist = fileutil.getFileList(localpath);
		// ����locallist���ݵ�ȫ�������б�
		addDate(DateBaseHelper.getTablelocalname(), locallist);
	}

	public void dbClose() {
		db.close();
	}


	public Map<String, String> nextMp3(String mp3path, String listname) {
		String sql1 = "select a.* from " + DateBaseHelper.getTablelocalname()
				+ " a where " + PlayMp3ListTable.getState()
				+ " = '3'  and 'ȫ������' = '" + listname
				+ "' or exists (select 1 from "
				+ DateBaseHelper.getAlltablename() + " b where b."
				+ PlayListTable.getTableName() + " = '" + listname
				+ "' and a._id = b." + PlayListTable.getMp3Id()
				+ " ) order by a._id";
		Cursor cursor = db.rawQuery(sql1, null);
		cursor.moveToFirst();
		while (!(cursor.getString(cursor
				.getColumnIndex(PlayMp3ListTable.getMp3path())).equals(mp3path)))
		{
			cursor.moveToNext();
		}
		if (!cursor.isLast()) {
			cursor.moveToNext();
		} else {
			cursor.moveToFirst();
		}
		String nextmp3name = cursor.getString(cursor
				.getColumnIndex(PlayMp3ListTable.getMp3name()));
		String nextmp3path = cursor.getString(cursor
				.getColumnIndex(PlayMp3ListTable.getMp3path()));
		Map<String, String> map = new HashMap<String, String>();
		map.put("mp3name", nextmp3name);
		map.put("mp3path", nextmp3path);
		return map;
	}
}
