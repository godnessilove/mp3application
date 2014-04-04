package com.example.xml;

import java.text.DecimalFormat;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.example.xmlmodel.Mp3Info;

public class XmlConnentHanndle extends DefaultHandler{
	private Mp3Info infos = null;
	private List<Mp3Info> info = null;
	
	public List<Mp3Info> getInfo() {
		return info;
	}


	public XmlConnentHanndle(List<Mp3Info> info) {
		super();
		this.info = info;
	}


	public void setInfo(List<Mp3Info> info) {
		this.info = info;
	}

	private String sagname = null;
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String temp = new String(ch,start,length);
		if(sagname.equals("id")){
			infos.setId(temp);
		}else if(sagname.equals("mp3name")){
			infos.setMp3name(temp);
		}else if(sagname.equals("mp3size")){
			infos.setMp3size(String.valueOf(new DecimalFormat("#.00").format(Float.parseFloat(temp)/1024/1024))+"MB");
		}else if(sagname.equals("lrcname")){
			infos.setLrcname(temp);
		}else if(sagname.equals("lrcsize")){
			infos.setLrcsize(temp);
		}
	}
	
	
	@Override
	public void endDocument() throws SAXException {
	
		System.out.println("成功结束读取文档");
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(qName.equals("resource")){
			info.add(infos);
		}
		sagname = "";
	}

	@Override
	public void startDocument() throws SAXException {
		System.out.println("成功开始读取文档");
		super.startDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		sagname = localName;
		if(sagname.equals("resource")){
			infos = new Mp3Info();
		}
	}
	
}
