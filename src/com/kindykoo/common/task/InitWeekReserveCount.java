package com.kindykoo.common.task;
/**
 * currentDate问题修改未提版
 */

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.plugin.activerecord.Db;
import com.kindykoo.common.model.Paras;
import com.kindykoo.common.model.ReserveCourse;
import com.kindykoo.common.model.Student;
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
		
		//获取参数
		//当前周数
		Paras paras0 = parasService.selectMember("currentWeekCount");
		int weekCount = Integer.parseInt(paras0.getValue());//该值不变
		//本阶段最大周数
		Paras paras = parasService.selectMember("maxReserveWeekCount");
		int maxWeekCount = Integer.parseInt(paras.getValue());//该值不变
//		int tempMaxWeekCount = maxWeekCount;//该值可被修改
		//本阶段最小周数
		Paras parasMin = parasService.selectMember("minReserveWeekCount");
		int minWeekCount = Integer.parseInt(parasMin.getValue());//该值不变
		//周末批量初始化执行标志
		Paras initparas = parasService.selectMember("initWeekFlag");
		int initWeekFlag = Integer.parseInt(initparas.getValue());//每周末定时任务执行标志，1-已执行，0-未执行
		
		if(initWeekFlag == 1) {
			info = "start and end run() weekCount="+weekCount+" initWeekFlag="+initWeekFlag+" minWeekCount="+minWeekCount+" maxWeekCount="+maxWeekCount;
			System.out.println(info);
			LogsService.insert(info);
			return;
		}else {
			info = "start run() weekCount="+weekCount+" initWeekFlag="+initWeekFlag+" minWeekCount="+minWeekCount+" maxWeekCount="+maxWeekCount;
			System.out.println(info);
			LogsService.insert(info);
		}
		
		//2019-07-01 修复查询出去年同样周数约课记录的问题
		//当前阶段第一天的日期
		Paras paraDate = parasService.selectMember("currentDate");
		String currentDateStr = paraDate.getValue();
	
//		String sql1 = "update student set enable=1 where weekReserveCount=weekMaxCount";
		String sql1 = "update student set enable=1,weekReserveCount=0";
		String sql2 = "update student set enable=0 where mainUserFlag='子用户' and mainUserName in (select * from (select name from student where mainUserFlag='主用户' and counts >= 2) temp)";
		String sql22 = "update student set enable=0 where mainUserFlag='主用户' and counts >= 2";
		String sql3 = "update student set counts=0 where mainUserFlag='主用户' and counts > 0";
		String sql33 = "update student set weekMaxCount=4 where weekMaxCount>4 and vipType!='课时卡'";
		String sql4 = "update student set enable=0 where mainUserFlag='子用户' and mainUserName in (select * from (select name from student where remainCourseCount<=disableCourseCount and mainUserFlag='主用户' and vipType='课时卡') temp)";
		String sql44 = "update student set enable=0 where remainCourseCount<=disableCourseCount and mainUserFlag='主用户' and vipType='课时卡'";
		String sql5 = "update student set enable=0 where mainUserFlag='子用户' and mainUserName in (select * from (select name from student where remainCourseCount<=0 and mainUserFlag='主用户' and vipType='课时卡') temp)";
		String sql55 = "update student set enable=0 where remainCourseCount<=0 and mainUserFlag='主用户' and vipType='课时卡'";
		String sql6 = "update student set enable=0 where mainUserFlag='子用户' and mainUserName in (select * from (select name from student where mainUserFlag='主用户' and endDate<date_sub(now(), interval 1 day)) temp)";
		String sql66 = "update student set enable=0 where mainUserFlag='主用户' and endDate<date_sub(now(), interval 1 day)";
		info = service.initWeekReserveCount(weekCount,sql1,sql2,sql22,sql3,sql33,sql4,sql44,sql5,sql55,sql6,sql66);
		LogsService.insert("InitWeekReserveCount "+info);
		
		//新的最大最小周数，仅在本阶段最后一周会更新这两个参数。
		int newMinReserveWeekCount = 0;
		int newMaxReserveWeekCount = 0;
		
		//判断本阶段是否结束
		if(maxWeekCount == weekCount){
			//年终标志 1-下一阶段是新年的第一个阶段，0-其他
			Paras parasYearEndFlag = parasService.selectMember("yearEndFlag");
			int yearEndFlag = Integer.parseInt(parasYearEndFlag.getValue());//该值不变
			//更新当前周数
			int tmpcount = parasService.updateCurrentWeekCount2(weekCount,yearEndFlag);
			if(tmpcount == 1){
				info = "weekCount="+weekCount+" currentWeekCount updated success";
				System.out.println(info);
				LogsService.insert(info);
			}else{
				info = "weekCount="+weekCount+" currentWeekCount updated fail";
				System.out.println(info);
				LogsService.insert(info);
			}
			
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
			
			//将本阶段（四周/一周）课程移至历史表
			reserveCourseService.moveReserveCourseHistory(maxWeekCount,currentDateStr,minWeekCount);
			//单周放课参数
			Paras parasType = parasService.selectMember("reserveCourseType");
			int reserveCourseTypePara = Integer.parseInt(parasType.getValue());
			//更新minReserveWeekCount，maxReserveWeekCount，并且将新的值返回。
			Map<String,String> parasWeekCount = new HashMap<String, String>();
			int count = parasService.updateWeekCount(yearEndFlag,reserveCourseTypePara,maxWeekCount,minWeekCount,parasWeekCount);
			
			if(count == 2){
				info = "weekCount="+weekCount+" reserveCourseTypePara="+reserveCourseTypePara+" minReserveWeekCount and maxReserveWeekCount updated success";
				System.out.println(info);
				LogsService.insert(info);
				
				//新的最小和最大周数的值
				newMinReserveWeekCount = Integer.parseInt(parasWeekCount.get("minReserveWeekCount"));
				newMaxReserveWeekCount = Integer.parseInt(parasWeekCount.get("maxReserveWeekCount"));
				info = "weekCount="+weekCount+" newMinReserveWeekCount="+newMinReserveWeekCount+" newMaxReserveWeekCount=" + newMaxReserveWeekCount;
				System.out.println(info);
				LogsService.insert(info);
				
				if(reserveCourseTypePara == 1) {
//					tempMaxWeekCount = maxWeekCount+1;
					//禁止固定课程
					String tempsql = "update courseTable set allowFixed=0 where allowFixed=1 and weekCount >= "+newMinReserveWeekCount+" and weekCount <= "+newMaxReserveWeekCount;
					info = courseTableService.initWeekReserveCount(weekCount,tempsql);
					LogsService.insert("InitWeekReserveCount reserveCourseTypePara=1 "+info);
				}else {
//					tempMaxWeekCount = maxWeekCount+4;
				}
				
			}else{
				info = "weekCount="+weekCount+" reserveCourseTypePara="+reserveCourseTypePara+" minReserveWeekCount and maxReserveWeekCount updated fail";
				System.out.println(info);
				LogsService.insert(info);
//				return;
			}
			
			//禁止所有课程的预约
			String sql = "update courseTable set enable=0 where enable=1 and weekCount >= "+newMinReserveWeekCount+" and weekCount <= "+newMaxReserveWeekCount;
			info = courseTableService.initWeekReserveCount(weekCount,sql);
			LogsService.insert("InitWeekReserveCount update courseTable set enable=0 "+info);
			
		}else{
			
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
			}
			
			//禁止所有课程的预约
			String sql = "update courseTable set enable=0 where enable=1 and weekCount > "+weekCount+" and weekCount <= "+maxWeekCount;
			info = courseTableService.initWeekReserveCount(weekCount,sql);
			LogsService.insert("InitWeekReserveCount update courseTable set enable=0 "+info);
			
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
			
			newMinReserveWeekCount = minWeekCount;
			newMaxReserveWeekCount = maxWeekCount;
		}
		
		//更新会员和用户年龄
		new StudentController().studentAgeUpdateCore();
		
		//更新initWeekFlag为1-已执行
		int tmpInitWeekFlagCount = parasService.updateInitWeekFlag(1);
		if(tmpInitWeekFlagCount == 1) {
			info = "InitWeekReserveCount weekCount="+weekCount+" update initWeekFlag success and initWeekFlag=1";
		}else {
			info = "InitWeekReserveCount weekCount="+weekCount+" update initWeekFlag fail and initWeekFlag=0";
		}
		System.out.println(info);
		LogsService.insert(info);
		
		info = "end run() weekCount="+weekCount+" initWeekFlag="+initWeekFlag+" minWeekCount="+newMinReserveWeekCount+" maxWeekCount="+newMaxReserveWeekCount;
		System.out.println(info);
		LogsService.insert(info);
	}
}
