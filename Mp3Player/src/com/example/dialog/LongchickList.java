package com.example.dialog;

import com.example.mp3player.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

public class LongchickList extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String listname = savedInstanceState.getString("listname");
		System.out.println("listname is "+ listname);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setView(inflater.inflate(R.layout.addmp3button, null))
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						System.out.println("点击的是确定");
						// 去数据库删除记录

					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						LongchickList.this.getDialog().cancel();
						System.out.println("点击的是取消");
					}
				});
		return builder.create();
	}

}
