package com.example.dialog;

import java.util.ArrayList;

import com.example.mp3player.ChooseMp3Activity;
import com.example.mp3player.R;
import com.example.sqlite.DProvider;
import com.example.sqlite.DateBaseHelper;

import android.app.AlertDialog;
import android.app.Dialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class NewPlaylistDialog extends DialogFragment {
	private EditText edittext;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		final LayoutInflater inflater = getActivity().getLayoutInflater();
		final View view = inflater.inflate(R.layout.addplaylist, null);
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(view)
				// Add action buttons
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// sign in the user ...
						System.out.println("点击的是确认");

						DProvider dprovider = new DProvider(getActivity());
						ArrayList<String> list = (ArrayList<String>) dprovider
								.queryList(DateBaseHelper.getAlltablename());

						edittext = (EditText) view.findViewById(R.id.adddialog);
						String text  = edittext.getText().toString();
						if (!list.contains(edittext.getText())) {
							Intent intent = new Intent();
							intent.putExtra("listname", text);
							intent.setClass(getActivity(),
									ChooseMp3Activity.class);
							startActivity(intent);
						} else {
							Toast.makeText(getActivity(),
									edittext.getText() + "播放列表已存在",
									Toast.LENGTH_SHORT).show();
						}
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// LoginDialogFragment.this.getDialog().cancel();
						NewPlaylistDialog.this.getDialog().cancel();
						System.out.println("点击的是取消");
					}
				});
		return builder.create();
	}

}
