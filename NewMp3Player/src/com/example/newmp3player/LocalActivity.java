package com.example.newmp3player;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.util.Log;
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
import android.widget.Toast;

import com.example.dialog.NewPlaylistDialog;
import com.example.sqlite.DProvider;
import com.example.newmp3player.RefreshableView.PullToRefreshListener;

@SuppressLint({ "HandlerLeak", "NewApi" })
public class LocalActivity extends ListFragment {
	private Spinner spinner = null;
	private String selection = null;
	private ArrayAdapter<String> arrayadapter = null;
	private SimpleCursorAdapter adapter;
	private ArrayList<String> list = new ArrayList<String>();
	private Cursor cursor;
	public final static String MP3_SHARED = "MP3_SHARED";
	// 下拉框播放列表的共享文件，用于保存用户选择的播放列表便于返回
	private SharedPreferences mPrefs;
	private MyHandler myhandler;
	private DProvider dprovider;
	private RefreshableView refreshableView;
	private String tag = "LocalActivity";

	private Receiver1 receiver1;

	public interface LocalFragmentListener {
		public void onMp3Selected(Bundle bundle);
	}

	LocalFragmentListener mListener;

	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the DialogListener so we can send events to the
			// host
			mListener = (LocalFragmentListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement LocalFragmentListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i("LocalActivity", "LocalActivity is onCreate");
		// 设置菜单可用，使用的是mainactivity里设置的菜单
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i("LocalActivity", "LocalActivity is onCreateView");
		return inflater.inflate(R.layout.local_mp3_item, container, false);

	}

	@Override
	public void onStart() {

		// 注册广播
		if (receiver1 == null) {
			receiver1 = new Receiver1();
			// 若是想接收到mediaScanner相关的广播，必须加
			IntentFilter filter = new IntentFilter(
					Intent.ACTION_MEDIA_SCANNER_STARTED);
			filter.addDataScheme("file");
			filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
			LocalActivity.this.getActivity()
					.registerReceiver(receiver1, filter);
		}

		// 下拉更新
		refreshableView = (RefreshableView) getActivity().findViewById(
				R.id.refreshable_view);

		refreshableView.setOnRefreshListener(new PullToRefreshListener() {

			public void onRefresh() {
				getActivity().sendBroadcast(
						new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri
								.parse("file://"
										+ Environment
												.getExternalStorageDirectory()
												.getAbsolutePath())));
			}
		}, 0);
		// 同一个应用中使用的getpreferences
		mPrefs = getActivity().getSharedPreferences(MP3_SHARED,
				Context.MODE_PRIVATE);
		dprovider = DProvider.getInstance(getActivity());
		myhandler = new MyHandler();
		spinner = (Spinner) getActivity().findViewById(R.id.spinner1);
		View view = getView();
		view.setBackgroundResource(R.drawable.listimage);
		view.getBackground().setAlpha(100);
		super.onStart();
	}

	@Override
	public void onResume() {
		Log.i("LocalActivity", "LocalActivity is onResume");
		super.onResume();
		Log.i("onResume", "spinner " + spinner);
		// 新线程访问数据库，用来获取全部播放列表
		Thread1 thread1 = new Thread1();
		thread1.start();

	}

	// 查询spinner所有的播放列表
	private final class Thread1 extends Thread {
		@Override
		public void run() {
			// 查詢全部播放列表
			list = (ArrayList<String>) dprovider.queryList();
			// 展示spinner
			myhandler.sendEmptyMessage(1);
		}

	}

	// 查詢mp3列表
	private final class Thread2 extends Thread {
		@Override
		public void run() {
			// 更具选择的spinner查询mp3列表
			if (selection.equals(getString(R.string.playlist_default))) {
				cursor = dprovider.querydate();
			} else {
				cursor = dprovider.querydate(selection);
			}
			// 根据spinner展示mp3列表
			myhandler.sendEmptyMessage(2);
		}

	}

	// 刪除指定播放列表
	private final class Thread3 extends Thread {
		private String listname;

		public Thread3(String listname) {
			super(listname);
			this.listname = listname;
		}

		@Override
		public void run() {
			// 返回刪除是否成功,0表示成功,-1表示失敗
			int b = dprovider.deleteDate(listname);

			Message msg = new Message();
			msg.what = 3;
			msg.arg1 = b;
			myhandler.sendMessage(msg);
		}
	}

	// 处理需要在主线程中操作控件的handler
	private final class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			// 显示播放列表下拉框
			case 1:
				arrayadapter = new ArrayAdapter<String>(getActivity(),
						R.layout.item, R.id.textViewId, list);
				Log.i("MyHandler", "arrayadapter " + arrayadapter);
				Log.i("MyHandler", "spinner " + spinner);
				spinner.setAdapter(arrayadapter);
				spinner.setOnItemSelectedListener(new spinnerOnchick());
				// 获取保存的播放列表名
				String selection1 = mPrefs.getString("spinner_value",
						getString(R.string.playlist_default));
				Log.i("LocalActivity", "保存的播放列表名为：" + selection1);
				// 如果保存的播放列表没有被被删除，并且不是新增播放列表，那么可以默认选择改项，否则选择默认的全部歌曲列表
				if (list.contains(selection1)
						&& !selection1
								.equals(getString(R.string.playlist_default_last))) {
					int position = arrayadapter.getPosition(selection1);
					spinner.setSelection(position, true);
				} else {
					spinner.setSelection(0, true);
				}
				break;
			// 展示mp3list
			case 2:
				String[] from = new String[] { getString(R.string.TILTE),
						getString(R.string.ARTIST) };
				int[] to = new int[] { R.id.mp3name, R.id.mp3size };
				adapter = new SimpleCursorAdapter(getActivity(),
						R.layout.mp3info_item, cursor, from, to, 0);
				setListAdapter(adapter);
				break;
			// 提示删除播放列表是否成功，并刷新
			case 3:
				if (msg.arg1 == 0) {
					Toast.makeText(getActivity(),
							getString(R.string.deletesuccess),
							Toast.LENGTH_SHORT).show();
				} else if (msg.arg1 == -1) {
					Toast.makeText(getActivity(),
							getString(R.string.deletefail), Toast.LENGTH_SHORT)
							.show();

				}
				LocalActivity.this.onResume();
				break;
			case 4:
				Toast.makeText(getActivity(), getString(R.string.updateOk), Toast.LENGTH_SHORT).show();
				break;
			case 5:
				Toast.makeText(getActivity(), getString(R.string.deleteFalse), Toast.LENGTH_SHORT).show();
				break;
			}

		}

	}

	// 选择下拉列表的播放列表事件
	class spinnerOnchick implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			selection = arg0.getItemAtPosition(arg2).toString();
			// 点击是新建播放列表的时候，弹出输入新列表名称的对话框，否则直接刷新listview展示
			if (selection.equals(getString(R.string.playlist_default_last))) {
				NewPlaylistDialog newdialog = new NewPlaylistDialog();
				newdialog.show(getFragmentManager(), "NewPlayList");
			} else {
				// 新线程更新全部播放列表
				Thread2 thread2 = new Thread2();
				thread2.start();
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			Log.i("spinner", "spinner 无选择");
		}

	}

	// 点击播放列表里的歌曲，跳转播放页面
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		cursor.moveToPosition(position);
		String mp3name = cursor.getString(cursor
				.getColumnIndex(getString(R.string.TILTE)));
		String lrcpath = cursor.getString(cursor
				.getColumnIndex(getString(R.string.LRCPATH)));
		String mp3path = cursor.getString(cursor
				.getColumnIndex(getString(R.string.URL)));

		Bundle bundle = new Bundle();
		bundle.putString("mp3name", mp3name);
		bundle.putString("mp3path", mp3path);
		bundle.putString("lrcpath", lrcpath);
		bundle.putString("mp3listname", selection);
		mListener.onMp3Selected(bundle);
	}

	// 点击菜单项的事件
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i("LocalActivity", "LocalActivity is isVisible" + isVisible());
		if (isVisible()) {
			String listname = spinner.getSelectedItem().toString();
			// 当选择删除当前播放列表的时候，删除数据库中的对应值，刷新该activity
			switch (item.getItemId()) {
			case 2:
				// 开线程删除播放李彪
				if(listname.equals(getString(R.string.playlist_default))||listname.equals(getString(R.string.playlist_default_last))){
				Thread3 thread3 = new Thread3(listname);
				thread3.start();
				}else {
					Message msg = new Message();
					msg.what = 5;
					myhandler.sendMessage(msg);
				}
				break;
			case 1:
				// 进入选择添加mp3的activity
				Intent intent = new Intent();
				intent.setClass(getActivity(), ChooseMp3Activity.class);
				intent.putExtra("listname", listname);
				startActivity(intent);
				break;
			}

			return super.onOptionsItemSelected(item);
		} else
			return false;
	}

	// 监听扫描sd卡mp3文件信息广播
	class Receiver1 extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if (arg1.getAction().equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {
				Log.i(tag, "MediaScannerReceiver 开始扫描");
			} else if (arg1.getAction().equals(
					Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
				Log.i(tag, "MediaScannerReceiver 结束扫描");
				DProvider dprovider = DProvider.getInstance(getActivity());
				dprovider.InitDate();
				Thread2 thread2 = new Thread2();
				thread2.start();
				refreshableView.finishRefreshing();
				Message msg = new Message();
				msg.what = 4;
				myhandler.sendMessage(msg);
			}
		}

	}

	@Override
	public void onPause() {
		Log.i("LocalActivity", "LocalActivity onPause");
		super.onPause();
		// activiti暂停时保存当前选择的播放列表
		SharedPreferences.Editor ed = mPrefs.edit();
		if (!selection.equals(getString(R.string.playlist_default_last))) {
			ed.putString("spinner_value", selection);
			Log.i("LocalActivity ", "SharedPreferences is" + selection);
		} else {
			ed.putString("spinner_value", getString(R.string.playlist_default));
			Log.i("LocalActivity ", "SharedPreferences is"
					+ getString(R.string.playlist_default));
		}
		ed.commit();
	}

	@Override
	public void onStop() {
		Log.i("LocalActivity", "LocalActivity is onStop");
		super.onStop();
		if (myhandler != null) {
			myhandler.removeCallbacksAndMessages(null);
		}
		if (receiver1 != null) {
			getActivity().unregisterReceiver(receiver1);
			receiver1 = null;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (myhandler != null) {
			myhandler.removeCallbacksAndMessages(null);
		}
		if (receiver1 != null) {
			getActivity().unregisterReceiver(receiver1);
			receiver1 = null;
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
			Log.i("localactivity onDestroy",
					"localactivity onDestroy cursor 关闭");
		}
	}

}
