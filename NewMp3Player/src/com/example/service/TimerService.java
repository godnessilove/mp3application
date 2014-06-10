package com.example.service;

import com.example.newmp3player.MainActivity;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

public class TimerService extends Service {
	private MyHandler myhandler;
	private String msg = null;
	private int alltime = 0;
	private int i = 0;
	public static String TIMESTOPACTION = "time_out";
	private String tag = "TimerService";

	private final MyBinder myBinder = new MyBinder();

	public class MyBinder extends Binder {
		public TimerService getService() {
			return TimerService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return myBinder;
	}

	@Override
	public void onCreate() {
		// 创建ui线程之外的handler，用来执行时间计算
		HandlerThread handlerthread = new HandlerThread("hthread");
		handlerthread.start();
		// 传入新线程的looper
		myhandler = new MyHandler(handlerthread.getLooper());
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Log.i(tag, " TimerService is onDestroy");
		super.onDestroy();
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
	}

	// 自定义handler
	class MyHandler extends Handler {

		public MyHandler() {
			super();
		}

		public MyHandler(Looper looper) {
			super(looper);
		}

	}

	Runnable count = new Runnable() {

		@Override
		public void run() {
			i += 1;
			if (i < alltime) {
				Log.i(tag, " TimerService`i is " + i);
				myhandler.postDelayed(count, 1000);
			} else {
				
				
					
				SharedPreferences sh = PreferenceManager
						.getDefaultSharedPreferences(getApplicationContext());
				SharedPreferences.Editor ed = sh.edit();
				ed.putString("timer", "0");
				ed.commit();
				
				//休眠1秒，为了保证所有activity都正常退出，必须要等待preferenceacti发起的FLAG_ACTIVITY_CLEAR_TOPintent，此intent去让主mainactivity自主关闭，从而达到播放界面自动关闭，从而释放mp3service的绑定，一切正常处理
				  try { Thread.sleep(1000); } catch (InterruptedException e) {
				  // TODO Auto-generated catch block 
					  e.printStackTrace(); }
				
				  
				  Intent stopintent = new Intent(Mp3PlayService.STOP_ACTION);
					sendBroadcast(stopintent);
					
				stopService();
			}
		}
	};

	public void stopService() {
		myhandler.removeCallbacks(count);
		stopSelf();
	}

	public int getTime() {
		return alltime - i;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		msg = intent.getStringExtra("flag");
		alltime = Integer.valueOf(intent.getStringExtra("alltime"));
		Log.i(tag, "msg is" + msg + ",alltime is " + alltime);
		if (msg.equals("start")) {
			myhandler.removeCallbacks(count);
			// 重置计数变量
			i = 0;
			myhandler.post(count);
		} else if (msg.equals("end")) {
			stopService();
		}
		return START_NOT_STICKY;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(tag, " TimerService is onUnbind");
		return super.onUnbind(intent);
	}

}
