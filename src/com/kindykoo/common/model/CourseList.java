package com.kindykoo.common.model;

public class CourseList {

	private int id;//课表id
	private String para;//课程
	private String time;
	private String fixedFlag;
	private int remainNumber;//剩余可预约人数
	private String url;
	
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getRemainNumber() {
		return remainNumber;
	}
	public void setRemainNumber(int remainNumber) {
		this.remainNumber = remainNumber;
	}
	public String getPara() {
		return para;
	}
	public void setPara(String para) {
		this.para = para;
	}
	public String getFixedFlag() {
		return fixedFlag;
	}
	public void setFixedFlag(String fixedFlag) {
		this.fixedFlag = fixedFlag;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	
}
