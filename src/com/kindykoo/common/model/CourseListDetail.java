package com.kindykoo.common.model;


public class CourseListDetail {

	private int id;//课表id
	private String weekCount;//周数
	private String week;//星期
	private String date;
	private String courseTime;//上课时间
	private String courseName;//课程
	private String teacherID1;//主教
	private String teacherID2;//助教
	private String classroom;//教室
	private int stuNumber;//已预约人数
	private int maxNumber;//最大人数
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getWeekCount() {
		return weekCount;
	}
	public void setWeekCount(String weekCount) {
		this.weekCount = weekCount;
	}
	public String getWeek() {
		return week;
	}
	public void setWeek(String week) {
		this.week = week;
	}
	public String getCourseTime() {
		return courseTime;
	}
	public void setCourseTime(String courseTime) {
		this.courseTime = courseTime;
	}
	public String getCourseName() {
		return courseName;
	}
	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}
	public String getTeacherID1() {
		return teacherID1;
	}
	public void setTeacherID1(String teacherID1) {
		this.teacherID1 = teacherID1;
	}
	public String getTeacherID2() {
		return teacherID2;
	}
	public void setTeacherID2(String teacherID2) {
		this.teacherID2 = teacherID2;
	}
	public String getClassroom() {
		return classroom;
	}
	public void setClassroom(String classroom) {
		this.classroom = classroom;
	}
	public int getStuNumber() {
		return stuNumber;
	}
	public void setStuNumber(int stuNumber) {
		this.stuNumber = stuNumber;
	}
	public int getMaxNumber() {
		return maxNumber;
	}
	public void setMaxNumber(int maxNumber) {
		this.maxNumber = maxNumber;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	
	
}
