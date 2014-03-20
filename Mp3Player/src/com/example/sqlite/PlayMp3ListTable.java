package com.example.sqlite;

public class PlayMp3ListTable {

	// public static final String TABLENAME = "local_mp3_list_all";
	// 每张mp3播放列表的列都一样
	private static final String MP3NAME = "mp3name";
	private static final String MP3SIZE = "mp3size";
	private static final String MP3PATH = "mp3path";
	private static final String LRCPATH = "lrcpath";
	private static final String STATE = "state";
	
	public static String getLrcpath() {
		return LRCPATH;
	}
	public static String getMp3name() {
		return MP3NAME;
	}

	public static String getMp3size() {
		return MP3SIZE;
	}

	public static String getMp3path() {
		return MP3PATH;
	}

	public static String getState() {
		return STATE;
	}

	public String[] getColumArray() {
		return new String[] { MP3NAME, MP3SIZE, MP3PATH,LRCPATH ,STATE };
	}

}
