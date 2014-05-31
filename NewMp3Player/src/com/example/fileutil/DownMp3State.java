package com.example.fileutil;

import android.annotation.SuppressLint;

import java.util.HashMap;

@SuppressLint("UseSparseArrays")
public class DownMp3State {
	private HashMap<Integer, Integer> map;
	private static DownMp3State Instance = null;

	private DownMp3State() {
		super();
		if (map == null) {
			this.map = new HashMap<Integer, Integer>();
		}
	}

	public static DownMp3State getInstance() {
		if (Instance == null) {
			Instance = new DownMp3State();
		}
		return Instance;
	}

	public HashMap<Integer, Integer> getMap() {
		return map;
	}

	public void setMap(HashMap<Integer, Integer> map) {
		this.map = map;
	}

	@Override
	public String toString() {
		return "DownMp3State [map=" + map + "]";
	}
	

}
