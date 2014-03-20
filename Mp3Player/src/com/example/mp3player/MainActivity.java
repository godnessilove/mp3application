package com.example.mp3player;


import com.example.sqlite.DProvider;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.Menu;

public class MainActivity extends FragmentActivity {
	private static final int UPDATE = 1;
	private static final int ABOUT = 2;
	private FragmentTabHost tab;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		tab = (FragmentTabHost) findViewById(android.R.id.tabhost);
		tab.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		tab.addTab(tab.newTabSpec("tab111").setIndicator("�������б�"),
				RemoteActivity.class, null);
		tab.addTab(tab.newTabSpec("tab222").setIndicator("�����б�"),
				LocalActivity.class, null);
		
	}
	
	

	@Override
	protected void onResume() {
		//ÿ�λص�activity�ĕr��ˢ������ȫ��MP3�������
		Thread thread = new Thread(createlocaltable);
		thread.start();
		super.onResume();
	}



	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		String fag = tab.getCurrentTabTag();
		if (fag.equals("tab111")) {
			menu.clear();
			menu.add(0, UPDATE, 0, "�����б�");
			menu.add(0, ABOUT, 0, "����");
		} else if (fag.equals("tab222")) {
			menu.clear();
			menu.add(0, UPDATE, 0, "���²����б�");
			menu.add(0, ABOUT, 0, "�h��Ŀǰ�Ĳ����б�");
		}
		return super.onPrepareOptionsMenu(menu);
	}

	Runnable createlocaltable = new Runnable() {

		@Override
		public void run() {
			DProvider dprovider = DProvider.getInstance(getApplicationContext());//new DProvider(getApplicationContext());
			dprovider.initAllList();
		}
	};

}
