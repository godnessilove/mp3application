package com.example.newmp3player;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.download.HttpDownLoad;
import com.example.service.DownLoadService;
import com.example.xml.XmlConnentHanndle;
import com.example.xmlmodel.Mp3Info;

@SuppressLint({ "NewApi", "ShowToast", "HandlerLeak" })
public class RemoteActivity extends ListFragment {
	private static final int UPDATE = 1;
	private static final int ABOUT = 2;
	private SimpleAdapter sim = null;
	private List<Mp3Info> mp3list = null;
	private List<HashMap<String, String>> adapterlist;
	private String list = null;
	private String https = null;
	private MyHander myhandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// 允许菜单点击有用
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
		// 获取服务器中mp3列表信息地址
		https = getString(R.string.https) + "resources.xml";
		myhandler = new MyHander();
		Log.i("RemoteActivity", "RemoteActivity is onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i("RemoteActivity", "RemoteActivity is onCreateView");
		return inflater.inflate(R.layout.remote_mp3_item, container, false);
	}
	
	

	@Override
	public void onResume() {
		Log.i("RemoteActivity", "RemoteActivity is onResume");
		super.onResume();
	}

	@Override
	public void onStart() {
		Log.i("RemoteActivity", "RemoteActivity is onStart");
		View view = getView();
		view.setBackgroundResource(R.drawable.remoteimage);
		view.getBackground().setAlpha(100);
		super.onStart();
	}

	@Override
	public void onStop() {
		Log.i("RemoteActivity", "RemoteActivity is onStop");
		super.onStop();
	}

	// 处理菜单点击事件，下载解析 服务器MP3列表，并展示
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(isVisible()){
		if (UPDATE == item.getItemId()) {
			new Thread() {
				public void run() {
					list = DownLoad(https);
					Log.i("list is ", list);
					if (list.equals("error")) {
						// 更新失败
						myhandler.sendEmptyMessage(1);
					} else {
						// 成功，解析列表，并且展示
						mp3list = parse(list);
						if (mp3list != null && adapterlist != null) {
							myhandler.sendEmptyMessage(2);
						} else {
							// 更新失败
							myhandler.sendEmptyMessage(1);
						}

					}
				}
			}.start();
		} else if (ABOUT == item.getItemId()) {
			Toast.makeText(this.getActivity(), "开发中", Toast.LENGTH_SHORT)
					.show();
		}
		return super.onOptionsItemSelected(item);
		}else return false;
	}

	private final class MyHander extends Handler {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			// 更新失败
			case 1:
				Toast.makeText(getActivity().getApplicationContext(),
						"更新下载列表失败", Toast.LENGTH_SHORT).show();
				break;
			// 更新成功，展示列表
			case 2:
				sim = buildSimpleAdapter(adapterlist);
				setListAdapter(sim);
				break;
			}
		}

	}

	// 点击列表中的mp3，开始下载歌曲
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.i("remoteactivity ", "点击的是：" + mp3list.get(position).getMp3name());
		Mp3Info mp3info = mp3list.get(position);
		Intent intent = new Intent();
		intent.putExtra("mp3info", mp3info);
		intent.putExtra("msg", "NEWCREATE");
		intent.setClass(getActivity(), DownLoadService.class);
		getActivity().startService(intent);
	}

	/**
	 * 求取展示列表对应的适配器
	 * @param list 适配器数据-mp3信息列表
	 * @return 适配器
	 */
	protected SimpleAdapter buildSimpleAdapter(
			List<HashMap<String, String>> list) {
		SimpleAdapter simpad = new SimpleAdapter(getActivity(), list,
				R.layout.mp3info_item, new String[] { "mp3name", "mp3size" },
				new int[] { R.id.mp3name, R.id.mp3size });
		return simpad;
	}

	/**
	 * 读取服务器上mp3信息
	 * @param strurl  下载地址
	 * @return 返回下载结果
	 */
	private String DownLoad(String strurl) {
		HttpDownLoad hdl = new HttpDownLoad();
		String result = hdl.DonwLoad(strurl);
		return result;
	}

	/**
	 * 解析xml文件
	 * @param list 远程列表文件内容
	 * @return 返回解析mp3信息列表
	 */
	private List<Mp3Info> parse(String list) {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		List<Mp3Info> info = new ArrayList<Mp3Info>();
		adapterlist = new ArrayList<HashMap<String, String>>();
		try {
			XMLReader xmlReader = spf.newSAXParser().getXMLReader();
			XmlConnentHanndle xch = new XmlConnentHanndle(info);
			xmlReader.setContentHandler(xch);
			xmlReader.parse(new InputSource(new StringReader(list)));
			for (Iterator<Mp3Info> iterator = info.iterator(); iterator
					.hasNext();) {
				Mp3Info mp3Info = iterator.next();
				HashMap<String, String> map = new HashMap<String, String>();
				// adapterlist数据为adapter准备
				map.put("mp3name", mp3Info.getMp3name());
				map.put("mp3size", mp3Info.getMp3size());
				adapterlist.add(map);
				Log.i("remoteactivity", "解析的MP3下载列表 ：" + mp3Info);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return info;

	}
	
	@Override
	public void onPause() {
		super.onDestroy();
		Log.i("remoteactivity", "remoteactivity onPause");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("remoteactivity", "remoteactivity onDestroy");
		myhandler.removeCallbacksAndMessages(null);
	}

}
