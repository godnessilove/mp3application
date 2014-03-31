package com.example.mp3player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.example.adapter.mp3ListAdapter;
import com.example.sqlite.DProvider;
import com.example.sqlite.DateBaseHelper;
import com.example.sqlite.PlayMp3ListTable;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;


@SuppressLint("CommitPrefEdits")
public class ChooseMp3Activity extends ListActivity {
	private String listname;
	private mp3ListAdapter listadapter;
	private Button button1;
	private Button button2;
	private Cursor cursor;
	private Handler handler;
	private DProvider dprovider;
	private SharedPreferences mpfs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		System.out.println("choose activity onCreate");
		Intent intent = getIntent();
		listname = intent.getStringExtra("listname");
		mpfs = getPreferences(LocalActivity.SPINNER_STATE);
		
		setContentView(R.layout.addmp3button);

		button1 = (Button) findViewById(R.id.add);
		button2 = (Button) findViewById(R.id.choose);

		button1.setOnClickListener(new button1chick());
		button2.setOnClickListener(new button2chick());
		handler = new Handler();
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		System.out.println("choose activity onResume");
		handler.post(createlocaltable);
		super.onResume();
	}

	Runnable createlocaltable = new Runnable() {

		@Override
		public void run() {
			dprovider = DProvider.getInstance(getApplicationContext());//new DProvider(getApplicationContext());
			dprovider.initAllList();
			String table_name = DateBaseHelper.getTablelocalname();
			cursor = dprovider.querydate(table_name);
			String[] from = new String[] { PlayMp3ListTable.getMp3name(),
					PlayMp3ListTable.getMp3size() };
			int[] to = new int[] { R.id.mp3name, R.id.mp3size };

			listadapter = new mp3ListAdapter(getApplicationContext(),
					R.layout.mp3info_item_choose, cursor, from, to, 0);
			setListAdapter(listadapter);
		}
	};

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// ȡ��ǰ���ӵĵ�һ��item����ʵposition
		int j = l.getFirstVisiblePosition();
		// ȡ�õ������ʵitem
		position = position - j;
		View view = l.getChildAt(position);
		CheckBox checkbox = (CheckBox) view
				.findViewById(R.id.multiple_checkbox);
		if (checkbox.isChecked()) {
			checkbox.setChecked(false);
		} else {
			checkbox.setChecked(true);
		}
		int count = listadapter.getCount();
		int chickcount = listadapter.count();
		button1.setText("���(" + chickcount + ")");
		if (chickcount == count) {
			button2.setText("��ѡ");
		} else if (chickcount != count) {
			button2.setText("ȫѡ");
		}
	}

	class button2chick implements OnClickListener {

		@Override
		public void onClick(View v) {
			int count = listadapter.getCount();

			if (button2.getText().equals("ȫѡ")) {
				button1.setText("���(" + count + ")");
				button2.setText("��ѡ");
			} else if (button2.getText().equals("��ѡ")) {
				button1.setText("���(0)");
				button2.setText("ȫѡ");
			}

			HashMap<Integer, Boolean> list = listadapter.getIsSelected();
			Iterator<Entry<Integer, Boolean>> iter = list.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<Integer, Boolean> entry = (Map.Entry<Integer, Boolean>) iter
						.next();
				Integer key = (Integer) entry.getKey();
				if (button2.getText().equals("ȫѡ")) {
					list.put(key, false);
				} else if (button2.getText().equals("��ѡ")) {
					list.put(key, true);
				}
			}
			listadapter.notifyDataSetChanged();
		}
	}

	class button1chick implements OnClickListener {

		@Override
		public void onClick(View v) {
			HashMap<Integer, Boolean> list = listadapter.getIsSelected();
			Iterator<Entry<Integer, Boolean>> iter = list.entrySet().iterator();
			List<Integer> chooselist = new ArrayList<Integer>();
			chooselist = new ArrayList<Integer>();
			while (iter.hasNext()) {
				Map.Entry<Integer, Boolean> entry = (Map.Entry<Integer, Boolean>) iter
						.next();
				Integer key = (Integer) entry.getKey();
				Boolean val = (Boolean) entry.getValue();
				if (val) {
					chooselist.add(key);
					System.out.println(chooselist);
				}
			}
			dprovider.insertList(listname, chooselist);
			SharedPreferences.Editor ed = mpfs.edit();
			ed.putString("spinner_value", listname);
			ed.commit();
			System.out.println(mpfs.getString("spinner_value", "0"));
			ChooseMp3Activity.this.finish();
		}
	}

	@Override
	protected void onDestroy() {
		System.out.println("choose activity onDestroy");
		if (!cursor.isClosed()) {
			cursor.close();
			System.out.println("choose cursor �ر�");
		}
		super.onDestroy();
	}
}
