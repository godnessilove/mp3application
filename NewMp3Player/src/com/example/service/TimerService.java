package com.example.service;


import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;

public class TimerService extends Service {
	private MyHandler myhandler;
	private String msg = null;
	private int alltime = 0;
	private int i = 0;
	public static String TIMESTOPACTION = "time_out";
	//private String tag = "TimerService";

	private final MyBinder myBinder = new MyBinder();

	public class MyBinder extends Binder {
		/**
		 * 返回改service
		 * @return
		 */
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

	//计时.service内部的计时，为了activity退出后倒计时继续prefereceActivity倒计时每次进入的时候读取的就是这里的数据
	Runnable count = new Runnable() {

		@Override
		public void run() {
			i += 1;
			if (i < alltime) {
				myhandler.postDelayed(count, 1000);
			} else {
				//计时结束后，将偏好设置里的定时选项重置为0
				SharedPreferences sh = PreferenceManager
						.getDefaultSharedPreferences(getApplicationContext());
				SharedPreferences.Editor ed = sh.edit();
				ed.putString("timer", "0");
				ed.commit();

				// 休眠1秒，为了保证所有activity都正常退出，必须要等待preferenceacti发起的FLAG_ACTIVITY_CLEAR_TOPintent，此intent去让主mainactivity自主关闭，从而达到播放界面自动关闭，从而释放mp3service的绑定，一切正常处理
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//通知mp3service，倒计时结束，关闭service
				Intent stopintent = new Intent(Mp3PlayService.TIME_OUT_ACTION);
				sendBroadcast(stopintent);

				//停止自身service
				stopService();
			}
		}
	};

	/**
	 * 停止service
	 */
	public void stopService() {
		myhandler.removeCallbacks(count);
		stopSelf();
	}

	/**
	 * 获取还剩下多少时间
	 * @return
	 */
	public int getTime() {
		return alltime - i;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		msg = intent.getStringExtra("flag");
		alltime = Integer.valueOf(intent.getStringExtra("alltime"));
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
		return super.onUnbind(intent);
	}

}
