package com.example.newmp3player;

import com.example.newmp3player.TabPlayFragment.Receiver;
import com.example.service.Mp3PlayService;
import com.example.service.TimerService;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.util.Log;

@SuppressLint("NewApi")
public class PreferencesActi extends Activity {
	private static String tag = "PreferencesActi";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferece_main);
	}

	public static class Prefs1Fragment extends PreferenceFragment implements
			OnPreferenceChangeListener {
		private Service timeservice;
		private ListPreference listpreference_size;
		private ListPreference listpreference_timer;
		private CheckBoxPreference checkboxpreferece;
		private TimeCount timecount;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preference);
			listpreference_size = (ListPreference) findPreference("choose_size");
			listpreference_size
					.setSummary(listpreference_size.getEntry() == null ? listpreference_size
							.getEntries()[0] : listpreference_size.getEntry());
			listpreference_size.setOnPreferenceChangeListener(this);

			listpreference_timer = (ListPreference) findPreference("timer");
			CharSequence summary = listpreference_timer.getEntry() == null ? listpreference_timer
					.getEntries()[0] : listpreference_timer.getEntry();
			Log.i(tag, "list 选择的是 " + summary.toString());
			listpreference_timer.setSummary(summary.toString());
			listpreference_timer.setOnPreferenceChangeListener(this);

			checkboxpreferece = (CheckBoxPreference) findPreference("choose_timer");
		}

		@Override
		public void onStart() {
			if (timeservice == null) {
				Intent intent1 = new Intent();
				intent1.setClass(getActivity(), TimerService.class);
				getActivity().bindService(intent1, conn,
						Service.BIND_AUTO_CREATE);
			}
			super.onStart();

		}

		// 设置与mp3service绑定时的操作
		private ServiceConnection conn = new ServiceConnection() {

			// 连接时，获得mp3service的实例
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(tag, "onServiceConnected");
				timeservice = ((TimerService.MyBinder) service).getService();
				int time = ((TimerService) timeservice).getTime();
				Log.i(tag, " time is" + time);
				if (time != 0) {
					timecount = new TimeCount(time * 1000, 1000);
					timecount.start();
				}
			}

			// 链接失败
			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.i(tag, "onServiceDisconnected");
				timeservice = null;
			}

		};

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String enter = null;
			newValue = (String) newValue;

			if (preference.getKey().equals("timer")) {
				if (!newValue.equals("0")) {
					Intent timerservice = new Intent(getActivity(),
							TimerService.class);
					timerservice.putExtra("flag", "start");
					timerservice.putExtra("alltime", newValue.toString());
					getActivity().startService(timerservice);
					// int time = ((TimerService) timeservice).getTime();
					// og.i(tag, " time is" +time);
					if (timecount != null) {
						timecount.cancel();
					}
					timecount = new TimeCount(
							Long.valueOf(newValue.toString()) * 1000, 1000);
					timecount.start();
				} else {
					getActivity().unbindService(conn);
					if (timecount != null) {
						timecount.cancel();
					}
					timecount = null;
					Intent timerservice = new Intent(getActivity(),
							TimerService.class);
					timerservice.putExtra("flag", "end");
					timerservice.putExtra("alltime", newValue.toString());
					getActivity().startService(timerservice);
					timeservice = null;
				}
			}

			CharSequence[] enteries = ((ListPreference) preference)
					.getEntries();
			CharSequence[] entervalues = ((ListPreference) preference)
					.getEntryValues();
			int length = entervalues.length;
			for (int i = 0; i < length; i++) {
				String entervalue = (String) entervalues[i];
				if (entervalue.equals(newValue)) {
					enter = (String) enteries[i];
					break;
				}
			}
			preference.setSummary(enter);

			return true;
		}

		public String showTime(long duration) {
			duration = duration / 1000;
			int min = (int) (duration / 60);
			int second = (int) duration % 60;
			return String.format("%02d:%02d", min, second);
		}

		class TimeCount extends CountDownTimer {

			public TimeCount(long millisInFuture, long countDownInterval) {
				super(millisInFuture, countDownInterval);
			}

			@Override
			public void onFinish() {
				// 完成的时候，如果还在绑定，则释放，不然service停止不了
				if (timeservice != null) {
					getActivity().unbindService(conn);
					timeservice = null;
				}
				//可见说明父activity也没有onDestroy,所以一并关闭掉，这个activity会自动关闭，因为使用了finishOnTaskLaunch
				if (Prefs1Fragment.this.isVisible()) {
					Intent intent = new Intent();
					intent.setClass(getActivity(), MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
			}

			@Override
			public void onTick(long arg0) {
				String time = showTime(arg0);
				Log.i(tag, "arg0 is " + arg0 / 1000);
				listpreference_timer.setSummary(time);
			}

		}

		@Override
		public void onDestroy() {
			// 退出就不需要显示倒计时了，service里面有继续计时的
			if (timecount != null) {
				timecount.cancel();
				timecount = null;
			}
			if (timeservice != null) {
				getActivity().unbindService(conn);
				timeservice = null;
			}
			Log.i(tag, "PreferencesActi is onDestroy ");
			super.onDestroy();
		}

		@Override
		public void onStop() {
			super.onStop();
		}

	}

}
