package com.kindykoo.controller.reserveCourse;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.kit.Ret;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kindykoo.common.model.CourseTable;
import com.kindykoo.common.model.Paras;
import com.kindykoo.common.model.ReserveCourse;
import com.kindykoo.common.model.Student;
import com.kindykoo.common.tool.ToolClass;
import com.kindykoo.controller.courseTable.CourseTableService;
import com.kindykoo.controller.paras.ParasService;
import com.kindykoo.controller.reserveCourse.ReserveCourseService;
import com.kindykoo.controller.student.StudentService;

public class ReserveCourseService {

	public static final ReserveCourseService me = new ReserveCourseService();
	private static final StudentService stuService = StudentService.me;
	private static final CourseTableService courseTableService = CourseTableService.me;
	private static final ReserveCourse dao = new ReserveCourse().dao();
	private static ParasService parasService = ParasService.me;
	private static String []  reserveType = {"非固定课","固定课"};
	
	/**
	 * 分页查询
	 * @param pageNumber
	 * @param pageSize
	 * @param keyword	关键词查询
	 * @param enable	是否启用
	 * @return
	 */
	public Page<ReserveCourse> paginate(int pageNumber, int pageSize, String keyword, String courseTime,String course, String weekCount, String week, String status,String minReserveWeekCount,String maxReserveWeekCount){
		String table = "reserveCourse";
		
		//2019-07-01 修复查询出去年同样周数约课记录的问题
		Paras paraDate = parasService.selectMember("currentDate");
		String currentDateStr = paraDate.getValue();
		
		StringBuilder where = new StringBuilder();
		if(StrKit.notBlank(status)){
			where.append(" status = '").append(status).append("'");
		}
		if(StrKit.notBlank(weekCount)){
//			if(Integer.parseInt(weekCount) < Integer.parseInt(minReserveWeekCount)) {
//				table = "reserveCourseHistory";
//			}
			if(where.length() > 0){
				where.append(" and ");
			}
			where.append(" weekCount = ").append(weekCount);
		}
		if(StrKit.notBlank(week)){
			if(where.length() > 0){
				where.append(" and ");
			}
			where.append(" week = '").append(week).append("'");
		}
		if(StrKit.notBlank(courseTime)){
			if(where.length() > 0){
				where.append(" and ");
			}
			where.append(" courseTime = '").append(courseTime).append("'");
		}
		if(StrKit.notBlank(course)){
			if(where.length() > 0){
				where.append(" and ");
			}
			where.append(" course = '").append(course).append("'");
		}
		if(StrKit.notBlank(keyword)){
			if(where.length() > 0){
				where.append(" and ");
			}
			where.append("(")
			.append(" studentName like '%").append(keyword).append("%' or ")
			.append(" teacher1 like '%").append(keyword).append("%' ")
			.append(")");
		}
		if(where.length()>0){
			where.insert(0, " from "+table+" where ");
			where.append(" order by courseTime,course asc");
		}else{
			where.append("from "+table+" where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and weekCount<= ").append(maxReserveWeekCount).append(" and weekCount>= ").append(minReserveWeekCount).append(" order by id desc");
		}
		System.out.println(where.toString());
		return dao.paginate(pageNumber, pageSize, "select * ", where.toString());
	}
	
	/**
	 * 查询课程约课详情单独出来
	 * 2019-07-14
	 * @param pageNumber
	 * @param pageSize
	 * @param courseTableId
	 * @param courseTime
	 * @param course
	 * @param weekCount
	 * @param week
	 * @param status
	 * @param minReserveWeekCount
	 * @param maxReserveWeekCount
	 * @return
	 */
	public Page<ReserveCourse> paginate2(int pageNumber, int pageSize, String courseTableId, String courseTime,String course, String weekCount, String week, String status,String minReserveWeekCount,String maxReserveWeekCount){
		String table = "reserveCourse";
		
		//2019-07-01 修复查询出去年同样周数约课记录的问题
		Paras paraDate = parasService.selectMember("currentDate");
		String currentDateStr = paraDate.getValue();
		
		StringBuilder where = new StringBuilder();
		if(StrKit.notBlank(status)){
			where.append(" status = '").append(status).append("'");
		}
		if(StrKit.notBlank(weekCount)){
//			if(Integer.parseInt(weekCount) < Integer.parseInt(minReserveWeekCount)) {
//				table = "reserveCourseHistory";
//			}
			if(where.length() > 0){
				where.append(" and ");
			}
			where.append(" weekCount = ").append(weekCount);
		}
		if(StrKit.notBlank(week)){
			if(where.length() > 0){
				where.append(" and ");
			}
			where.append(" week = '").append(week).append("'");
		}
		
		if(StrKit.notBlank(courseTableId)){
			if(where.length() > 0){
				where.append(" and ");
			}
			where.append(" courseTableID = '").append(courseTableId).append("'");
		}
		
		if(StrKit.notBlank(courseTime)){
			if(where.length() > 0){
				where.append(" and ");
			}
			where.append(" courseTime = '").append(courseTime).append("'");
		}
		if(StrKit.notBlank(course)){
			if(where.length() > 0){
				where.append(" and ");
			}
			where.append(" course = '").append(course).append("'");
		}
		if(where.length()>0){
			where.insert(0, " from "+table+" where ");
			where.append(" order by courseTime,course asc");
		}else{
			where.append("from "+table+" where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and weekCount<= ").append(maxReserveWeekCount).append(" and weekCount>= ").append(minReserveWeekCount).append(" order by id desc");
		}
		System.out.println(where.toString());
		return dao.paginate(pageNumber, pageSize, "select * ", where.toString());
	}
	
	/**
	 * 查询预约课程详情
	 * @param id
	 * @return
	 */
	public ReserveCourse getReserveCourse(String id) {
		return dao.findById(id);
	}

	/**
	 * 切换enable状态
	 * @param id
	 * @return
	 */
	public Ret doToggleEnable(Integer id) {
		int count = Db.update("update reserveCourse set enable = (!enable), enableTime = NOW() where id = ?", id);
		if(count==1){
			return Ret.ok();
		}else{
			return Ret.fail();
		}
	}

	public boolean addMember(ReserveCourse reserveCourse) {
		return reserveCourse.save();
	}

	public List<ReserveCourse> selectMember(ReserveCourse reserveCourse,String flag) {
		//2019-07-01 修复查询出去年同样周数约课记录的问题
		Paras paraDate = parasService.selectMember("currentDate");
		String currentDateStr = paraDate.getValue();
		String sql = "";
		if("qryCourse".equals(flag)){
			if(reserveCourse.getStatus()!=null && !"".equals(reserveCourse.getStatus())){
				if(reserveCourse.getTeacher2() != null && !"".equals(reserveCourse.getTeacher2())){//老师查看请假学生
					sql = "select * from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and status='"+reserveCourse.getStatus()+"' and (teacher1='"+reserveCourse.getTeacher1()+"' or teacher2 ='"+reserveCourse.getTeacher2()+"') and weekCount="+reserveCourse.getWeekCount()+" order by date, studentName asc";
				}else if("已预约".equals(reserveCourse.getStatus())){//家长查看学生已预约课程 date>now()
					sql = "select * from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and phone='"+reserveCourse.getPhone()+"' and date>now() and status='"+reserveCourse.getStatus()+"' and studentName='"+reserveCourse.getStudentName()+"' and weekCount="+reserveCourse.getWeekCount()+" order by date, studentName asc";
				}else if("已请假".equals(reserveCourse.getStatus())){
					sql = "select * from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and phone='"+reserveCourse.getPhone()+"' and status='"+reserveCourse.getStatus()+"' and studentName='"+reserveCourse.getStudentName()+"' and weekCount="+reserveCourse.getWeekCount()+" order by date, studentName asc";
				}
			}else{//家长查看所有预约课程
				sql = "select * from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and phone='"+reserveCourse.getPhone()+"' and studentName='"+reserveCourse.getStudentName()+"' and weekCount="+reserveCourse.getWeekCount()+" and status != '已请假'"+" order by date asc";
			}
		}else if("reserveOrFixedCourse".equals(flag)){
			sql = "select * from reserveCourse where phone='"+reserveCourse.getPhone()+"' and status in ('已预约','上课中') and studentName='"+reserveCourse.getStudentName()+"' and courseTableID="+reserveCourse.getCourseTableID();
		}else if("repeatTimeReserve".equals(flag)){
			sql = "select * from reserveCourse where phone='"+reserveCourse.getPhone()+"' and status in ('已预约','上课中') and studentName='"+reserveCourse.getStudentName()+"' and date='"+reserveCourse.getDate()+"'";
		}else if("confirmCourse".equals(flag)){
			sql = "select * from reserveCourse where phone='"+reserveCourse.getPhone()+"' and status = '未确认' and studentName='"+reserveCourse.getStudentName()+"' and courseTableID="+reserveCourse.getCourseTableID();
		}else if("myConfirmCourses".equals(flag)){
			sql = "select * from reserveCourse where status = '"+reserveCourse.getStatus()+"' and courseTableID="+reserveCourse.getCourseTableID()+" order by date, studentName asc";
		}else if("courseDetail".equals(flag)){
			sql = "select * from reserveCourse where status in ('已预约','上课中') and courseTableID="+reserveCourse.getCourseTableID()+" order by date, studentName asc";
		}else if("repeatReserve".equals(flag)){
			sql = "select * from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and phone='"+reserveCourse.getPhone()+"' and status != '已请假' and studentName='"+reserveCourse.getStudentName()+"' and course='"+reserveCourse.getCourse()+"' and weekCount = "+reserveCourse.getWeekCount();
		}else if ("test".equals(flag)){
			sql = "select * from reserveCourse where phone='"+reserveCourse.getPhone()+"' and studentName='"+reserveCourse.getStudentName()+"' and status not in ('已请假','已确认')";
		}else if ("updatePhone".equals(flag)){
			sql = "select * from reserveCourse where studentName='"+reserveCourse.getStudentName()+"'";
		}else if ("reserveCourseCore1".equals(flag)){
			sql = "select * from reserveCourse where phone='"+reserveCourse.getPhone()+"' and status = '已预约' and studentName='"+reserveCourse.getStudentName()+"' and week='"+reserveCourse.getWeek()+"' and courseTime='"+reserveCourse.getCourseTime()+"' and course='"+reserveCourse.getCourse()+"'";
		}else if ("reserveCourseCore2".equals(flag)){
			sql = "select * from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and phone='"+reserveCourse.getPhone()+"' and status = '已预约' and studentName='"+reserveCourse.getStudentName()+"' and weekCount="+reserveCourse.getWeekCount();
		}else if("doSubmit".equals(flag)) {
			sql = "select * from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and weekCount = "+reserveCourse.getWeekCount()+" and week='"+reserveCourse.getWeek()+"' and courseTime='"+reserveCourse.getCourseTime()+"' and course='"+reserveCourse.getCourse()+"'";
		}else if("doSubmit_phone".equals(flag)) {
			sql = "select * from reserveCourse where status not in ('已请假','已确认') and studentName='"+reserveCourse.getStudentName()+"' and phone='"+reserveCourse.getPhone()+"'";
		}else if("reserveCourseFixedCondition".equals(flag)) {
			Paras paras = parasService.selectMember("maxReserveWeekCount");
			int maxReserveWeekCount = Integer.parseInt(paras.getValue());
			sql = "select * from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and studentName='"+reserveCourse.getStudentName()+"' and phone='"+reserveCourse.getPhone()+"' and week='"+reserveCourse.getWeek()+"' and courseTime='"+reserveCourse.getCourseTime()+"' and course='"+reserveCourse.getCourse()+"' and operator='"+reserveCourse.getOperator()+"' and reserveType='"+reserveCourse.getReserveType()+"' and weekCount>="+reserveCourse.getWeekCount()+" and weekCount<="+maxReserveWeekCount;
		}else if("enableCourseCore".equals(flag)) {
			sql = "select * from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and status in ('已预约','上课中') and week='"+reserveCourse.getWeek()+"' and courseTime='"+reserveCourse.getCourseTime()+"' and course='"+reserveCourse.getCourse()+"' and weekCount="+reserveCourse.getWeekCount()+" and teacher1='"+reserveCourse.getTeacher1()+"'";
		}
		List<ReserveCourse> reserveCourses = dao.find(sql);
		if(reserveCourses == null || reserveCourses.size() == 0)
			return null;
		else
			return reserveCourses;
	}

	public boolean update(ReserveCourse reserveCourse) {
		return reserveCourse.update();
	}

	/**
	 * 更新预约课程的课程类别
	 * @param reserveCourse
	 * @return
	 */
//	public Ret updateReserveType(ReserveCourse reserveCourse) {
//		int count = Db.update("update reserveCourse set reserveType = ? where studentName= ? and courseTableID = ?", reserveCourse.getReserveType(),reserveCourse.getStudentName(),reserveCourse.getCourseTableID());
//		if(count==1){
//			return Ret.ok();
//		}else{
//			return Ret.fail();
//		}
//	}
	
	@Before(Tx.class)
	public boolean transDisableCourse(ReserveCourse reserveCourse, CourseTable courseTable, Student student,Student subStudent){
		if(me.update(reserveCourse) && courseTableService.update(courseTable) && stuService.update(student)){
			if(subStudent!=null){
				stuService.update(subStudent);
			}
			return true;
		}
		return false;
	}
	
	@Before(Tx.class)
	public boolean confirmCourse(JSONArray studentsJson,CourseTable courseTable,String operator){
		for (int i=0; i<studentsJson.size(); i++) {
			JSONObject studentJson = (JSONObject) studentsJson.get(i);
			//获取reserveCourse
			ReserveCourse reserveCourse = new ReserveCourse();
			reserveCourse.setCourseTableID(courseTable.getId());
			reserveCourse.setStudentName(studentJson.getString("name"));
			reserveCourse.setPhone(studentJson.getString("phone"));
			reserveCourse = me.selectMember(reserveCourse, "confirmCourse").get(0);
			reserveCourse.setStatus("已确认");
			reserveCourse.setPresent(true);
			reserveCourse.setConfirmTime(new Date());
			reserveCourse.setConfirmMan(operator);
			//获取student
			Student student = new Student();
			student.setName(studentJson.getString("name"));
			student.setPhone(studentJson.getString("phone"));
			List<Student> studentList = stuService.selectMember(student);
			if (studentList == null || studentList.size() != 1) {
				return false;
			} 
			student = studentList.get(0);
			if("子用户".equals(student.getMainUserFlag())){
				Student subStudent = new Student();
				subStudent.setName(student.getMainUserName());
				subStudent.setPhone(student.getPhone());
				List<Student> studentList2 = stuService.selectMember(subStudent);
				if (studentList2 == null || studentList2.size() != 1) {
					return false;
				} 
				student = studentList2.get(0);
			}
			if("未上课".equals(studentJson.getString("status"))){
				reserveCourse.setPresent(false);
				student.setCounts(student.getCounts()+1);
				if(student.getCounts()>=2){
					//student.setRemarks(new StringBuilder(student.getRemarks()).append("禁止约课原因为旷课大于两次").toString());
					student.setEnable(false);
				}
			}
			student.setDisableCourseCount(student.getDisableCourseCount()-1);
			if(student.getDisableCourseCount()<0) {
				student.setDisableCourseCount(0);
			}
			if("课时卡".equals(student.getVipType())){
				student.setUseCourseCount(student.getUseCourseCount()+1);
				student.setRemainCourseCount(student.getRemainCourseCount()-1);
				if(student.getRemainCourseCount()<0) {
					student.setRemainCourseCount(0);
				}
			}
			if(!me.update(reserveCourse) || !stuService.update(student)){
				return false;
			}
		}
		if(!courseTableService.update(courseTable)){
			return false;
		}
		return true;
	}
	
	public boolean DbconfirmCourse(JSONArray studentsJson,CourseTable courseTable,String operator){
		List<ReserveCourse> reserveCourses = new ArrayList<>();
		List<Student> students = new ArrayList<>();
		for (int i=0; i<studentsJson.size(); i++) {
			JSONObject studentJson = (JSONObject) studentsJson.get(i);
			//获取reserveCourse
			ReserveCourse reserveCourse = new ReserveCourse();
			reserveCourse.setCourseTableID(courseTable.getId());
			reserveCourse.setStudentName(studentJson.getString("name"));
			reserveCourse.setPhone(studentJson.getString("phone"));
			reserveCourse = me.selectMember(reserveCourse, "confirmCourse").get(0);
			reserveCourse.setStatus("已确认");
			reserveCourse.setPresent(true);
			reserveCourse.setConfirmTime(new Date());
			reserveCourse.setConfirmMan(operator);
			//获取student
			Student student = new Student();
			student.setName(studentJson.getString("name"));
			student.setPhone(studentJson.getString("phone"));
			List<Student> studentList = stuService.selectMember(student);
			if (studentList == null || studentList.size() != 1) {
				return false;
			} 
			student = studentList.get(0);
			if("子用户".equals(student.getMainUserFlag())){
				Student subStudent = new Student();
				subStudent.setName(student.getMainUserName());
				subStudent.setPhone(student.getPhone());
				List<Student> studentList2 = stuService.selectMember(subStudent);
				if (studentList2 == null || studentList2.size() != 1) {
					return false;
				} 
				student = studentList2.get(0);
			}
			if("未上课".equals(studentJson.getString("status"))){
				reserveCourse.setPresent(false);
				student.setCounts(student.getCounts()+1);
				if(student.getCounts()>=2){
					//student.setRemarks(new StringBuilder(student.getRemarks()).append("禁止约课原因为旷课大于两次").toString());
					student.setEnable(false);
				}
			}
			student.setDisableCourseCount(student.getDisableCourseCount()-1);
			if(student.getDisableCourseCount()<0) {
				student.setDisableCourseCount(0);
			}
			if("课时卡".equals(student.getVipType())){
				student.setUseCourseCount(student.getUseCourseCount()+1);
				student.setRemainCourseCount(student.getRemainCourseCount()-1);
				if(student.getRemainCourseCount()<0) {
					student.setRemainCourseCount(0);
				}
			}
			reserveCourses.add(reserveCourse);
			students.add(student);
//			if(!me.update(reserveCourse) || !stuService.update(student)){
//				return false;
//			}
		}
//		if(!courseTableService.update(courseTable)){
//			return false;
//		}
		return DbconfirmCourseCore(reserveCourses, courseTable, students);
	}
	
	/**
	 * 确认课程
	 * @param reserveCourses
	 * @param courseTable
	 * @param students
	 * @return
	 */
	public boolean DbconfirmCourseCore(List<ReserveCourse> reserveCourses, CourseTable courseTable, List<Student> students) {
		return Db.tx(() -> {
			
			for(int i=0; i<reserveCourses.size(); i++) {
				
			}
			
			
			return true;
		});
		
	}
	
	@Before(Tx.class)
	public boolean confirmCourseAdmin(ReserveCourse reserveCourse, Student student) {
		if("子用户".equals(student.getMainUserFlag())){
			Student subStudent = new Student();
			subStudent.setName(student.getMainUserName());
			subStudent.setPhone(student.getPhone());
			List<Student> studentList2 = stuService.selectMember(subStudent);
			if (studentList2 == null || studentList2.size() != 1) {
				return false;
			} 
			student = studentList2.get(0);
		}
		if(!reserveCourse.getPresent()){
			student.setCounts(student.getCounts()+1);
			if(student.getCounts()>=2){
				//student.setRemarks(new StringBuilder(student.getRemarks()).append("禁止约课原因为旷课大于两次").toString());
				student.setEnable(false);
			}
		}
		student.setDisableCourseCount(student.getDisableCourseCount()-1);
		if(student.getDisableCourseCount()<0) {
			student.setDisableCourseCount(0);
		}
		if("课时卡".equals(student.getVipType())){
			student.setUseCourseCount(student.getUseCourseCount()+1);
			student.setRemainCourseCount(student.getRemainCourseCount()-1);
			if(student.getRemainCourseCount()<0) {
				student.setRemainCourseCount(0);
			}
		}
		if(!me.update(reserveCourse) || !stuService.update(student)){
			return false;
		}
		return true;
	}
	
	/**
	 * 2018年11月25日
	 * @param reserveCourse
	 * @param courseTable
	 * @param student
	 * @param subStudent
	 * @return
	 */
	public boolean DbReserveCourse(ReserveCourse reserveCourse, CourseTable courseTable, Student student,
			Student subStudent) {
		return Db.tx(() -> {
//			int weekCount = ToolClass.getWeekCount(new Date());
			Paras tmpparas = parasService.selectMember("currentWeekCount");
			int weekCount = Integer.parseInt(tmpparas.getValue());
			
			boolean reserveCourseflag = Db.save("reserveCourse", reserveCourse.toRecord());
			int courseTablecount = Db.update("update courseTable set stuNumber=?,fixedNum=?,allowFixed=?,enable=?,updateTime=now() where id=? and stuNumber=?",
					courseTable.getStuNumber(),courseTable.getFixedNum(),courseTable.getAllowFixed(),courseTable.getEnable(),courseTable.getId(),courseTable.getStuNumber()-1);
			if(subStudent == null) {
				int tmpweekReserveCount = student.getWeekReserveCount();
				if(weekCount != reserveCourse.getWeekCount() && ("admin".equals(reserveCourse.getOperator()) || reserveCourse.getReserveType().equals(reserveType[1]))) {
					tmpweekReserveCount = student.getWeekReserveCount()+1;
				}
				int studentcount = Db.update("update student set disableCourseCount=?,weekReserveCount=?,enable=? where id=? and weekReserveCount=?",
						student.getDisableCourseCount(),student.getWeekReserveCount(),student.getEnable(),student.getId(),tmpweekReserveCount-1);
				return reserveCourseflag && courseTablecount==1 && studentcount==1;
			}else {
				int tmpweekReserveCount = subStudent.getWeekReserveCount();
				if(weekCount != reserveCourse.getWeekCount() && ("admin".equals(reserveCourse.getOperator()) || reserveCourse.getReserveType().equals(reserveType[1]))) {
					tmpweekReserveCount = subStudent.getWeekReserveCount()+1;
				}
				int studentcount = Db.update("update student set disableCourseCount=?,weekReserveCount=?,enable=? where id=? and disableCourseCount=?",
						student.getDisableCourseCount(),student.getWeekReserveCount(),student.getEnable(),student.getId(),student.getDisableCourseCount()-1);
				System.out.println(subStudent.toJson());
				int subStudentcount = Db.update("update student set weekReserveCount=?,enable=? where id=? and weekReserveCount=?",
						subStudent.getWeekReserveCount(),subStudent.getEnable(),subStudent.getId(),tmpweekReserveCount-1);
				return reserveCourseflag && courseTablecount==1 && studentcount==1 && subStudentcount==1;
			}
		});
	}
	
	public boolean DbOnlyFixedReserveCourse(List<ReserveCourse> reserveCourseList, List<CourseTable> courseTableList, Student student,
			Student subStudent) {
		return Db.tx(() -> {
//			int weekCount = ToolClass.getWeekCount(new Date());
			Paras tmpparas = parasService.selectMember("currentWeekCount");
			int weekCount = Integer.parseInt(tmpparas.getValue());
			
			boolean reserveCourseflag = true;
			boolean CourseTableflag = true;
			boolean studentflag = true;
			boolean subStudentflag = true;
			
			for(int i=0; i<reserveCourseList.size(); i++) {
				ReserveCourse reserveCourse = reserveCourseList.get(i);
				CourseTable courseTable = courseTableList.get(i);
				if(!Db.save("reserveCourse", reserveCourse.toRecord())) {
					reserveCourseflag = false;
					break;
				}
				
				int courseTablecount = Db.update("update courseTable set stuNumber=?,fixedNum=?,allowFixed=?,enable=?,updateTime=now() where id=? and stuNumber=?",
						courseTable.getStuNumber(),courseTable.getFixedNum(),courseTable.getAllowFixed(),courseTable.getEnable(),courseTable.getId(),courseTable.getStuNumber()-1);
				
				if(courseTablecount != 1) {
					CourseTableflag = false;
					break;
				}
			}
			
			if(subStudent == null) {
				
				System.out.println("主用户固定约课...");
				System.out.println("主用户姓名："+student.getName());
				
				int tmpweekReserveCount = student.getWeekReserveCount();
				int studentcount = Db.update("update student set disableCourseCount=?,weekReserveCount=?,enable=? where id=? and weekReserveCount=?",
						student.getDisableCourseCount(),student.getWeekReserveCount(),student.getEnable(),student.getId(),tmpweekReserveCount-1);
				if(studentcount != 1) {
					studentflag = false;
				}
				
				return reserveCourseflag && CourseTableflag && studentflag;
				
			}else {
				
				System.out.println("子用户固定约课...");
				System.out.println("主用户姓名："+student.getName());
				System.out.println("子用户姓名："+subStudent.getName());
				
				int tmpweekReserveCount = subStudent.getWeekReserveCount();
				int studentcount = Db.update("update student set disableCourseCount=?,weekReserveCount=?,enable=? where id=? and disableCourseCount=?",
						student.getDisableCourseCount(),student.getWeekReserveCount(),student.getEnable(),student.getId(),student.getDisableCourseCount()-reserveCourseList.size());
				if(studentcount != 1) {
					studentflag = false;
				}
				
				System.out.println(subStudent.toJson());
				int subStudentcount = Db.update("update student set weekReserveCount=?,enable=? where id=? and weekReserveCount=?",
						subStudent.getWeekReserveCount(),subStudent.getEnable(),subStudent.getId(),tmpweekReserveCount-1);
				
				if(subStudentcount != 1) {
					subStudentflag = false;
				}
				
				return reserveCourseflag && CourseTableflag && studentflag && subStudentflag;
			}
			
		});
	}
	
	@Before(Tx.class)
	public boolean transReserveCourse(ReserveCourse reserveCourse, CourseTable courseTable, Student student,
			Student subStudent) {
		if(me.addMember(reserveCourse) && courseTableService.update(courseTable) && stuService.update(student)){
			if(subStudent!=null){
				stuService.update(subStudent);
			}
			return true;
		}
		return false;
	}

	public List<HashMap<String,Object>> selectMember(String beginDate, String endDate) {
		String sql = "select * from reserveCourse where DATE_FORMAT(date,'%Y-%m-%d %H:%i')>=DATE_FORMAT('"+beginDate+"','%Y-%m-%d %H:%i' ) and DATE_FORMAT(date,'%Y-%m-%d %H:%i')<=DATE_FORMAT('"+endDate+"','%Y-%m-%d %H:%i' ) order by date asc";
		List<ReserveCourse> reserveCourses = dao.find(sql);
		
		if(reserveCourses == null || reserveCourses.size() == 0){
			return null;
		}else{
			List<HashMap<String,Object>> list= new ArrayList<>();
			HashMap<String, Object> map = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			for (ReserveCourse reserveCourse : reserveCourses) {
				map = new HashMap<>();
				map.put("id", reserveCourse.getId());
				map.put("studentName", reserveCourse.getStudentName());
				map.put("courseTableID", reserveCourse.getCourseTableID());
				map.put("weekCount", reserveCourse.getWeekCount());
				map.put("week", reserveCourse.getWeek());
				map.put("date", reserveCourse.getDate()==null?"":sdf.format(reserveCourse.getDate()));
				map.put("courseTime", reserveCourse.getCourseTime());
				map.put("course", reserveCourse.getCourse());
				map.put("teacher1", reserveCourse.getTeacher1());
				map.put("teacher2", reserveCourse.getTeacher2());
				//map.put("classroom", reserveCourse.getClassroom());
				map.put("operator", reserveCourse.getOperator());
				map.put("reserveType", reserveCourse.getReserveType());
				map.put("reserveTime", reserveCourse.getReserveTime()==null?"":sdf1.format(reserveCourse.getReserveTime()));
				map.put("enableTime", reserveCourse.getEnableTime()==null?"":sdf1.format(reserveCourse.getEnableTime()));
				map.put("confirmTime", reserveCourse.getConfirmTime()==null?"":sdf1.format(reserveCourse.getConfirmTime()));
				if(reserveCourse.getPresent()){
					map.put("present", "已上课");
				}else{
					map.put("present", "未上课");
				}
				map.put("status", reserveCourse.getStatus());
				list.add(map);
			}
			return list;
		}
	}
	
	/**
	 * 获取指定课程状态,指定周数的所有学生
	 * @param weekCount
	 * @param status
	 * @return
	 */
	public List<ReserveCourse> getStudentNameByWeekCount( String status, int weekCount) {
		
		//2019-07-01 修复查询出去年同样周数约课记录的问题
		Paras paraDate = parasService.selectMember("currentDate");
		String currentDateStr = paraDate.getValue();
		
		String sql = "select DISTINCT(phone),studentName from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and status in ('已预约','上课中','未确认','已确认') and weekCount="+weekCount;
		List<ReserveCourse> reserveCourses = dao.find(sql);
		if(reserveCourses != null && reserveCourses.size() > 0){
			return reserveCourses;
		}
		return null;
	}
	
	public List<ReserveCourse> getStudentNameFixedCondition(int weekCount) {
		
		//2019-07-01 修复查询出去年同样周数约课记录的问题
		Paras paraDate = parasService.selectMember("currentDate");
		String currentDateStr = paraDate.getValue();
		
		String sql = "select * from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and reserveType='固定课' and weekCount="+weekCount;
		List<ReserveCourse> reserveCourses = dao.find(sql);
		if(reserveCourses != null && reserveCourses.size() > 0){
			return reserveCourses;
		}
		return null;
	}

	public int getWeekReserveCount(int weekCount, ReserveCourse reserveCourse) {
		
		//2019-07-01 修复查询出去年同样周数约课记录的问题
		Paras paraDate = parasService.selectMember("currentDate");
		String currentDateStr = paraDate.getValue();
		
		String sql = "select * from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and phone='"+reserveCourse.getPhone()+"' and studentName='"+reserveCourse.getStudentName()+"' and status='已预约' and weekCount="+weekCount;
		List<ReserveCourse> reserveCourses = dao.find(sql);
		if(reserveCourses != null && reserveCourses.size() > 0){
			return reserveCourses.size();
		}
		return 0;
	}
	
	public int getWeekReserveCountTest(int weekCount, ReserveCourse reserveCourse) {
		//2019-07-01 修复查询出去年同样周数约课记录的问题
		Paras paraDate = parasService.selectMember("currentDate");
		String currentDateStr = paraDate.getValue();
				
		String sql = "select * from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and phone='"+reserveCourse.getPhone()+"' and studentName='"+reserveCourse.getStudentName()+"' and status in ('已预约','上课中','未确认','已确认') and weekCount="+weekCount;
		List<ReserveCourse> reserveCourses = dao.find(sql);
		if(reserveCourses != null && reserveCourses.size() > 0){
			return reserveCourses.size();
		}
		return 0;
	}
	
	public int getDisableCourseCountTest(int minReserveWeekCount, int maxReserveWeekCount, ReserveCourse reserveCourse) {
		
		//2019-07-01 修复查询出去年同样周数约课记录的问题
		Paras paraDate = parasService.selectMember("currentDate");
		String currentDateStr = paraDate.getValue();
		
		String sql = "select * from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and phone='"+reserveCourse.getPhone()+"' and studentName='"+reserveCourse.getStudentName()+"' and status in ('已预约','上课中','未确认') and weekCount>="+minReserveWeekCount+" and weekCount<="+maxReserveWeekCount;
		List<ReserveCourse> reserveCourses = dao.find(sql);
		if(reserveCourses != null && reserveCourses.size() > 0){
			return reserveCourses.size();
		}
		return 0;
	}

	public boolean transEnableCourse(ReserveCourse reserveCourse, CourseTable courseTable, Student students,
			Student subStudent) {
		return this.transDisableCourse(reserveCourse, courseTable, students, subStudent);
	}
	
	/**
     * 更新预约课程状态
     * @param sql
     */
	public int UpdateReserveCourseStatus(String sql) {
		int count = Db.update(sql);
		return count;
	}
	
	
	public List<ReserveCourse> selectMemberTest2() {
		return dao.find("select DISTINCT(studentName),operator from reserveCourse");
	}
	
	/**
	 * 查找指定id的数据
	 * @param id
	 * @return
	 */
	public ReserveCourse getByID(Integer id) {
		return dao.findById(id);
	}

	public void moveReserveCourseHistory(int maxWeekCount, String currentDateStr, int minWeekCount) {
		
		int i=0,j=0;
		i = Db.update("insert into reserveCourseHistory (select * from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and weekCount>="+(minWeekCount)+" and weekCount<="+maxWeekCount+")");
		if(i != 0) {
			//j = Db.update("delete from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and weekCount>="+(maxWeekCount-3)+" and weekCount<="+maxWeekCount);
		}
		System.out.println("InitWeekReserveCount.moveReserveCourseHistory i="+i+" j="+j);
	}

	public List<HashMap<String, Object>> selectMember2(String babyName) {
		String sql = "select * from reserveCourse where studentName='"+babyName+"' order by id asc";
		List<ReserveCourse> reserveCourses = dao.find(sql);
		
		if(reserveCourses == null || reserveCourses.size() == 0){
			return null;
		}else{
			List<HashMap<String,Object>> list= new ArrayList<>();
			HashMap<String, Object> map = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			for (ReserveCourse reserveCourse : reserveCourses) {
				map = new HashMap<>();
				map.put("id", reserveCourse.getId());
				map.put("studentName", reserveCourse.getStudentName());
				map.put("courseTableID", reserveCourse.getCourseTableID());
				map.put("weekCount", reserveCourse.getWeekCount());
				map.put("week", reserveCourse.getWeek());
				map.put("date", reserveCourse.getDate()==null?"":sdf.format(reserveCourse.getDate()));
				map.put("courseTime", reserveCourse.getCourseTime());
				map.put("course", reserveCourse.getCourse());
				map.put("teacher1", reserveCourse.getTeacher1());
				map.put("teacher2", reserveCourse.getTeacher2());
				//map.put("classroom", reserveCourse.getClassroom());
				map.put("operator", reserveCourse.getOperator());
				map.put("reserveType", reserveCourse.getReserveType());
				map.put("reserveTime", reserveCourse.getReserveTime()==null?"":sdf1.format(reserveCourse.getReserveTime()));
				map.put("enableTime", reserveCourse.getEnableTime()==null?"":sdf1.format(reserveCourse.getEnableTime()));
				map.put("confirmTime", reserveCourse.getConfirmTime()==null?"":sdf1.format(reserveCourse.getConfirmTime()));
				if(reserveCourse.getPresent()){
					map.put("present", "已上课");
				}else{
					map.put("present", "未上课");
				}
				map.put("status", reserveCourse.getStatus());
				list.add(map);
			}
			return list;
		}
	}

	/**
	 * 检查本阶段所有课程是否全部确认完，且所有课程已经上完，满足条件，返回 true,否则，返回false
	 * @param maxWeekCount 
	 * @param minReserveWeekCount 
	 * @return
	 */
	public boolean checkReserveCourseStatus(int minWeekCount, int maxWeekCount) {
		//2019-07-01 修复查询出去年同样周数约课记录的问题
		Paras paraDate = parasService.selectMember("currentDate");
		String currentDateStr = paraDate.getValue();
		String sql = "select count(*) as id from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and status in ('已预约','上课中','未确认') and weekCount>="+minWeekCount+" and weekCount<="+maxWeekCount;
		List<Object>  sql_str = Db.query(sql);
		int result = Integer.parseInt(sql_str.get(0).toString());
		if(result == 0) {
			return true;
		}else {
			return false;
		}
	}
}
