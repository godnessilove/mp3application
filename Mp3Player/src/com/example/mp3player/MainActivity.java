package com.example.mp3player;

import com.example.sqlite.DProvider;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.Menu;
import android.widget.Spinner;

import com.example.dialog.NewPlaylistDialog;

@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity implements
		NewPlaylistDialog.DialogListener {
	private static final int UPDATE = 1;
	private static final int ABOUT = 2;
	private FragmentTabHost tab;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		tab = (FragmentTabHost) findViewById(android.R.id.tabhost);
		tab.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		tab.addTab(tab.newTabSpec("tab222").setIndicator("本地列表"),
				LocalActivity.class, null);
		tab.addTab(tab.newTabSpec("tab111").setIndicator("服务器列表"),
				RemoteActivity.class, null);

	}

	@Override
	protected void onPause() {
		super.onPause();
		System.out.println("MainActivity is onPause");
	}

	@Override
	protected void onResume() {
		// 每次回到activity的時候刷新现有全波MP3播放类表
		super.onResume();
		System.out.println("MainActivity is onResume");
		/*
		 * Thread thread = new Thread(createlocaltable); thread.start();
		 */
		if (handler == null) {
			handler = new Handler();
		}
		handler.post(createlocaltable);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		String fag = tab.getCurrentTabTag();
		if (fag.equals("tab111")) {
			menu.clear();
			menu.add(0, UPDATE, 0, "更新列表");
			menu.add(0, ABOUT, 0, "关于");
		} else if (fag.equals("tab222")) {
			menu.clear();
			menu.add(0, UPDATE, 0, "更新播放列表");
			menu.add(0, ABOUT, 0, "刪除目前的播放列表");
		}
		return super.onPrepareOptionsMenu(menu);
	}

	Runnable createlocaltable = new Runnable() {

		@Override
		public void run() {
			DProvider dprovider = DProvider
					.getInstance(getApplicationContext());// new
															// DProvider(getApplicationContext());
			dprovider.initAllList();
		}
	};

	@Override
	public void onArticleSelected(int position) {
		System.out.println("mainactivity响应dialogfragment " + position);
		LocalActivity fm = (LocalActivity) getSupportFragmentManager()
				.findFragmentByTag("tab222");
		Spinner spinner = (Spinner) fm.getActivity()
				.findViewById(R.id.spinner1);
		spinner.setSelection(0);
	}

}
