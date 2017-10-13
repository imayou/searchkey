package org.ayou.search.domain;

import java.io.Serializable;
import java.util.List;

public class DownFileEntity implements Serializable{
	private static final long serialVersionUID = -3534862761131418925L;
	private String key; //关键字
	private String path;//路径
	private String name;//名称
	private String time;//时间
	private String type;//类型
	private List<String> list;//下载列表
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<String> getList() {
		return list;
	}
	public void setList(List<String> list) {
		this.list = list;
	}
}
