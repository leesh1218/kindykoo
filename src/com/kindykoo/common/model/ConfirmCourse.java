package com.kindykoo.common.model;

import java.util.List;

public class ConfirmCourse {
	private String id;
	private String courseTime;
	private String course;
	private String teacher1;
	private String teacher2;
	private boolean open;
	private String status;
	private int stuNumber;
	private int presentNum;
	
	
	private List<StudentStatus> students;
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
	public String getCourse() {
		return course;
	}
	public void setCourse(String course) {
		this.course = course;
	}
	public String getTeacher2() {
		return teacher2;
	}
	public void setTeacher2(String teacher2) {
		this.teacher2 = teacher2;
	}
	public boolean isOpen() {
		return open;
	}
	public void setOpen(boolean open) {
		this.open = open;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public List<StudentStatus> getStudents() {
		return students;
	}
	public void setStudents(List<StudentStatus> students) {
		this.students = students;
	}
	public int getStuNumber() {
		return stuNumber;
	}
	public void setStuNumber(int stuNumber) {
		this.stuNumber = stuNumber;
	}
	public int getPresentNum() {
		return presentNum;
	}
	public void setPresentNum(int presentNum) {
		this.presentNum = presentNum;
	}
	public String getTeacher1() {
		return teacher1;
	}
	public void setTeacher1(String teacher1) {
		this.teacher1 = teacher1;
	}
	

}
