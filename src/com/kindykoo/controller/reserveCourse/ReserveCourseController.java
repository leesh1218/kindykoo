package com.kindykoo.controller.reserveCourse;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.core.Controller;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.kindykoo.common.model.ConfirmCourse;
import com.kindykoo.common.model.Course;
import com.kindykoo.common.model.CourseTable;
import com.kindykoo.common.model.CourseTime;
import com.kindykoo.common.model.Paras;
import com.kindykoo.common.model.ReserveCourse;
import com.kindykoo.common.model.Student;
import com.kindykoo.common.model.StudentStatus;
import com.kindykoo.common.model.Teacher;
import com.kindykoo.common.model.User;
import com.kindykoo.common.tool.ExcelUtil;
import com.kindykoo.common.tool.ToolClass;
import com.kindykoo.controller.course.CourseService;
import com.kindykoo.controller.courseTable.CourseTableService;
import com.kindykoo.controller.courseTime.CourseTimeService;
import com.kindykoo.controller.paras.ParasService;
import com.kindykoo.controller.reserveCourse.ReserveCourseService;
import com.kindykoo.controller.student.StudentService;
import com.kindykoo.controller.teacher.TeacherService;
import com.kindykoo.controller.user.UserController;
import com.kindykoo.controller.user.UserService;

public class ReserveCourseController extends Controller {

	private static ReserveCourseService service = ReserveCourseService.me;
	private static StudentService stuService = StudentService.me;
	private static TeacherService teaService = TeacherService.me;
	private static UserService userService = UserService.me;
	private static CourseTimeService courseTimeService = CourseTimeService.me;
	private static CourseTableService courseTableService = CourseTableService.me;
	private static CourseService courseService = CourseService.me;
	private static ParasService parasService = ParasService.me;
	public static final int FIXEDNUM = 6;
	private static String []  weekCounts = {"第一周", "第二周","第三周","第四周"};
	private static String []  weeks = {"星期一", "星期二", "星期三","星期四","星期五","星期六","星期日"};
	private static String []  reserveType = {"非固定课","固定课"};
	private static String pathName = "";
	private static int maxReserveWeekCount;
	private static int minReserveWeekCount;
	private static int counts;
	private static String infoMsg;
	

	/**
	 * 直接访问course进入list.jsp
	 */
	public void index() {
		setAttr("weekCounts", weekCounts);
		setAttr("weeks", weeks);
		List<CourseTime> courseTimes = courseTimeService.getAllCourseTime();
		setAttr("courseTimes", courseTimes);
		List<Course> courses = courseService.getAllCourse();
		setAttr("courses", courses);
		render("index.html");
	}
	
	public void confirmAdmin() {
		Ret ret = Ret.ok();
		
		String id = getPara("id");
		String babyName = getPara("babyName");
		String phone = getPara("phone");
//		String operator = getPara("operator");
		String status = getPara("status");
		ReserveCourse reserveCourse = service.getByID(Integer.parseInt(id));
		if(!"未确认".equals(reserveCourse.getStatus())){
			ret = Ret.fail();
			renderJson(ret);
			return;
		}
		
		//获取student
		Student student = new Student();
		student.setName(babyName);
		student.setPhone(phone);
		List<Student> studentList = stuService.selectMember(student);
		if (studentList == null || studentList.size() != 1) {
			ret = Ret.fail();
			renderJson(ret);
			return;
		} 
		student = studentList.get(0);
		
		reserveCourse.setStatus("已确认");
		reserveCourse.setPresent(true);
		if("未上课".equals(status)) {
			reserveCourse.setPresent(false);
		}
		reserveCourse.setConfirmTime(new Date());
		reserveCourse.setConfirmMan("admin");
		
		ReserveCourseService transService = enhance(ReserveCourseService.class);
		boolean res = transService.confirmCourseAdmin(reserveCourse, student);
		if(!res){
			ret = Ret.fail();
		}
		renderJson(ret);
	}
	
	public void condition() {
		//从参数表获取maxReserveWeekCount的值
		Paras paras1 = parasService.selectMember("minReserveWeekCount");
		minReserveWeekCount = Integer.parseInt(paras1.getValue());
		String keywords = getPara("keywords");
		String weekCount = getPara("weekCount");
		String week = getPara("week");
		String courseTime = getPara("courseTime");
		String course = getPara("course");
		String status = getPara("status");
		setAttr("keywords", keywords);
		if(weekCount!=null && !"".equals(weekCount.trim())) {
			setAttr("weekCount1", Integer.parseInt(weekCount)-minReserveWeekCount);
		}
		setAttr("week1", week);
		setAttr("courseTime1", courseTime);
		setAttr("course1", course);
		setAttr("status1", status);
		setAttr("weekCounts", weekCounts);
		setAttr("weeks", weeks);
		List<CourseTime> courseTimes = courseTimeService.getAllCourseTime();
		setAttr("courseTimes", courseTimes);
		List<Course> courses = courseService.getAllCourse();
		setAttr("courses", courses);
		render("condition.html");
	}
	
	public void reserveCourseAdmin(){
		String babyName = getPara("babyName");
		String phone = getPara("phone");
		String id = getPara("id");
		int weekCount = ToolClass.getWeekCount(new Date());
//		if(weekCount == 26){
//			weekCount++;
//		}
		User user = new User();
		user.setPhone(phone);
		boolean res = this.reserveCourseCore("reserve",id,"","admin",babyName,weekCount, user);
		if(res){
			render("/WEB-INF/view/admin/common/success.html");
		}else{
			render("/WEB-INF/view/admin/common/error.html");
		}
	}
	
	/**
	 * 查询待确认/已确认课程
	 */
	public void myConfirmCourses(){
		String operator = getPara("operator");
		String role = getPara("role");
		String status = getPara("status");
		String flag = "";
		if("已确认2".equals(status)){
//			status = "已确认";
			flag = "助教";
		}
		if(!"教师".equals(role)){
			setAttr("isOK", false);
			setAttr("infoMsg", "您的登录信息有误！");
			renderJson();
			return;
		}
		Teacher teacher = new Teacher();
		 teacher.setName(operator);
		 Teacher teachers = teaService.selectMember(teacher);
		 if (teachers == null) {
				setAttr("isOK", false);
				setAttr("infoMsg", "您的登录信息有误！");
				renderJson();
				return;
		}
		CourseTable courseTable = new CourseTable();
		courseTable.setTeacher1(operator);
		courseTable.setTeacher2("");
		if("助教".equals(flag)){
			courseTable.setTeacher2(operator);
		}
//		courseTable.setStatus(status);
		Date date = new Date();
		courseTable.setWeekCount(ToolClass.getWeekCount(date));
		courseTable.setWeek(ToolClass.getWeekOfDate(date));
		int i = 0;
		String currentId = "";
		String qryFlag = "myConfirmCourses";
		if("已确认".equals(status)) {
			qryFlag = "myConfirmCourses2";//查询已经确认的课程
		}
		List<CourseTable> courseTables = courseTableService.selectMember(courseTable,qryFlag);
		List<ConfirmCourse> list = null;
		if(courseTables !=null && courseTables.size()>0){
			list = new ArrayList<>();
			for (CourseTable courseTable2 : courseTables) {
				ConfirmCourse confirmCourse = new ConfirmCourse();
				
				ReserveCourse reserveCourse = new ReserveCourse();
				reserveCourse.setCourseTableID(courseTable2.getId());
				if("未上课".equals(courseTable2.getStatus())) {
					reserveCourse.setStatus("已预约");
				}else {
					reserveCourse.setStatus(courseTable2.getStatus());
				}
				List<ReserveCourse> reserveCourses = service.selectMember(reserveCourse,"myConfirmCourses");
				List<StudentStatus> students = new ArrayList<>();
				int presentNum = 0;
				if(reserveCourses!=null && reserveCourses.size()>0){
					confirmCourse.setStuNumber(reserveCourses.size());
					for (ReserveCourse reserveCourse2 : reserveCourses) {
						StudentStatus student = new StudentStatus();
						student.setId(String.valueOf(reserveCourse2.getId()));
						student.setName(reserveCourse2.getStudentName());
						student.setPhone(reserveCourse2.getPhone());
						//获取 nickName
						Student student2 = new Student();
						student2.setName(reserveCourse2.getStudentName());
						student2.setPhone(reserveCourse2.getPhone());
						List<Student> studentList = stuService.selectMember(student2);
						if (studentList == null || studentList.size() != 1) {
							setAttr("isOK", false);
							setAttr("infoMsg", "会员信息有误！");
							renderJson();
							return;
						} 
						student2 = studentList.get(0);
						student.setNickName(student2.getNickName());
						student.setStatus("已上课");
						if("已确认".equals(courseTable2.getStatus())){
							student.setStatus(reserveCourse2.getPresent()?"已上课":"未上课");
							if(reserveCourse2.getPresent()){
								presentNum++;
							}
						}
						students.add(student);
					}
				}else{
					confirmCourse.setStuNumber(0);
				}
				confirmCourse.setPresentNum(presentNum);
				confirmCourse.setStudents(students);
				confirmCourse.setId(String.valueOf(courseTable2.getId()));
				if(courseTable2.getCourse().contains("-")){
					courseTable2.setCourse(StringUtils.substringAfter(courseTable2.getCourse(), "-"));
				}
				confirmCourse.setCourse(courseTable2.getCourse());
				if("助教".equals(flag)){
					confirmCourse.setTeacher1(courseTable2.getTeacher1());
				}else{
					confirmCourse.setTeacher2(courseTable2.getTeacher2());
				}
				confirmCourse.setStatus(courseTable2.getStatus());
				confirmCourse.setCourseTime(courseTable2.getCourseTime());
				confirmCourse.setOpen(false);
				if(i == 0){
					confirmCourse.setOpen(true);
					currentId = confirmCourse.getId();
				}
				list.add(confirmCourse);
				i++;
			}
			
		}
		if(list!=null){
			setAttr("isOK", true);
			setAttr("list", list);
			setAttr("currentId", currentId);
		}else{
			setAttr("isOK", false);
			setAttr("infoMsg", "没有查到记录！");
		}
		renderJson();
	}
	
	/**
	 * 确认课程
	 */
	public void confirmCourse(){
		String operator = getPara("operator");
		int id = getParaToInt("id");
		String students = getPara("students");
		CourseTable courseTable = courseTableService.getByID(id);
		if(!"未确认".equals(courseTable.getStatus())){
			setAttr("isOK", false);
			setAttr("infoMsg", "课程状态不是未确认！");
			renderJson();
			return;
		}
		courseTable.setStatus("已确认");
		JSONArray studentsJson = JSON.parseArray(students);
		ReserveCourseService transService = enhance(ReserveCourseService.class);
		if(!transService.confirmCourse(studentsJson, courseTable, operator)){
			setAttr("isOK", false);
			setAttr("infoMsg", "确认课程失败！");
			renderJson();
			return;
		}
		setAttr("isOK", true);
		setAttr("infoMsg", "确认课程成功");
		renderJson();
	}
	
	/**
	 * 后台请假
	 */
	public void enableAdmin(){
		String id = getPara("id");
		String babyName = getPara("babyName");
		String phone = getPara("phone");
		String operator = getPara("operator");
		
		boolean res = this.enableCourseCore(operator, babyName, id, null, phone,"admin");
		Ret ret = Ret.ok();
		if(!res){
			ret = Ret.fail();
		}
		renderJson(ret);
		
	}
	
	/**
	 * 小程序请假
	 */
	public void enableCourse(){
		String operator = getPara("operator");
		String babyName = getPara("babyName");
		String id = getPara("id");
		String formId = getPara("formId");
		
		//查询用户信息
		User user = new User();
		user.setName(operator);
		user.setBabyName(babyName);
		List<User> userList = userService.selectMember(user);
		if (userList == null || userList.size() != 1) {
			setAttr("isOK", false);
			setAttr("infoMsg", "用户信息有误！");
			renderJson();
			return;
		} 
		User users = userList.get(0);
		if(users == null){
			setAttr("isOK", false);
			setAttr("infoMsg", "user未找到记录！");
			renderJson();
			return;
		}
		
		boolean res = this.enableCourseCore(operator, babyName, id, formId, users.getPhone(),"miniPro");
		
		if(res){
			setAttr("isOK", true);
		}else{
			setAttr("isOK", false);
		}
		renderJson();
	}
	
	private boolean enableCourseCore(String operator, String babyName, String id, String formId, String phone, String channel){
		
		ReserveCourse reserveCourse = service.getReserveCourse(id);
		reserveCourse.setEnableMan(operator);
		if("admin".equals(channel)){
			reserveCourse.setEnableMan(channel);
		}
		reserveCourse.setStatus("已请假");
		reserveCourse.setEnableTime(new Date());
		
		CourseTable courseTable = courseTableService.getByID(reserveCourse.getCourseTableID());
		
		if(!"admin".equals(channel) && !checkCourseTime(courseTable.getDate(),60)){
			setAttr("infoMsg", "请假必须提前1小时进行！");
			return false;
		}

		if(!courseTable.getEnable()){
			courseTable.setEnable(true);
		}
		courseTable.setStuNumber(courseTable.getStuNumber()-1);
		if(courseTable.getStuNumber()<0) {
			courseTable.setStuNumber(0);
		}
		
		Student student = new Student();
		student.setPhone(phone);
		student.setName(reserveCourse.getStudentName());
		List<Student> studentList = stuService.selectMember(student);
		if (studentList == null || studentList.size() != 1) {
			setAttr("infoMsg", "会员信息有误！");
			return false;
		} 
		Student students = studentList.get(0);
		
		//有关会员信息的必须是主用户
		Student subStudent = null;
		if("子用户".equals(students.getMainUserFlag())){
			//子用户信息
			subStudent = students;
			Student tmpStudent = new Student();
			tmpStudent.setName(students.getMainUserName());
			tmpStudent.setPhone(phone);//主用户与子用户手机号必须一致
			List<Student> studentList2 = stuService.selectMember(tmpStudent);
			if (studentList2 == null || studentList2.size() != 1) {
				setAttr("infoMsg", "会员信息有误！");
				return false;
			} 
			students = studentList2.get(0);
			if(students == null){
				setAttr("infoMsg", "您的绑定的主用户信息有误，预约课程失败！");
				return false;
			}
//					if(!"课时卡".equals(students.getVipType())){
//						if(!subStudent.getEnable()){
//							subStudent.setEnable(true);
//						}
//						subStudent.setWeekReserveCount(subStudent.getWeekReserveCount()-1);
//					}
		}
		
		if(reserveCourse.getWeekCount() == ToolClass.getWeekCount(new Date())){
			if(subStudent!=null){
				if(!"课时卡".equals(students.getVipType()) && subStudent.getWeekReserveCount() == subStudent.getWeekMaxCount()){
					subStudent.setEnable(true);
				}
				subStudent.setWeekReserveCount(subStudent.getWeekReserveCount()-1);
				if(subStudent.getWeekReserveCount()<0) {
					subStudent.setWeekReserveCount(0);
				}
			}else{
				if(!"课时卡".equals(students.getVipType()) && students.getWeekReserveCount() == students.getWeekMaxCount()){
					students.setEnable(true);
				}
				students.setWeekReserveCount(students.getWeekReserveCount()-1);
				if(students.getWeekReserveCount()<0) {
					students.setWeekReserveCount(0);
				}
			}
		}
		if(subStudent!=null){
			if("课时卡".equals(students.getVipType()) && students.getDisableCourseCount() == students.getRemainCourseCount()){
				students.setEnable(true);
				subStudent.setEnable(true);
			}
		}else{
			if("课时卡".equals(students.getVipType()) && students.getDisableCourseCount() == students.getRemainCourseCount()){
				students.setEnable(true);
			}
		}
		students.setDisableCourseCount(students.getDisableCourseCount()-1);
		if(students.getDisableCourseCount()<0) {
			students.setDisableCourseCount(0);
		}
		
//				if(service.update(reserveCourse) && courseTableService.update(courseTable) && stuService.update(students)){
//					if(subStudent!=null){
//						stuService.update(subStudent);
//					}
//					setAttr("isOK", true);
//					setAttr("infoMsg", "本节课请假成功！");	
//				}
		ReserveCourseService transService = enhance(ReserveCourseService.class);
		if(transService.transEnableCourse(reserveCourse,courseTable,students,subStudent)){
			if(formId != null){
				boolean res = sendEnableInfo(reserveCourse.getStudentName(),courseTable , formId, operator);
				if(res){
					System.out.println(reserveCourse.getStudentName()+"请假"+courseTable.getCourse()+"课成功，发送服务通知成功！");
				}else{
					System.out.println(reserveCourse.getStudentName()+"请假"+courseTable.getCourse()+"课成功，发送服务通知失败！");
				}
			}
			setAttr("infoMsg", "本节课请假成功！");
			return true;
		}else{
			setAttr("infoMsg", "本节课请假失败！");
			return false;
		}
	}
	
	/**
	 * 查询课程
	 * @param babyName 
	 */
	private void qryCourse(String operator, String weekCount,String role,String status, String babyName){
		if(weekCount == null || "".equals(weekCount)){
			setAttr("isOK", false);
			setAttr("infoMsg", "weekCount参数有误！");
			renderJson();
			return;
		}
		if(babyName == null || "".equals(babyName)){
			setAttr("isOK", false);
			setAttr("infoMsg", "babyName参数有误！");
			renderJson();
			return;
		}
		if(operator == null || "".equals(operator)){
			setAttr("isOK", false);
			setAttr("infoMsg", "operator参数有误！");
			renderJson();
			return;
		}
		if(role == null || "".equals(role)){
			setAttr("isOK", false);
			setAttr("infoMsg", "role参数有误！");
			renderJson();
			return;
		}
		User user = new User();
		user.setName(operator);
		user.setBabyName(babyName);
		user.setRole(role);
		List<User> userList = userService.selectMember(user);
		if (userList == null || userList.size() != 1) {
			setAttr("isOK", false);
			setAttr("infoMsg", "用户信息有误！");
			renderJson();
			return;
		} 
		User users = userList.get(0);
		if(users == null){
			setAttr("isOK", false);
			setAttr("infoMsg", "user未找到记录！");
			renderJson();
			return;
		}
		ReserveCourse reserveCourse = new ReserveCourse();
		if("enable".equals(status)){
			if("家长".equals(role)){
				reserveCourse.setStatus("已预约");
				reserveCourse.setStudentName(users.getBabyName());
				reserveCourse.setPhone(users.getPhone());
			}else if("教师".equals(role)){
				reserveCourse.setStatus("已请假");
				reserveCourse.setTeacher1(users.getName());
				reserveCourse.setTeacher2(users.getName());
			}
		}else if("disable".equals(status)){
			reserveCourse.setStudentName(users.getBabyName());
			reserveCourse.setPhone(users.getPhone());
			reserveCourse.setStatus("已请假");
		}else if("myCourses".equals(status)){
			reserveCourse.setStudentName(users.getBabyName());
			reserveCourse.setPhone(users.getPhone());
			reserveCourse.setStatus(null);
		}
		Paras paras1 = parasService.selectMember("minReserveWeekCount");
		weekCount = Integer.parseInt(paras1.getValue())+Integer.parseInt(weekCount)+"";
		reserveCourse.setWeekCount(Integer.parseInt(weekCount));
		
		List<ReserveCourse> reserveCourses = service.selectMember(reserveCourse,"qryCourse");
		
		if(reserveCourses!=null){
			List<ReserveCourse> reserveCourses2 = new ArrayList<>();
			for (ReserveCourse reserveCourse2 : reserveCourses) {
				if(reserveCourse2.getCourse().contains("-")){
					reserveCourse2.setCourse(StringUtils.substringAfter(reserveCourse2.getCourse(), "-"));
				}
				reserveCourses2.add(reserveCourse2);
			}
			setAttr("isOK", true);
			setAttr("list", reserveCourses2);
		}else{
			setAttr("isOK", false);
			setAttr("infoMsg", "没有查到记录！");
		}
		renderJson();
	}
	
	/**
	 * 查询已经预约的课程
	 */
	public void myCourses(){
		String operator = getPara("operator");
		String babyName = getPara("babyName");
		String weekCount = getPara("weekCount");
//		if("-1".equals(weekCount)){
//			weekCount = "0";
//		}
		String role = getPara("role");
		String status = getPara("status");
		this.qryCourse(operator, weekCount,role,status,babyName);
	}
	
	/**
	 * 小程序预约课程
	 */
	public void reserveCourse(){
		String flag = getPara("flag");
		String id = getPara("id");
		String formId = getPara("formId");
		String operator = getPara("operator");
		String babyName = getPara("babyName");
		//查询用户信息
		User user = new User();
		user.setName(operator);
		user.setBabyName(babyName);
		user.setRole("家长");
		user.setBinded(true);
		List<User> userList = userService.selectMember(user);
		if (userList == null || userList.size() != 1) {
			setAttr("isOK", false);
			setAttr("infoMsg", "用户信息有误，请重新绑定");
			renderJson();
			return;
		}
		User users = userList.get(0);
		if(users == null){
			setAttr("isOK", false);
			setAttr("infoMsg", "user未找到记录！");
			renderJson();
			return;
		}
//		String babyName = users.getBabyName();
		int weekCount = ToolClass.getWeekCount(new Date());
//		if(weekCount == 26){
//			weekCount++;
//		}
		this.reserveCourseCore(flag,id,formId,operator,babyName,weekCount,users);
	}
	
	/**
	 * 发送约课成功消息
	 * @param babyName
	 * @param id
	 * @param formId
	 * @param flag 
	 * @return
	 */
	private boolean sendReserveInfo(String babyName, CourseTable courseTable, String formId,String operator, String flag) {
		UserController userController = new UserController();
		String access_token = userController.getAccessTtoken();
		if(access_token == null || "".equals(access_token))
			return false;
		User user = new User();
		user.setBabyName(babyName);
		user.setName(operator);
		List<User> userList = userService.selectMember(user);
		if (userList == null || userList.size() != 1) {
			return false;
		} 
		user = userList.get(0);
		String openId = user.getOpenid();
		System.out.println("openId==" + openId);
		JSONObject reqData = new JSONObject();
		reqData.put("touser", openId);
		reqData.put("template_id", ToolClass.reserveModelID);
//		reqData.put("page", "myCourses");
		reqData.put("form_id", formId);
		JSONObject data = new JSONObject();
		JSONObject keyword1 = new JSONObject();
		keyword1.put("value", babyName);
		data.put("keyword1", keyword1);
		JSONObject keyword2 = new JSONObject();
		keyword2.put("value", courseTable.getCourse());
		data.put("keyword2", keyword2);
		JSONObject keyword3 = new JSONObject();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String courseDate = sdf.format(courseTable.getDate());
		keyword3.put("value", courseDate);
		data.put("keyword3", keyword3);
		JSONObject keyword4 = new JSONObject();
		keyword4.put("value", courseTable.getTeacher1());
		data.put("keyword4", keyword4);
		JSONObject keyword5 = new JSONObject();
		keyword5.put("value", courseTable.getTeacher2());
		data.put("keyword5", keyword5);
		JSONObject keyword6 = new JSONObject();
		keyword6.put("value", ToolClass.courseTime+"分钟");
		data.put("keyword6", keyword6);
		JSONObject keyword7 = new JSONObject();
		if("reserve".equals(flag)){
			keyword7.put("value", "临时约课");
		}else if("reserveAndFixed".equals(flag)){
			keyword7.put("value", "固定约课");
		}
		data.put("keyword7", keyword7);
		
		reqData.put("data", data);
		return userController.sendModelMsg(access_token,reqData.toString());
	}
	
	/**
	 * 发送请假成功消息
	 * @param babyName
	 * @param id
	 * @param formId
	 * @return
	 */
	private boolean sendEnableInfo(String babyName, CourseTable courseTable, String formId, String operator) {
		UserController userController = new UserController();
		String access_token = userController.getAccessTtoken();
		if(access_token == null || "".equals(access_token))
			return false;
		User user = new User();
		user.setBabyName(babyName);
		user.setName(operator);
		List<User> userList = userService.selectMember(user);
		if (userList == null || userList.size() != 1) {
			return false;
		} 
		user = userList.get(0);
		String openId = user.getOpenid();
		System.out.println("openId==" + openId);
		JSONObject reqData = new JSONObject();
		reqData.put("touser", openId);
		reqData.put("template_id", ToolClass.enableModelID);
//		reqData.put("page", "disable");
		reqData.put("form_id", formId);
		JSONObject data = new JSONObject();
		JSONObject keyword1 = new JSONObject();
		keyword1.put("value", babyName);
		data.put("keyword1", keyword1);
		JSONObject keyword2 = new JSONObject();
		keyword2.put("value", courseTable.getCourse());
		data.put("keyword2", keyword2);
		JSONObject keyword3 = new JSONObject();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String courseDate = sdf.format(courseTable.getDate());
		keyword3.put("value", courseDate);
		data.put("keyword3", keyword3);
		JSONObject keyword4 = new JSONObject();
		keyword4.put("value", courseTable.getTeacher1());
		data.put("keyword4", keyword4);
		JSONObject keyword5 = new JSONObject();
		keyword5.put("value", courseTable.getTeacher2());
		data.put("keyword5", keyword5);
		JSONObject keyword6 = new JSONObject();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		keyword6.put("value", sdf1.format(new Date()));
		data.put("keyword6", keyword6);
		JSONObject keyword7 = new JSONObject();
		keyword7.put("value", operator);
		data.put("keyword7", keyword7);
		
		reqData.put("data", data);
		return userController.sendModelMsg(access_token,reqData.toString());
	}
	
	/**
	 * 预约或固定课程
	 * @param reserveCourse
	 * @param users
	 * @param courseTable
	 * @param flag
	 * @param weekCount 
	 * @param user 
	 */
	public boolean reserveOrFixedCourse(ReserveCourse reserveCourse,Student students,Student subStudent,CourseTable courseTable,String flag, int weekCount, User user){
		boolean res = "课时卡".equals(students.getVipType());
		if(!res){
			Student temp = students;
			if(subStudent != null){
				temp = subStudent;
			}
			if(temp.getAge()>30){
				ReserveCourse tempReserveCourse = new ReserveCourse();
				tempReserveCourse.setStudentName(temp.getName());
				tempReserveCourse.setPhone(user.getPhone());
				tempReserveCourse.setCourse(courseTable.getCourse());
				tempReserveCourse.setWeekCount(courseTable.getWeekCount());
				List<ReserveCourse> tempList = service.selectMember(tempReserveCourse,"repeatReserve");
				if (tempList != null) {
						setAttr("isOK", false);
						setAttr("infoMsg", "您本周已经预约过"+courseTable.getCourse()+"课程了，该课程不允许重复预约");
						renderJson();
						return false;
				}
			}
		}
		reserveCourse.setCourseTableID(courseTable.getId());
		//查看是否已经预约本节课
		List<ReserveCourse> list1 = service.selectMember(reserveCourse,"reserveOrFixedCourse");
		if (list1 != null) {
				setAttr("isOK", false);
				setAttr("infoMsg", "您已经预约过本节课了，可以去个人中心查看已经预约的课程");
				renderJson();
				return false;
		}
		
		reserveCourse.setDate(courseTable.getDate());
		List<ReserveCourse> list2 = service.selectMember(reserveCourse,"repeatTimeReserve");
		if (list2 != null) {
				setAttr("isOK", false);
				setAttr("infoMsg", "您该时间段已经预约过别的课程了，时间冲突，预约失败");
				renderJson();
				return false;
		}
		
		reserveCourse.setWeekCount(courseTable.getWeekCount());
		reserveCourse.setWeek(courseTable.getWeek());
		reserveCourse.setDate(courseTable.getDate());
		reserveCourse.setCourseTime(courseTable.getCourseTime());
		reserveCourse.setCourse(courseTable.getCourse());
		reserveCourse.setTeacher1(courseTable.getTeacher1());
		reserveCourse.setTeacher2(courseTable.getTeacher2());
		reserveCourse.setClassroom(courseTable.getClassroom());
		reserveCourse.setPresent(false);
		reserveCourse.setStatus("已预约");
		if("上课中".equals(courseTable.getStatus())) {
			reserveCourse.setStatus("上课中");
		}
		reserveCourse.setReserveTime(new Date());
		//更新课程预约人数
		courseTable.setStuNumber(courseTable.getStuNumber()+1);
		if(courseTable.getStuNumber()>=courseTable.getMaxNumber()){
			courseTable.setEnable(false);
			courseTable.setAllowFixed(false);
		}
		if(!"admin".equals(reserveCourse.getOperator()) && !checkCourseTime(courseTable.getDate(),5)){
			setAttr("isOK", false);
			setAttr("infoMsg", "课程开始时间前5分钟禁止约课！");
			renderJson();
			return false;
		}
		
		students.setDisableCourseCount(students.getDisableCourseCount()+1);
		if("课时卡".equals(students.getVipType()) && students.getDisableCourseCount() >= students.getRemainCourseCount()){
			students.setEnable(false);
		}
		if(courseTable.getWeekCount() == weekCount){
			//更新学员本周预约课程的节数及累积预约课程次数
			if(subStudent!=null){
				subStudent.setWeekReserveCount(subStudent.getWeekReserveCount()+1);
				if(!"课时卡".equals(students.getVipType()) && subStudent.getWeekReserveCount() == subStudent.getWeekMaxCount()){
					subStudent.setEnable(false);
				}
				if("课时卡".equals(students.getVipType()) && (students.getUseCourseCount()+students.getDisableCourseCount()) == students.getCourseCount()){
					students.setEnable(false);
					subStudent.setEnable(false);
				}
			}else{
				students.setWeekReserveCount(students.getWeekReserveCount()+1);
				if(!"课时卡".equals(students.getVipType()) && students.getWeekReserveCount() == students.getWeekMaxCount()){
					students.setEnable(false);
				}
				if("课时卡".equals(students.getVipType()) &&(students.getUseCourseCount()+students.getDisableCourseCount()) == students.getCourseCount()){
					students.setEnable(false);
				}
			}
		}
		//ReserveCourseService transService = enhance(ReserveCourseService.class);
//		if(transService.transReserveCourse(reserveCourse,courseTable,students,subStudent)){
		if(service.DbReserveCourse(reserveCourse,courseTable,students,subStudent)){
			if("reserve".equals(flag)){
				setAttr("isOK", true);
				setAttr("infoMsg", "预约课程成功！");	
				renderJson();
			}
			return true;
		}else{
			setAttr("isOK", false);
			setAttr("infoMsg", "预约课程失败,请您稍候再试");
			renderJson();
			return false;
		}
	}
	
	/**
	 * 
	 * @param courseTable
	 * @param minutes
	 * @return
	 */
	public boolean checkCourseTime(Date date,int minutes){
		Calendar calendar=Calendar.getInstance(Locale.CHINA);
		calendar.add(Calendar.MINUTE, minutes);
		if(!date.after(calendar.getTime())){
			return false;
		}
		return true;
	}
	
	public boolean checkCourseTimeSub(Date date,int minutes){
		Calendar calendar=Calendar.getInstance(Locale.CHINA);
		calendar.add(Calendar.MINUTE, minutes);
		if(!date.before(calendar.getTime())){
			return false;
		}
		return true;
	}
	
	/**
	 * 查询数据
	 */
	public void list(){
		Integer pageSize = getParaToInt("limit", 10);
		int offset = getParaToInt("offset", 0);
		String keyword = getPara("keywords").trim();
		String courseTime = getPara("courseTime");
		String course = getPara("course");
		String status = getPara("status");
		String realWeekCount = getPara("realWeekCount");
		String weekCount = getPara("weekCount");
		Paras paras1 = parasService.selectMember("minReserveWeekCount");
		if(weekCount != null && !"".equals(weekCount)){
			weekCount = Integer.parseInt(paras1.getValue())+Integer.parseInt(getPara("weekCount"))+"";
		}
		if(realWeekCount != null && !"".equals(realWeekCount)){
			weekCount = realWeekCount;
		}
		String week = getPara("week");
		int pageNumber = offset/pageSize+1;
		Paras paras = parasService.selectMember("maxReserveWeekCount");
		Page<ReserveCourse> coursePageData = service. paginate(pageNumber, pageSize, keyword, courseTime,course, weekCount, week,status,paras1.getValue(),paras.getValue());
		setAttr("total", coursePageData.getTotalRow());
		setAttr("rows", coursePageData.getList());
		renderJson(); 
	}

	/**
	 * 导出记录
	 */
	public void export(){
		String beginDatePara = getPara("beginDate");
		String endDatePara = getPara("endDate");
		String beginDate = beginDatePara+" 00:00:00";
		String endDate = endDatePara+" 00:00:00";
		
		List<HashMap<String, Object>> data = service.selectMember(beginDate, endDate);
		if(data == null || data.size()==0){
			setAttr("isOK", false);
			setAttr("infoMsg", "没有记录！");
			renderJson();
			return;
		}
		String[] title ={"编号","会员姓名","课程编号","周数","星期","上课日期","时间段","课程","主教","助教","操作人","预约类型","预约时间","请假时间","确认时间","是否上课","约课状态"};
		String[] field ={"id","studentName","courseTableID","weekCount","week","date","courseTime","course","teacher1","teacher2","operator","reserveType","reserveTime","enableTime","confirmTime","present","status"};
		beginDatePara = beginDatePara.replaceAll("-", "");
		endDatePara = endDatePara.replaceAll("-", "");
		String fileName = "ReserveCourse-"+beginDatePara+"-"+endDatePara+".xls";
		String path = getSession().getServletContext().getRealPath("/")+"export/";
		pathName = path+fileName;
		File file = new File(pathName);
		
		boolean res = ExcelUtil.exportData(title, field, data, file);
		if(!res){
			setAttr("isOK", false);
			setAttr("infoMsg", "导出失败！");
			renderJson();
			return;
		}
		setAttr("isOK", true);
		setAttr("path", ToolClass.BATHPATH+ToolClass.EXPORTPATH+fileName);
		setAttr("times", data.size());
		renderJson();
	}
	
	public void download(){
		setAttr("path", pathName);
		render("../common/download.html");
	}
	
	private boolean reserveCourseCore(String flag, String id, String formId, String operator, String babyName, int weekCount, User user){
		//从参数表获取maxReserveWeekCount的值
		Paras paras = parasService.selectMember("maxReserveWeekCount");
		Paras paras1 = parasService.selectMember("minReserveWeekCount");
		maxReserveWeekCount = Integer.parseInt(paras.getValue());
		minReserveWeekCount = Integer.parseInt(paras1.getValue());
		
		//查看该学员是否可以预约课程
		Student student = new Student();
		student.setName(babyName);
		student.setPhone(user.getPhone());
		List<Student> studentList = stuService.selectMember(student);
		if (studentList == null || studentList.size() != 1) {
			setAttr("isOK", false);
			setAttr("infoMsg", "会员信息有误！");
			renderJson();
			return false;
		} 
		Student students = studentList.get(0);
		if(students == null){
			setAttr("isOK", false);
			setAttr("infoMsg", "student未找到记录，如有问题请联系前台老师！");
			renderJson();
			return false;
		}
		
//			if(!students.getEnable()){
//				setAttr("isOK", false);
//				setAttr("infoMsg", "您被禁止预约课程了，如有问题请联系前台老师！");
//				renderJson();
//				return false;
//			}
		
		CourseTable courseTable = courseTableService.getCourseTable(id);
		//有关会员信息的必须是主用户
		Student subStudent = null;
		if("子用户".equals(students.getMainUserFlag())){
			//子用户信息
			subStudent = students;
			//获取主用户信息
			Student tmpStudent = new Student();
			tmpStudent.setName(students.getMainUserName());
			tmpStudent.setPhone(user.getPhone());//主用户与子用户手机号必须一致
			List<Student> studentList2 = stuService.selectMember(tmpStudent);
			if (studentList2 == null || studentList2.size() != 1) {
				setAttr("isOK", false);
				setAttr("infoMsg", "会员信息有误！");
				renderJson();
				return false;
			} 
			students = studentList2.get(0);
			if(students == null){
				setAttr("isOK", false);
				setAttr("infoMsg", "您的绑定的主用户信息有误，预约课程失败！");
				renderJson();
				return false;
			}
			if(!"课时卡".equals(students.getVipType()) &&  courseTable.getWeekCount()==weekCount &&  subStudent.getWeekReserveCount() >= subStudent.getWeekMaxCount()){
				setAttr("isOK", false);
				setAttr("infoMsg", "您本周约课次数已经达到最大值，如有问题请联系前台老师！");
				renderJson();
				return false;
			}
			if(("reserve".equals(flag) || ("reserveAndFixed".equals(flag) && courseTable.getWeekCount()==weekCount)) && !subStudent.getEnable()){
				setAttr("isOK", false);
				setAttr("infoMsg", "您被禁止预约课程了，如有问题请联系前台老师！");
				renderJson();
				return false;
			}
		}else{
			if(!"课时卡".equals(students.getVipType()) && courseTable.getWeekCount()==weekCount &&  students.getWeekReserveCount() >= students.getWeekMaxCount()){
				setAttr("isOK", false);
				setAttr("infoMsg", "您本周约课次数已经达到最大值，如有问题请联系前台老师！");
				renderJson();
				return false;
			}
			if(("reserve".equals(flag) || ("reserveAndFixed".equals(flag) && courseTable.getWeekCount()==weekCount)) && !students.getEnable()){
				setAttr("isOK", false);
				setAttr("infoMsg", "您被禁止预约课程了，如有问题请联系前台老师！");
				renderJson();
				return false;
			}
		}
		
//			if(("reserve".equals(flag) || ("reserveAndFixed".equals(flag)) && courseTable.getWeekCount()==weekCount) && !students.getEnable()){
//				if("课时卡".equals(students.getVipType()) || subStudent == null || students.getDisableCourseCount() == students.getRemainCourseCount() || students.getWeekReserveCount()<students.getWeekMaxCount()){
//					setAttr("isOK", false);
//					setAttr("infoMsg", "您被禁止预约课程了，如有问题请联系前台老师！");
//					renderJson();
//					return false;
//				}
//			}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
		String endDate = sdf.format(students.getEndDate());
		if(!checkCourseTime(students.getEndDate(),-(60*24))){
			setAttr("isOK", false);
			setAttr("infoMsg", "您的会员卡"+endDate+"到期，如有问题请联系前台老师！");
			renderJson();
			return false;
		}
		
		Calendar calendar=Calendar.getInstance(Locale.CHINA);
		calendar.setTime(students.getEndDate());
		if("reserveAndFixed".equals(flag)) {
			counts = maxReserveWeekCount-ToolClass.getWeekCount(new Date());
			if(counts < 0) {
				setAttr("isOK", false);
				setAttr("infoMsg", "系统参数有误，请联系前台老师！");
				renderJson();
				return false;
			}
			calendar.add(Calendar.DATE, 1-(counts*7));
			infoMsg = "您的会员卡"+endDate+"到期，固定约课失败，如有问题请联系前台老师！";
		}else {
			calendar.add(Calendar.DATE, 1);
			infoMsg = "您的会员卡"+endDate+"到期，如有问题请联系前台老师！";
		}
		if(calendar.getTime().before(courseTable.getDate())){
			setAttr("isOK", false);
			setAttr("infoMsg", infoMsg);
			renderJson();
			return false;
		}
		
		if("课时卡".equals(students.getVipType()) && students.getDisableCourseCount() >= students.getRemainCourseCount()){
			setAttr("isOK", false);
			setAttr("infoMsg", "您的课时已经约完，如有问题请联系前台老师！");
			renderJson();
			return false;
		}
		
		if("reserveAndFixed".equals(flag) && "课时卡".equals(students.getVipType()) && (students.getDisableCourseCount()+counts) >= students.getRemainCourseCount()) {
			setAttr("isOK", false);
			setAttr("infoMsg", "您剩余课时不够固定约"+(counts+1)+"节课，如有问题请联系前台老师！");
			renderJson();
			return false;
		}
		
		if(courseTable.getStuNumber() >= courseTable.getMaxNumber()){
			setAttr("isOK", false);
			setAttr("infoMsg", "该课程预约人数已满！");
			renderJson();
			return false;
		}
		if(!courseTable.getEnable()){
			System.out.println("hehehe....");
			setAttr("isOK", false);
			setAttr("infoMsg", "该课程禁止预约！");
			renderJson();
			return false;
		}
		ReserveCourse reserveCourse = new ReserveCourse();
		if("reserve".equals(flag)){
			//预约课程
			reserveCourse.setPhone(user.getPhone());
			reserveCourse.setStudentName(babyName);
			reserveCourse.setReserveType(reserveType[0]);
			reserveCourse.setOperator(operator);
			if(!this.reserveOrFixedCourse(reserveCourse, students, subStudent,courseTable, flag,weekCount,user))
				return false;
			if(!"".equals(formId)){
				boolean res = sendReserveInfo(babyName,courseTable , formId, operator,flag);
				if(res){
					System.out.println(babyName+"临时约"+courseTable.getCourse()+"课成功，发送服务通知成功！");
				}else{
					System.out.println(babyName+"临时约"+courseTable.getCourse()+"课成功，发送服务通知失败！");
				}
			}
		}else if("reserveAndFixed".equals(flag)){
			
			ReserveCourse temp = new ReserveCourse();
			temp.setWeek(courseTable.getWeek());
			temp.setCourseTime(courseTable.getCourseTime());
			temp.setCourse(courseTable.getCourse());
			temp.setStudentName(babyName);
			temp.setPhone(user.getPhone());
			List<ReserveCourse> list1 = service.selectMember(temp,"reserveCourseCore1");
			if(list1 != null && list1.size() >0 ){
				setAttr("isOK", false);
				setAttr("infoMsg", "您已经固定约课过该课程！请前往个人中心查看");
				renderJson();
				return false;
			}
			
			List<CourseTable> list = courseTableService.selectMember(courseTable,maxReserveWeekCount);
			if(list == null || list.size() != (4-(courseTable.getWeekCount()-minReserveWeekCount))){
				setAttr("isOK", false);
				setAttr("infoMsg", "该课程禁止固定约课，请联系前台老师！");
				renderJson();
				return false;
			}
			
			//判断每一周的周预约数有没有超过最大值
			for (int i=0; i<list.size(); i++) {
				CourseTable tempCourseTable = list.get(i);
				temp = new ReserveCourse();
				temp.setStudentName(babyName);
				temp.setPhone(user.getPhone());
				temp.setWeekCount(tempCourseTable.getWeekCount());
				List<ReserveCourse> list2 = service.selectMember(temp,"reserveCourseCore2");
				if(list2 != null && list2.size() >= students.getWeekMaxCount() ){
					setAttr("isOK", false);
					setAttr("infoMsg", "您第"+(i+4-counts)+"周的约课数已经达到最大值！请前往个人中心查看");
					renderJson();
					return false;
				}
			}
			
			for (int i=0; i<list.size(); i++) {
				reserveCourse = new ReserveCourse();
				reserveCourse.setPhone(user.getPhone());
				reserveCourse.setStudentName(babyName);
				reserveCourse.setReserveType(reserveType[1]);
				reserveCourse.setOperator(operator);
				CourseTable courseTable2 = list.get(i);
				//固定人数加1
				if(i==0 && courseTable2.getFixedNum()>=courseTable2.getMaxFixedNum()){
					setAttr("isOK", false);
					setAttr("infoMsg", "该课程固定预约人数已满！");
					renderJson();
					return false;
				}
				courseTable2.setFixedNum(courseTable2.getFixedNum()+1);
				if(courseTable2.getFixedNum()>=courseTable2.getMaxFixedNum()){
					courseTable2.setAllowFixed(false);
				}
				if(!this.reserveOrFixedCourse(reserveCourse, students, subStudent,courseTable2, flag,weekCount,user))
					return false;
				if(!"".equals(formId) && i == 0){
					boolean res = sendReserveInfo(babyName,courseTable2 , formId, operator,flag);
					if(res){
						System.out.println(babyName+"固定约"+courseTable.getCourse()+"课成功，发送服务通知成功！");
					}else{
						System.out.println(babyName+"固定约"+courseTable.getCourse()+"课成功，发送服务通知失败！");
					}
				}
			}
			setAttr("isOK", true);
			setAttr("infoMsg", "您已经成功的固定预约了接下来共"+(4-(courseTable.getWeekCount()-Integer.parseInt(paras1.getValue())))+"周的课程");
			renderJson();
		}
		return true;
	}
	
}
