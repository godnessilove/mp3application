package com.example.download;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.example.fileutil.FileUtil;

@SuppressLint("DefaultLocale")
public class HttpDownLoad {
	private FileUtil fileutil = new FileUtil();
	private FileInputStream fis;

	/**
	 * 下载远程歌词列表文件
	 * 
	 * @param strUrl
	 *            远程mp3列表xml的地址
	 * @return 返回null正确或者error错误
	 */

	public String DonwLoad(String strUrl) {
		String result = "error";
		StringBuffer sb = new StringBuffer();
		BufferedReader buff = null;
		String a;
		try {
			URL url = new URL(strUrl);
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setConnectTimeout(10000);// 链接超时
			huc.setReadTimeout(30000); // 传输数据超时
			if (huc.getResponseCode() == 200) {
				buff = new BufferedReader(new InputStreamReader(
						huc.getInputStream(), "UTF-8"));

				while ((a = buff.readLine()) != null) {
					sb.append(a);
					result = sb.toString();
				}
			}
		} catch (Exception e) {
			result = "error";
			e.printStackTrace();
		} finally {
			try {
				if (buff != null) {
					buff.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 下载mp3和歌词
	 * 
	 * @param urlstr
	 *            远程mp3文件地址
	 * @param path
	 *            本地存放mp3的目录
	 * @param filenames
	 *            mp3名字
	 * @return 返回下载信息-1下早錯誤，2文件已存在，0正确下载
	 */
	public int downFile(String urlstr, String path, String filenames,
			int threadid) {
		int result = 0;
		InputStream input = null;
		URL url = null;
		if (fileutil.isFileExist(path + "/" + filenames)) {
			result = 2;
		} else {
			try {
				url = new URL(urlstr + URLEncoder.encode(filenames, "UTF-8"));
				HttpURLConnection urlconnect = (HttpURLConnection) url
						.openConnection();
				urlconnect.setConnectTimeout(10000);// 链接超时
				urlconnect.setReadTimeout(30000); // 传输数据超时
				if (urlconnect.getResponseCode() == 200) {
					input = urlconnect.getInputStream();
					// 判断空间是否够
					long availablespace = fileutil.getSDAvailableSpace();
					if (availablespace > urlconnect.getContentLength()) {
						File file = fileutil.writeDate(path, filenames, input,
								threadid);
						if (file == null) {
							// 正常结束缺没有成功写成文件，说明是人工取消了
							result = -2;
						}
					} else {
						Log.i("downFile", "SD空间不够,sd空间剩余：" + availablespace);
						result = -1;
					}
				} else
					result = -1;
			} catch (FileNotFoundException e) {
				Log.d("downFile", "远程 " + filenames + "文件不存在");
				result = -1;
			} catch (Exception e) {
				e.printStackTrace();
				result = -1;
			} finally {
				if (input != null)
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
		return result;
	}

	/**
	 * 获取mp3name的下载进度，返回%前面部分的int值
	 * 
	 * @param mp3name
	 *            mp3名字
	 * @param mp3size
	 *            mp3总大小
	 * @param downmp3path
	 *            存放mp3目录名
	 * @return 返回下载百分数
	 */

	public int downPercentage(String mp3name, String mp3size, String downmp3path) {
		// 记录下载文件百分比
		int result = 0;
		// mp3大小
		Float fullsize = Float.parseFloat(mp3size.substring(0,
				mp3size.length() - 2));
		// 临时文件
		String downpath = fileutil.getSDPath() + downmp3path + "/" + mp3name
				+ ".temp";
		Log.i("HttpDownLoad", "downpath is " + downpath);
		// 下载完成后的文件
		String newdownpath = fileutil.getSDPath() + downmp3path + "/" + mp3name;
		File file = new File(downpath);
		File newfile = new File(newdownpath);
		// 如果temp文件存在则说明没有完成下载
		if (file.exists()) {
			try {
				fis = new FileInputStream(file);
				result = (int) (Float.parseFloat(String.format("%.2f",
						(double) (fis.available() / 1024 / 1024 / fullsize))) * 100);
				Log.i("HttpDownLoad", "downPercentage is " + result);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (newfile.exists()) {
			// 如果存在说明已经有了
			result = 100;
		}
		return result;
	}

}
