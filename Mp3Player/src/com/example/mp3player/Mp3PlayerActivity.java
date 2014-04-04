package com.example.mp3player;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.lrc.LrcProcess;
import com.example.service.Mp3PlayService;
import com.example.sqlite.DProvider;


@SuppressLint("CommitPrefEdits")
public class Mp3PlayerActivity extends Activity {
	private ImageButton button1 = null;
	private ImageButton button2 = null;
	private ImageButton button3 = null;
	private TextView textview = null;
	private TextView mp3nametext = null;
	private String mp3name = null;
	private String mp3path = null;
	private String lrcpath = null;
	private Long[] longtime = null;
	long offset = 0;
	long wait = 0;
	private long playtime = 0;
	private Mp3PlayService mp3serivce = null;

	private HashMap<Long, String> lrcs = null;
	private String lrc = null;
	private Handler handler = null;

	private Boolean ispause = true;
	private Boolean isstop = false;
	
	private String mp3listname;
	private Receiver receiver ;
	//用来判断广播是否已经注册
	private Boolean ISREGISTER = false;
	//设置接收广播的条件
	public static final String UPDATE_ACTION = "chris.mp3.action.UPDATE_ACTION";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		System.out.println("activity 创建");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.mp3player);
		Intent intent = getIntent();
		mp3listname = intent.getStringExtra("mp3listname");
		mp3name = intent.getStringExtra("mp3name");
		mp3path = intent.getStringExtra("mp3path");
		
		SharedPreferences sdpfs = getPreferences(LocalActivity.SPINNER_STATE);
		SharedPreferences.Editor ed = sdpfs.edit();
		ed.putString("spinner_value", mp3listname);
		
		button1 = (ImageButton) findViewById(R.id.imageButton1);
		button2 = (ImageButton) findViewById(R.id.imageButton2);
		button3 = (ImageButton) findViewById(R.id.imageButton3);
		button1.setOnClickListener(new Mp3Start());
		button2.setOnClickListener(new Mp3NextOne());
		button3.setOnClickListener(new Mp3End());
		textview = (TextView) findViewById(R.id.textView1);
		mp3nametext = (TextView) findViewById(R.id.textView2);
		
		lrcpath = intent.getStringExtra("lrcpath");
		LrcProcess lrcprocess = new LrcProcess();
		lrcs = lrcprocess.process(lrcpath);
		longtime = LrcProcess.GetAllTime(lrcs);
		if (handler == null) {
			handler = new Handler();
		}
		
		button1.setImageDrawable(getResources().getDrawable(
				android.R.drawable.ic_media_pause));
	}

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mp3serivce = ((Mp3PlayService.MyBinder) service).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mp3serivce = null;
		}

	};

	@Override
	protected void onStart() {
		System.out.println("activity 开始");
		AutoPlay();
		super.onStart();
	}

	@Override
	protected void onRestart() {
		System.out.println("activity 重新开始");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		System.out.println("activity 恢复");
		//bindservice
		if (mp3serivce == null) {
			Intent intent1 = new Intent();
			intent1.setClass(Mp3PlayerActivity.this, Mp3PlayService.class);
			bindService(intent1, conn, BIND_AUTO_CREATE);
		}
		
		//注册广播
		receiver = new Receiver();
		IntentFilter filter = new IntentFilter(Mp3PlayerActivity.UPDATE_ACTION);
		Mp3PlayerActivity.this.registerReceiver(receiver, filter);
		ISREGISTER = true;
		
		handler.removeCallbacksAndMessages(null);
		handler.post(updatemp3name);
		handler.post(r);
		super.onResume();
	}

	@Override
	protected void onPause() {
		System.out.println("activity 暂停");
		handler.removeCallbacksAndMessages(null);// 点其他歌曲播放的时候，删掉前一个歌词的线程
		super.onPause();
	}

	@Override
	protected void onStop() {
		System.out.println("activity 停止");
		if(ISREGISTER){
		unregisterReceiver(receiver);
		ISREGISTER = false;
		}
		handler.removeCallbacksAndMessages(null);// 点其他歌曲播放的时候，删掉前一个歌词的线程
		super.onStop();
	}

	public void AutoPlay() {
		Intent intent = new Intent();
		intent.setClass(Mp3PlayerActivity.this, Mp3PlayService.class);
		intent.putExtra("mp3path", mp3path);
		intent.putExtra("msg", "START");
		intent.putExtra("mp3name", mp3name);
		intent.putExtra("mp3listname", mp3listname);
		ispause = false;
		isstop = false;
		startService(intent);
		handler.post(update);// 更换开始暂停image
	}

	class Mp3Start implements OnClickListener {

		@Override
		public void onClick(View v) {
			// 点其他歌曲播放的时候，删掉前一个歌词的线程
			handler.removeCallbacks(r);
			if (!ispause) {
				Intent intent = new Intent();
				intent.setClass(Mp3PlayerActivity.this, Mp3PlayService.class);
				intent.putExtra("msg", "PAUSE");
				intent.putExtra("mp3name", mp3name);
				startService(intent);
				ispause = true;
				handler.post(update);// 更换开始暂停image
			} else {
				Intent intent = new Intent();
				intent.setClass(Mp3PlayerActivity.this, Mp3PlayService.class);
				intent.putExtra("mp3path", mp3path);
				intent.putExtra("msg", "START");
				intent.putExtra("mp3name", mp3name);
				ispause = false;
				isstop = false;
				startService(intent);
				handler.post(update);// 更换开始暂停image

			}
			handler.post(r);
		}

	}

	
	
	class Mp3NextOne implements OnClickListener {

		@Override
		public void onClick(View v) {
			DProvider dprovider = DProvider.getInstance(getApplicationContext());
			Map<String,String> map = dprovider.nextMp3(mp3path, mp3listname);
			mp3name = map.get("mp3name");
			mp3path = map.get("mp3path");
			AutoPlay();
			lrcpath = mp3path.replace("mp3", "lrc");
			LrcProcess lrcprocess = new LrcProcess();
			lrcs = lrcprocess.process(lrcpath);
			longtime = LrcProcess.GetAllTime(lrcs);
			//更新播放mp3名称
			handler.post(updatemp3name);
			//重新加载歌词
			handler.removeCallbacks(r);
			handler.post(r);
		}

	}
	
	//public 

	class Mp3End implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(Mp3PlayerActivity.this, Mp3PlayService.class);
			intent.putExtra("msg", "STOP");
			startService(intent);
			ispause = false;
			isstop = true;
			handler.post(update);// 更换开始暂停image
			finish();
			System.out.println("activity退出");
		}

	}

	Runnable updatemp3name = new Runnable(){

		@Override
		public void run() {
			mp3nametext.setText(mp3name);
		}
		
	};

	Runnable update = new Runnable() {

		@Override
		public void run() {
			if (!ispause) {
				button1.setImageDrawable(getResources().getDrawable(
						android.R.drawable.ic_media_pause));
			} else {
				button1.setImageDrawable(getResources().getDrawable(
						android.R.drawable.ic_media_play));
			}
		}
	};

	Runnable r = new Runnable() {
		@Override
		public void run() {
			if (longtime != null) {
				if (mp3serivce != null) {
					playtime = mp3serivce.getPlayTime();
				} else
					playtime = 0;
				System.out.println("playtime is " + playtime);
				if (!ispause && !isstop)
					kan(playtime);
			}else textview.setText("未找到歌词");
		}
	};

	public void kan(long playtime) {
		for (int i = 0; i < longtime.length - 1; i++) {
			if (playtime >= longtime[longtime.length - 1]) {
				lrc = lrcs.get(longtime[longtime.length - 1]);
				textview.setText(lrc);
				System.out.println("开始更换歌词 ,歌词时间"
						+ longtime[longtime.length - 1] + "playtime is "
						+ playtime + " lrc :" + lrc);
				break;
			} else if (longtime[i] <= playtime && playtime < longtime[i + 1]) {
				lrc = lrcs.get(longtime[i]);
				textview.setText(lrc);
				System.out.println("开始更换歌词  ,歌词时间"
						+ longtime[longtime.length - 1] + "playtime is "
						+ playtime + " lrc :" + lrc);
				handler.postDelayed(r, longtime[i + 1] - playtime);
			}
		}
	}

	class Receiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			mp3name = arg1.getStringExtra("mp3name");
			mp3path = arg1.getStringExtra("mp3path");
			AutoPlay();
			lrcpath = mp3path.replace("mp3", "lrc");
			LrcProcess lrcprocess = new LrcProcess();
			lrcs = lrcprocess.process(lrcpath);
			longtime = LrcProcess.GetAllTime(lrcs);
			//更新播放mp3名称
			handler.post(updatemp3name);
			//重新加载歌词
			handler.removeCallbacks(r);
			handler.post(r);
		}
		
	}

	
	
	

	@Override
	protected void onDestroy() {
		System.out.println("activity 被销毁");
		unbindService(conn);
		if(ISREGISTER){
		unregisterReceiver(receiver);
		ISREGISTER = false;
		}
		super.onDestroy();
	}

}
