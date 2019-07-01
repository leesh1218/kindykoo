package com.kindykoo.common.task;
/**
 * currentDate问题修改未提版
 */

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.kindykoo.common.model.Paras;
import com.kindykoo.common.model.ReserveCourse;
import com.kindykoo.common.model.Student;
import com.kindykoo.common.tool.ToolClass;
import com.kindykoo.controller.courseTable.CourseTableService;
import com.kindykoo.controller.logs.LogsService;
import com.kindykoo.controller.paras.ParasService;
import com.kindykoo.controller.reserveCourse.ReserveCourseService;
import com.kindykoo.controller.student.StudentController;
import com.kindykoo.controller.student.StudentService;

/**
 * 1.每周定时更新周预约课程数
 * 2.每四周更新参数表
 * @author leeshua
 *
 */
public class InitWeekReserveCount implements Runnable {

	private static StudentService service = StudentService.me;
	private static ReserveCourseService reserveCourseService = ReserveCourseService.me;
	private static CourseTableService courseTableService = CourseTableService.me;
	private static ParasService parasService = ParasService.me;
	
	@Override
	public void run() {
		String info = "";
		int weekCount = ToolClass.getWeekCount(new Date());
		Paras paras = parasService.selectMember("maxReserveWeekCount");
		int maxWeekCount = Integer.parseInt(paras.getValue());
		
		//2019-07-01 修复查询出去年同样周数约课记录的问题
		Paras paraDate = parasService.selectMember("currentDate");
		String currentDateStr = paraDate.getValue();
	
//		String sql1 = "update student set enable=1 where weekReserveCount=weekMaxCount";
		String sql1 = "update student set enable=1,weekReserveCount=0";
		String sql2 = "update student set enable=0 where mainUserFlag='子用户' and mainUserName in (select * from (select name from student where mainUserFlag='主用户' and counts >= 2) temp)";
		String sql22 = "update student set enable=0 where mainUserFlag='主用户' and counts >= 2";
		String sql3 = "update student set counts=0 where mainUserFlag='主用户' and counts > 0";
		String sql33 = "update student set weekMaxCount=4 where weekMaxCount!=4 and vipType!='课时卡'";
		String sql4 = "update student set enable=0 where mainUserFlag='子用户' and mainUserName in (select * from (select name from student where remainCourseCount<=disableCourseCount and mainUserFlag='主用户' and vipType='课时卡') temp)";
		String sql44 = "update student set enable=0 where remainCourseCount<=disableCourseCount and mainUserFlag='主用户' and vipType='课时卡'";
		String sql5 = "update student set enable=0 where mainUserFlag='子用户' and mainUserName in (select * from (select name from student where remainCourseCount<=0 and mainUserFlag='主用户' and vipType='课时卡') temp)";
		String sql55 = "update student set enable=0 where remainCourseCount<=0 and mainUserFlag='主用户' and vipType='课时卡'";
		String sql6 = "update student set enable=0 where mainUserFlag='子用户' and mainUserName in (select * from (select name from student where mainUserFlag='主用户' and endDate<date_sub(now(), interval 1 day)) temp)";
		String sql66 = "update student set enable=0 where mainUserFlag='主用户' and endDate<date_sub(now(), interval 1 day)";
		info = service.initWeekReserveCount(weekCount,sql1,sql2,sql22,sql3,sql33,sql4,sql44,sql5,sql55,sql6,sql66);
		LogsService.insert("InitWeekReserveCount "+info);
		
		//更新当前周数
		int tmpcount = parasService.updateCurrentWeekCount(weekCount);
		if(tmpcount == 1){
			info = "weekCount="+weekCount+" currentWeekCount updated success";
			System.out.println(info);
			LogsService.insert(info);
		}else{
			info = "weekCount="+weekCount+" currentWeekCount updated fail";
			System.out.println(info);
			LogsService.insert(info);
//			return;
		}
		
		//判断本阶段四周是否结束
		if(maxWeekCount == weekCount){
			//更新本阶段第一周第一天的日期
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar calendar = Calendar.getInstance();
			calendar.setFirstDayOfWeek(Calendar.MONDAY);  
			calendar.add(Calendar.DATE, 1);
			int tmpcountDate = parasService.updateCurrentDate(sdf.format(calendar.getTime()));
			if(tmpcountDate == 1){
				info = "weekCount="+weekCount+" currentDate updated success";
				System.out.println(info);
				LogsService.insert(info);
			}else{
				info = "weekCount="+weekCount+" currentDate updated fail";
				System.out.println(info);
				LogsService.insert(info);
//				return;
			}
			
			//删除courseTable里面下下个阶段weekCount 即将重复的数据（例如当前周22，下下一阶段为27-30周，courseTable如果有该周数的数据，删除）
			int RepeatData = courseTableService.deleteRepeatData(maxWeekCount);
			if(RepeatData > 0) {
				info = "weekCount="+weekCount+" courseTable deleteRepeatData success and RepeatData="+RepeatData;
				System.out.println(info);
				LogsService.insert(info);
			}
			
			//讲本阶段四周课程移至历史表
			reserveCourseService.moveReserveCourseHistory(maxWeekCount,currentDateStr);
			int count = parasService.updateWeekCount();
			if(count == 2){
				info = "weekCount="+weekCount+" minReserveWeekCount and maxReserveWeekCount updated success";
				maxWeekCount = maxWeekCount+4;
				System.out.println(info);
				LogsService.insert(info);
			}else{
				info = "weekCount="+weekCount+" minReserveWeekCount and maxReserveWeekCount updated fail";
				System.out.println(info);
				LogsService.insert(info);
//				return;
			}
		}else{
//			weekCount = 26;
			String status = "已预约";
			List<ReserveCourse> reserveCourses = reserveCourseService.getStudentNameByWeekCount( status, weekCount+1);
			List<Student> students = new ArrayList<>();
			if(reserveCourses != null && reserveCourses.size() > 0){
				for (ReserveCourse reserveCourse : reserveCourses) {
					int weekReserveCount = reserveCourseService.getWeekReserveCount(weekCount+1,reserveCourse);
					Student student = new Student();
					student.setName(reserveCourse.getStudentName());
					student.setPhone(reserveCourse.getPhone());
					List<Student> studentList = service.selectMember(student);
					student = studentList.get(0);
					student.setWeekReserveCount(weekReserveCount);
					if(!"课时卡".equals(student.getVipType()) && student.getWeekReserveCount() >= student.getWeekMaxCount()){
						student.setEnable(false);
					}
					students.add(student);
				}
				Db.batchUpdate(students, students.size());
				info = "weekCount="+weekCount+" batch update "+students.size();
				LogsService.insert(info);
			}
		}
		
		//禁止所有课程的预约
		String sql = "update courseTable set enable=0 where enable=1 and weekCount > "+weekCount+" and weekCount <= "+maxWeekCount;
		info = courseTableService.initWeekReserveCount(weekCount,sql);
		LogsService.insert("InitWeekReserveCount "+info);
		
		//更新会员和用户年龄
		new StudentController().studentAgeUpdateCore();
	}
}
