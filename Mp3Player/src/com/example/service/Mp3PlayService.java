package com.example.service;

import java.io.IOException;
import java.util.Map;

import com.example.mp3player.Mp3PlayerActivity;
import com.example.sqlite.DProvider;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
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

public class Mp3PlayService extends Service implements
		MediaPlayer.OnErrorListener {
	private String msg = null;
	private String mp3name = null;
	private MediaPlayer m = null;
	private NotificationManager manager = null;
	private Builder builder = null;
	private int threadid;
	private int audiofocus = AudioManager.AUDIOFOCUS_REQUEST_FAILED;// Ĭ�ϸտ�ʼ��ʱ����û�л�ý����
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
		System.out.println("����mp3service");
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return myBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		msg = intent.getStringExtra("msg");
		mp3path = intent.getStringExtra("mp3path");
		mp3name = intent.getStringExtra("mp3name");
		mp3listname = intent.getStringExtra("mp3listname");
		if (msg.equals("START")) {
			// ����]�в�����
			if (m == null) {
				initMediaPlayer();
				initMp3Notifi();
				getAudioFocus();
			} 
			//�л�������ʱ��
			else if (mp3name.equals(intent.getStringExtra("mp3name")) == false) {
				initMediaPlayer();
				initMp3Notifi();
				mp3start();
			}
			//����û���ʱ��
			else
				mp3start();
		} else if (msg.equals("PAUSE")) {
			mp3pause();
		} else {
			mp3stop();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	// ��ȡ��ǰaudio���㣬�ɹ��˲��ܲ���
	public void getAudioFocus() {
		if (audiomanager == null) {
			getApplicationContext();
			audiomanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			audiofocus = audiomanager.requestAudioFocus(listener,
					AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

		}
		if (audiofocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			mp3start();
			System.out.println("���audio����");
		} else
			System.out.println("δ���audio����");
	}

	// ��ʼ��������
	public void initMediaPlayer() {
		// ��������ڲ��������½�һ��,���������
		if (m == null) {
			m = new MediaPlayer();
			threadid = Thread.currentThread().hashCode();
		} else {
			m.reset();// �����һ�ף������ͷ���Դ��������һ�׸�
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
		// ��ѭ��
		m.setLooping(false);
		// �����ֻ�����ʱ���������Կ���������,������ͣ��ֹͣ�����п��ܻᱻ�ر�
		m.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		// ���ô������
		m.setOnErrorListener(this);
		//���ò�����һ�׺��Զ�������һ��
		m.setOnCompletionListener(completion);
	}

	public void mp3start() {
		m.start();
	}

	public void mp3pause() {
		if (m != null && m.isPlaying()) {
			m.pause();
			System.out.println("������ͣ״̬");
		}
	}

	public void mp3stop() {
		if (m != null) {
			m.stop();
			m.release();
			System.out.println("ֹͣ���ͷ���Դ");
			m = null;
		}
		// ����֪ͨ״̬
		stopForeground(true);
		// ������Ƶ����
		audiomanager.abandonAudioFocus(listener);
		Mp3PlayService.this.stopSelf();
		System.out.println("Mp3PlayService ��ɱ");
	}

	@Override
	public void onDestroy() {
		if (m != null) {
			m.stop();
			m.release();
			System.out.println("ֹͣ���ͷ���Դ");
			m = null;
		}
		// ������Ƶ����
		audiomanager.abandonAudioFocus(listener);
		// ����֪ͨ״̬
		stopForeground(true);
		super.onDestroy();
	}

	@Override
	public String toString() {
		return "Mp3PlayService [msg=" + msg + ", m=" + m + ", ispause="
				+ ispause + ", mp3path=" + mp3path + ", myBinder=" + myBinder
				+ "]";
	}

	// �������������ʱ��,��Ҫ���ã�����reset����Զ��ͷ���Դ�������ֶ�����release
	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		m.reset();
		m = null;
		stopForeground(true);
		// ������Ƶ����
		audiomanager.abandonAudioFocus(listener);
		return true;
	}

	public void initMp3Notifi() {
		if (manager == null) {
			builder = new NotificationCompat.Builder(getApplicationContext());
			Intent intent = new Intent(getApplicationContext(),
					com.example.mp3player.Mp3PlayerActivity.class);
			PendingIntent pendingintent = PendingIntent.getActivity(
					getApplicationContext(), 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			builder.setContentIntent(pendingintent);
			builder.setSmallIcon(android.R.drawable.ic_media_play);
			builder.setOngoing(true);
		}
		builder.setContentTitle(mp3name);
		builder.setContentText("���ڲ���");
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
			//֪ͨMp3PlayerActivity���и�����Ϣ����
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
				System.out.println("focusChange , ��ý���");
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
				System.out.println("focusChange , ʧȥ����");
				if (m.isPlaying())
					mp3stop();
				break;

			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				// Lost focus for a short time, but we have to stop
				// playback. We don't release the media player because playback
				// is likely to resume
				System.out.println("focusChange , ����ʧȥ");
				if (m.isPlaying())
					mp3pause();
				break;

			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				// Lost focus for a short time, but it's ok to keep playing
				// at an attenuated level
				System.out.println("focusChange , ���ֲ��ţ���������");
				if (m.isPlaying())
					m.setVolume(0.1f, 0.1f);
				break;
			}

		}
	};
}
