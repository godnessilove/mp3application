package com.example.adapter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.example.mp3player.R;
import com.example.sqlite.PlayMp3ListTable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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

	public mp3ListAdapter(Context context, int layout, Cursor c, String[] from,
			int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		this.inflater = LayoutInflater.from(context);
		this.cursor = c;
		// 初始化状态表，全都是未选状态
		while (c.moveToNext()) {
			int id = cursor.getInt(cursor
					.getColumnIndex(android.provider.BaseColumns._ID));
			isSelected.put(id, false);
		}
	}

	public HashMap<Integer, Boolean> getIsSelected() {
		return isSelected;
	}

	@Override
	public int getCount() {
		return super.getCount();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolde viewholde ;
		
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

		// 取当前记录的_id
		cursor.moveToPosition(position);

		final int id = cursor.getInt(cursor
				.getColumnIndex(android.provider.BaseColumns._ID));
		String name = cursor.getString(cursor.getColumnIndex(PlayMp3ListTable
				.getMp3name()));
		String size = cursor.getString(cursor.getColumnIndex(PlayMp3ListTable
				.getMp3size()));
		viewholde.nametext.setText(name);
		viewholde.sizetext.setText(size);

		viewholde.check
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
							isSelected.put(id, isChecked);
					}
				});

		// 防止拖动列表造成状态丢失
		viewholde.check.setChecked(isSelected.get(id));
		return convertView;
	}

	// 求取有多少个是选上的
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

	class ViewHolde {

		public TextView nametext;

		public TextView sizetext;

		public CheckBox check;

	}

}
