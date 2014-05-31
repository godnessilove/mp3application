package com.example.dialog;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.example.newmp3player.ChooseMp3Activity;
import com.example.newmp3player.LocalActivity;
import com.example.newmp3player.R;
import com.example.sqlite.DProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

@SuppressLint("NewApi")
public class NewPlaylistDialog extends DialogFragment {
	private String text;

	public interface DialogListener {
		public void onArticleSelected(int index, Bundle bundle);
	}

	DialogListener mListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the DialogListener so we can send events to the
			// host
			mListener = (DialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement DialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		final LayoutInflater inflater = getActivity().getLayoutInflater();
		final View view = inflater.inflate(R.layout.addplaylist, null);
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(view)
				// 点击确认事件
				.setPositiveButton(getString(R.string.dialogpositive),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								DProvider dprovider = DProvider
										.getInstance(getActivity()
												.getApplicationContext());
								// 查询所有的播放列表，用于判断新增的播放列表是否已经存在
								ArrayList<String> list = (ArrayList<String>) dprovider
										.queryList();
								EditText edittext = (EditText) view
										.findViewById(R.id.adddialog);
								text = edittext.getText().toString();
								if (text.equals("")) {
									Toast.makeText(
											getActivity(),
											getString(R.string.playlistnotnull),
											Toast.LENGTH_SHORT).show();
									// 不关闭通知栏
									closedialog(dialog, false);
								} else if (!list.contains(text)) {
									Intent intent = new Intent();
									intent.putExtra("listname", text);
									intent.setClass(getActivity(),
											ChooseMp3Activity.class);
									startActivity(intent);
									// 关闭通知栏
									closedialog(dialog, true);
								} else {
									Toast.makeText(
											getActivity(),
											edittext.getText()
													+ getString(R.string.playlistexist),
											Toast.LENGTH_SHORT).show();
									// 不关闭对话框
									closedialog(dialog, false);
								}

							}
						})
				// 点击取消事件
				.setNegativeButton(getString(R.string.dialognegative),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								closedialog(dialog, true);
								// 通过通知activity来控制lcoalfragment回到正确的spinner播放列表选项,选择全部列表
								mListener.onArticleSelected(0, null);
								closedialog(dialog, true);
							}
						})
				// 设置背景不可点击
				.setInverseBackgroundForced(false);
		// 设置按返回键不能取消通知栏
		NewPlaylistDialog.this.setCancelable(false);
		return builder.create();
	}

	/**
	 * 关闭对话框
	 * 
	 * @param dialog
	 * @param isclose
	 *            是否关闭对话框，false表示不关闭，true表示关闭
	 */
	public void closedialog(DialogInterface dialog, Boolean isclose) {
		try {
			Field field = dialog.getClass().getSuperclass()
					.getDeclaredField("mShowing");
			field.setAccessible(true);
			// 将mShowing变量设为false，表示对话框已关闭 ,即欺骗源码，做到dialog不关闭，反之将恢复正常
			field.set(dialog, isclose);
			dialog.dismiss();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i("NewPlaylistDialog", "NewPlaylistDialog onPause");
		SharedPreferences mpfs = getActivity().getSharedPreferences(LocalActivity.MP3_SHARED,Context.MODE_PRIVATE);
		SharedPreferences.Editor ed = mpfs.edit();
		ed.putString("spinner_value", text);
		ed.commit();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("NewPlaylistDialog", "NewPlaylistDialog onDestroy");
	}

}
