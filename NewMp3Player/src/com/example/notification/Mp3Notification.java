package com.example.notification;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.widget.RemoteViews;

import com.example.newmp3player.MainActivity;
import com.example.newmp3player.R;
import com.example.service.Mp3PlayService;
import com.example.sqlite.DProvider;

@SuppressLint("NewApi")
public class Mp3Notification {
	public static Mp3Notification sInstance;
	private Context context;
	private NotificationManager manager;
	private Builder builder;

	private String currlrcpath;
	private String currmp3path;
	private String currmp3name;
	private String currmp3listname;
	private RemoteViews remote;
	private Intent backactivityintent;
	private TaskStackBuilder stackBuilder;
	private PendingIntent backactivitypendingintent;

	/**
	 * 获取实例，单例模式
	 * 
	 * @param context
	 * @return
	 */
	public static Mp3Notification getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new Mp3Notification(context.getApplicationContext());
		}
		return sInstance;
	}

	public Builder getBuilder() {
		return builder;
	}

	
	/**
	 * 构造函数，以下部分经常要用，所以只在堆内存中创建一份
	 * 
	 * @param context
	 */
	private Mp3Notification(Context context) {
		super();
		this.context = context;
		manager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		builder = new NotificationCompat.Builder(
				context.getApplicationContext());
		remote = new RemoteViews(context.getPackageName(),
				R.layout.notification);
		// 重新启动Mp3PlayerActivity需要的参数
		backactivityintent = new Intent(context.getApplicationContext(),
				com.example.newmp3player.MainActivity.class);
	}

	/**
	 * 重新设置pendingintent,当歌曲变化的时候，可以正确返回播放界面
	 */
	public void backActivityPending() {
		/*
		 * currlrcpath = currmp3path.replace("mp3", "lrc");
		 * backactivityintent.putExtra("lrcpath", currlrcpath);
		 * backactivityintent.putExtra("mp3name", currmp3name);
		 * backactivityintent.putExtra("mp3path", currmp3path);
		 * backactivityintent.putExtra("mp3listname", currmp3listname);
		 */
		// 新建个栈 把black
		// stack放入这个栈里，为了让通知打开的activity按返回键可以回到播放类表，这里必须要在mainfast里配置与activity亲近的activity
		stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(backactivityintent);
		backactivitypendingintent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

	/**
	 * 只是返回
	 */
	public void backfragmentpending() {
		currlrcpath = currmp3path.replace("mp3", "lrc");
		backactivityintent.putExtra("lrcpath", currlrcpath);
		backactivityintent.putExtra("mp3name", currmp3name);
		backactivityintent.putExtra("mp3path", currmp3path);
		backactivityintent.putExtra("mp3listname", currmp3listname);

		backactivitypendingintent = PendingIntent.getActivity(context, 0,
				backactivityintent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	/**
	 * 设置下一首按钮的pendingintent
	 */
	public void nextButtonPending() {
		Intent buttonintent1 = new Intent();
		buttonintent1.putExtra("msg", "NEXT");

		buttonintent1.setClass(context.getApplicationContext(),
				Mp3PlayService.class);
		// 第二个参数requestCode设置与下面一个按钮的设置为不同，这样在同一个通知栏内才，才能使用只是extra不同的intent
		PendingIntent pendinginteng1 = PendingIntent.getService(
				context.getApplicationContext(), 0, buttonintent1,
				PendingIntent.FLAG_UPDATE_CURRENT);
		remote.setOnClickPendingIntent(R.id.imageButton2, pendinginteng1);
	}

	/**
	 * 创建播放、暂停按钮的pendingintent
	 */
	public void playButtonPending() {
		Intent buttonintent2 = new Intent();
		buttonintent2.putExtra("msg", "PLAY-PAUSE");
		buttonintent2.setClass(context.getApplicationContext(),
				Mp3PlayService.class);
		PendingIntent pendinginteng2 = PendingIntent.getService(
				context.getApplicationContext(), 1, buttonintent2,
				PendingIntent.FLAG_UPDATE_CURRENT);
		remote.setOnClickPendingIntent(R.id.imageButton1, pendinginteng2);
	}

	/**
	 * 创建notification
	 * 
	 * @param threadid
	 *            service对应的id
	 * @param mp3name
	 *            mp3名称
	 * @param mp3path
	 *            mp3路径
	 * @param mp3listname
	 *            当前播放列表
	 */
	public void createNotifi(int threadid, String mp3name, String mp3path,
			String mp3listname) {
		currmp3path = mp3path;
		currmp3name = mp3name;
		currmp3listname = mp3listname;

		// 自定义通知栏，添加播放暂停按钮、下一首按钮
		remote.setTextViewText(R.id.textView1, currmp3name);
		remote.setTextViewText(R.id.textView2, "正在播放");

		String thumb = getimage(mp3name);
		if (thumb == null) {
			remote.setImageViewResource(R.id.imageView1,
					R.drawable.albumart_mp_unknown_list);
		} else {
			Bitmap bm = BitmapFactory.decodeFile(thumb);
			if (bm != null) {
				Matrix matrix = new Matrix();
				matrix.postScale(0.5f, 0.5f); // 长和宽放大缩小的比例
				Bitmap resizeBmp = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
						bm.getHeight(), matrix, true);
				remote.setImageViewBitmap(R.id.imageView1, resizeBmp);
			} else {
				remote.setImageViewResource(R.id.imageView1,
						R.drawable.albumart_mp_unknown_list);
			}
		}

		backfragmentpending();
		// backActivityPending();
		playButtonPending();
		nextButtonPending();

		builder.setContentIntent(backactivitypendingintent);
		builder.setContent(remote);
		builder.setSmallIcon(android.R.drawable.ic_media_play);
		builder.setOngoing(true);
		builder.setAutoCancel(false);
		// 放在前台...感觉浪费电
		//startForeground(startId, builder.build());
		manager.notify(threadid, builder.build());
	}
	
	public void notificationstartForeground(Service service,int id){
		service.startForeground(id, builder.build());
	}

	public String getimage(String title) {
		DProvider dprovider = DProvider.getInstance(context);
		return dprovider.queryimage(title);
	}

	/**
	 * 更新通知栏并
	 * 
	 * @param threadid
	 * @param mp3name
	 * @param mp3path
	 * @param mp3listname
	 */
	public void updateMp3Notifi(int threadid, String mp3name, String mp3path,
			String mp3listname) {
		currmp3name = mp3name;
		currmp3path = mp3path;
		currmp3listname = mp3listname;
		String thumb = getimage(mp3name);
		if (thumb == null) {
			remote.setImageViewResource(R.id.imageView1,
					R.drawable.albumart_mp_unknown_list);
		} else {
			Bitmap bm = BitmapFactory.decodeFile(thumb);
			if (bm != null) {
				Matrix matrix = new Matrix();
				matrix.postScale(0.5f, 0.5f); // 长和宽放大缩小的比例
				Bitmap resizeBmp = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
						bm.getHeight(), matrix, true);
				remote.setImageViewBitmap(R.id.imageView1, resizeBmp);
			} else {
				remote.setImageViewResource(R.id.imageView1,
						R.drawable.albumart_mp_unknown_list);
			}
		}
		backActivityPending();
		builder.setContentIntent(backactivitypendingintent);

		remote.setTextViewText(R.id.textView1, currmp3name);
		manager.notify(threadid, builder.build());
	}

	/**
	 * 删除通知栏
	 * 
	 * @param threadid
	 *            通知栏id
	 */
	public void deleteNotifi(int threadid) {
		manager.cancel(threadid);
	}

	/**
	 * 更新通知栏按钮图标
	 * 
	 * @param threadid
	 *            通知栏id
	 * @param isplaying
	 *            播放状态
	 */
	public void updateButtonImage(int threadid, boolean isplaying) {
		if (isplaying) {
			remote.setImageViewResource(R.id.imageButton1,
					android.R.drawable.ic_media_pause);
			remote.setTextViewText(R.id.textView2,
					this.context.getString(R.string.mp3NotifiPlayTextIng));
		} else {
			remote.setImageViewResource(R.id.imageButton1,
					android.R.drawable.ic_media_play);
			remote.setTextViewText(R.id.textView2,
					this.context.getString(R.string.mp3NotifiPlayTextPause));
		}
		manager.notify(threadid, builder.build());
	}

}
