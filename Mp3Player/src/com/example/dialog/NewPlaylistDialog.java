package com.example.dialog;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.example.mp3player.ChooseMp3Activity;
import com.example.mp3player.LocalActivity;
import com.example.mp3player.R;
import com.example.sqlite.DProvider;
import com.example.sqlite.DateBaseHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

@SuppressLint({ "CommitPrefEdits", "InlinedApi", "NewApi" })
public class NewPlaylistDialog extends DialogFragment {
	private String text;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		System.out.println("NewPlaylistDialog  onCreateDialog");
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
						DProvider dprovider = DProvider.getInstance(getActivity().getApplicationContext());
						ArrayList<String> list = (ArrayList<String>) dprovider
								.queryList(DateBaseHelper.getAlltablename());
						EditText edittext = (EditText) view
								.findViewById(R.id.adddialog);
						text = edittext.getText().toString();
						if (text.equals("")) {
							Toast.makeText(getActivity(), "播放列表不能为空",
									Toast.LENGTH_SHORT).show();
							closedialog(dialog,false);
						} else if (!list.contains(text)) {
							Intent intent = new Intent();
							intent.putExtra("listname", text);
							intent.setClass(getActivity(),
									ChooseMp3Activity.class);
							startActivity(intent);
							closedialog(dialog,true);
						} else {
							Toast.makeText(getActivity(),
									edittext.getText() + "播放列表已存在",
									Toast.LENGTH_SHORT).show();
							closedialog(dialog,false);
						}
						
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						NewPlaylistDialog.this.getDialog().cancel();
						System.out.println("点击的是取消");
						mListener.onArticleSelected(1);
						closedialog(dialog,true);
					}
				}).setInverseBackgroundForced(false);
		NewPlaylistDialog.this.setCancelable(false);
		return builder.create();
	}

	public interface DialogListener {
		public void onArticleSelected(int position);
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

	public void closedialog(DialogInterface dialog, Boolean isclose){
		try{
		    Field field = dialog.getClass()
		            .getSuperclass().getDeclaredField(
		                     "mShowing" );
		    field.setAccessible( true );
		     //   将mShowing变量设为false，表示对话框已关闭 ,即欺骗源码，做到dialog不关闭，反之将恢复正常
		    field.set(dialog, isclose );
		    dialog.dismiss();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void onPause() {
		super.onPause();
		SharedPreferences mpfs = getActivity().getPreferences(
				LocalActivity.SPINNER_STATE);
		SharedPreferences.Editor ed = mpfs.edit();
		ed.putString("spinner_value", text);
		ed.commit();
		System.out.println("NewPlaylistDialog onPause");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		System.out.println("NewPlaylistDialog onDestroy");
	}

}
