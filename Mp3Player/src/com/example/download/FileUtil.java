package com.example.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.example.xmlmodel.Mp3Info;

import android.os.Environment;
import android.os.StatFs;

public class FileUtil {
	private String SDPath = null;

	public FileUtil() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			SDPath = Environment.getExternalStorageDirectory() + "/";
		} else
			System.out.println("û�д洢��");
	}

	public String getSDPath() {
		return SDPath;
	}

	public long getSDFeeSpace() {
		StatFs sf = new StatFs(SDPath);
		long feespace = sf.getFreeBlocks() * sf.getBlockSize();
		return feespace;
	}

	public long getSDAvailableSpace() {
		StatFs sf = new StatFs(SDPath);
		long availablespace = sf.getAvailableBlocks() * sf.getBlockSize();
		return availablespace;
	}

	public long getSDBlockSpace() {
		StatFs sf = new StatFs(SDPath);
		long blockspace = sf.getBlockCount() * sf.getBlockSize();
		return blockspace;
	}

	public File createDir(String dirname) {
		File dir = new File(SDPath + dirname);
		// System.out.println("dir is " + SDPath + dirname);
		if (!dir.exists() && !dir.isDirectory()) {
			dir.mkdir();
		}
		return dir;
	}

	public File createFile(String filename) throws IOException {
		// System.out.println("file is " + SDPath + filename);
		File files = new File(SDPath + filename);
		files.createNewFile();
		return files;
	}

	public Boolean isFileExist(String filename) {
		File files = new File(SDPath + filename);
		// System.out.println("isFileExist " + SDPath + filename);
		// System.out.println(files.exists());
		return files.exists();
	}
	
	public Boolean isfullFileExist(String filename) {
		File files = new File(filename);
		// System.out.println("isFileExist " + SDPath + filename);
		// System.out.println(files.exists());
		return files.exists();
	}

	public File writeDate(String dirname, String filename, InputStream fis)
			throws IOException {
		File files = null;
		FileOutputStream fos = null;
		try {
			createDir(dirname);
			System.out.println("writepathdir is " + dirname + filename
					+ ".temp");
			files = createFile(dirname + "/" + filename + ".temp");// �ȴ�����.temp��β����ʱ�ļ�
			fos = new FileOutputStream(files);
			byte[] buffer = new byte[1024];
			int data = 0;
			while ((data = fis.read(buffer)) > 0) {
				fos.write(buffer, 0, data);
			}
			File newfile = new File(SDPath + dirname + "/" + filename);// �����ļ���
			files.renameTo(newfile);// ��.temp�ļ�����Ϊ�����ļ���
			System.out.println("�ɹ�����" + SDPath + dirname + "/" + filename);
		} catch (IOException e) {
			// ������ɾ����ǰ��ʱ�ļ�
			files.delete();
			System.out.println("����ɾ����ʱ�ļ���" + files);
			e.printStackTrace();
		} finally {
			fos.close();
			fis.close();
		}
		return files;
	}

	public List<Mp3Info> getFileList(String filename) {
		ArrayList<Mp3Info> list = new ArrayList<Mp3Info>();
		File file = new File(SDPath + filename);
		System.out.println("local dir is " + SDPath + filename);
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
	
	public String getMp3Path(String name,String dirname){
		String mp3path = SDPath + dirname + "/" + name;
		return mp3path;
	}

}