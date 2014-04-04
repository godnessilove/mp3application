package com.example.service;

import java.io.IOException;
import java.util.Map;

import com.example.mp3player.Mp3PlayerActivity;
import com.example.sqlite.DProvider;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

@SuppressLint("NewApi")
public class Mp3PlayService extends Service implements
		MediaPlayer.OnErrorListener {
	private String msg = null;
	private String mp3name = null;
	private MediaPlayer m = null;
	private NotificationManager manager = null;
	private Builder builder = null;
	private int threadid;
	private int audiofocus = AudioManager.AUDIOFOCUS_REQUEST_FAILED;// 默认刚开始的时候是没有获得焦点的
	private Boolean ispause = false;
	private String mp3path = null;
	private AudioManager audiomanager = null;
	private String mp3listname;

	public long getPlayTime() {
		if (m != null) {
			return m.getCurrentPosition();
		} else
			return 0;
	}

	public String getPlayingName() {
		if (m != null) {
			return mp3name;
		} else
			return null;
	}

	public MediaPlayer getMedia() {
		if (m != null) {
			return m;
		} else
			return null;
	}

	public class MyBinder extends Binder {
		public Mp3PlayService getService() {
			return Mp3PlayService.this;
		}
	}

	private final MyBinder myBinder = new MyBinder();

	@Override
	public void onCreate() {
		System.out.println("创建mp3service");
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return myBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		msg = intent.getStringExtra("msg");
		if (msg.equals("START")) {
			// 如果沒有播放器
			if (m == null) {
				mp3path = intent.getStringExtra("mp3path");
				mp3name = intent.getStringExtra("mp3name");
				mp3listname = intent.getStringExtra("mp3listname");
				initMediaPlayer();
				initMp3Notifi();
				getAudioFocus();
			} 
			//切换歌曲的时候
			else if (mp3name.equals(intent.getStringExtra("mp3name")) == false) {
				mp3path = intent.getStringExtra("mp3path");
				mp3name = intent.getStringExtra("mp3name");
				mp3listname = intent.getStringExtra("mp3listname");
				initMediaPlayer();
				initMp3Notifi();
				mp3start();
			}
			//歌曲没变的时候
			else
				mp3start();
		} else if (msg.equals("PAUSE")) {
			mp3pause();
		} else {
			mp3stop();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	// 获取当前audio焦点，成功了才能播放
	public void getAudioFocus() {
		if (audiomanager == null) {
			getApplicationContext();
			audiomanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			audiofocus = audiomanager.requestAudioFocus(listener,
					AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

		}
		if (audiofocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			mp3start();
			System.out.println("获得audio焦点");
		} else
			System.out.println("未获得audio焦点");
	}

	// 初始化播放器
	public void initMediaPlayer() {
		// 如果不存在播放器则新建一个,否则就重置
		if (m == null) {
			m = new MediaPlayer();
			threadid = Thread.currentThread().hashCode();
		} else {
			m.reset();// 点击下一首，重置释放资源，加载下一首歌
		}
		try {
			m.setDataSource(mp3path);
			m.prepare();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 不循环
		m.setLooping(false);
		// 设置手机休眠时，播放器仍可正常播放,但是暂停和停止后还是有可能会被关闭
		m.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		// 设置错误监听
		m.setOnErrorListener(this);
		//设置播放完一首后自动播放下一首
		m.setOnCompletionListener(completion);
	}

	public void mp3start() {
		m.start();
	}

	public void mp3pause() {
		if (m != null && m.isPlaying()) {
			m.pause();
			System.out.println("进入暂停状态");
		}
	}

	public void mp3stop() {
		if (m != null) {
			m.stop();
			m.release();
			System.out.println("停止、释放资源");
			m = null;
		}
		// 撤销通知状态
		stopForeground(true);
		// 放弃音频焦点
		audiomanager.abandonAudioFocus(listener);
		Mp3PlayService.this.stopSelf();
		System.out.println("Mp3PlayService 已杀");
	}

	@Override
	public void onDestroy() {
		if (m != null) {
			m.stop();
			m.release();
			System.out.println("停止、释放资源");
			m = null;
		}
		// 放弃音频焦点
		audiomanager.abandonAudioFocus(listener);
		// 撤销通知状态
		stopForeground(true);
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
		m.reset();
		m = null;
		stopForeground(true);
		// 放弃音频焦点
		audiomanager.abandonAudioFocus(listener);
		return true;
	}

	public void initMp3Notifi() {
		if (manager == null) {
			builder = new NotificationCompat.Builder(getApplicationContext());
			Intent intent = new Intent(getApplicationContext(),
					com.example.mp3player.Mp3PlayerActivity.class);
			String lrcpath = mp3path.replace("mp3", "lrc");
			intent.putExtra("lrcpath", lrcpath);
			intent.putExtra("mp3name", mp3name);
			intent.putExtra("mp3path", mp3path);
			intent.putExtra("mp3listname", mp3listname);
			//新建个栈 把black stack放入这个栈里，为了让通知打开的activity按返回键可以回到播放类表，这里必须要在mainfast里配置与activity亲近的activity
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			stackBuilder.addParentStack(Mp3PlayerActivity.class);
			stackBuilder.addNextIntent(intent);
			PendingIntent pendingintent =
			        stackBuilder.getPendingIntent(
			            0,
			            PendingIntent.FLAG_UPDATE_CURRENT
			        );
			
			/*PendingIntent pendingintent = PendingIntent.getActivity(
					getApplicationContext(), 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);*/
			builder.setContentIntent(pendingintent);
			builder.setSmallIcon(android.R.drawable.ic_media_play);
			builder.setOngoing(true);
		}
		builder.setContentTitle(mp3name);
		builder.setContentText("正在播放");
		startForeground(threadid, builder.build());
	}
	
	OnCompletionListener completion = new OnCompletionListener() {
		
		@Override
		public void onCompletion(MediaPlayer mp) {
			DProvider dprovider = DProvider.getInstance(getApplicationContext());
			Map<String,String> map = dprovider.nextMp3(mp3path, mp3listname);
			mp3name = map.get("mp3name");
			mp3path = map.get("mp3path");
			initMediaPlayer();
			initMp3Notifi();
			mp3start();
			//通知Mp3PlayerActivity进行歌曲信息更新
			Intent intent = new Intent(Mp3PlayerActivity.UPDATE_ACTION);
			intent.putExtra("mp3name", mp3name);
			intent.putExtra("mp3path", mp3path);
			sendBroadcast(intent);
		}
	};

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
				// ispause = false;
				m.setVolume(1.0f, 1.0f);
				break;

			case AudioManager.AUDIOFOCUS_LOSS:
				// Lost focus for an unbounded amount of time: stop playback and
				// release media player
				System.out.println("focusChange , 失去焦点");
				if (m.isPlaying())
					mp3stop();
				break;

			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				// Lost focus for a short time, but we have to stop
				// playback. We don't release the media player because playback
				// is likely to resume
				System.out.println("focusChange , 短暂失去");
				if (m.isPlaying())
					mp3pause();
				break;

			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				// Lost focus for a short time, but it's ok to keep playing
				// at an attenuated level
				System.out.println("focusChange , 保持播放，降低声音");
				if (m.isPlaying())
					m.setVolume(0.1f, 0.1f);
				break;
			}

		}
	};
}
