package com.example.mp3player;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.example.dialog.NewPlaylistDialog;
import com.example.sqlite.DProvider;
import com.example.sqlite.DateBaseHelper;
import com.example.sqlite.PlayMp3ListTable;

@SuppressLint("NewApi")
public class LocalActivity extends ListFragment {
	private Handler handler = null;
	private Spinner spinner = null;
	private String selection = null;
	private String table_name = null;
	ArrayAdapter<String> arrayadapter = null;
	ArrayAdapter<String> arrayadapter1 = null;
	ArrayList<String> list = new ArrayList<String>();
	private Cursor cursor;
	public final static int SPINNER_STATE = 0;
	private SharedPreferences mPrefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		System.out.println("LocalActivity is onResume");
		super.onResume();
		mPrefs = getActivity().getPreferences(SPINNER_STATE);
		spinner = (Spinner) getActivity().findViewById(R.id.spinner1);
		DProvider dprovider = DProvider.getInstance(getActivity());
		list = (ArrayList<String>) dprovider.queryList(DateBaseHelper
				.getAlltablename());
		arrayadapter1 = new ArrayAdapter<String>(getActivity(), R.layout.item,
				R.id.textViewId, list);
		spinner.setAdapter(arrayadapter1);
		// spinner.setPrompt("歌曲类型"); 显示的时候让默认显示歌曲类型
		spinner.setOnItemSelectedListener(new spinnerOnchick());
		handler = new Handler();

		String selection1 = mPrefs.getString("spinner_value", "全部歌曲");
		System.out.println("selection1 is " + selection1);
		// 如果保存的播放列表没有被被删除
		if (list.contains(selection1) && !selection1.equals("新增播放列表")) {
			int position = arrayadapter1.getPosition(selection1);
			spinner.setSelection(position, true);
		} else {
			spinner.setSelection(0, true);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.local_mp3_item, container, false);
	}

	Runnable s = new Runnable() {

		@Override
		public void run() {
			// 刷新现有全部MP3播放列表
			DProvider dprovider = DProvider.getInstance(getActivity());
			dprovider.initAllList();
			if (selection.equals("全部歌曲")) {
				table_name = DateBaseHelper.getTablelocalname();
				cursor = dprovider.querydate(table_name);
			} else {
				table_name = DateBaseHelper.getAlltablename();
				cursor = dprovider.querydate(table_name, selection);
			}
			String[] from = new String[] { PlayMp3ListTable.getMp3name(),
					PlayMp3ListTable.getMp3size() };
			int[] to = new int[] { R.id.mp3name, R.id.mp3size };
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(
					getActivity(), R.layout.mp3info_item, cursor, from, to, 0);
			setListAdapter(adapter);
		}

	};

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		cursor.moveToPosition(position);
		int mp3id = cursor.getInt(cursor
				.getColumnIndex(android.provider.BaseColumns._ID));
		String mp3name = cursor.getString(cursor
				.getColumnIndex(PlayMp3ListTable.getMp3name()));
		String lrcpath = cursor.getString(cursor
				.getColumnIndex(PlayMp3ListTable.getLrcpath()));
		String mp3path = cursor.getString(cursor
				.getColumnIndex(PlayMp3ListTable.getMp3path()));

		Intent intent = new Intent();
		intent.putExtra("mp3id", mp3id);
		intent.putExtra("mp3name", mp3name);
		intent.putExtra("mp3path", mp3path);
		intent.putExtra("lrcpath", lrcpath);
		intent.putExtra("mp3listname", selection);
		intent.setClass(getActivity(), Mp3PlayerActivity.class);
		// 0表示播放界面回来
		startActivityForResult(intent, 0);
	}

	class spinnerOnchick implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			selection = arg0.getItemAtPosition(arg2).toString();
			if (selection.equals("新增播放列表")) {
				NewPlaylistDialog newdialog = new NewPlaylistDialog();
				newdialog.show(getFragmentManager(), "NewPlayList");
			} else {
				handler.post(s);
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			System.out.println("spinner 无选择");
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String listname = spinner.getSelectedItem().toString();
		// 当选择删除当前播放列表的时候，删除数据库中的对应值，刷新该activity
		switch (item.getItemId()) {
		case 2:
			DProvider dprovider = DProvider.getInstance(getActivity());
			dprovider.deleteDate(listname);
			// LocalActivity.this.onResume();
			spinner.setSelection(0);
		case 1:
			System.out.println(item.getItemId());
			Intent intent = new Intent();
			intent.setClass(getActivity(), ChooseMp3Activity.class);
			intent.putExtra("listname", listname);
			startActivity(intent);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPause() {
		System.out.println("LocalActivity onPause");
		super.onPause();
		SharedPreferences.Editor ed = mPrefs.edit();
		if (!selection.equals("新建播放列表")) {
			ed.putString("spinner_value", selection);
			System.out.println("put " + selection);
		} else {
			ed.putString("spinner_value", "全部歌曲");
			System.out.println("put 全部歌曲");
		}
		ed.commit();
	}

	@Override
	public void onDestroy() {
		if (!cursor.isClosed()) {
			cursor.close();
			System.out.println("localactivity cursor 关闭");
		}
		super.onDestroy();
	}

}
