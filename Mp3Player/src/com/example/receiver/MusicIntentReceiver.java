package com.example.receiver;

import com.example.service.Mp3PlayService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MusicIntentReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		//���統�����γ���ʱ��ֹͣ����
		if(intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)){
			Intent stopIntent = new Intent();
			stopIntent.putExtra("msg", "STOP");
			stopIntent.setClass( context, Mp3PlayService.class);
		}
		
	}

}
