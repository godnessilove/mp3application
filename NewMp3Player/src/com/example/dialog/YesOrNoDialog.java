package com.example.dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.example.fileutil.FileUtil;
import com.example.newmp3player.R;
import com.example.sqlite.DProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

@SuppressLint("ValidFragment")
public class YesOrNoDialog extends DialogFragment {
	private String listname;
	private String mp3name;
	private String mp3path;
	private int id;
	private boolean b;
	/**
	 * 0表示只删除记录，1表示删除文件
	 */
	private int mode;

	public YesOrNoDialog() {
		super();

	}

	public YesOrNoDialog(String listname, String mp3name, String mp3path, int id) {
		super();
		this.listname = listname;
		this.mp3name = mp3name;
		this.mp3path = mp3path;
		this.id = id;
	}

	public interface YesOrNoDialogListener {
		public void onItemSelected();
	}

	YesOrNoDialogListener mListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (YesOrNoDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement DialogListener");
		}
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final DProvider dprovider = new DProvider(getActivity());
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		String title = this.mode == 0 ? getString(R.string.deleterecord)
				: getString(R.string.deletefile);
		builder.setTitle(title)
				.setMessage(title + ": " + mp3name + "  ??")
				.setNegativeButton(getString(R.string.dialognegative),
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								YesOrNoDialog.this.getDialog().cancel();
							}
						})
				.setPositiveButton(getString(R.string.dialogpositive),
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// 判断是删除记录还是删除文件
								switch (mode) {
								case 0:

									break;
								case 1:
									FileUtil fileutil = new FileUtil();
									fileutil.deleteFile(new File(mp3path));
									dprovider.deleteFile(id);
									// 通知媒体库刷新

									getActivity()
											.sendBroadcast(
													new Intent(
															Intent.ACTION_MEDIA_MOUNTED,
															Uri.parse("file://"
																	+ Environment
																			.getExternalStorageDirectory()
																			.getAbsolutePath())));

									/*
									 * Uri data = Uri.parse("file:///" +
									 * mp3path); getActivity() .sendBroadcast(
									 * new Intent(
									 * Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
									 * data));
									 */
									break;
								}
								List<Integer> chooselist = new ArrayList<Integer>();
								chooselist.add(id);
								b = dprovider.deleteList(listname, chooselist);
								if (b) {
									Toast.makeText(getActivity(),
											getString(R.string.deletetrue),
											Toast.LENGTH_SHORT).show();
									mListener.onItemSelected();
								} else {
									Toast.makeText(getActivity(),
											getString(R.string.deletefalse),
											Toast.LENGTH_SHORT).show();
								}
							}
						});
		return builder.create();
	}
}
