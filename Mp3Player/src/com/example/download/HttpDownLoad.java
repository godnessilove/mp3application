package com.example.download;

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

public class HttpDownLoad {
	private FileUtil fileutil = new FileUtil();
	private FileInputStream fis;

	public String DonwLoad(String strUrl) {
		StringBuffer sb = new StringBuffer();
		BufferedReader buff = null;
		String a;
		try {
			URL url = new URL(strUrl);
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setConnectTimeout(30000);// ���ӳ�ʱ
			huc.setReadTimeout(30000); // �������ݳ�ʱ
			buff = new BufferedReader(new InputStreamReader(
					huc.getInputStream(), "UTF-8"));

			while ((a = buff.readLine()) != null) {
				sb.append(a);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				buff.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public int downFile(String urlstr, String path, String filenames)
			throws IOException {
		int result = 0;
		InputStream input = null;
		URL url = null;
		if (fileutil.isFileExist(path + "/" + filenames)) {
			result = 1;
		} else {
			try {
				url = new URL(urlstr
						+ URLEncoder.encode(filenames, "UTF-8"));
				HttpURLConnection urlconnect = (HttpURLConnection) url
						.openConnection();
				urlconnect.setConnectTimeout(30000);// ���ӳ�ʱ
				urlconnect.setReadTimeout(30000); // �������ݳ�ʱ
				input = urlconnect.getInputStream();
				// �жϿռ��Ƿ�
				long feespace = fileutil.getSDFeeSpace();
				long availablespace = fileutil.getSDAvailableSpace();
				long blockspace = fileutil.getSDBlockSpace();
				System.out.println("feespace is " + feespace);
				System.out.println("availablespace is " + availablespace);
				System.out.println("blockspace is " + blockspace);
				if (availablespace > urlconnect.getContentLength()) {
					fileutil.writeDate(path, filenames, input);
				} else
					System.out.println("SD�ռ䲻��");
			} catch (FileNotFoundException e) {
				System.out.println("Զ�� " + filenames + "�ļ�������");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (input != null)
					input.close();
			}
		}
		return result;
	}

	/*
	 * ��ȡmp3name�����ؽ��ȣ�����%ǰ�沿�ֵ�intֵ
	 */
	public int downPercentage(String mp3name, String mp3size, String downmp3path) {
		int result = 0;
		Float fullsize = Float.parseFloat(mp3size.substring(0,
				mp3size.length() - 2));
		String downpath = fileutil.getSDPath() + downmp3path + "/" + mp3name
				+ ".temp";// ��ʱ�ļ�
		System.out.println("downpath is " + downpath);
		String newdownpath = fileutil.getSDPath() + downmp3path + "/" + mp3name;// ������ɺ���ļ�
		File file = new File(downpath);
		File newfile = new File(newdownpath);
		// ���temp�ļ�������˵��û���������
		if (file.exists()) {
			try {
				fis = new FileInputStream(file);
				result = (int) (Float.parseFloat(String.format("%.2f",
						(double) (fis.available() / 1024 / 1024 / fullsize))) * 100);
				System.out.println("downPercentage is " + result);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else if (newfile.exists()) {
			result = 100;
		}
		return result;
	}

}
