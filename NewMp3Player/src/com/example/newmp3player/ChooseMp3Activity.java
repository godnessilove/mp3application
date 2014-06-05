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
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

@SuppressLint("NewApi")
public class ChooseMp3Activity extends ListActivity {
	private String listname;
	private SimpleCursorAdapter listadapter;
	private Button button1;
	private Button button2;
	private ImageButton imagebutton;
	private Cursor cursor;
	private Handler handler;
	private DProvider dprovider;
	private SharedPreferences mpfs;
	private View invisibleview;
	/*
	 * flag,0表示双模式，包含添加与删除，1表示单模式，只有添加
	 */
	private int flag;
	private String[] from;
	private int[] to;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("ChooseMp3Activity", "ChooseMp3Activity onCreate");
		Intent intent = getIntent();
		listname = intent.getStringExtra("listname");
		flag = intent.getIntExtra("mode", 0);
		// 获取保存的播放列表名称的文件
		mpfs = getSharedPreferences(LocalActivity.MP3_SHARED,
				Context.MODE_PRIVATE);
		Log.i("ChooseMp3Activity", "listname is " + listname);
		setContentView(R.layout.addmp3button);

		invisibleview = findViewById(R.id.invisiblebutton);
		button1 = (Button) findViewById(R.id.add);
		button2 = (Button) findViewById(R.id.choose);
		imagebutton = (ImageButton) findViewById(R.id.imageButton1);

		button1.getBackground().setAlpha(0);
		button2.getBackground().setAlpha(0);
		imagebutton.getBackground().setAlpha(0);
		// 添加按钮
		button1.setOnClickListener(new button1chick());
		// 全选/反选按钮
		button2.setOnClickListener(new button2chick());
		//
		imagebutton.setOnClickListener(new imagebuttonclick());
		if (flag == 0) {
			invisibleview.setFocusable(false);
			invisibleview.setVisibility(View.INVISIBLE);
			imagebutton.setFocusable(true);
			imagebutton.setVisibility(View.VISIBLE);
			getListView().setOnItemLongClickListener(new itemLongClick());
		} else {
			invisibleview.setFocusable(true);
			invisibleview.setVisibility(View.VISIBLE);
			imagebutton.setFocusable(false);
			imagebutton.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	protected void onResume() {
		Log.i("ChooseMp3Activity", "ChooseMp3Activity onResume");
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
			dprovider = DProvider.getInstance(getApplicationContext());
			// dprovider.initAllList();
			// 查询展示所有的mp3歌曲名
			// String table_name = getString(R.string.tablelocalname);
			

			from = new String[] { getString(R.string.TILTE),
					getString(R.string.ARTIST) };
			to = new int[] { R.id.mp3name, R.id.mp3size };

			//双模式默认是展示当前播放列表现有个歌曲
			if(flag == 0){
				cursor = dprovider.querydate(listname);
				listadapter = new SimpleCursorAdapter(getApplicationContext(),
						R.layout.mp3info_item, cursor, from, to, 0);
			}else if (flag == 1){
				//单模式展示的是所有的歌曲
				cursor = dprovider.querydate();
				 listadapter = new mp3ListAdapter(getApplicationContext(),
				 R.layout.mp3info_item_choose, cursor, from, to, 0);
				 
			}
			

			
			setListAdapter(listadapter);
		}
	};

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
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
		int chickcount = ((mp3ListAdapter) listadapter).count();
		if (button1.getText().toString()
				.contains(getString(R.string.addbutton))) {
			button1.setText(getString(R.string.addbutton) + "(" + chickcount
					+ ")");
		} else if (button1.getText().toString()
				.contains(getString(R.string.deletebutton))) {
			button1.setText(getString(R.string.deletebutton) + "(" + chickcount
					+ ")");
		}
		if (chickcount == count) {
			button2.setText(getString(R.string.unchoose));
		} else if (chickcount != count) {
			button2.setText(getString(R.string.choose));
		}
	}

	class itemLongClick implements OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			View view = findViewById(R.id.LinearLayout1);

			button1.setText(getString(R.string.deletebutton));
			button2.setText(getString(R.string.choose));
			invisibleview.setFocusable(true);
			invisibleview.setVisibility(View.VISIBLE);

			//长按弹出删除模式，展示当前播放列表包含的所有歌曲
			cursor = dprovider.querydate(listname);
			listadapter = new mp3ListAdapter(getApplicationContext(),
					R.layout.mp3info_item_choose, cursor, from, to, 0);
			setListAdapter(listadapter);

			AnimationSet animations = new AnimationSet(true);
			TranslateAnimation translate = new TranslateAnimation(0, 0,
					view.getHeight() + invisibleview.getHeight(),
					imagebutton.getHeight());
			translate.setDuration(100);
			animations.addAnimation(translate);
			invisibleview.setAnimation(animations);

			imagebutton.setFocusable(false);
			imagebutton.setVisibility(View.INVISIBLE);

			return false;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (flag == 0 && invisibleview.isFocusable()) {
				cursor = dprovider.querydate(listname);
				listadapter = new SimpleCursorAdapter(getApplicationContext(),
						R.layout.mp3info_item, cursor, from, to, 0);
				setListAdapter(listadapter);
				invisibleview.setFocusable(false);
				invisibleview.setVisibility(View.INVISIBLE);
				imagebutton.setFocusable(true);
				imagebutton.setVisibility(View.VISIBLE);
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	class imagebuttonclick implements OnClickListener {

		@Override
		public void onClick(View v) {
			View view = findViewById(R.id.LinearLayout1);

			button1.setText(getString(R.string.addbutton));
			button2.setText(getString(R.string.choose));
			invisibleview.setFocusable(true);
			invisibleview.setVisibility(View.VISIBLE);

			//点击添加图标，展示出了当前列表歌曲之外的所有歌曲
			cursor = dprovider.queryoutsidedate(listname);
			listadapter = new mp3ListAdapter(getApplicationContext(),
					R.layout.mp3info_item_choose, cursor, from, to, 0);
			setListAdapter(listadapter);

			AnimationSet animations = new AnimationSet(true);
			TranslateAnimation translate = new TranslateAnimation(0, 0,
					view.getHeight() + invisibleview.getHeight(),
					imagebutton.getHeight());
			translate.setDuration(500);
			animations.addAnimation(translate);
			invisibleview.setAnimation(animations);

			imagebutton.setFocusable(false);
			imagebutton.setVisibility(View.INVISIBLE);
		}

	}

	class button2chick implements OnClickListener {

		@Override
		public void onClick(View v) {
			int count = listadapter.getCount();
			String buttontext = null;
			if(button1.getText().toString()
					.contains(getString(R.string.addbutton))){
				buttontext = getString(R.string.addbutton);
			}else if(button1.getText().toString()
					.contains(getString(R.string.deletebutton))){
				buttontext = getString(R.string.deletebutton);
			}
			if (button2.getText().equals(getString(R.string.choose))) {
				button1.setText(buttontext + "(" + count
						+ ")");
				button2.setText(getString(R.string.unchoose));
			} else if (button2.getText().equals(getString(R.string.unchoose))) {
				button1.setText(buttontext + "(0)");
				button2.setText(getString(R.string.choose));
			}

			// 更新记录的选择框状态
			HashMap<Integer, Boolean> list = ((mp3ListAdapter) listadapter)
					.getIsSelected();
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
			HashMap<Integer, Boolean> list = ((mp3ListAdapter) listadapter)
					.getIsSelected();
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
					Log.i("ChooseMp3Activity",
							"添加的mp3_id" + chooselist.toString());
				}
			}
			if (button1.getText().toString()
					.contains(getString(R.string.addbutton))) {
				// 插入新的播放列表中
				boolean b = dprovider.insertList(listname, chooselist);

				// 如果插入成功就记录下新增的播放列表名字在共享文件里，否则不记录，并且提示插入失败
				if (b) {
					SharedPreferences.Editor ed = mpfs.edit();
					ed.putString("spinner_value", listname);
					ed.commit();
					Toast.makeText(getApplicationContext(),
							getString(R.string.addtrue), Toast.LENGTH_SHORT)
							.show();
				} else {
					Toast.makeText(getApplicationContext(),
							getString(R.string.addfalse), Toast.LENGTH_SHORT)
							.show();
				}
			} else if (button1.getText().toString()
					.contains(getString(R.string.deletebutton))) {
				// 删除选中的mp3歌曲记录
				boolean b = dprovider.deleteList(listname, chooselist);
				if (b) {
					Toast.makeText(getApplicationContext(),
							getString(R.string.deletetrue), Toast.LENGTH_SHORT)
							.show();
				} else {
					Toast.makeText(getApplicationContext(),
							getString(R.string.deletefalse), Toast.LENGTH_SHORT)
							.show();
				}
			}

			ChooseMp3Activity.this.finish();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i("ChooseMp3Activity", "ChooseMp3Activity onStop");
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
			Log.i("ChooseMp3Activity", "ChooseMp3Activity cursor 关闭");
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
			Log.i("ChooseMp3Activity", "ChooseMp3Activity cursor 关闭");
		}
	}
}
