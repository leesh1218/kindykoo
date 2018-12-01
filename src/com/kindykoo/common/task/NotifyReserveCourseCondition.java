package com.kindykoo.common.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.kindykoo.common.model.CourseTable;
import com.kindykoo.common.model.ReserveCourse;
import com.kindykoo.common.model.Student;
import com.kindykoo.common.tool.ToolClass;
import com.kindykoo.controller.courseTable.CourseTableController;
import com.kindykoo.controller.logs.LogsService;
import com.kindykoo.controller.reserveCourse.ReserveCourseService;
import com.kindykoo.controller.student.StudentService;

public class NotifyReserveCourseCondition  implements Runnable{

	private static StudentService service = StudentService.me;
	private static ReserveCourseService reserveCourseService = ReserveCourseService.me;
	
	@Override
	public void run() {
		//约课情况
		List<CourseTable> courseTables = new CourseTableController().courseTableCondition();
		int error = 0;
		if(courseTables != null && courseTables.size() > 0){
			for (CourseTable courseTable : courseTables) {
				String info = "info: ";
				if(courseTable.getStuNumber() > courseTable.getMaxNumber()){
					error++;
					info = "error: ";
				}
				LogsService.insert(info+courseTable.toJson());
			}
			System.out.println("courseTableCondition: count="+courseTables.size()+" error="+error);
		}
		//固定约课情况
		courseTables = new CourseTableController().courseTableFixedCondition();
		error = 0;
		if(courseTables != null && courseTables.size() > 0){
			for (CourseTable courseTable : courseTables) {
				String info = "info/Fixed: ";
				if(courseTable.getFixedNum() > courseTable.getMaxFixedNum()){
					error++;
					info = "error/Fixed: ";
				}
				LogsService.insert(info+courseTable.toJson());
			}
			System.out.println("courseTableFixedCondition: count="+courseTables.size()+" error="+error);
		}
		
		String info = "";
		int weekCount = ToolClass.getWeekCount(new Date());
		String status = "已预约";
		List<ReserveCourse> reserveCourses = reserveCourseService.getStudentNameByWeekCount( status, weekCount);
		List<Student> students = new ArrayList<>();
		if(reserveCourses != null && reserveCourses.size() > 0){
			for (ReserveCourse reserveCourse : reserveCourses) {
				int i = 0;
				int weekReserveCount = reserveCourseService.getWeekReserveCountTest(weekCount,reserveCourse);
				Student student = new Student();
				student.setName(reserveCourse.getStudentName());
				student.setPhone(reserveCourse.getPhone());
				List<Student> studentList = service.selectMember(student);
				student = studentList.get(0);
				if(student.getWeekReserveCount() != weekReserveCount){
					System.out.println("weekReserveCount: student.getName()="+student.getName()+" student.getPhone()="+student.getPhone());
					student.setWeekReserveCount(weekReserveCount);
					i++;
				}
				if(!"课时卡".equals(student.getVipType()) && student.getWeekReserveCount() >= student.getWeekMaxCount() && student.getEnable()){
					System.out.println("enable: student.getName()="+student.getName()+" student.getPhone()="+student.getPhone());
					student.setEnable(false);
					i++;
				}
				if(i>0){
					students.add(student);
				}
			}
			Db.batchUpdate(students, students.size());
			info = "check weekReserveCount==== weekCount="+weekCount+" batch update "+students.size();
			LogsService.insert(info);
		}
		//冻结课时情况
		//List<ReserveCourse> tempreserveCourses = reserveCourseService.getStudentNameByWeekCount( status, weekCount);
		List<Student> tempstudents = new ArrayList<>();
		if(reserveCourses != null && reserveCourses.size() > 0){
			for (ReserveCourse reserveCourse : reserveCourses) {
				int i = 0;
				Student student = new Student();
				student.setName(reserveCourse.getStudentName());
				student.setPhone(reserveCourse.getPhone());
				List<Student> studentList = service.selectMember(student);
				student = studentList.get(0);
				if("主用户".equals(student.getMainUserFlag())) {
					List<Student> tmpstudentList = service.selectMemberTemp(student);
					if(tmpstudentList!=null && tmpstudentList.size()==1){
						int disableCourseCount = reserveCourseService.getDisableCourseCountTest(weekCount,reserveCourse);
						if(student.getDisableCourseCount() != disableCourseCount) {
							System.out.println("disableCourseCount: student.getName()="+student.getName()+" student.getPhone()="+student.getPhone());
							student.setDisableCourseCount(disableCourseCount);
							i++;
						}
					}
					if(tmpstudentList!=null && tmpstudentList.size()==2) {//有子卡
						reserveCourse.setStudentName(tmpstudentList.get(0).getName());
						int disableCourseCount1 = reserveCourseService.getDisableCourseCountTest(weekCount,reserveCourse);
						reserveCourse.setStudentName(tmpstudentList.get(1).getName());
						int disableCourseCount2 = reserveCourseService.getDisableCourseCountTest(weekCount,reserveCourse);
						if(student.getDisableCourseCount() != (disableCourseCount1+disableCourseCount2)) {
							System.out.println("disableCourseCount: student.getName()="+student.getName()+" student.getPhone()="+student.getPhone());
							student.setDisableCourseCount(disableCourseCount1+disableCourseCount2);
							i++;
						}
					}
				}
				if(i>0){
					tempstudents.add(student);
				}
			}
			Db.batchUpdate(tempstudents, tempstudents.size());
			info = "check disableCourseCount===== batch update "+tempstudents.size();
			LogsService.insert(info);
		}
	}

}