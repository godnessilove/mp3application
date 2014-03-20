package com.example.mp3player;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.example.download.HttpDownLoad;
import com.example.service.DownLoadService;
import com.example.xml.XmlConnentHanndle;
import com.example.xmlmodel.Mp3Info;

import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.support.v4.app.ListFragment;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

@SuppressLint("NewApi")
public class RemoteActivity extends ListFragment {
	private static final int UPDATE = 1;
	private static final int ABOUT = 2;
	private Handler handler = null;
	private SimpleAdapter sim = null;
	private List<Mp3Info> mp3list = null;
	private String list = null;


	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
		handler = new Handler();
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.remote_mp3_item, container, false);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (UPDATE == item.getItemId()) {
			new Thread() {
				public void run() {
					list = DownLoad("http://192.168.0.101:8080/mp3/resources.xml");
					System.out.println("下载列表成功：" + list);
					handler.post(s);
				}
			}.start();
		}else if (ABOUT == item.getItemId()){
			System.out.println("关于：开发中");
		}
		return super.onOptionsItemSelected(item);
	}

	Runnable s = new Runnable() {

		@Override
		public void run() {
			mp3list = parse(list);
			sim = buildSimpleAdapter(mp3list);
			//必须在UI线程中操作
			setListAdapter(sim);
		}
	};
	

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		System.out.println("点击的是：" + mp3list.get(position).getMp3name() );
		Mp3Info mp3info = mp3list.get(position);
		Intent intent = new Intent();
		intent.putExtra("mp3info", mp3info);
		intent.setClass(getActivity(),DownLoadService.class);
		getActivity().startService(intent);
	}

	protected SimpleAdapter buildSimpleAdapter(List<Mp3Info> mp3list) {
		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		for (Iterator<Mp3Info> iterator = mp3list.iterator(); iterator
				.hasNext();) {
			Mp3Info mp3lists = iterator.next();
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("mp3name", mp3lists.getMp3name());
			map.put("mp3size", mp3lists.getMp3size());
			list.add(map);
		}
		SimpleAdapter simpad = new SimpleAdapter(getActivity(), list,
				R.layout.mp3info_item, new String[] { "mp3name", "mp3size" },
				new int[] { R.id.mp3name, R.id.mp3size });
		return simpad;
	}

	private String DownLoad(String strurl) {
		HttpDownLoad hdl = new HttpDownLoad();
		String result = hdl.DonwLoad(strurl);
		return result;
	}

	private List<Mp3Info> parse(String list) {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		List<Mp3Info> info = new ArrayList<Mp3Info>();
		try {
			XMLReader xmlReader = spf.newSAXParser().getXMLReader();
			XmlConnentHanndle xch = new XmlConnentHanndle(info);
			xmlReader.setContentHandler(xch);
			xmlReader.parse(new InputSource(new StringReader(list)));
			for (Iterator<Mp3Info> iterator = info.iterator(); iterator
					.hasNext();) {
				Mp3Info mp3Info = iterator.next();
				System.out.print(mp3Info);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return info;

	}

}
