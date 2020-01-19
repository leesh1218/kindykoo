package com.kindykoo.controller.student;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.core.Controller;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.kindykoo.common.model.CourseDetail;
import com.kindykoo.common.model.Paras;
import com.kindykoo.common.model.Student;
import com.kindykoo.common.model.User;
import com.kindykoo.common.tool.ToolClass;
import com.kindykoo.controller.courseTable.CourseTableService;
import com.kindykoo.controller.logs.LogsService;
import com.kindykoo.controller.paras.ParasService;
import com.kindykoo.controller.user.UserService;

public class StudentController extends Controller {
	private static StudentService service = StudentService.me;
	private static UserService userService = UserService.me;
	private static ParasService parasService = ParasService.me;

	/**
	 * 直接访问student进入list.jsp
	 */
	public void index() {
		render("index.html");
	}
	
	/**
	 * 查询数据
	 */
	public void list(){
		Integer pageSize = getParaToInt("limit", 10);
		int offset = getParaToInt("offset", 0);
		String keyword = getPara("keywords").trim();
		Boolean enable = getParaToBoolean("enable");
		String mainUserFlag = getPara("mainUserFlag");
		String vipType = getPara("vipType");
		int pageNumber = offset/pageSize+1;
		Page<Student> studentPageData = service. paginate(pageNumber, pageSize, keyword, enable, mainUserFlag, vipType);
		setAttr("total", studentPageData.getTotalRow());
		setAttr("rows", studentPageData.getList());
//		setAttr("maxYear", PropKit.get("maxYear"));
		renderJson(); 
	}
	
	public void userinfo(){
		String operator = getPara("operator");
		String babyName = getPara("babyName");
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
		//查看学员信息
		Student student = new Student();
		student.setName(users.getBabyName());
		student.setPhone(users.getPhone());
		List<Student> studentList = service.selectMember(student);
		if (studentList == null || studentList.size() != 1) {
			setAttr("isOK", false);
			setAttr("infoMsg", "会员信息有误！");
			renderJson();
			return;
		} 
		Student students = studentList.get(0);
		if(students == null){
			setAttr("isOK", false);
			setAttr("infoMsg", "student未找到记录，如有问题请联系客服！");
			renderJson();
			return;
		}
		Map<String,Object> map = null;
		try {
			map = convertBean(students);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IntrospectionException e) {
			e.printStackTrace();
		}
		List<CourseDetail> courseDetails = new ArrayList<CourseDetail>();
		if("子用户".equals(students.getMainUserFlag())){
			String[] names = {"name","nickName","age","birthday","sex","mainUserFlag","mainUserName","weekReserveCount","weekMaxCount","enable"};
			for (String name : names) {
				CourseDetail courseDetail = new CourseDetail();
				courseDetail.setName(name);
				if("age".equals(name)){
					int age = Integer.parseInt(map.get(name).toString());
					map.put("age", ToolClass.getStringAge(age));
				}
				courseDetail.setValue(map.get(name).toString());
				courseDetails.add(courseDetail);
			}
		}else{
			String[] names = {"name","nickName","age","birthday","sex","mainUserFlag","vipCardNo","vipType","endDate","disableCourseCount","useCourseCount","courseCount","weekReserveCount","weekMaxCount","enable"};
			for (String name : names) {
				CourseDetail courseDetail = new CourseDetail();
				courseDetail.setName(name);
				if("age".equals(name)){
					int age = Integer.parseInt(map.get(name).toString());
					if(age<12){
						map.put("age", new StringBuilder(age).append("个月"));
					}else if(Integer.parseInt(map.get(name).toString())%12!=0){
						map.put("age", new StringBuilder((Math.round(age/12)+"")).append("岁").append(age%12).append("个月"));
					}else{
						map.put("age", new StringBuilder(Math.round(age/12)+"").append("岁"));
					}
				}
				courseDetail.setValue(map.get(name).toString());
				courseDetails.add(courseDetail);
			}
		}
		setAttr("list", courseDetails);
		setAttr("mainUserFlag", students.getMainUserFlag());
		setAttr("vipType", students.getVipType());
		renderJson();
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
	 * 编辑时获取所选id的所有字段的信息
	 */
	public void edit(){
		Integer id = getParaToInt(0);
		if(id != null && id>0){
			setAttr("student", service.getByID(id));
		}
		render("form.html");
	}
	
	/**
	 * form表单提交
	 */
	public void submit(){
		String oldPhone = getPara("oldPhone");
		Student student = getModel(Student.class, "student");
		int age = 0;
		try {
			age = service.getAge(student.getBirthday());
			if(student.getId() == null || student.getId() <= 0 || age != student.getAge()){
				student.setAge(age);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		StudentService transService = enhance(StudentService.class);
		boolean success = transService.doSubmit(student,oldPhone);
		if(success){
			User user = new User();
			user.setBabyName(student.getName());
			user.setPhone(student.getPhone());
			List<User> userList = userService.selectMember(user);
			if(userList != null && userList.size()>0){
				for (User user2 : userList) {
					if(user2.getBabyAge() != age){
						user2.setBabyAge(age);
						userService.updMember(user2);
					}
				}
			}
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
		Student student = getModel(Student.class, "student");
		Student students = Student.dao.findById(student.getId());
		if(student.getRemainCourseCount() != students.getRemainCourseCount()) {
			Paras paras = parasService.selectMember("remainCountFlag");
			int remainCourseCountFlag = Integer.parseInt(paras.getValue());//该值不变
			if(remainCourseCountFlag <= 0) {
				renderJson(Ret.fail());
				return;
			}
			if(student.getRemainCourseCount() > students.getRemainCourseCount() || (students.getRemainCourseCount()-student.getRemainCourseCount()>remainCourseCountFlag)) {
				renderJson(Ret.fail());
				return;
			}
			student.setUseCourseCount(students.getCourseCount()-student.getRemainCourseCount());
			LogsService.insert("update remainCourseCount name="+ students.getName()+ " from " +students.getRemainCourseCount()+" to "+ student.getRemainCourseCount());
		}
		
		if(!"主用户".equals(students.getMainUserFlag()) || !"课时卡".equals(students.getVipType())) {
			renderJson(Ret.fail());
			return;
		}
		if(student.getWeekMaxCount() < students.getWeekReserveCount() ) {
			renderJson(Ret.fail());
			return;
		}else if(student.getWeekMaxCount() == students.getWeekReserveCount() ) {
			student.setEnable(false);
		}else {
			student.setEnable(true);
		}
		student.setUpdateTime(new Date());
		boolean success = service.update(student);
		renderJson(success?Ret.ok():Ret.fail());
	}
	
	/**
	 * 校验用户是否存在
	 */
	public void checkBabyName(){
		Ret ret =  Ret.fail();
		String babyName = getPara("babyName");
		Student student = new Student();
		student.setName(babyName);
		student.setRemarks("checkBabyName");
		List<Student> studentList = service.selectMember(student);
		if (studentList == null || studentList.size()==0) {
			ret.set("infoMsg", "会员不存在！");
			renderJson(ret);
			return;
		} 
		Student students = studentList.get(0);
		if(students!=null){
			ret =  Ret.ok();
			ret.set("vipType", students.getVipType());
		}
		renderJson(ret);
	}
	
	/**
	 * 校验主用户是否存在
	 */
	public void checkMainUserName(){
		Ret ret =  Ret.fail();
		String mainUserName = getPara("mainUserName");
		Student student = new Student();
		student.setName(mainUserName);
		student.setRemarks("checkMainUserName");
		List<Student> studentList = service.selectMember(student);
		if (studentList == null  || studentList.size()==0) {
			ret.set("infoMsg", "主用户不存在！");
			renderJson(ret);
			return;
		} 
		Student students = studentList.get(0);
		if(students!=null){
			ret =  Ret.ok();
			ret.set("vipType", students.getVipType());
//			ret.set("phone", students.getPhone());
		}
		renderJson(ret);
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
	 * 更新会员年龄
	 */
	public void studentAgeUpdate(){
		this.studentAgeUpdateCore();
		render("/WEB-INF/view/admin/common/success.html");
	}
	
	public void studentAgeUpdateCore(){
		//更新会员年龄
		List<Student> list = service.selectMember();
		List<Student> list2 = new ArrayList<>();
		List<User> list3 = new ArrayList<>();
		for (Student student : list) {
			try {
				int age = service.getAge(student.getBirthday());
				if(student.getAge() != age){
					student.setAge(age);
					list2.add(student);
					User user = new User();
					user.setBabyName(student.getName());
					user.setPhone(student.getPhone());
					List<User> userList = userService.selectMember(user);
					if(userList != null && userList.size()>0){
						for (User user2 : userList) {
							if(user2.getBabyAge() != age){
								user2.setBabyAge(age);
								list3.add(user2);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(list2.size()>0){
			Db.batchUpdate(list2, list2.size());
		}
		if(list3.size()>0){
			Db.batchUpdate(list3, list3.size());
		}
		System.out.println("总会员数："+list.size()+" 更新会员年龄  "+list2.size()+" 条记录");
		System.out.println("更新用户年龄  "+list3.size()+" 条记录");
	}
}
