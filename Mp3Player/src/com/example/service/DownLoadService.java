package com.example.service;

import java.io.IOException;

import com.example.download.HttpDownLoad;
import com.example.xmlmodel.Mp3Info;

import android.R;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

public class DownLoadService extends Service {
	private String downmp3path = "mp3";
	private String downlrcpath = "lrc";
	private HttpDownLoad hdl;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		hdl = new HttpDownLoad();
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Mp3Info mp3info = (Mp3Info) intent.getSerializableExtra("mp3info");
		//���жϣ�������������򲻽������²���
		// ���������߳�
		DownLoad download = new DownLoad(mp3info);
		new Thread(download).start();
		// ��������֪ͨ���������߳�
		Updateinfo updateinfo = new Updateinfo(mp3info);
		new Thread(updateinfo).start();
		return super.onStartCommand(intent, flags, startId);
	}

	class DownLoad implements Runnable {
		private Mp3Info mp3info = null;

		public DownLoad(Mp3Info mp3info) {
			super();
			this.mp3info = mp3info;
		}

		public void run() {
			try {
				// ����mp3
				hdl.downFile("http://192.168.0.101:8080/mp3/",
						downmp3path, mp3info.getMp3name());
				// ���ظ��
				hdl.downFile("http://192.168.0.101:8080/mp3/", downlrcpath,
						mp3info.getMp3name().replace("mp3", "lrc"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	class Updateinfo implements Runnable {
		private Mp3Info mp3info = null;
		private int percentage = 0;
		private NotificationManager manager;
		private Builder builder;
		private int threadid;

		public Updateinfo(Mp3Info mp3info) {
			super();
			this.mp3info = mp3info;
		}

		@Override
		public void run() {
			threadid = Thread.currentThread().hashCode();// ����֪ͨ�����߳�id���Դ���Ϊ֪ͨ��id��ʵ�ֶ�֪ͨͬʱ����
			//��ʼ��֪ͨ��ÿ�ζ��½�һ��֪ͨ
			manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			builder = new NotificationCompat.Builder(getApplicationContext());
			builder.setSmallIcon(R.drawable.arrow_down_float)
					.setProgress(100, 0, false)
					.setContentTitle(mp3info.getMp3name()).setContentInfo("0%")
					.setContentText("׼������");
			manager.notify(threadid, builder.build());
			System.out.println("׼������");
			//��ѭ��3�����һ��
			while (percentage <= 100) {
				System.out
						.println("�����߳�ID " + threadid + "���ذٷ���Ϊ" + percentage);
				// ��ȡ��ǰ���ؽ���
				percentage = hdl.downPercentage(mp3info.getMp3name(),
						mp3info.getMp3size(), downmp3path);
				if (percentage > 0 && percentage < 100) {
					builder.setProgress(100, percentage, false)
							.setContentText("��������")
							.setContentInfo(percentage + "%");
					System.out.println("����֪ͨID " + threadid);
					manager.notify(threadid,  builder.build());
				} else if (100 == percentage) {
					builder.setContentText("�������").setProgress(0, 0, false)
							.setContentInfo("100%");
					manager.notify(threadid,  builder.build());
					System.out.println("�������");
					break;
				}
				try {
					Thread.sleep(3 * 1000);//����3��
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
