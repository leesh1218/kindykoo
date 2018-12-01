package com.kindykoo.common.model;

import java.util.List;

public class CourseTableList {
	
	private String id;
	private String courseTime;
	private boolean open;
	private List<CourseList> courses;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCourseTime() {
		return courseTime;
	}
	public void setCourseTime(String courseTime) {
		this.courseTime = courseTime;
	}
	public boolean isOpen() {
		return open;
	}
	public void setOpen(boolean open) {
		this.open = open;
	}
	public List<CourseList> getCourses() {
		return courses;
	}
	public void setCourses(List<CourseList> courses) {
		this.courses = courses;
	}

}
