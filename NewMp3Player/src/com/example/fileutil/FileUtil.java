package com.example.fileutil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.xmlmodel.Mp3Info;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class FileUtil {
	private static String SDPath = null;

	/**
	 * 构造函数，判断是否有存储卡，有则初始化存储卡地址
	 */
	public FileUtil() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			SDPath = Environment.getExternalStorageDirectory() + "/";
		} else
			Log.i("FileUtil", "没有存储卡");

	}

	/**
	 * 获取存储卡地址
	 * 
	 * @return 返回SD卡地址
	 */
	public String getSDPath() {
		return SDPath;
	}

	/**
	 * 获取存储卡空闲空间大小，单位byte(包含reserved blocks，一般应用程序不可用)
	 * 
	 * @return 返回空闲空间大小(包含不可用部分)
	 */
	public long getSDFeeSpace() {
		StatFs sf = new StatFs(SDPath);
		long feespace = (long) sf.getFreeBlocks() * sf.getBlockSize();
		return feespace;
	}

	/**
	 * 获取存储卡可用空间大小，单位byte
	 * 
	 * @return 返回可用空间大小
	 */
	public long getSDAvailableSpace() {
		StatFs sf = new StatFs(SDPath);
		long availablespace = (long) sf.getAvailableBlocks()
				* sf.getBlockSize();
		return availablespace;
	}

	/**
	 * 获取存储卡总共空间大小，单位byte
	 * 
	 * @return 返回总空间大小
	 */
	public long getSDBlockSpace() {
		StatFs sf = new StatFs(SDPath);
		long blockspace = (long) sf.getBlockCount() * sf.getBlockSize();
		return blockspace;
	}

	/**
	 * 创建目录
	 * 
	 * @param dirname
	 *            目录名
	 * @return 返回目录
	 */
	public File createDir(String dirname) {
		File dir = new File(SDPath + dirname);
		if (!dir.exists() && !dir.isDirectory()) {
			dir.mkdir();
		}
		return dir;
	}

	/**
	 * 创建文件
	 * 
	 * @param filename
	 *            文件名
	 * @return 返回文件
	 * @throws IOException
	 */
	public File createFile(String filename) throws IOException {
		File files = new File(SDPath + filename);
		files.createNewFile();
		return files;
	}

	/**
	 * 判断文件是否存在
	 * 
	 * @param filename
	 *            文件名
	 * @return boolean
	 */
	public Boolean isFileExist(String filename) {
		File files = new File(SDPath + filename);
		return files.exists();
	}

	/**
	 * 判断文件是否存在(全路径)
	 * 
	 * @param filename
	 *            文件名
	 * @return boolean
	 */
	public Boolean isfullFileExist(String filename) {
		File files = new File(filename);
		return files.exists();
	}

	public void deleteFile(File files) {
		if (files.exists()) {
			files.delete();
		}
	}

	/**
	 * 写数据
	 * 
	 * @param dirname
	 *            目录名
	 * @param filename
	 *            写入的文件名
	 * @param fis
	 *            输入流
	 * @param threadid
	 *            该下载线程
	 * @return
	 */
	public File writeDate(String dirname, String filename, InputStream fis,
			int threadid) {
		// 临时文件名.temp结尾
		File files = null;
		// 最终文件名
		File newfile = null;
		FileOutputStream fos = null;
		DownMp3State downmp3state = DownMp3State.getInstance();
		try {
			createDir(dirname);
			// 先创建以.temp结尾的临时文件
			files = createFile(dirname + "/" + filename + ".temp");
			fos = new FileOutputStream(files);
			byte[] buffer = new byte[1024];
			int data = 0;

			// 每次写数据判断该下载线程是否需要停止,false表示人为停止下载，需要正常退出
			HashMap<Integer, Integer> map = downmp3state.getMap();
			int down = map.get(threadid)==null?-2:map.get(threadid);
			while (down == 1 && (data = fis.read(buffer)) > 0) {
				down = map.get(threadid)==null?-2:map.get(threadid);
				fos.write(buffer, 0, data);
			}
			Log.i("FileUtil", filename + ",是否人为干预取消下载："
					+ downmp3state.getMap().get(threadid));
			if (down == 1) {
				// 最终文件名
				newfile = new File(SDPath + dirname + "/" + filename);
				// 将.temp文件改名为最终文件名
				files.renameTo(newfile);
			} else {
				// 人为取消，删除临时文件
				deleteFile(files);
			}
		} catch (Exception e) {
			// 出错就删除当前临时文件
			deleteFile(files);
			Log.d("FileUtil", "出错删除临时文件：" + files);
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return newfile;
	}

	/**
	 * 获得本地存在的mp3列表信息
	 * 
	 * @param filename
	 *            本地存放Mp3的目录名
	 * @return 返回mp3信息列表
	 */
	public List<Mp3Info> getFileList(String filename) {
		ArrayList<Mp3Info> list = new ArrayList<Mp3Info>();
		File file = new File(SDPath + filename);
		Log.i("FileUtil", "local dir is " + SDPath + filename);
		if (file.exists()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length && files.length != 0; i++) {
				String mp3name = files[i].getName();
				if (mp3name.endsWith(".mp3")) {
					Mp3Info mp3info = new Mp3Info();
					mp3info.setMp3name(mp3name);
					mp3info.setMp3size(new DecimalFormat("#.00").format(Float
							.parseFloat(files[i].length() + "") / 1024 / 1024)
							+ "MB");
					list.add(mp3info);
				}
			}
		}
		return list;
	}

	/**
	 * 获取mp3路径
	 * 
	 * @param name
	 *            mp3名字
	 * @param dirname
	 *            存放mp3目录名
	 * @return 返回mp3路径全地址
	 */
	public  String getMp3Path(String name, String dirname) {
		String mp3path = SDPath + dirname + "/" + name + ".mp3";
		return mp3path;
	}
	
	/**
	 * 获取歌词路径
	 * @param name mp3名字
	 * @param dirname 存放目录
	 * @return
	 */
	public  String getLrcPath(String name, String dirname){
		String lrcpath = SDPath + dirname + "/" + name + ".lrc";
		return lrcpath;
	}

}
