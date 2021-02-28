package com.kindykoo.controller.courseTable;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.jfinal.core.Controller;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.kindykoo.common.model.Classroom;
import com.kindykoo.common.model.Course;
import com.kindykoo.common.model.CourseDetail;
import com.kindykoo.common.model.CourseList;
import com.kindykoo.common.model.CourseTable;
import com.kindykoo.common.model.CourseTableList;
import com.kindykoo.common.model.CourseTime;
import com.kindykoo.common.model.Paras;
import com.kindykoo.common.model.ReserveCourse;
import com.kindykoo.common.model.Teacher;
import com.kindykoo.common.model.User;
import com.kindykoo.common.tool.ToolClass;
import com.kindykoo.controller.classroom.ClassroomService;
import com.kindykoo.controller.course.CourseService;
import com.kindykoo.controller.courseTable.CourseTableService;
import com.kindykoo.controller.courseTime.CourseTimeService;
import com.kindykoo.controller.paras.ParasService;
import com.kindykoo.controller.reserveCourse.ReserveCourseService;
import com.kindykoo.controller.teacher.TeacherService;
import com.kindykoo.controller.user.UserService;


public class CourseTableController extends Controller {

	private static CourseTableService service = CourseTableService.me;
	private static ReserveCourseService reserveCourseService = ReserveCourseService.me;
	private static CourseService courseService = CourseService.me;
	private static CourseTimeService courseTimeService = CourseTimeService.me;
	private static TeacherService teacherService = TeacherService.me;
	private static ClassroomService classroomService = ClassroomService.me;
	private static UserService userService = UserService.me;
	private static ParasService parasService = ParasService.me;
	private static String []  weekCounts = {"第一周", "第二周","第三周","第四周"};
	private static String []  weeks = {"星期一", "星期二", "星期三","星期四","星期五","星期六","星期日"};

	/**
	 * 直接访问course进入list.jsp
	 */
	public void index() {
		//本阶段最大周数
		Paras parasMax = parasService.selectMember("maxReserveWeekCount");
		int maxWeekCount = Integer.parseInt(parasMax.getValue());//该值不变
		//本阶段最小周数
		Paras parasMin = parasService.selectMember("minReserveWeekCount");
		int minWeekCount = Integer.parseInt(parasMin.getValue());//该值不变
		//当前周数
		Paras parasCur = parasService.selectMember("currentWeekCount");
		int curWeekCount = Integer.parseInt(parasCur.getValue());//该值不变
		//reserveCourseType
		//当前周数
		Paras parasTyp = parasService.selectMember("currentWeekCount");
		int courseType = Integer.parseInt(parasTyp.getValue());//该值不变
		if(maxWeekCount == minWeekCount && minWeekCount == curWeekCount ) {//本阶段为单周
			if(courseType == 1) {//下阶段为单周放课
				weekCounts = new String[] {"第一周（当前周）", "第二周（下阶段第一周）"};
			}else {//下阶段为四周放课
				weekCounts = new String[] {"第一周（当前周）", "第二周（下阶段第一周）", "第三周（下阶段第二周）", "第四周（下阶段第三周）", "第五周（下阶段第四周）"};
			}
		}else if(maxWeekCount - minWeekCount == 3) {//本阶段为四周放课
			if(courseType == 1) {//下阶段为单周放课
				if(curWeekCount == minWeekCount) {
					weekCounts = new String[] {"第一周（当前周）", "第二周（本阶段第二周）", "第三周（本阶段第三周）", "第四周（本阶段第四周）", "第五周（下阶段第一周）"};
				}else if(curWeekCount == (minWeekCount + 1)) {
					weekCounts = new String[] {"第一周（本阶段第一周）", "第二周（当前周）", "第三周（本阶段第三周）", "第四周（本阶段第四周）", "第五周（下阶段第一周）"};
				}else if(curWeekCount == (minWeekCount + 2)) {
					weekCounts = new String[] {"第一周（本阶段第一周）", "第二周（本阶段第二周）", "第三周（当前周）", "第四周（本阶段第四周）", "第五周（下阶段第一周）"};
				}else {
					weekCounts = new String[] {"第一周（本阶段第一周）", "第二周（本阶段第二周）", "第三周（本阶段第三周）", "第四周（当前周）", "第五周（下阶段第一周）"};
				}
			}else {//下阶段为四周放课
				if(curWeekCount == minWeekCount) {
					weekCounts = new String[] {"第一周（当前周）", "第二周（本阶段第二周）", "第三周（本阶段第三周）", "第四周（本阶段第四周）", "第五周（下阶段第一周）", "第六周（下阶段第二周）", "第七周（下阶段第三周）", "第八周（下阶段第四周）"};
				}else if(curWeekCount == (minWeekCount + 1)) {
					weekCounts = new String[] {"第一周（本阶段第一周）", "第二周（当前周）", "第三周（本阶段第三周）", "第四周（本阶段第四周）", "第五周（下阶段第一周）", "第六周（下阶段第二周）", "第七周（下阶段第三周）", "第八周（下阶段第四周）"};
				}else if(curWeekCount == (minWeekCount + 2)) {
					weekCounts = new String[] {"第一周（本阶段第一周）", "第二周（本阶段第二周）", "第三周（当前周）", "第四周（本阶段第四周）", "第五周（下阶段第一周）", "第六周（下阶段第二周）", "第七周（下阶段第三周）", "第八周（下阶段第四周）"};
				}else {
					weekCounts = new String[] {"第一周（本阶段第一周）", "第二周（本阶段第二周）", "第三周（本阶段第三周）", "第四周（当前周）", "第五周（下阶段第一周）", "第六周（下阶段第二周）", "第七周（下阶段第三周）", "第八周（下阶段第四周）"};
				}
			}
		}
		
		setAttr("weekCounts", weekCounts);
		setAttr("weeks", weeks);
		List<CourseTime> courseTimes = courseTimeService.getAllCourseTime();
		setAttr("courseTimes", courseTimes);
		List<Course> courses = courseService.getAllCourse();
		setAttr("courses", courses);
		render("index.html");
	}
	
	/**
	 * 后台查询课程详情接口
	 */
	public void courseDetailAdmin(){
		String id = getPara(0);
		Map<String,Object> map = this.courseDetailCore(id);
		if(map == null){
			setAttr("infoMsg", "该课程不是未上课或者上课中状态，不允许预约！");
			render("/WEB-INF/view/admin/common/error.html");
			return;
		}
		setAttr("id", id);
		setAttr("weekCount", map.get("weekCount"));
		setAttr("week", map.get("week"));
		setAttr("courseTime", map.get("courseTime"));
		setAttr("course", map.get("course"));
		setAttr("teacher1", map.get("teacher1"));
		setAttr("teacher2", map.get("teacher2"));
//		setAttr("classroom", map.get("classroom"));
		setAttr("stuNumber", map.get("stuNumber"));
		setAttr("student", map.get("student"));
		setAttr("student", map.get("student"));
		setAttr("enable", map.get("enable"));
		render("addStudent.html");
	}
	
	/**
	 * 小程序查询课程详情接口
	 */
	public void courseDetail(){
		String id = getPara("id");
		Map<String,Object> map = this.courseDetailCore(id);
		
		List<CourseDetail> courseDetails = new ArrayList<CourseDetail>();
		String[] names = {"week","courseTime","course","teacher1","teacher2","stuNumber","student","enable"};
		for (String name : names) {
			CourseDetail courseDetail = new CourseDetail();
			courseDetail.setName(name);
			courseDetail.setValue(String.valueOf(map.get(name)));
			courseDetail.setImgUrl("/pages/courseDetail/icon/"+name+".png");
			courseDetails.add(courseDetail);
		}
		setAttr("list", courseDetails);
		renderJson();
	}
	
	private Map<String,Object> courseDetailCore(String id){
		CourseTable courseTable = service.getCourseTable(id);
		Map<String,Object> map = null;
		try {
			map = convertBean(courseTable);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IntrospectionException e) {
			e.printStackTrace();
		}
		if(!"未上课".equals(courseTable.getStatus()) && !"上课中".equals(courseTable.getStatus())){
			return null;
		}
		//查预约本节课的学生
		if(courseTable.getStuNumber()>0){
			ReserveCourse reserveCourse = new ReserveCourse();
			reserveCourse.setCourseTableID(Integer.parseInt(id));
			List<ReserveCourse> reserveCourses = reserveCourseService.selectMember(reserveCourse, "courseDetail");
			StringBuilder strb = new StringBuilder();
			int i = 0;
			if(reserveCourses !=null && reserveCourses.size() > 0) {
				for (ReserveCourse reserveCourse2 : reserveCourses) {
					if(i==0){
						strb.append("(");
					}
					strb.append(reserveCourse2.getStudentName());
					if("固定课".equals(reserveCourse2.getReserveType())){
						strb.append("[固]");
					}
					if(i<reserveCourses.size()-1){
						strb.append(",");
					}
					if(i==reserveCourses.size()-1){
						strb.append(")");
					}
					i++;
				}
			}
			map.put("student", strb);
		}else{
			map.put("student", "(无)");
		}
		map.put("stuNumber", map.get("stuNumber")+"/"+map.get("maxNumber"));
		
		return map;
	}
	
	public static Map<String,Object> convertBean(Object bean)    
            throws IntrospectionException, IllegalAccessException, InvocationTargetException {    
        Class type = bean.getClass();    
        Map<String,Object> returnMap = new HashMap<String,Object>();    
        BeanInfo beanInfo = Introspector.getBeanInfo(type);    
    
        PropertyDescriptor[] propertyDescriptors =  beanInfo.getPropertyDescriptors();    
        for (int i = 0; i< propertyDescriptors.length; i++) {    
            PropertyDescriptor descriptor = propertyDescriptors[i];    
            String propertyName = descriptor.getName();    
            if (!propertyName.equals("class")) {    
                Method readMethod = descriptor.getReadMethod();    
                Object result = readMethod.invoke(bean, new Object[0]);    
                if (result != null) {    
                    returnMap.put(propertyName, result);    
                } else {  
                    returnMap.put(propertyName, "");    
                }    
            }    
        }    
        return returnMap;    
    }
	
	/**
	 * 复制课表
	 */
	public void copy(){
		int inWeekCount = getParaToInt("inWeekCount");
		//System.out.println(inWeekCount);
		int outWeekCount = getParaToInt("outWeekCount");
		
		Paras paras1 = parasService.selectMember("currentWeekCount");
		int currentWeekCount = Integer.parseInt(paras1.getValue());//该值不变
		
		Paras paras = parasService.selectMember("maxReserveWeekCount");
		int maxWeekCount = Integer.parseInt(paras.getValue());//该值不变
		
		Paras paras2 = parasService.selectMember("minReserveWeekCount");
		int minReserveWeekCount = Integer.parseInt(paras2.getValue());//该值不变
		
		if(currentWeekCount != maxWeekCount) {
			//renderJson(Ret.fail("error", "当前周数是："+currentWeekCount+"，本阶段最后一周（"+maxWeekCount+"）才允许复制课表"));
			//return;
		}
		
		if(inWeekCount < minReserveWeekCount) {
			//renderJson(Ret.fail("error", "源周数必须大于等于本阶段第一周（"+minReserveWeekCount+"）"));
			//return;
		}
		
		if(outWeekCount <= maxWeekCount) {
			//renderJson(Ret.fail("error", "复制目标周数必须大于本阶段最后一周（"+maxWeekCount+"）"));
			//return;
		}
		
		//校验目标周数课表是否为空
		CourseTable temp = new CourseTable();
		temp.setWeekCount(outWeekCount);
		List<CourseTable> tempCourseTables = service.selectMember(temp,"copy");
		if(tempCourseTables != null && tempCourseTables.size() > 0) {
			renderJson(Ret.fail("error", "第"+outWeekCount+"周课表不为空，有"+ tempCourseTables.size() + "条记录"));
			return;
		}
		
		CourseTable courseTable = new CourseTable();
		courseTable.setWeekCount(inWeekCount);
		List<CourseTable> courseTables = service.selectMember(courseTable,"copy");
		int times = 0;
		Date date = new Date();
		SimpleDateFormat tmpsdf = new SimpleDateFormat("yyyy-MM-dd");
		Paras tmpparasDate = parasService.selectMember("currentDate");
		try {
			date = tmpsdf.parse(tmpparasDate.getValue());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		for (CourseTable courseTable2 : courseTables) {
			courseTable2.setWeekCount(outWeekCount);
			int weekIndex = ToolClass.getIndex(weeks, courseTable2.getWeek());
			courseTable2.setDate(ToolClass.getDateAndTime(ToolClass.getDate(date,courseTable2.getWeekCount(),weekIndex), courseTable2.getBeginTime()));
			courseTable2.setId(0);
			courseTable2.setUpdateTime(new Date());
			courseTable2.setStatus("未上课");
			if(service.doSubmit(courseTable2,"")){
				times++;
			}
		}
		setAttr("isOK", true);
		setAttr("intimes", courseTables.size());
		setAttr("outtimes", times);
		renderJson();
	}
	
	/**
	 * 小程序查询课表数据接口
	 */
	public void courseList(){
		String qryType = getPara("qryType");
		String para = getPara("para");
		String str_index_courseTime = getPara("index_courseTime");
		int index_courseTime = 0;
		if((str_index_courseTime!=null && !"".equals(str_index_courseTime))){
			index_courseTime = Integer.parseInt(str_index_courseTime);
		}
		Date date = new Date();
		SimpleDateFormat tmpsdf = new SimpleDateFormat("yyyy-MM-dd");
		Paras tmpparasDate = parasService.selectMember("currentDate");
		try {
			date = tmpsdf.parse(tmpparasDate.getValue());
		} catch (ParseException e) {
			e.printStackTrace();
		}
//		int weekCount = ToolClass.getWeekCount(date);
		Paras tmpparas = parasService.selectMember("currentWeekCount");
		int weekCount = Integer.parseInt(tmpparas.getValue());
//		if(weekCount == 26){
//			weekCount++;
//		}
		String week = ToolClass.getWeekOfDate(new Date());
		String[] weeksub = Arrays.copyOfRange(weeks, ToolClass.getIndex(weeks, week), 7);
		String operator = getPara("operator");
		String babyName = getPara("babyName");
		User user = new User();
		user.setName(operator);
		user.setBabyName(babyName);
		List<User> userList = userService.selectMember(user);
		if (userList == null || userList.size() != 1) {
			setAttr("isOK", false);
			setAttr("erroMsg", "用户信息有误！");
			renderJson();
			return;
		} 
		User users = userList.get(0);
		//获取最大周数参数
		Paras paras = parasService.selectMember("maxReserveWeekCount");
		//创建返回的list
		List<CourseTableList> courseTableLists = new ArrayList<CourseTableList>();
		//获取所有上课时间
		//List<CourseTime> CourseTimeList = courseTimeService.getCourseTimeList();
		String week1 = "";
		List<CourseTable> CourseTimeLists = null;
		if("time".equals(qryType)){
			week1 = weeksub[Integer.parseInt(para)];
			CourseTimeLists = service.getCourseTimeLists(qryType,week1, users);
		}else{
			CourseTimeLists = service.getCourseTimeLists(qryType,para, users);
		}
		
		CourseTableList courseTableList = null;
		int i = 0;
		if(CourseTimeLists!=null && CourseTimeLists.size()>0){
			for (CourseTable CourseTimeList : CourseTimeLists) {
				Date date2 = ToolClass.getDateAndTime(new Date(), CourseTimeList.getBeginTime());
				//Date date2 = CourseTimeList.getDate();
				Date date1 = null;
				if("time".equals(qryType)){
					date1 = ToolClass.getDate(date,weekCount, ToolClass.getIndex(weeks, week1));
					Calendar cal = Calendar.getInstance();
					if(date1.after(new Date()) || (ToolClass.compareDate(date1, new Date())  &&  date2.after(new Date()))){
						courseTableList = new CourseTableList();
						//设置值
						courseTableList.setId(i+"");
						courseTableList.setCourseTime(CourseTimeList.getCourseTime());
						if(i == index_courseTime)
							courseTableList.setOpen(true);
						else
							courseTableList.setOpen(false);
						//创建返回的list中对象的list
						List<CourseList> courses = null;
						//获取具体时间段的课程
						List<CourseTable> courseTables = service.getCourseTableList(qryType,week1,CourseTimeList.getCourseTime(),users,weeksub);
						courses = new ArrayList<CourseList>();
						for (CourseTable courseTable : courseTables) {
							CourseList courseList = new CourseList();
							//设置值
							courseList.setId(courseTable.getId());
							courseList.setUrl("/pages/courseDetail/courseDetail?name="+courseTable.getCourse()+"&id="+courseTable.getId());
							if(courseTable.getCourse().contains("-")){
								courseTable.setCourse(StringUtils.substringAfter(courseTable.getCourse(), "-"));
							}
							if(courseTable.getWeekCount()<Integer.parseInt(paras.getValue()) && courseTable.getAllowFixed()){
								courseList.setFixedFlag("固");
							}else{
								courseList.setFixedFlag("临");
							}
							courseList.setUrl(courseList.getUrl()+"&fixedFlag="+courseList.getFixedFlag());
							//if("time".equals(qryType)){
								courseList.setPara(courseTable.getCourse()+"("+courseTable.getTeacher1()+","+courseTable.getTeacher2()+")");
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
								Date date3 = ToolClass.getDate(date, weekCount, ToolClass.getIndex(weeks, courseTable.getWeek()));
								String time = courseTable.getWeek()+"("+sdf.format(date3)+")";
								courseList.setTime(time);
							//}
							courseList.setRemainNumber(courseTable.getMaxNumber()-courseTable.getStuNumber());
							courses.add(courseList);
						}
						courseTableList.setCourses(courses);
						courseTableLists.add(courseTableList);
						i++;
					}
				}else{
					courseTableList = new CourseTableList();
					//设置值
					courseTableList.setId(i+"");
					courseTableList.setCourseTime(CourseTimeList.getCourseTime());
					if(i == index_courseTime)
						courseTableList.setOpen(true);
					else
						courseTableList.setOpen(false);
					//创建返回的list中对象的list
					List<CourseList> courses = null;
					//获取具体时间段的课程
					List<CourseTable> courseTables = service.getCourseTableList(qryType,para,CourseTimeList.getCourseTime(),users,weeksub);
					courses = new ArrayList<CourseList>();
					for (CourseTable courseTable : courseTables) {
						CourseList courseList = new CourseList();
						//设置值
						courseList.setId(courseTable.getId());
						courseList.setUrl("/pages/courseDetail/courseDetail?name="+courseTable.getCourse()+"&id="+courseTable.getId());
						if(courseTable.getCourse().contains("-")){
							courseTable.setCourse(StringUtils.substringAfter(courseTable.getCourse(), "-"));
						}
						if(courseTable.getWeekCount()<Integer.parseInt(paras.getValue()) && courseTable.getAllowFixed()){
							courseList.setFixedFlag("固");
						}else{
							courseList.setFixedFlag("临");
						}
						courseList.setUrl(courseList.getUrl()+"&fixedFlag="+courseList.getFixedFlag());
						//if("course".equals(qryType)){
							courseList.setPara(courseTable.getCourse()+"("+courseTable.getTeacher1()+","+courseTable.getTeacher2()+")");
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
							Date date3 = ToolClass.getDate(date, weekCount, ToolClass.getIndex(weeks, courseTable.getWeek()));
							String time = courseTable.getWeek()+"("+sdf.format(date3)+")";
							courseList.setTime(time);
						//}
						courseList.setRemainNumber(courseTable.getMaxNumber()-courseTable.getStuNumber());
						courses.add(courseList);
					}
					courseTableList.setCourses(courses);
					courseTableLists.add(courseTableList);
					i++;
				}
			}
		}else{
			//星期数据
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			for (int j = 0; j < weeksub.length; j++) {
				Date date1 = ToolClass.getDate(date, weekCount, ToolClass.getIndex(weeks, weeksub[j]));
				weeksub[j] = weeksub[j]+"("+sdf.format(date1)+")";
			}
			setAttr("isOK", true);
			setAttr("weeks", weeksub);
			renderJson(); 
			return;
		}
		//星期数据
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for (int j = 0; j < weeksub.length; j++) {
			Date date1 = ToolClass.getDate(date, weekCount, ToolClass.getIndex(weeks, weeksub[j]));
			weeksub[j] = weeksub[j]+"("+sdf.format(date1)+")";
		}
		setAttr("isOK", true);
		setAttr("weeks", weeksub);
		setAttr("list", courseTableLists);
		renderJson(); 
	}
	
	/**
	 * 查询数据
	 */
	public void list(){
		Integer pageSize = getParaToInt("limit", 10);
		int offset = getParaToInt("offset", 0);
		String courseTime = getPara("courseTime");
		String course = getPara("course");
		Boolean enable = getParaToBoolean("enable");
		Boolean allowFixed = getParaToBoolean("allowFixed");
		String realWeekCount = getPara("realWeekCount");
		String weekCount = getPara("weekCount");
		Paras paras1 = parasService.selectMember("minReserveWeekCount");
		if(weekCount != null && !"".equals(weekCount)){
			weekCount = Integer.parseInt(paras1.getValue())+Integer.parseInt(weekCount)-1+"";
		}
		if(realWeekCount != null && !"".equals(realWeekCount)){
			weekCount = realWeekCount;
		}
		String week = getPara("week");
		int pageNumber = offset/pageSize+1;
		Paras paras = parasService.selectMember("maxReserveWeekCount");
		Page<CourseTable> coursePageData = service. paginate(pageNumber, pageSize, courseTime, course, weekCount, week,enable,allowFixed,paras1.getValue(),paras.getValue());
		setAttr("total", coursePageData.getTotalRow());
		setAttr("rows", coursePageData.getList());
		renderJson(); 
	}
	
	/**
	 * 编辑时获取所选id的所以字段的信息
	 */
	public void edit(){
		Integer id = getParaToInt(0);
		if(id != null && id>0){
			CourseTable courseTable = service.getByID(id);
			Paras paras1 = parasService.selectMember("minReserveWeekCount");
			courseTable.setWeekCount(courseTable.getWeekCount()-Integer.parseInt(paras1.getValue())+1);
			setAttr("courseTable", courseTable);
		}
		//获取周数字典
		//本阶段最大周数
		Paras parasMax = parasService.selectMember("maxReserveWeekCount");
		int maxWeekCount = Integer.parseInt(parasMax.getValue());//该值不变
		//本阶段最小周数
		Paras parasMin = parasService.selectMember("minReserveWeekCount");
		int minWeekCount = Integer.parseInt(parasMin.getValue());//该值不变
		//当前周数
		Paras parasCur = parasService.selectMember("currentWeekCount");
		int curWeekCount = Integer.parseInt(parasCur.getValue());//该值不变
		//reserveCourseType
		//当前周数
		Paras parasTyp = parasService.selectMember("currentWeekCount");
		int courseType = Integer.parseInt(parasTyp.getValue());//该值不变
		if(maxWeekCount == minWeekCount && minWeekCount == curWeekCount ) {//本阶段为单周
			if(courseType == 1) {//下阶段为单周放课
				weekCounts = new String[] {"第一周（当前周）", "第二周（下阶段第一周）"};
			}else {//下阶段为四周放课
				weekCounts = new String[] {"第一周（当前周）", "第二周（下阶段第一周）", "第三周（下阶段第二周）", "第四周（下阶段第三周）", "第五周（下阶段第四周）"};
			}
		}else if(maxWeekCount - minWeekCount == 3) {//本阶段为四周放课
			if(courseType == 1) {//下阶段为单周放课
				if(curWeekCount == minWeekCount) {
					weekCounts = new String[] {"第一周（当前周）", "第二周（本阶段第二周）", "第三周（本阶段第三周）", "第四周（本阶段第四周）", "第五周（下阶段第一周）"};
				}else if(curWeekCount == (minWeekCount + 1)) {
					weekCounts = new String[] {"第一周（本阶段第一周）", "第二周（当前周）", "第三周（本阶段第三周）", "第四周（本阶段第四周）", "第五周（下阶段第一周）"};
				}else if(curWeekCount == (minWeekCount + 2)) {
					weekCounts = new String[] {"第一周（本阶段第一周）", "第二周（本阶段第二周）", "第三周（当前周）", "第四周（本阶段第四周）", "第五周（下阶段第一周）"};
				}else {
					weekCounts = new String[] {"第一周（本阶段第一周）", "第二周（本阶段第二周）", "第三周（本阶段第三周）", "第四周（当前周）", "第五周（下阶段第一周）"};
				}
			}else {//下阶段为四周放课
				if(curWeekCount == minWeekCount) {
					weekCounts = new String[] {"第一周（当前周）", "第二周（本阶段第二周）", "第三周（本阶段第三周）", "第四周（本阶段第四周）", "第五周（下阶段第一周）", "第六周（下阶段第二周）", "第七周（下阶段第三周）", "第八周（下阶段第四周）"};
				}else if(curWeekCount == (minWeekCount + 1)) {
					weekCounts = new String[] {"第一周（本阶段第一周）", "第二周（当前周）", "第三周（本阶段第三周）", "第四周（本阶段第四周）", "第五周（下阶段第一周）", "第六周（下阶段第二周）", "第七周（下阶段第三周）", "第八周（下阶段第四周）"};
				}else if(curWeekCount == (minWeekCount + 2)) {
					weekCounts = new String[] {"第一周（本阶段第一周）", "第二周（本阶段第二周）", "第三周（当前周）", "第四周（本阶段第四周）", "第五周（下阶段第一周）", "第六周（下阶段第二周）", "第七周（下阶段第三周）", "第八周（下阶段第四周）"};
				}else {
					weekCounts = new String[] {"第一周（本阶段第一周）", "第二周（本阶段第二周）", "第三周（本阶段第三周）", "第四周（当前周）", "第五周（下阶段第一周）", "第六周（下阶段第二周）", "第七周（下阶段第三周）", "第八周（下阶段第四周）"};
				}
			}
		}
		
		setAttr("weekCounts", weekCounts);
		setAttr("weeks", weeks);
		List<Course> courses = courseService.getAllCourse();
		List<CourseTime> courseTimes = courseTimeService.getAllCourseTime();
		List<Teacher> teachers = teacherService.getAllTeacher();
		List<Classroom> classrooms = classroomService.getAllClassroom();
		setAttr("courseTimes", courseTimes);
		setAttr("courses", courses);
		setAttr("teachers", teachers);
		setAttr("classrooms", classrooms);
		render("form.html");
	}
	
	/**
	 * form表单提交
	 */
	public void submit(){
		String teacherStatus = getPara("teacherStatus");
		CourseTable courseTable = getModel(CourseTable.class, "courseTable");
		if(courseTable.getId() == null || courseTable.getId() <= 0) {//新增
			//同步添加年龄限制字段
			Course course = new Course();
			course.setName(courseTable.getCourse());
			Course courses = courseService.selectMember(course);
			courseTable.setMinAge(courses.getMinAge());
			courseTable.setMaxAge(courses.getMaxAge());
			
			CourseTime courseTime = new CourseTime();
			courseTime.setTime(courseTable.getCourseTime());
			CourseTime courseTimes = courseTimeService.selectMember(courseTime);
			courseTable.setBeginTime(courseTimes.getBeginTime());
			courseTable.setEndTime(courseTimes.getEndTime());
			
			Paras paras1 = parasService.selectMember("minReserveWeekCount");
			courseTable.setWeekCount(Integer.parseInt(paras1.getValue())+courseTable.getWeekCount()-1);
			
			Date date = new Date();
			SimpleDateFormat tmpsdf = new SimpleDateFormat("yyyy-MM-dd");
			Paras tmpparasDate = parasService.selectMember("currentDate");
			try {
				date = tmpsdf.parse(tmpparasDate.getValue());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			int weekIndex = ToolClass.getIndex(weeks, courseTable.getWeek());
			courseTable.setDate(ToolClass.getDateAndTime(ToolClass.getDate(date,courseTable.getWeekCount(),weekIndex), courseTable.getBeginTime()));
		}else {
			String tempweek = getPara("week");
			String tempcourseTime = getPara("courseTime");
			String tempcourse = getPara("course");
			int tempweekCount = Integer.parseInt(getPara("weekCount"));
			int stuNumber = Integer.parseInt(getPara("stuNumber"));
			courseTable.setWeekCount(tempweekCount);
			if(stuNumber > 0) {//week,courseTime,course三个字段未修改
				Paras paras1 = parasService.selectMember("minReserveWeekCount");
				courseTable.setWeekCount(Integer.parseInt(paras1.getValue())+courseTable.getWeekCount()-1);
				courseTable.setWeek(tempweek);
				courseTable.setCourseTime(tempcourseTime);
				courseTable.setCourse(tempcourse);
			}else {
				//同步添加年龄限制字段
				Course course = new Course();
				course.setName(courseTable.getCourse());
				Course courses = courseService.selectMember(course);
				courseTable.setMinAge(courses.getMinAge());
				courseTable.setMaxAge(courses.getMaxAge());
				
				CourseTime courseTime = new CourseTime();
				courseTime.setTime(courseTable.getCourseTime());
				CourseTime courseTimes = courseTimeService.selectMember(courseTime);
				courseTable.setBeginTime(courseTimes.getBeginTime());
				courseTable.setEndTime(courseTimes.getEndTime());
				
				Paras paras1 = parasService.selectMember("minReserveWeekCount");
				courseTable.setWeekCount(Integer.parseInt(paras1.getValue())+courseTable.getWeekCount()-1);
				
				Date date = new Date();
				SimpleDateFormat tmpsdf = new SimpleDateFormat("yyyy-MM-dd");
				Paras tmpparasDate = parasService.selectMember("currentDate");
				try {
					date = tmpsdf.parse(tmpparasDate.getValue());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				int weekIndex = ToolClass.getIndex(weeks, courseTable.getWeek());
				courseTable.setDate(ToolClass.getDateAndTime(ToolClass.getDate(date,courseTable.getWeekCount(),weekIndex), courseTable.getBeginTime()));
			}
		}
		
		courseTable.setUpdateTime(new Date());
		CourseTableService transService = enhance(CourseTableService.class);
		boolean success = transService.doSubmit(courseTable,teacherStatus);
		if(success){
			render("/WEB-INF/view/admin/common/success.html");
		}else{
			setAttr("infoMsg", "操作失败！");
			render("/WEB-INF/view/admin/common/error.html");
		}
	}
	
	/**
	 * 行内编辑处理部分
	 */
	public void save(){
		CourseTable courseTable = getModel(CourseTable.class, "courseTable");
		if(courseTable.getStuNumber()>courseTable.getMaxNumber()) {
			courseTable.setStuNumber(courseTable.getMaxNumber());
		}
		boolean success = service.doSubmit(courseTable,"");
		renderJson(success?Ret.ok():Ret.fail());
	}
	
	/**
	 * 删除
	 */
	public void delete(){
		Ret ret = service.deleteById(getParaToInt(0));
		 renderJson(ret);
	}
	
	/**
	 * 行内切换enable状态
	 */
	public void toggleEnable(){
		Ret ret = service.doToggleEnable(getParaToInt(0));
		 renderJson(ret);
	}
	
	/**
	 * 行内切换allowFixed状态
	 */
	public void toggleAllowFixed(){
		Ret ret = service.doToggleAllowFixed(getParaToInt(0));
		 renderJson(ret);
	}
	
	/**
	 * 根据课程年龄更新课表年龄
	 */
	public void ageUpdate(){
		List<Course> courses = courseService.selectMember();
		int num = 0;
		if(courses != null){
			for (Course course : courses) {
				CourseTable courseTable = new CourseTable();
				courseTable.setCourse(course.getName());
				courseTable.setMinAge(course.getMinAge());
				courseTable.setMaxAge(course.getMaxAge());
				boolean res = service.updateMore(courseTable, "ageUpdate");
				if(res){
					num++;
				}
			}
		}
		if(num == courses.size()){
			render("/WEB-INF/view/admin/common/success.html");
		}else{
			setAttr("infoMsg", "操作失败！");
			render("/WEB-INF/view/admin/common/error.html");
		}
	}

	public List<CourseTable> courseTableCondition() {
		return service.selectMember(null, "courseTableCondition");
	}

	public List<CourseTable> courseTableFixedCondition() {
		return service.selectMember(null, "courseTableFixedCondition");
	}
	
}
