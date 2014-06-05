package com.example.adapter;

import java.util.Collection;
import java.util.HashMap;

import com.example.newmp3player.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

@SuppressLint({ "NewApi", "UseSparseArrays" })
public class mp3ListAdapter extends SimpleCursorAdapter {
	private LayoutInflater inflater;
	// map用来记录选中项
	public HashMap<Integer, Boolean> isSelected = new HashMap<Integer, Boolean>();
	private Cursor cursor;
	private Context context;
	private String TITLE ;
	private String ARTIST ;

	public mp3ListAdapter(Context context, int layout, Cursor c, String[] from,
			int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		this.inflater = LayoutInflater.from(context);
		this.cursor = c;
		this.context = context.getApplicationContext();
		// 初始化状态表，全都是未选状态
		this.cursor.moveToPosition(-1);
		while (this.cursor.moveToNext()) {
			int id = this.cursor.getInt(this.cursor
					.getColumnIndex(android.provider.BaseColumns._ID));
			isSelected.put(id, false);
		}
		this.TITLE = this.context
				.getString(R.string.TILTE);
		this.ARTIST = this.context
				.getString(R.string.ARTIST);
	}

	/**
	 * 获取多选框状态
	 * 
	 * @return 返回状态表
	 */
	public HashMap<Integer, Boolean> getIsSelected() {
		return isSelected;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolde viewholde;
		// 屏幕往上滑动的时候convertView会使用最上面被隐藏掉的项的convertView,所以不用重新构建，提高效率
		if (convertView == null) {
			viewholde = new ViewHolde();
			convertView = inflater.inflate(R.layout.mp3info_item_choose, null);
			viewholde.nametext = (TextView) convertView
					.findViewById(R.id.mp3name);
			viewholde.sizetext = (TextView) convertView
					.findViewById(R.id.mp3size);
			viewholde.check = (CheckBox) convertView
					.findViewById(R.id.multiple_checkbox);
			convertView.setTag(viewholde);
		} else {
			viewholde = (ViewHolde) convertView.getTag();
		}

		// 取当前记录的_id，mp3名称，大小
		cursor.moveToPosition(position);
		final int id = cursor.getInt(cursor
				.getColumnIndex(android.provider.BaseColumns._ID));
		String name = cursor.getString(cursor.getColumnIndex(TITLE));
		String artist = cursor.getString(cursor.getColumnIndex(ARTIST));
		viewholde.nametext.setText(name);
		viewholde.sizetext.setText(artist);

		// 给checkbox添加选择变更监听事件
		viewholde.check
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// 记录下选择状态
						isSelected.put(id, isChecked);
					}
				});

		// 防止拖动列表造成状态丢失
		viewholde.check.setChecked(isSelected.get(id));
		return convertView;
	}

	/**
	 * 求取有多少个是选上的
	 * 
	 * @return 选中个数
	 */
	public int count() {
		Collection<Boolean> collection = isSelected.values();
		int count = 0;
		for (Boolean boolean1 : collection) {
			if (boolean1) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 存放每行裡面的組件
	 * 
	 * @author liming
	 * 
	 */
	public static final class ViewHolde {

		public TextView nametext;

		public TextView sizetext;

		public CheckBox check;

	}

}
