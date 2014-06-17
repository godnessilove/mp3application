package com.example.service;

import java.io.IOException;
import java.util.Map;

import com.example.newmp3player.LocalActivity;
import com.example.newmp3player.R;
import com.example.notification.Mp3Notification;
import com.example.sqlite.DProvider;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

@SuppressLint({ "NewApi", "CommitPrefEdits" })
public class Mp3PlayService extends Service implements
		MediaPlayer.OnErrorListener {
	private String tag = "Mp3PlayService";
	private String msg = null;
	private String mp3name = null;
	private MediaPlayer m = null;
	private int threadid;
	// private int startId;
	// 默认刚开始的时候是没有获得焦点的
	private int audiofocus = AudioManager.AUDIOFOCUS_REQUEST_FAILED;
	private Boolean ispause = false;
	private String mp3path = null;
	private AudioManager audiomanager = null;
	private String mp3listname;
	private Mp3Notification notification;
	private SharedPreferences mpfres;
	private MusicIntentReceiver stopreceiver;
	// 更新mp3activity显示
	public static final String UPDATE_ACTION = "com.example.service.UPDATE_ACTION";
	public static final String STOP_ACTION = "com.example.service.STOP_ACTION";
	public static final String TIME_OUT_ACTION = "com.example.service.TIME_OUT_ACTION";
	private boolean mbind = false;
	private String playmode;
	private int mDuration;

	/**
	 * 获取当前播放时间
	 * 
	 * @return 返回播放时间
	 */
	public int getPlayTime() {
		if (m != null) {
			return m.getCurrentPosition();
		} else
			return 0;
	}

	/**
	 * 获取当前歌曲总共时间
	 * 
	 * @return 整个时间
	 */
	public int getallTime() {
		if (m != null) {
			return m.getDuration();
		} else
			return 0;
	}

	/**
	 * 获取当前播放歌曲名
	 * 
	 * @return 返回现在播放的名称
	 */
	public String getPlayingName() {
		if (m != null) {
			return mp3name;
		} else
			return null;
	}

	/**
	 * 获取mp3播放器
	 * 
	 * @return 返回m播放器实例
	 */
	public MediaPlayer getMedia() {
		if (m != null) {
			return m;
		} else
			return null;
	}

	public void setPro(Float f) {
		if (m != null) {
			int postion = (int) (m.getDuration() * f);
			Log.i(tag, "f is " + f + ",postion is " + postion);
			m.seekTo(postion);
		}
	}

	@Override
	public void onCreate() {
		notification = Mp3Notification.getInstance(getApplicationContext());
		mpfres = getApplication().getSharedPreferences(
				LocalActivity.MP3_SHARED, Context.MODE_PRIVATE);
		if (stopreceiver == null) {
			stopreceiver = new MusicIntentReceiver();
			IntentFilter filter = new IntentFilter(
					android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY);
			filter.addAction(TIME_OUT_ACTION);
			this.registerReceiver(stopreceiver, filter);
		}
		Log.i(tag, "创建mp3service,注册noisy广播");
		super.onCreate();
	}

	public class MyBinder extends Binder {
		public Mp3PlayService getService() {
			return Mp3PlayService.this;
		}
	}

	private final MyBinder myBinder = new MyBinder();

	@Override
	public IBinder onBind(Intent arg0) {
		mbind = true;
		Log.i(tag, "绑定mp3service");
		return myBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		mbind = false;
		Log.i(tag, "解绑mp3service ");
		return true;
	}

	@Override
	public void onRebind(Intent intent) {
		mbind = true;
		Log.i(tag, "重新绑定mp3service");
		super.onRebind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		threadid = Thread.currentThread().hashCode();
		msg = intent.getStringExtra("msg");
		// this.startId = startId;
		// 放在前台，手机长时间休眠时，不会被kill
		notification.notificationstartForeground(Mp3PlayService.this, threadid);
		Log.i(tag, "onStartCommand" + msg);
		if (msg.equals("START")) {
			// 如果沒有播放器
			if (m == null) {
				mp3path = intent.getStringExtra("mp3path");
				mp3name = intent.getStringExtra("mp3name");
				mp3listname = intent.getStringExtra("mp3listname");
				initMediaPlayer();
				notification.createNotifi(threadid, mp3name, mp3path,
						mp3listname);
				mp3start();
				// getAudioFocus();
			}
			// 点击播放列表切换歌曲的时候
			else if (mp3name.equals(intent.getStringExtra("mp3name")) == false) {
				//切换歌曲.肯定是新的歌曲开头开始播放
				SharedPreferences.Editor ed = mpfres.edit();
				ed.putInt("mDuration", 0);
				ed.commit();
				mp3path = intent.getStringExtra("mp3path");
				mp3name = intent.getStringExtra("mp3name");
				mp3listname = intent.getStringExtra("mp3listname");
				initMediaPlayer();
				mp3start();
				notification.updateMp3Notifi(threadid, mp3name, mp3path,
						mp3listname);
			}
			// 歌曲没变的时候
			else {
				if (!m.isPlaying()) {
					mp3start();
				}
			}
		} else if (msg.equals("PAUSE")) {
			Log.i(tag, "PAUSE");
			mp3pause();
		} else if (msg.equals("STOP")) {
			Log.i(tag, "STOP");
			mp3stop();
		} else if (msg.equals("NEXT")) {
			Log.i(tag, "NEXT");
			nextmp3();
		} else if (msg.equals("PLAY-PAUSE")) {
			Log.i(tag, "PLAY-PAUSE");
			if (m.isPlaying()) {
				mp3pause();
				updateActivity(mp3name, mp3path);
			} else {
				mp3start();
				updateActivity(mp3name, mp3path);
			}
		}

		return START_NOT_STICKY;
	}

	/**
	 * 获取当前audio焦点，成功了才能播放
	 */
	public boolean getAudioFocus() {
		if (audiomanager == null) {
			getApplicationContext();
			audiomanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			audiofocus = audiomanager.requestAudioFocus(listener,
					AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

		}
		if (audiofocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			// mp3start();
			Log.i("Mp3PlayService getAudioFocus()", "获得audio焦点");
			return true;
		} else {
			Log.i("Mp3PlayService getAudioFocus()", "未获得audio焦点");
			return false;
		}
	}

	/**
	 * 初始化播放器
	 */
	public void initMediaPlayer() {
		// 如果不存在播放器则新建一个,否则就重置
		if (m == null) {
			m = new MediaPlayer();
		} else {
			m.reset();// 点击下一首，重置释放资源，加载下一首歌
		}
		try {
			m.setDataSource(mp3path);
			m.prepare();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 每次播放初始化的时候查询播放模式
		playmode = mpfres.getString("playmode", "orderplay");
		if (playmode.equals("loopplay")) {
			m.setLooping(true);
		} else {
			m.setLooping(false);
		}
		// 设置手机休眠时，播放器仍可正常播放,但是暂停和停止后还是有可能会被关闭
		m.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		// 设置错误监听
		m.setOnErrorListener(this);
		// 设置播放完一首后自动播放下一首
		m.setOnCompletionListener(completion);
	}

	/**
	 * 开始播放
	 */
	public void mp3start() {
		// 保存的暂停的时候，播放到了哪里
		mDuration = mpfres.getInt("mDuration", 0);
		m.seekTo(mDuration);
		if (getAudioFocus()) {
			m.start();
		}
		// 更新通知栏为相应的图标
		notification.updateButtonImage(threadid, true);
		// 保存播放状态
		SharedPreferences.Editor ed = mpfres.edit();
		ed.putBoolean("ispause", !m.isPlaying());
		ed.putString("play_list", mp3listname);
		ed.putString("play_name", mp3name);
		ed.putString("play_path", mp3path);
		ed.commit();
	}

	/**
	 * 暂停播放
	 */
	public void mp3pause() {
		if (m != null && m.isPlaying()) {
			m.pause();
			// 更新通知栏为相应的图标
			notification.updateButtonImage(threadid, false);
			SharedPreferences.Editor ed = mpfres.edit();
			ed.putBoolean("ispause", !m.isPlaying());
			ed.putString("play_list", mp3listname);
			ed.putString("play_name", mp3name);
			ed.putString("play_path", mp3path);
			ed.putInt("alltime", m.getDuration());
			ed.putInt("mDuration", m.getCurrentPosition());
			ed.commit();
		}
	}

	/**
	 * 停止播放
	 */
	public void mp3stop() {
		Log.i(tag, "stopself");
		SharedPreferences.Editor ed = mpfres.edit();
		ed.putString("play_list", mp3listname);
		ed.putString("play_name", mp3name);
		ed.putString("play_path", mp3path);
		ed.commit();
		stopSelf();
	}

	/**
	 * 读取下一首的时候判断播放模式，对应播放
	 */
	public void nextmp3() {
		DProvider dprovider = DProvider.getInstance(getApplicationContext());
		playmode = mpfres.getString("playmode", "orderplay");
		mp3listname = mpfres.getString("play_list",
				getString(R.string.playlist_default));
		if (mp3path == null) {
			mp3path = mpfres
					.getString("play_path", dprovider.querymp3defpath());
		}
		// 如果当前的播放列表被删除了，那么从"全部歌曲"本地存在的mp3里面查询
		if (!dprovider.isListExists(mp3listname)) {
			mp3listname = getString(R.string.playlist_default);
		}
		if (playmode.equals("orderplay")) {
			// 点击通知栏换下一首歌的时候
			Map<String, String> map = dprovider
					.nextMp3(mp3path, mp3listname, 0);
			mp3name = map.get("mp3name");
			mp3path = map.get("mp3path");
		} else if (playmode.equals("loopplay")) {
		} else if (playmode.equals("randomplay")) {
			Map<String, String> map = dprovider
					.nextMp3(mp3path, mp3listname, 1);
			mp3name = map.get("mp3name");
			mp3path = map.get("mp3path");
		}
		initMediaPlayer();
		// 下一首肯定是从头开始播放
		// mDuration = 0;
		SharedPreferences.Editor ed = mpfres.edit();
		ed.putInt("mDuration", 0);
		ed.commit();
		mp3start();
		notification.updateMp3Notifi(threadid, mp3name, mp3path, mp3listname);
		updateActivity(mp3name, mp3path);
	}

	@Override
	public void onDestroy() {
		if (m != null) {
			m.stop();
			SharedPreferences.Editor ed = mpfres.edit();
			ed.putBoolean("ispause", !m.isPlaying());
			ed.putInt("alltime", m.getDuration());
			ed.putInt("mDuration", m.getCurrentPosition());
			ed.commit();
			m.release();
			System.out.println("停止、释放资源");
			m = null;
		}
		// 放弃音频焦点
		if (audiomanager != null) {
			audiomanager.abandonAudioFocus(listener);
		}
		// 删除通知栏
		// notification.deleteNotifi(threadid);
		stopForeground(true);
		if (notification != null) {
			notification = null;
		}
		// 注销广播
		if (stopreceiver != null) {
			this.unregisterReceiver(stopreceiver);
			Log.i(this.toString(), "stopreceiver 解绑");
		}

		Log.i(this.toString(), "Mp3PlayService 已杀");
		super.onDestroy();
	}

	@Override
	public String toString() {
		return "Mp3PlayService [msg=" + msg + ", m=" + m + ", ispause="
				+ ispause + ", mp3path=" + mp3path + ", myBinder=" + myBinder
				+ "]";
	}

	// 当播放器出错的时候,需要重置，这里reset后会自动释放资源，不用手动调用release
	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		Log.i(tag, "onError ");
		m.reset();
		m = null;
		// stopForeground(true);
		// manager.cancelAll();
		// notification.deleteNotifi(threadid);
		// notification = null;
		stopForeground(true);
		if (notification == null) {
			notification = null;
		}
		// 放弃音频焦点
		audiomanager.abandonAudioFocus(listener);
		return true;
	}

	/**
	 * 通知Mp3PlayerActivity进行歌曲信息更新
	 * 
	 * @param mp3name
	 *            更新的mp3名字
	 * @param mp3path
	 *            更新的路径
	 */
	public void updateActivity(String mp3name, String mp3path) {
		Intent intent = new Intent(Mp3PlayService.UPDATE_ACTION);
		intent.putExtra("mp3name", mp3name);
		intent.putExtra("mp3path", mp3path);
		intent.putExtra("isplay", m.isPlaying());
		sendBroadcast(intent);
		// 保存下一首歌的播放列表和名字，方便
		SharedPreferences.Editor ed = mpfres.edit();
		ed.putString("play_list", mp3listname);
		ed.putString("play_name", mp3name);
		ed.commit();
	}

	// 自动播放完一首歌后 播放下一首歌
	OnCompletionListener completion = new OnCompletionListener() {

		@Override
		public void onCompletion(MediaPlayer mp) {
			nextmp3();
		}
	};

	// 监听声音焦点
	OnAudioFocusChangeListener listener = new OnAudioFocusChangeListener() {
		@Override
		public void onAudioFocusChange(int focusChange) {
			switch (focusChange) {
			case AudioManager.AUDIOFOCUS_GAIN:
				// resume playback
				System.out.println("focusChange , 获得焦点");
				if (m == null)
					initMediaPlayer();
				else if (!m.isPlaying())
					mp3start();
				m.setVolume(1.0f, 1.0f);
				break;

			case AudioManager.AUDIOFOCUS_LOSS:
				// Lost focus for an unbounded amount of time: stop playback and
				// release media player
				Log.i(this.toString(), "focusChange , 失去焦点");
				if (m.isPlaying())
					mp3stop();
				break;

			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				// Lost focus for a short time, but we have to stop
				// playback. We don't release the media player because playback
				// is likely to resume
				Log.i(this.toString(), "focusChange , 短暂失去");
				if (m.isPlaying())
					mp3pause();
				break;

			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				// Lost focus for a short time, but it's ok to keep playing
				// at an attenuated level
				Log.i(this.toString(), "focusChange , 保持播放，降低声音");
				if (m.isPlaying())
					m.setVolume(0.1f, 0.1f);
				break;
			}

		}
	};

	public class MusicIntentReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 例如当耳机拔出的时候,和定时到点的时候，停止播放
			if (intent.getAction().equals(
					android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)
					|| intent.getAction()
							.equals(Mp3PlayService.TIME_OUT_ACTION)) {
				Log.i(tag, "耳机拔出");
				Log.i(tag, "mbind is " + mbind);
				if (mbind) {
					Intent stopintent = new Intent(Mp3PlayService.STOP_ACTION);
					sendBroadcast(stopintent);
				} else {
					mp3stop();
				}
			}

		}

	}
}
