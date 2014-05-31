package com.example.service;

import com.example.download.HttpDownLoad;
import com.example.fileutil.DownMp3State;
import com.example.newmp3player.R;
import com.example.notification.Mp3DownLoadNotification;
import com.example.xmlmodel.Mp3Info;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

@SuppressLint({ "HandlerLeak", "UseSparseArrays" })
public class DownLoadService extends Service {
	private String https = null;
	private String downmp3path = "mp3";
	private String downlrcpath = "lrc";
	private HttpDownLoad hdl;
	private int mp3_resault;
	//private int lrc_resault;
	//最新的startId，为了保证可以正确关闭service
	private int newid;

	private DownMp3State downmp3state;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		Log.i("DownLoadService", "downloadservice is onCreate");
		System.out.println("downloadservice is onCreate");
		https = getResources().getString(R.string.https);
		hdl = new HttpDownLoad();
		downmp3state = DownMp3State.getInstance();
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Log.i("DownLoadService", "downloadservice is killed");
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String msg = intent.getStringExtra("msg");
		Mp3Info mp3info;
		newid = startId;
		if (msg.equals("NEWCREATE")) {
			//最多同时下载3个mp3
			if (downmp3state.getMap().size() < 3) {
				// newid表示最近一次的startId,为了可以正确结束service而不会结束没有完成的下载
				Log.i("DownLoadService", "newid is " + newid);
				mp3info = (Mp3Info) intent.getSerializableExtra("mp3info");
				// 加判断，如果正在下载则不进行以下操作
				// 启动下载线程
				DownLoad download = new DownLoad(mp3info);
				download.start();
			} else {
				Toast.makeText(getApplicationContext(), "为了保证速度与性能，最多同时下载三个",
						Toast.LENGTH_SHORT).show();
				stopService(0);
			}
		} else if (msg.equals("DELETE")) {
			int id = intent.getIntExtra("id", 0);
			// 如果是属于正在下载状态
			if (downmp3state.getMap().get(id) != null
					&& downmp3state.getMap().get(id) == 1) {
				downmp3state.getMap().put(id, -2);
			}
		}

		return super.onStartCommand(intent, flags, startId);
	}

	class MyHandle extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 2:
				int id = msg.arg1;
				Bundle bundle = msg.getData();
				String mp3name = bundle.getString("mp3name");
				String mp3size = bundle.getString("mp3zise");
				Updateinfo updateinfo = new Updateinfo(id, mp3name, mp3size);
				updateinfo.start();
				break;
			}

		}

	}

	class DownLoad extends Thread {
		private Mp3Info mp3info;

		public DownLoad(Mp3Info mp3info) {
			this.mp3info = mp3info;
		}

		public void run() {
			// 下载线程的hashcode，以此作为通知的id，实现多通知同时更新
			int threadid = Thread.currentThread().hashCode();
			Log.i("DownLoadService", "开始下载：" + mp3info.getMp3name() + "线程为："
					+ threadid);
			// 1表示允许下载
			downmp3state.getMap().put(threadid, 1);
			// 开启对应更新通知栏线程
			Updateinfo updateinfo = new Updateinfo(threadid,
					mp3info.getMp3name(), mp3info.getMp3size());
			updateinfo.start();

			// 先下载歌词(后下载歌词会造成mp3先下载完成，下载状态会被删除，导致歌词下载中断)，不提示下载歌词，让用户感觉不到下载歌词
			hdl.downFile(https, downlrcpath, mp3info.getMp3name()
					.replace("mp3", "lrc"), threadid);
			// 下载mp3
			mp3_resault = hdl.downFile(https, downmp3path,
					mp3info.getMp3name(), threadid);
			Log.i("DownLoadService", "mp3_resault is" + mp3_resault);
			// mp3_resault：-1下載錯誤，2文件已存在，0正确下载
			downmp3state.getMap().put(threadid, mp3_resault);
		}
	}

	/**
	 * 判断如果下载都完成了，则停止service
	 * @param threadid
	 */
	public synchronized  void stopService(int threadid) {
		if(threadid != 0){
		downmp3state.getMap().remove(threadid);
		}
		if (downmp3state.getMap().size() == 0) {
			stopSelf(newid);
			}
	}
	
	

	class Updateinfo extends Thread {
		private int percentage = 0;
		private int threadid;
		private String mp3name;
		private String mp3zise;

		public Updateinfo(int id, String mp3name, String mp3size) {
			this.threadid = id;
			this.mp3name = mp3name;
			this.mp3zise = mp3size;
		}

		@Override
		public void run() {
			Log.i("DownLoadService", "开始更新通知線程：" + mp3name + "线程为：" + threadid);
			// 初始化通知。每次都新建一个通知
			Mp3DownLoadNotification notificaition = new Mp3DownLoadNotification(
					getApplication());
			notificaition.CreateNotification(threadid, mp3name, mp3zise);
			// 更循环3秒更新一次
			while (percentage <= 100) {
				Log.i("DownLoadService", "map :"
						+ downmp3state.getMap().toString());
				// index：-1下載錯誤，2文件已存在，0正确下载，1表示允许下载
				int index = downmp3state.getMap().get(threadid);
				if (-1 == index) {
					notificaition.updateNotification(threadid, percentage,
							index);
					stopService(threadid);
					break;
				} else if (2 == index) {
					notificaition.updateNotification(threadid, percentage, 2);
					stopService(threadid);
					break;
				} else if (-2 == index) {
					notificaition.updateNotification(threadid, percentage, -2);
					stopService(threadid);
					break;
				} else if (0 == index) {
					notificaition.updateNotification(threadid, percentage, 1);
					stopService(threadid);
					break;
				} else if (1 == index) {
					// 获取当前下载进度
					percentage = hdl.downPercentage(mp3name, mp3zise,
							downmp3path);
					// 正在下载
					if (percentage > 0 && percentage < 100) {
						notificaition.updateNotification(threadid, percentage,
								0);
					} else if (100 == percentage) {
						// stopSelf(newid);
						notificaition.updateNotification(threadid, percentage,
								1);
						stopService(threadid);
						break;
					}
				}

				try {
					// 休眠3秒
					Thread.sleep(3 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
