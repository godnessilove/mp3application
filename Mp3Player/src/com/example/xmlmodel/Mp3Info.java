package com.example.xmlmodel;

import java.io.Serializable;

public class Mp3Info implements  Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private String mp3name;
	private String mp3size;
	private String lrcname;
	private String lrcsize;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getMp3name() {
		return mp3name;
	}
	public void setMp3name(String mp3name) {
		this.mp3name = mp3name;
	}
	public String getMp3size() {
		return mp3size;
	}
	public void setMp3size(String mp3size) {
		this.mp3size = mp3size;
	}
	public String getLrcname() {
		return lrcname;
	}
	public void setLrcname(String lrcname) {
		this.lrcname = lrcname;
	}
	public String getLrcsize() {
		return lrcsize;
	}
	public void setLrcsize(String lrcsize) {
		this.lrcsize = lrcsize;
	}
	@Override
	public String toString() {
		return "Mp3Info [id=" + id + ", mp3name=" + mp3name + ", mp3size="
				+ mp3size + ", lrcname=" + lrcname + ", lrcsize=" + lrcsize
				+ "]";
	}
	
	public Mp3Info(String id, String mp3name, String mp3size, String lrcname,
			String lrcsize) {
		super();
		this.id = id;
		this.mp3name = mp3name;
		this.mp3size = mp3size;
		this.lrcname = lrcname;
		this.lrcsize = lrcsize;
	}
	
	public Mp3Info() {
		super();
	}
	
	
}
