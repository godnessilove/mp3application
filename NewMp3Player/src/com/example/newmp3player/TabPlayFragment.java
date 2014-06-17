package com.example.newmp3player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.example.lrc.LrcProcess;
import com.example.lrc.LrcView;
import com.example.service.Mp3PlayService;
import com.example.sqlite.DProvider;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

@SuppressLint("NewApi")
public class TabPlayFragment extends Fragment {
	// private String tag = "TabPlayFragment";

	private ImageButton button1 = null;
	private ImageButton button2 = null;
	private ImageButton button3 = null;
	private SeekBar seekbar = null;
	// private TextView textview = null;
	private com.example.lrc.LrcView lrctext;
	private View view;
	private TextView mp3nametext = null;
	private TextView textDuration;
	private TextView textAlltime;
	private TextView artist;
	private TextView album;
	private String thumb;
	private String mp3name = null;
	private String mp3path = null;
	private String artisttext = null;
	private String albumtext = null;
	private String lrcpath = null;
	private Long[] longtime = null;
	private int alltime = 100;
	private int mDuration = 0;

	long offset = 0;
	long wait = 0;
	private Mp3PlayService mp3serivce;
	private SharedPreferences mPrefs;

	private HashMap<Long, String> lrcs = null;
	private Handler handler = null;

	private Boolean ispause;

	private String mp3listname;
	private Receiver receiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
		mPrefs = getActivity().getSharedPreferences(LocalActivity.MP3_SHARED,
				Context.MODE_PRIVATE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.playfragment, container, false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (isVisible()) {
			switch (item.getItemId()) {
			case 1:
				Intent intent1 = new Intent(getActivity(),
						PreferencesActi.class);
				intent1.putExtra("flag", "end");
				getActivity().startService(intent1);
				SharedPreferences sh = PreferenceManager
						.getDefaultSharedPreferences(getActivity());
				SharedPreferences.Editor ed = sh.edit();
				ed.putString("timer", "0");
				ed.commit();
				StopPlay();
			case 2:
				Intent intent = new Intent();
				intent.setClass(getActivity(), PreferencesActi.class);
				startActivity(intent);
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mp3serivce = null;

		seekbar = (SeekBar) getActivity().findViewById(R.id.seekBar1);
		button1 = (ImageButton) getActivity().findViewById(R.id.PlayButton);
		button2 = (ImageButton) getActivity().findViewById(R.id.NextButton);
		button3 = (ImageButton) getActivity().findViewById(R.id.PlayMode);
		lrctext = (LrcView) getActivity().findViewById(R.id.Lyric);
		mp3nametext = (TextView) getActivity().findViewById(R.id.Mp3Name);
		textDuration = (TextView) getActivity().findViewById(R.id.mDuration);
		textAlltime = (TextView) getActivity().findViewById(R.id.alltime);
		artist = (TextView) getActivity().findViewById(R.id.Singer);
		album = (TextView) getActivity().findViewById(R.id.Album);

		lrctext.setAlpha(150);

		setPlayModeImage();

		button1.setOnClickListener(new Mp3Start());
		button2.setOnClickListener(new Mp3NextOne());
		button3.setOnClickListener(new PlayModeSelect());
		button1.getBackground().setAlpha(0);
		button2.getBackground().setAlpha(0);
		button3.getBackground().setAlpha(0);

		seekbar.setOnSeekBarChangeListener(new seekBarListener());
		if (handler == null) {
			handler = new Handler();
		}

	}

	/**
	 * 设置播放模式的图标
	 */
	public void setPlayModeImage() {
		String playmode = mPrefs.getString("playmode", "orderplay");
		if (playmode.equals("orderplay")) {
			button3.setImageResource(R.drawable.ic_mp_repeat_all_btn);
		} else if (playmode.equals("loopplay")) {
			button3.setImageResource(R.drawable.ic_mp_repeat_once_btn);
		} else {
			button3.setImageResource(R.drawable.ic_mp_shuffle_on_btn);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		SharedPreferences.Editor ed = mPrefs.edit();
		ed.putString("play_list", mp3listname);
		ed.putString("play_name", mp3name);
		ed.commit();
	}

	/**
	 * 初始化播放信息
	 * 
	 * @param bundle
	 *            正确保存的歌曲信息
	 */
	public void InitPlayInfo() {
		DProvider dprovider = DProvider.getInstance(getActivity());
		mp3listname = mPrefs.getString("play_list",
				getString(R.string.playlist_default));
		List<String> playlist = (ArrayList<String>) dprovider.queryList();
		if (playlist.contains(mp3listname)) {
			mp3name = mPrefs.getString("play_name", dprovider.querymp3def());
			// 判斷改文件列表中是否包含此mp3歌曲
			boolean isexists = dprovider.querymp3(mp3listname, mp3name);
			if (!isexists) {
				mp3name = dprovider.querymp3def();
			}
		} else {
			mp3listname = getString(R.string.tablelocalname);
			mp3name = dprovider.querymp3def();
		}
		updateMp3info();
		// 处理歌词
		Lrc();
		// 设置当前进度条
		if (mPrefs.getBoolean("ispause", false)) {
			alltime = mPrefs.getInt("alltime", 100);
			seekbar.setMax(alltime);
			mDuration = mPrefs.getInt("mDuration", 0);
			seekbar.setProgress(mDuration);
			textDuration.setText(showTime(mDuration));
			textAlltime.setText(showTime(alltime));
		}
	}

	/**
	 * 换歌曲的时候更新mp3信息
	 */
	public void updateMp3info() {
		DProvider dprovider = DProvider.getInstance(getActivity());
		if (mp3name != null) {
			String[] mp3info = dprovider.queryMp3info(mp3name);
			mp3path = mp3info[0];
			lrcpath = mp3info[1];
			artisttext = mp3info[2];
			albumtext = mp3info[3];
			thumb = mp3info[4];
		}
	}

	// 设置与mp3service绑定时的操作
	private ServiceConnection conn = new ServiceConnection() {

		// 连接时，获得mp3service的实例
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mp3serivce = ((Mp3PlayService.MyBinder) service).getService();
		}

		// 链接失败
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mp3serivce = null;
		}

	};

	@Override
	public void onStart() {
		view = getView();
		InitPlayInfo();
		ispause = mPrefs.getBoolean("ispause", true);
		// bindservice
		if (mp3serivce == null) {
			Intent intent1 = new Intent();
			intent1.setClass(TabPlayFragment.this.getActivity(),
					Mp3PlayService.class);
			getActivity().bindService(intent1, conn, Service.BIND_AUTO_CREATE);
		}

		// 注册广播
		if (receiver == null) {
			receiver = new Receiver();
			IntentFilter filter = new IntentFilter(Mp3PlayService.UPDATE_ACTION);
			filter.addAction(Mp3PlayService.STOP_ACTION);
			TabPlayFragment.this.getActivity().registerReceiver(receiver,
					filter);
		}

		// 自动播放
		if (!ispause) {
			AutoPlay();
		} else {
			handler.removeCallbacksAndMessages(null);
			// 更换开始暂停image
			handler.post(update);
			// 更新显示当前播放mp3名字
			handler.post(updatemp3info);
			// 更新seek
			handler.post(updateSeek);
			// 播放歌词
			handler.post(r);
		}
		super.onStart();
	}

	@Override
	public void onStop() {
		// 点其他歌曲播放的时候，删掉前一个歌词的线程
		handler.removeCallbacksAndMessages(null);
		/*
		 * // 注销广播 if (receiver != null) {
		 * getActivity().unregisterReceiver(receiver); receiver = null; }
		 */
		// 解綁service
		if (mp3serivce != null) {
			getActivity().unbindService(conn);
			mp3serivce = null;
		}
		super.onStop();
	}

	/**
	 * 点击播放列表中的歌曲，立即在该页面中更新播放
	 * 
	 * @param bundle
	 */
	public void SetMp3Info(Bundle bundle) {
		mp3listname = bundle.getString("mp3listname");
		mp3name = bundle.getString("mp3name");
		updateMp3info();
		Lrc();
		AutoPlay();
	}

	/**
	 * 自動播放
	 */
	public void AutoPlay() {
		Intent intent = new Intent();
		intent.setClass(TabPlayFragment.this.getActivity(),
				Mp3PlayService.class);
		intent.putExtra("mp3path", mp3path);
		intent.putExtra("msg", "START");
		intent.putExtra("mp3name", mp3name);
		intent.putExtra("mp3listname", mp3listname);
		ispause = false;
		getActivity().startService(intent);
		// 清除所有已有的操作
		handler.removeCallbacksAndMessages(null);
		// 更换开始暂停image
		handler.post(update);
		// 更新显示当前播放mp3名字
		handler.post(updatemp3info);
		// 更新seek
		handler.post(updateSeek);
		// 播放歌词
		handler.post(r);
	}

	
	/**
	 * 解析歌词
	 */
	public void Lrc() {
		LrcProcess lrcprocess = new LrcProcess();
		if (lrcpath != null) {
			lrcs = lrcprocess.process(lrcpath);
			longtime = LrcProcess.GetAllTime(lrcs);
			int maxtime = getMaxLength(lrcs);
			lrctext.setMax(maxtime);
			lrctext.setTimes(longtime);
		}
	}

	/**
	 * 获取歌词中长度最长的长度
	 * @param lrcs
	 * @return
	 */
	public int getMaxLength(HashMap<Long, String> lrcs) {
		int max = 15;
		if (lrcs != null) {
			Collection<String> temp = lrcs.values();
			for (Iterator<String> iterator = temp.iterator(); iterator
					.hasNext();) {
				String string = (String) iterator.next();
				if (max <= string.length()) {
					max = string.length();
				}
			}
		}
		return max;
	}

	/**
	 * 将毫秒转换为00:00格式
	 * 
	 * @param duration
	 * @return
	 */
	public String showTime(int duration) {
		duration = duration / 1000;
		int min = duration / 60;
		int second = duration % 60;
		return String.format(Locale.getDefault(), "%02d:%02d", min, second);
	}

	class seekBarListener implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (fromUser) {
				Float f = (float) progress / seekBar.getMax();
				mp3serivce.setPro(f);
				handler.removeCallbacks(r);
				handler.post(r);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {

		}

	}

	class Mp3Start implements OnClickListener {

		@Override
		public void onClick(View v) {
			// 有可能是换了个mp3，删掉前一个歌词的runnable
			handler.removeCallbacks(updateSeek);
			handler.removeCallbacks(r);
			// 如果已经是开始播放，点击按钮意味着暂停播放，反之开始播放
			if (!ispause) {
				Intent intent = new Intent();
				intent.setClass(TabPlayFragment.this.getActivity(),
						Mp3PlayService.class);
				intent.putExtra("msg", "PAUSE");
				intent.putExtra("mp3name", mp3name);
				getActivity().startService(intent);
				ispause = true;
				// 更换开始暂停image
				handler.post(update);
			} else {
				Intent intent = new Intent();
				intent.setClass(TabPlayFragment.this.getActivity(),
						Mp3PlayService.class);
				intent.putExtra("mp3path", mp3path);
				intent.putExtra("msg", "START");
				intent.putExtra("mp3name", mp3name);
				intent.putExtra("mDuration", mDuration);
				ispause = false;
				getActivity().startService(intent);
				// 更换开始暂停image
				handler.post(update);

			}
			// 重新开始显示歌词和seek
			handler.post(updateSeek);
			handler.post(r);
		}

	}

	class Mp3NextOne implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(TabPlayFragment.this.getActivity(),
					Mp3PlayService.class);
			intent.putExtra("msg", "NEXT");
			intent.putExtra("mp3path", mp3path);
			getActivity().startService(intent);
		}
	}

	class PlayModeSelect implements OnClickListener {

		@Override
		public void onClick(View v) {
			String playmode = mPrefs.getString("playmode", "orderplay");
			SharedPreferences.Editor ed = mPrefs.edit();
			if (playmode.equals("orderplay")) {
				ed.putString("playmode", "loopplay");
				button3.setImageResource(R.drawable.ic_mp_repeat_once_btn);
			} else if (playmode.equals("loopplay")) {
				ed.putString("playmode", "randomplay");
				button3.setImageResource(R.drawable.ic_mp_shuffle_on_btn);
			} else {
				ed.putString("playmode", "orderplay");
				button3.setImageResource(R.drawable.ic_mp_repeat_all_btn);
			}
			ed.commit();
		}

	}

	/**
	 * 停止改activity並且停止mp3service，
	 */
	public void StopPlay() {
		// 当mp3service被绑定的时候，并且使用过startservice，如果要停止必须要先解绑
		if (mp3serivce != null) {
			getActivity().unbindService(conn);
			mp3serivce = null;
		}
		Intent intent = new Intent();
		intent.setClass(TabPlayFragment.this.getActivity(),
				Mp3PlayService.class);
		intent.putExtra("msg", "STOP");
		getActivity().startService(intent);
		ispause = true;
		handler.post(update);
		// getActivity().finish();
		System.exit(0);
	}

	// 更新当天播放Mp3名字
	Runnable updatemp3info = new Runnable() {

		@Override
		public void run() {
			mp3nametext.setText(mp3name);
			artist.setText(artisttext);
			album.setText(albumtext);
			if (thumb == null) {
				view.setBackgroundResource(R.drawable.albumart_mp_unknown);
			} else {
				Bitmap bm = BitmapFactory.decodeFile(thumb);
				if (bm != null) {
					BitmapDrawable bd = new BitmapDrawable(getResources(), bm);
					view.setBackground(bd);
				} else {
					view.setBackgroundResource(R.drawable.albumart_mp_unknown);
				}
			}
			view.getBackground().setAlpha(100);
		}

	};

	// 更新按钮图标
	Runnable update = new Runnable() {

		@Override
		public void run() {
			if (!ispause) {
				button1.setImageResource(android.R.drawable.ic_media_pause);
			} else {
				button1.setImageResource(android.R.drawable.ic_media_play);
			}
		}
	};

	// 更新歌词
	Runnable r = new Runnable() {

		@Override
		public void run() {
			if (longtime != null) {
				if (mp3serivce != null) {
					// 通过service获取当前播放时间点
					mDuration = mp3serivce.getPlayTime();
				}
				kan(mDuration);
			} else {
				lrctext.invalidate();
			}
		}
	};

	/**
	 * 更新歌词
	 * 
	 * @param mDuration
	 *            当前播放时间
	 */
	public void kan(long mDuration) {
		for (int i = 0; i < longtime.length - 1; i++) {
			if (longtime[i] <= mDuration && mDuration < longtime[i + 1]) {
				lrctext.setIndex(i);
				lrctext.invalidate();
				// 等待本行歌词与下行歌词之间的时间差作为参数，再一次执行显示歌词runnable,不是暂停就继续更新跟新
				if (!ispause) {
					handler.postDelayed(r, longtime[i + 1] - mDuration);
				}
				break;
			} else if (mDuration >= longtime[longtime.length - 1]) {
				lrctext.setIndex(longtime.length - 1);
				lrctext.invalidate();
			}
		}

	}

	// 根据当前播放时间来跟新seekbar
	Runnable updateSeek = new Runnable() {

		@Override
		public void run() {
			if (mp3serivce != null) {
				alltime = mp3serivce.getallTime();
				mDuration = mp3serivce.getPlayTime();
			}
			if (!ispause && mDuration >= 0 && mDuration < alltime) {
				textDuration.setText(showTime(mDuration));
				textAlltime.setText(showTime(alltime));
				seekbar.setMax(alltime);
				seekbar.setProgress(mDuration);
				handler.postDelayed(updateSeek, 1000);
			}
		}
	};

	// 接收广播处理,自动播放下一首的时候、通知栏点击下一首、开始暂停按钮的时候
	class Receiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if (arg1.getAction().equals(Mp3PlayService.UPDATE_ACTION)) {
				mp3name = arg1.getStringExtra("mp3name");
				updateMp3info();
				// mp3path = arg1.getStringExtra("mp3path");
				// 当前是否在播放
				boolean isplay = arg1.getBooleanExtra("isplay", ispause);
				if (isplay) {
					ispause = false;
				} else {
					ispause = true;
				}
				Lrc();
				// 更新图标
				handler.post(update);
				// 更新播放mp3名称
				handler.post(updatemp3info);
				// 重新加载歌词和更新seek
				handler.removeCallbacks(updateSeek);
				handler.removeCallbacks(r);
				handler.post(updateSeek);
				handler.post(r);
			} else if (arg1.getAction().equals(Mp3PlayService.STOP_ACTION)) {
				StopPlay();
			}
		}
	}

	@Override
	public void onDestroy() {
		// 注销广播接收
		if (receiver != null) {
			getActivity().unregisterReceiver(receiver);
			receiver = null;
		}
		// 解綁service
		if (mp3serivce != null) {
			getActivity().unbindService(conn);
			mp3serivce = null;
		}
		handler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}

}
