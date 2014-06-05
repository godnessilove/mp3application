package com.example.newmp3player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.example.adapter.mp3ListAdapter;
import com.example.sqlite.DProvider;

import android.annotation.SuppressLint;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class Choose_fragment_1 extends ListFragment{
	private String listname;
	private mp3ListAdapter listadapter;
	private Button button1;
	private Button button2;
	private Cursor cursor;
	private Handler handler;
	private DProvider dprovider;
	private SharedPreferences mpfs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("Choose_fragment_1", "Choose_fragment_1 onCreate");
		Intent intent = getActivity().getIntent();
		listname = intent.getStringExtra("listname");
		// 获取保存的播放列表名称的文件
		mpfs = getActivity().getSharedPreferences(LocalActivity.MP3_SHARED, Context.MODE_PRIVATE);
		Log.i("Choose_fragment_1", "listname is " + listname);
	}

	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.addmp3button, container, false);
	}

	

	@Override
	public void onStart() {
		super.onStart();
		button1 = (Button) getActivity().findViewById(R.id.add);
		button2 = (Button) getActivity().findViewById(R.id.choose);
		button1.getBackground().setAlpha(0);
		button2.getBackground().setAlpha(0);
		// 添加按钮
		button1.setOnClickListener(new button1chick());
		// 全选/反选按钮
		button2.setOnClickListener(new button2chick());
	}



	@Override
	public void onResume() {
		Log.i("Choose_fragment_1", "Choose_fragment_1 onResume");
		if (handler == null) {
			handler = new Handler();
		}
		// 展示歌曲列表供选择
		handler.post(createlocaltable);
		super.onResume();
	}

	Runnable createlocaltable = new Runnable() {

		@Override
		public void run() {
			dprovider = DProvider.getInstance(getActivity());
			//dprovider.initAllList();
			// 查询展示所有的mp3歌曲名
			//String table_name = getString(R.string.tablelocalname);
			cursor = dprovider.querydate();

			String[] from = new String[] { getString(R.string.TILTE),
					getString(R.string.SIZE) };
			int[] to = new int[] { R.id.mp3name, R.id.mp3size };

			listadapter = new mp3ListAdapter(getActivity(),
					R.layout.mp3info_item_choose, cursor, from, to, 0);
			setListAdapter(listadapter);
		}
	};

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// 这里的position是点击的项处理整个listview的位置(包括看不见的)
		// 取当前可视的第一个item在整个listview中的位置j(包括看不见的)
		int j = l.getFirstVisiblePosition();
		// 取得点击的真实item(可见的位置)
		position = position - j;
		// 获取屏幕上点击的项.view
		View view = l.getChildAt(position);
		// 获取该view中的多选框
		CheckBox checkbox = (CheckBox) view
				.findViewById(R.id.multiple_checkbox);
		// 更新勾选状态
		if (checkbox.isChecked()) {
			checkbox.setChecked(false);
		} else {
			checkbox.setChecked(true);
		}
		// 计算listview的总个数
		int count = listadapter.getCount();
		// 计算勾选的个数
		int chickcount = listadapter.count();
		button1.setText(getString(R.string.addbutton) + "(" + chickcount + ")");
		if (chickcount == count) {
			button2.setText(getString(R.string.unchoose));
		} else if (chickcount != count) {
			button2.setText(getString(R.string.choose));
		}
	}

	class button2chick implements OnClickListener {

		@Override
		public void onClick(View v) {
			int count = listadapter.getCount();

			if (button2.getText().equals(getString(R.string.choose))) {
				button1.setText(getString(R.string.addbutton) + "(" + count
						+ ")");
				button2.setText(getString(R.string.unchoose));
			} else if (button2.getText().equals(getString(R.string.unchoose))) {
				button1.setText(getString(R.string.addbutton) + "(0)");
				button2.setText(getString(R.string.choose));
			}

			// 更新记录的选择框状态
			HashMap<Integer, Boolean> list = listadapter.getIsSelected();
			Iterator<Entry<Integer, Boolean>> iter = list.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<Integer, Boolean> entry = (Map.Entry<Integer, Boolean>) iter
						.next();
				Integer key = (Integer) entry.getKey();
				if (button2.getText().equals(getString(R.string.choose))) {
					list.put(key, false);
				} else if (button2.getText().equals(
						getString(R.string.unchoose))) {
					list.put(key, true);
				}
			}
			// 根据新的勾选状态更新listview
			listadapter.notifyDataSetChanged();
		}
	}

	class button1chick implements OnClickListener {

		@Override
		public void onClick(View v) {
			// 获取打钩的mp3，在表中的_id信息，插入新的播放列表中
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
					Log.i("Choose_fragment_1",
							"添加的mp3_id" + chooselist.toString());
				}
			}
			// 插入新的播放列表中
			boolean b = dprovider.insertList(listname, chooselist);

			// 如果插入成功就记录下新增的播放列表名字在共享文件里，否则不记录，并且提示插入失败
			if (b) {
				SharedPreferences.Editor ed = mpfs.edit();
				ed.putString("spinner_value", listname);
				ed.commit();
			} else {
				Toast.makeText(getActivity(),
						getString(R.string.addfalse), Toast.LENGTH_SHORT)
						.show();
			}
			Choose_fragment_1.this.getActivity().finish();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.i("Choose_fragment_1", "Choose_fragment_1 onStop");
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
			Log.i("Choose_fragment_1", "Choose_fragment_1 cursor 关闭");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
			Log.i("Choose_fragment_1", "Choose_fragment_1 cursor 关闭");
		}
	}
}
