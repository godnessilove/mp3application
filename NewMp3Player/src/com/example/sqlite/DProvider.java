package com.example.sqlite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.example.fileutil.FileUtil;
import com.example.newmp3player.LocalActivity;
import com.example.newmp3player.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.MediaStore;
import android.util.Log;

public class DProvider {
	private static DProvider sInstance;
	private DateBaseHelper dhelper;
	private FileUtil fileUtil;
	private SQLiteDatabase db;
	private Context context;
	private SharedPreferences mPrefs;
	private String TABLENAME;
	private String ALLTABLENAME;
	private String ID;
	private String TILTE;
	private String THUMB;
	private String ARTIST;
	private String ALBUM;
	private String ALBUM_ID;
	private String URL;
	private String DURATION;
	private String SIZE;
	private String LRCPATH;
	private String STATE;
	private String DEFAULT;
	private String DEFAULT_LAST;
	private String TABLE_NAME;
	private String MP3_ID;

	// private String TAG = "DProvider";

	public static DProvider getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new DProvider(context.getApplicationContext());
		}
		return sInstance;
	}

	public DProvider(Context context) {
		dhelper = new DateBaseHelper(context);
		db = dhelper.getWritableDatabase();
		this.context = context;
		mPrefs = this.context.getSharedPreferences(LocalActivity.MP3_SHARED,
				Context.MODE_PRIVATE);
		TABLENAME = this.context.getString(R.string.tablelocalname);
		ALLTABLENAME = this.context.getString(R.string.alltablename);
		ID = this.context.getString(R.string.ID);
		TILTE = this.context.getString(R.string.TILTE);
		THUMB = this.context.getString(R.string.THUMB);
		ARTIST = this.context.getString(R.string.ARTIST);
		ALBUM = this.context.getString(R.string.ALBUM);
		ALBUM_ID = this.context.getString(R.string.ALBUM_ID);
		URL = this.context.getString(R.string.URL);
		DURATION = this.context.getString(R.string.DURATION);
		SIZE = this.context.getString(R.string.SIZE);
		LRCPATH = this.context.getString(R.string.LRCPATH);
		STATE = this.context.getString(R.string.STATE);
		DEFAULT = this.context.getString(R.string.playlist_default);
		DEFAULT_LAST = this.context.getString(R.string.playlist_default_last);
		TABLE_NAME = this.context.getString(R.string.TABLE_NAME);
		MP3_ID = this.context.getString(R.string.MP3_ID);
		fileUtil = new FileUtil();
	}

	public String querythumb(int id) {
		Cursor thumbCursor = null;
		String thumb = null;
		String selection = MediaStore.Audio.Albums._ID + "=?";
		String[] selectionArgs = new String[] { id + "" };
		try {
			thumbCursor = this.context.getContentResolver().query(
					MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
					new String[] { MediaStore.Audio.Albums.ALBUM_ART,
							MediaStore.Audio.Albums._ID }, selection,
					selectionArgs, null);
			while (thumbCursor.moveToNext()) {
				thumb = thumbCursor
						.getString(thumbCursor
								.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (thumbCursor != null && !thumbCursor.isClosed()) {
				thumbCursor.close();
			}
		}
		return thumb;
	}

	/**
	 * 返回对应mp3的image
	 * 
	 * @param title
	 * @return
	 */
	public String queryimage(String title) {
		String thumb = null;
		Cursor result = null;
		String sql = "select " + THUMB + " from " + TABLENAME + " where "
				+ TILTE + " =  '" + title + "'";
		try {
			result = db.rawQuery(sql, null);
			while (result.moveToNext()) {
				thumb = result.getString(result.getColumnIndex(THUMB));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (result != null && !result.isClosed()) {
				result.close();
			}
		}
		return thumb;
	}

	/**
	 * 返回对应mp3的信息
	 * 
	 * @param title
	 * @return
	 */
	public String[] queryMp3info(String title) {
		String url = null;
		String lrcpath = null;
		String artist = null;
		String album = null;
		String thumb = null;
		String[] mp3info = null;
		Cursor result = null;
		try {
			result = db.query(TABLENAME, new String[] { URL, LRCPATH, ARTIST,
					ALBUM, THUMB }, TILTE + " = ? ", new String[] { title },
					null, null, null);
			while (result.moveToNext()) {
				url = result.getString(result.getColumnIndex(URL));
				lrcpath = result.getString(result.getColumnIndex(LRCPATH));
				artist = result.getString(result.getColumnIndex(ARTIST));
				album = result.getString(result.getColumnIndex(ALBUM));
				thumb = result.getString(result.getColumnIndex(THUMB));
			}
			mp3info = new String[] { url, lrcpath, artist, album, thumb };
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (result != null && !result.isClosed()) {
				result.close();
			}
		}
		return mp3info;
	}

	public void InitDate() {
		Cursor cursor = null;
		Cursor result = null;
		// 当表里不存在的时候，增加一条mp3记录
		String sql = "insert into " + TABLENAME
				+ " values(?,?,?,?,?,?,?,?,?,?,?,?) ";
		// 当前表里是否存在这条MP3记录(状态可以是2也可以是3)
		String sql1 = "select * from " + TABLENAME + " where " + TILTE
				+ " = ? ";
		// 如果当前有这条记录，不管状态是几，都置成3，表示Mp3在本地了
		String sql2 = "update " + TABLENAME + " set " + STATE + "= '3' , " + ID
				+ " = ?,   " + ARTIST + " = ? ,   " + ALBUM + " = ? ,  "
				+ ALBUM_ID + " = ? , " + URL + " = ? ,   " + DURATION
				+ " = ? ,   " + SIZE + " = ?,  " + THUMB + " = ?,  " + LRCPATH
				+ " = ?  where " + TILTE + " = ? ";
		// 将所有mp3置成状态2,表示曾经存在过，现在没了
		String sql3 = "update " + TABLENAME + " set " + STATE + " = '2' ";
		// 开始事务
		db.beginTransaction();
		try {
			db.execSQL(sql3);
			cursor = this.context.getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					new String[] { MediaStore.Audio.Media._ID,
							MediaStore.Audio.Media.TITLE,
							MediaStore.Audio.Media.ARTIST,
							MediaStore.Audio.Media.ALBUM,
							MediaStore.Audio.Media.ALBUM_ID,
							MediaStore.Audio.Media.DATA,
							MediaStore.Audio.Media.DURATION,
							MediaStore.Audio.Media.SIZE }, null, null,
					MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
			while (cursor.moveToNext()) {
				// 歌曲名称
				String tilte = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
				// 歌曲id
				int id = cursor.getInt(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
				// 歌手名
				String artist = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
				// 歌曲专辑名
				String album = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
				// 歌曲专辑Id
				int album_id = cursor
						.getInt(cursor
								.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
				// 歌曲缩微图
				String thumb = querythumb(album_id);
				// String thumb = getAlbumArt(album_id);
				// 歌曲路径
				String url = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
				// 歌曲的总播放时长
				int duration = cursor
						.getInt(cursor
								.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
				// 歌曲文件大小
				long size = cursor.getLong(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
				// 歌词路径
				String lrcpath = fileUtil.getLrcPath(tilte, "lrc");
				result = db.rawQuery(sql1, new String[] { tilte });
				if (result.getCount() == 0) {
					db.execSQL(sql, new Object[] { null, id, tilte, thumb,
							artist, album, album_id, url, duration, size,
							lrcpath, 3 });
				} else if ((result.getCount() == 1)) {
					db.execSQL(sql2, new String[] { id + "", artist, album,
							album_id + "", url, duration + "", size + "",
							thumb, lrcpath, tilte });
				}
				result.close();
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			if (result != null && !result.isClosed()) {
				result.close();
			}
			db.endTransaction();
		}
	}

	/**
	 * 每次的初始化全部歌曲播放列表插入数据
	 * 
	 * @param tablename
	 *            全部歌曲表名
	 * @param locallist
	 *            本地mp3列表
	 */
	/*
	 * public synchronized void addDate(String tablename, List<Mp3Info>
	 * locallist) { Cursor result = null; // 当表里不存在的时候，增加一条mp3记录 String sql =
	 * "insert into " + tablename + " values(?,?,?,?,?,?) "; //
	 * 当前表里是否存在这条MP3记录(状态可以是2也可以是3) String sql1 = "select * from " + tablename +
	 * " where " + this.context.getString(R.string.MP3NAME) + " = ? "; //
	 * 如果当前有这条记录，不管状态是几，都置成3，表示Mp3在本地了 String sql2 = "update " + tablename +
	 * " set state = '3' where " + this.context.getString(R.string.MP3NAME) +
	 * " = ? "; // 将所有mp3置成状态2,表示曾经存在过，现在没了 String sql3 = "update " + tablename
	 * + " set state = '2' "; ; // 开始事务 db.beginTransaction(); try {
	 * db.execSQL(sql3); for (Mp3Info mp3Info : locallist) { result =
	 * db.rawQuery(sql1, new String[] { mp3Info.getMp3name() }); if
	 * (result.getCount() == 0) { String mp3path =
	 * FileUtil.getMp3Path(mp3Info.getMp3name(), "mp3"); String lrcpath =
	 * FileUtil.getMp3Path(mp3Info.getMp3name() .replace(".mp3", ".lrc"),
	 * "lrc"); db.execSQL(sql, new Object[] { null, mp3Info.getMp3name(),
	 * mp3Info.getMp3size(), mp3path, lrcpath, 3 }); } else if
	 * ((result.getCount() == 1)) { db.execSQL(sql2, new String[] {
	 * mp3Info.getMp3name() }); } result.close(); }
	 * db.setTransactionSuccessful(); } catch (Exception e) {
	 * e.printStackTrace(); } finally { if (result != null &&
	 * !result.isClosed()) { result.close(); } db.endTransaction(); } }
	 */

	/**
	 * 新建播放列表,插入数据
	 * 
	 * @param listname
	 *            播放列表名
	 * @param chooselist
	 *            勾选的mp3的_id
	 * @return 是否执行成功
	 */
	public boolean insertList(String listname, List<Integer> chooselist) {
		// 默认返回值为true
		boolean b = true;
		String sql = "insert into " + ALLTABLENAME + " values( ?,?)";
		db.beginTransaction();
		try {
			for (Integer integer : chooselist) {
				db.execSQL(sql, new Object[] { listname, integer });
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
			b = false;
		} finally {
			db.endTransaction();
		}
		return b;
	}

	public boolean deleteList(String listname, List<Integer> chooselist) {
		// 默认返回值为true
		boolean b = true;
		String sql = "delete from " + ALLTABLENAME + "  where " + TABLE_NAME
				+ " = ? and " + MP3_ID + " = ? ";
		db.beginTransaction();
		try {
			for (Integer integer : chooselist) {
				db.execSQL(sql, new Object[] { listname, integer });
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
			b = false;
		} finally {
			db.endTransaction();
		}
		return b;
	}

	/**
	 * 查詢全部播放列表
	 * 
	 * @return 返回完整的全部播放列表
	 */
	public List<String> queryList() {
		List<String> list = new ArrayList<String>();
		// 第一行加入全部歌曲播放列表
		list.add(DEFAULT);
		String sql = "select distinct " + TABLE_NAME + " from " + ALLTABLENAME;
		Cursor result = db.rawQuery(sql, null);
		while (result.moveToNext()) {
			int index_name = result.getColumnIndex(TABLE_NAME);
			String name = result.getString(index_name);
			Log.i("queryList", "name is " + name);
			list.add(name);
		}
		// 最后加入新增播放表列选项
		list.add(DEFAULT_LAST);
		result.close();
		Log.i("queryList", "spinner list is " + list);
		return list;
	}

	/**
	 * 查询现有的歌曲列表
	 * 
	 * @return 返回現有mp3信息cursor
	 */
	public Cursor querydate() {
		String sql = "select * from " + TABLENAME
				+ " where state = '3' order by _id";
		Cursor result = db.rawQuery(sql, null);
		return result;
	}

	/**
	 * 查询指定播放列表的歌曲列表
	 * 
	 * @param selection
	 *            指定查詢的播放列表
	 * @return mp3信息cursor
	 */
	public Cursor querydate(String selection) {
		String sql = "select * from " + TABLENAME
				+ " a where state = '3' and exists (select 1 from "
				+ ALLTABLENAME + " b where a._id = b." + MP3_ID + " and b."
				+ TABLE_NAME + " = '" + selection + "' ) order by a._id";
		Cursor result = db.rawQuery(sql, null);
		return result;
	}
	
	/**
	 * 查询指定播放列表的歌曲列表
	 * 
	 * @param selection
	 *            指定查詢的播放列表
	 * @return mp3信息cursor
	 */
	public Cursor queryoutsidedate(String selection) {
		String sql = "select * from " + TABLENAME
				+ " a where state = '3' and not exists (select 1 from "
				+ ALLTABLENAME + " b where a._id = b." + MP3_ID + " and b."
				+ TABLE_NAME + " = '" + selection + "' ) order by a._id";
		Cursor result = db.rawQuery(sql, null);
		return result;
	}

	/**
	 * 根据Mp3名字查询是否存在
	 * 
	 * @param mp3name
	 *            MP3名称
	 * @param mp3list
	 *            需要查询的播放列表
	 * @return
	 */
	public boolean querymp3(String mp3list, String mp3name) {
		Cursor result = null;
		boolean isexist = false;
		String sql = "select * from " + TABLENAME + " b where b." + STATE
				+ " = '3' and b." + TILTE + " = '" + mp3name + "' and ('"
				+ DEFAULT + "' = '" + mp3list + "' or exists (select 1 from "
				+ ALLTABLENAME + " a where a." + TABLE_NAME + " = '" + mp3list
				+ "' and b._id = a." + MP3_ID + "))";
		try {
			result = db.rawQuery(sql, null);
			if (result.getCount() != 0) {
				isexist = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			result.close();
		}
		return isexist;
	}

	/**
	 * 获取存在的mp3歌曲中的第一个
	 * 
	 * @return
	 */
	public String querymp3def() {
		String mp3name = null;
		Cursor result = null;
		String sql = "select " + TILTE + " from " + TABLENAME
				+ " where _id = (select min(_id) from " + TABLENAME
				+ " where state = '3' )";
		try {
			result = db.rawQuery(sql, null);
			while (result.moveToNext()) {
				mp3name = result.getString(result.getColumnIndex(TILTE));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (result != null && !result.isClosed()) {
				result.close();
			}
		}
		return mp3name;
	}

	/**
	 * 获取存在的mp3歌曲中的第一个
	 * 
	 * @return
	 */
	public String querymp3defpath() {
		String mp3path = null;
		Cursor result = null;
		String sql = "select " + URL + " from " + TABLENAME
				+ " where _id = (select min(_id) from " + TABLENAME
				+ " where state = '3' )";
		try {
			result = db.rawQuery(sql, null);
			while (result.moveToNext()) {
				mp3path = result.getString(result.getColumnIndex(URL));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (result != null && !result.isClosed()) {
				result.close();
			}
		}
		return mp3path;
	}

	/**
	 * 删除指定播放列表
	 * 
	 * @param listname
	 *            指定需要刪除的播放列表名字
	 * @return 刪除是否成功
	 */
	public int deleteDate(String listname) {
		try {
			db.execSQL("delete from " + ALLTABLENAME + " where " + TABLE_NAME
					+ "= '" + listname + "' ");
		} catch (Exception e) {
			return -1;
		}
		return 0;
	}

	/**
	 * 指定播放列表是否存在
	 * 
	 * @param mp3list
	 * @return
	 */
	public boolean isListExists(String mp3list) {
		Cursor result = null;
		boolean isexist = false;
		String sql = "select * from " + ALLTABLENAME + " a where a."
				+ TABLE_NAME + " = '" + mp3list + "' ";
		try {
			result = db.rawQuery(sql, null);
			if (result.getCount() != 0) {
				isexist = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (result != null && !result.isClosed()) {
				result.close();
			}
		}
		return isexist;
	}

	/**
	 * 每次初始化全部播放列表里的歌曲
	 */
	/*
	 * public void initAllList() { String localpath = "mp3"; FileUtil fileutil =
	 * new FileUtil(); List<Mp3Info> locallist =
	 * fileutil.getFileList(localpath); // 插入locallist数据到全部播放列表
	 * addDate(this.context.getString(R.string.tablelocalname), locallist); }
	 */

	/**
	 * 关闭数据库
	 */
	public void dbClose() {
		db.close();
	}

	/**
	 * 求取下一首
	 * 
	 * @param mp3path
	 *            当前歌曲的路径
	 * @param listname
	 *            当前播放列表
	 * @param mode
	 *            顺序0/随机1
	 * @return 下一首歌的信息
	 */
	public Map<String, String> nextMp3(String mp3path, String listname, int mode) {
		String sql1 = "select a.* from " + TABLENAME + " a where " + STATE
				+ " = '3' and ('" + DEFAULT + "' = '" + listname
				+ "' or exists (select 1 from " + ALLTABLENAME + " b where b."
				+ TABLE_NAME + " = '" + listname + "' and a._id = b." + MP3_ID
				+ " )) order by a._id";
		Cursor result = db.rawQuery(sql1, null);
		if (0 == mode) {
			result.moveToFirst();
			while (!(result.getString(result.getColumnIndex(URL))
					.equals(mp3path))) {
				result.moveToNext();
			}
			if (!result.isLast()) {
				result.moveToNext();
			} else {
				result.moveToFirst();
			}
		} else if (1 == mode) {
			// 随机,前后连续2次不会重复播放，最多试3次
			Random rand = new Random();
			int random = rand.nextInt(result.getCount());
			int mprefsrandom = mPrefs.getInt("random", random);
			int index = 0;
			do {
				random = rand.nextInt(result.getCount());
				index++;
			} while (mprefsrandom == random && index < 3);
			result.moveToPosition(random);
			SharedPreferences.Editor ed = mPrefs.edit();
			ed.putInt("random", random);
			ed.commit();
		}
		String nextmp3name = result.getString(result.getColumnIndex(TILTE));
		String nextmp3path = result.getString(result.getColumnIndex(URL));
		Map<String, String> map = new HashMap<String, String>();
		map.put("mp3name", nextmp3name);
		map.put("mp3path", nextmp3path);
		result.close();
		return map;
	}
}
