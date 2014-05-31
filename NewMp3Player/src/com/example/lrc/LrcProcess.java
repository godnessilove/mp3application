package com.example.lrc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import com.example.fileutil.FileUtil;

public class LrcProcess {
	private HashMap<Long, String> lrcs = new HashMap<Long, String>();
	private BufferedReader fin;
	private long longtime;

	public long getLongtime() {
		return longtime;
	}

	/**
	 * 正则表达式解析歌词，规整为<时间，歌词>格式
	 * @param mp3lrc 歌词文件路径
	 * @return 返回歌词信息
	 */
	public HashMap<Long, String> process(String mp3lrc) {
		File file = new File(mp3lrc);
		FileUtil fileutil = new FileUtil();
		if(fileutil.isfullFileExist(mp3lrc)){
			//按行读取
		String line;
		try {
			fin = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), "GBK"));
			while ((line = fin.readLine()) != null) {
				//正则表达式,读取歌词
				Pattern p = Pattern.compile("\\[(\\d{2}:\\d{2}\\.\\d{2})\\]");
				Matcher mth = p.matcher(line);
				//将时间对应的歌词放入hashmap中
				while (mth.find()) {
					longtime = StrToLong(mth.group(1));
					String[] str = p.split(line);
					String lrc = str.length > 0 ? str[str.length - 1] : "";
					lrcs.put(longtime, lrc);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(fin != null){
				fin.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return lrcs;
		}else return null;
	}

	/**
	 * 将00:00.00格式的时间转化为毫秒返回
	 * @param timestr 时间
	 * @return 返回long型毫秒数
	 */
	public static long StrToLong(String timestr) {
		long min = Long.valueOf(timestr.substring(0, 2));
		long sec = Long.valueOf(timestr.substring(3, 5));
		long mill = Long.valueOf(timestr.substring(6, 8));
		return min * 60 * 1000 + sec * 1000 + mill * 10;
	}

	/**
	 * 获取排序后的时间列表
	 * @param lrcs 歌词信息
	 * @return 返回信息里所有的时间，并排序
	 */
	public static Long[] GetAllTime(HashMap<Long, String> lrcs) {
		if (lrcs != null) {
			Long[] temp = new Long[lrcs.keySet().size()];
			lrcs.keySet().toArray(temp);
			Arrays.sort(temp);
			Log.i("LrcProcess", "temp is " + temp);
			return temp;
		} else
			return null;
	}
	
	public static int getindex(HashMap<Long, String> lrcs,int mDuration){
		Long[] indexs = GetAllTime(lrcs);
		 for (int i = 0; i < indexs.length - 1; i++) {
			             if (mDuration >= indexs[i] && mDuration < indexs[i + 1]) {
			                 return i;
			             }
			         }
			         return indexs.length - 1;
	}
}
