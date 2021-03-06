package com.kindykoo.common.task;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.kindykoo.common.model.Paras;
import com.kindykoo.common.model.ReserveCourse;
import com.kindykoo.common.model.Student;
import com.kindykoo.controller.logs.LogsService;
import com.kindykoo.controller.paras.ParasService;
import com.kindykoo.controller.reserveCourse.ReserveCourseService;
import com.kindykoo.controller.student.StudentService;

public class Test {
	private static StudentService service = StudentService.me;
	private static ReserveCourseService reserveCourseService = ReserveCourseService.me;
	private static ParasService parasService = ParasService.me;
	
	public void test(){
		/*List<Student> list = service.selectMemberTest();
		List<Student> list2 = new ArrayList<>();
		for (Student student : list) {
			ReserveCourse reserveCourse = new ReserveCourse();
			reserveCourse.setStudentName(student.getName());
			reserveCourse.setPhone(student.getPhone());
			List<ReserveCourse> reserveCourses = reserveCourseService.selectMember(reserveCourse, "test");
			if(reserveCourses != null && reserveCourses.size() > 0){
				if(student.getDisableCourseCount() != reserveCourses.size()){
					student.setDisableCourseCount(reserveCourses.size());
					list2.add(student);
				}
			}
		}
		Db.batchUpdate(list2, list2.size());
		System.out.println("总会员数："+list.size()+" 更新会员冻结课程  "+list2.size()+" 条记录");
		
		List<ReserveCourse> list = reserveCourseService.selectMemberTest2();
		List<ReserveCourse> list2 = new ArrayList<>();
		for (ReserveCourse reserveCourse : list) {
			Student student = new Student();
			student.setName(reserveCourse.getStudentName());
			student.setContact(reserveCourse.getOperator());
			List<Student> students = service.selectMemberTest(student);
			student = students.get(0);
			if(students.size()>1){
				System.out.println("++++++++"+students.get(0).getName());
			}
			
			List<ReserveCourse> list3 = reserveCourseService.selectMember(reserveCourse, "updatePhone");
			for (ReserveCourse reserveCourse2 : list3) {
				reserveCourse2.setPhone(student.getPhone());
				list2.add(reserveCourse2);
			}
		}
		Db.batchUpdate(list2, list2.size());
		System.out.println("总会员数："+list.size()+" 更新手机号  "+list2.size()+" 条记录");
		String info = "";
		Paras tmpparas = parasService.selectMember("currentWeekCount");
		int weekCount = Integer.parseInt(tmpparas.getValue());
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
			info = "weekCount="+weekCount+" batch update "+students.size();
			LogsService.insert(info);
		}*/
	}

}
