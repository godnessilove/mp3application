package com.example.notification;

import com.example.newmp3player.R;
import com.example.service.DownLoadService;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.widget.RemoteViews;

public class Mp3DownLoadNotification {
	public static Mp3DownLoadNotification sInstance;
	private Context context;
	private NotificationManager manager;
	private Builder builder;
	private RemoteViews remote;

	public Mp3DownLoadNotification(Context context) {
		this.context = context.getApplicationContext();
	}

	public void NegativeButtonPending(int id) {
		Intent buttonintent = new Intent();
		buttonintent.putExtra("msg", "DELETE");
		buttonintent.putExtra("id", id);
		buttonintent.setClass(context.getApplicationContext(),
				DownLoadService.class);
		PendingIntent pendinginteng = PendingIntent.getService(
				context.getApplicationContext(), id, buttonintent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		remote.setOnClickPendingIntent(R.id.imageBUtton4, pendinginteng);
	}

	public void CreateNotification(int id, String mp3name, String mp3size) {
		manager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		builder = new NotificationCompat.Builder(
				context.getApplicationContext());
		remote = new RemoteViews(context.getPackageName(),
				R.layout.downloadnotification);

		remote.setTextViewText(R.id.textView1, mp3name);
		remote.setTextViewText(R.id.textView2, mp3size);
		remote.setTextViewText(R.id.textView3,
				context.getString(R.string.downNotifiText));
		remote.setTextViewText(R.id.textView4, "0%");
		remote.setProgressBar(R.id.progressBar1, 100, 0, false);
		
		NegativeButtonPending(id);
		
		builder.setSmallIcon(android.R.drawable.arrow_down_float);
		builder.setContent(remote);
		builder.setOngoing(true);
		manager.notify(id, builder.build());
	}

	public void updateNotification(int id, int percentage, int index) {
		switch(index){
		case 1:
			remote.setTextViewText(R.id.textView3,
					context.getString(R.string.downNotifiTextOK));
			remote.setProgressBar(R.id.progressBar1, 0, 0, false);
			remote.setTextViewText(R.id.textView4, "100%");
			//remote.removeAllViews(R.id.imageBUtton4);
			builder.setOngoing(false);
			break;
		case -1:
			remote.setTextViewText(R.id.textView3,
					context.getString(R.string.downNotifiTextErr));
			builder.setOngoing(false);
			break;
		case 2:
			remote.setTextViewText(R.id.textView3,
					context.getString(R.string.downNotifiTextExist));
			remote.setTextViewText(R.id.textView4, "100%");
			builder.setOngoing(false);
			break;
		case -2:
			remote.setTextViewText(R.id.textView3,
					context.getString(R.string.downNotifiTextNegative));
			builder.setOngoing(false);
			break;
		case 0:
			remote.setProgressBar(R.id.progressBar1, 100, percentage, false);
			remote.setTextViewText(R.id.textView3,
					context.getString(R.string.downNotifiTextIng));
			remote.setTextViewText(R.id.textView4, percentage + "%");
			builder.setOngoing(true);
			break;
		}
		manager.notify(id, builder.build());
	}
	
	public void deleteNotifi(int id){
		manager.cancel(id);
	}
}
