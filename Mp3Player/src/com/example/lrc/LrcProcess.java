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

import com.example.download.FileUtil;

public class LrcProcess {
	private HashMap<Long, String> lrcs = new HashMap<Long, String>();
	BufferedReader fin;

	public HashMap<Long, String> process(String mp3lrc) {
		File file = new File(mp3lrc);
		FileUtil fileutil = new FileUtil();
		if(fileutil.isfullFileExist(mp3lrc)){
		String line;
		try {
			fin = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), "GBK"));
			while ((line = fin.readLine()) != null) {
				Pattern p = Pattern.compile("\\[(\\d{2}:\\d{2}\\.\\d{2})\\]");
				Matcher mth = p.matcher(line);
				while (mth.find()) {
					long time = StrToLong(mth.group(1));
					String[] str = p.split(line);
					String lrc = str.length > 0 ? str[str.length - 1] : "";
					lrcs.put(time, lrc);
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
				fin.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return lrcs;
		}else return null;
	}

	/*
	 * 00:00.00
	 */
	public static long StrToLong(String timestr) {
		int min = Integer.valueOf(timestr.substring(0, 2));
		int sec = Integer.valueOf(timestr.substring(3, 5));
		int mill = Integer.valueOf(timestr.substring(6, 8));
		return min * 60 * 1000 + sec * 1000 + mill * 10;
	}

	public static Long[] GetAllTime(HashMap<Long, String> lrcs) {
		if (lrcs != null) {
			Long[] temp = new Long[lrcs.keySet().size()];
			lrcs.keySet().toArray(temp);
			Arrays.sort(temp);
			System.out.println("temp is " + temp);
			return temp;
		} else
			return null;
	}
}
