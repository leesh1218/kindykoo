package com.kindykoo.controller.user;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.ehcache.CacheKit;
import com.kindykoo.common.model.Menu;
import com.kindykoo.common.model.Paras;
import com.kindykoo.common.model.Student;
import com.kindykoo.common.model.Teacher;
import com.kindykoo.common.model.User;
import com.kindykoo.common.tool.ToolClass;
import com.kindykoo.controller.menu.MenuService;
import com.kindykoo.controller.paras.ParasService;
import com.kindykoo.controller.student.StudentService;
import com.kindykoo.controller.teacher.TeacherService;

public class UserController extends Controller {
	private static UserService service = UserService.me;
	private static StudentService stuService = StudentService.me;
	private static MenuService menuService = MenuService.me;
	private static TeacherService teaService = TeacherService.me;
	private static ParasService parasService = ParasService.me;

	public void index() {
		render("index.html");
	}
	
	/**
	 * 查询数据
	 */
	public void list(){
		Integer pageSize = getParaToInt("limit", 5);
		int offset = getParaToInt("offset", 0);
		String keyword = getPara("keywords").trim();
		Boolean binded = getParaToBoolean("binded");
		int pageNumber = offset/pageSize+1;
		Page<User> userPageData = service. paginate(pageNumber, pageSize, keyword, binded);
		setAttr("total", userPageData.getTotalRow());
		setAttr("rows", userPageData.getList());
		renderJson(); 
	}
	
	/**
	 * 小程序初始化数据
	 */
	public void initData(){
		Paras paras1 = parasService.selectMember("minReserveWeekCount");
		int index_weekCount = ToolClass.getWeekCount(new Date())-Integer.parseInt(paras1.getValue());
		if(index_weekCount == -1){
			index_weekCount = 0;
		}
		String week = ToolClass.getWeekOfDate(new Date());
		String[] weeksub = Arrays.copyOfRange(ToolClass.weeks, ToolClass.getIndex(ToolClass.weeks, week), 7);
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < weeksub.length; i++) {
			Date date1 = ToolClass.getDate(date, ToolClass.getWeekCount(date), ToolClass.getIndex(ToolClass.weeks, weeksub[i]));
			weeksub[i] = weeksub[i]+"("+sdf.format(date1)+")";
		}
		setAttr("index_weekCount", index_weekCount);
		setAttr("weeks", weeksub);
		setAttr("isOK", true);
		renderJson();
	}
	
	/**
	 * 绑定会员信息
	 */
	@SuppressWarnings("unchecked")
	public void bindUser(){
		String rdSessionKey = getPara("rdSessionKey");
		String bindInfo = getPara("bindInfo");
		String formId = getPara("formId");
		if (rdSessionKey == null || "".equals(rdSessionKey)) {
			setAttr("isOK", false);
			setAttr("erroMsg", "rdSessionKey 为空");
			renderJson();
			return;
		}
		//验证rdSessionKey的有效性
		String openIdInfo = CacheKit.get("kindy20", rdSessionKey);
		if (openIdInfo == null || "".equals(openIdInfo)) {
			setAttr("isOK", false);
			setAttr("erroMsg", "rdSessionKey服务器缓存过期！");
			renderJson();
			return;
		}
		Map<String,String> json = (Map<String, String>) JSONObject.parse(openIdInfo.trim()); 
		String openId = json.get("openId");
		
		User user = new User();
		user.setOpenid(openId);
		List<User> userList = service.selectMember(user);
		if (userList == null || userList.size() != 1) {
			setAttr("isOK", false);
			setAttr("erroMsg", "openId数据库未找到记录！");
			renderJson();
			return;
		} 
		User users = userList.get(0);
		//已经绑定会员信息
		if(users.getBinded()){
			setAttr("isOK", false);
			setAttr("erroMsg", "已经绑定会员信息！");
			renderJson();
			return;
		}
		
		//绑定会员信息和 openId
		//1.获取上送的信息
		Map<String,Object> jsonBindInfo = (Map<String, Object>) JSONObject.parse(bindInfo.trim()); 
		String babyName = jsonBindInfo.get("babyName").toString();
		String name = jsonBindInfo.get("name").toString();
		String phone = jsonBindInfo.get("phone").toString();
		String role = jsonBindInfo.get("role").toString();

		Student students = null;
		//2.验证会员信息是否存在
		if("家长".equals(role)){
			Student student = new Student();
			student.setName(babyName);
			student.setPhone(phone);
			List<Student> studentList = stuService.selectMember(student);
			if (studentList == null || studentList.size() != 1) {
				setAttr("isOK", false);
				setAttr("erroMsg", "会员信息有误！");
				renderJson();
				return;
			} 
			students = studentList.get(0);
			if (students == null) {
				setAttr("isOK", false);
				setAttr("erroMsg", "请确认填写的信息与在亲亲袋鼠中心办理会员时所登记的信息一致！");
				renderJson();
				return;
			}
			//判断家长姓名是否重复
			user = new User();
			user.setBabyName(babyName);
			user.setName(name);
			user.setRole(role);
			List<User> userList2 = service.selectMember(user);
			if (userList2 != null && userList2.size() == 1 && !(userList2.get(0).getOpenid().equals(openId))) {
				setAttr("isOK", false);
				setAttr("erroMsg", "姓名不允许重复！");
				renderJson();
				return;
			}
			
			users.setBabyName(babyName);
			users.setBabyAge(students.getAge());
		}else if("教师".equals(role)){
			 Teacher teacher = new Teacher();
			 teacher.setName(name);
			 teacher.setPhone(phone);
			 Teacher teachers = teaService.selectMember(teacher);
			 if (teachers == null) {
					setAttr("isOK", false);
					setAttr("erroMsg", "请确认填写的信息与在亲亲袋鼠中心登记的信息一致！");
					renderJson();
					return;
			}
			//判断是否该教师已经绑定
			user = new User();
			user.setName(name);
			user.setPhone(phone);
			user.setRole(role);
			List<User> userList2 = service.selectMember(user);
			if (userList2 != null && userList2.size() > 0) {
				setAttr("isOK", false);
				setAttr("erroMsg", "该教师已经绑定！");
				renderJson();
				return;
			} 
			
			users.setBabyName("");
			users.setBabyAge(0);
		}
		users.setName(name);
		users.setPhone(phone);
		users.setRole(role);
		users.setBinded(true);
		users.setUpdateTime(new Date());
		if(!service.updMember(users)){
			setAttr("isOK", false);
			setAttr("erroMsg", "更新用户信息失败");
			renderJson();
			return;
		}
		Menu menu = new Menu();
		menu.setChannel("小程序");
		menu.setRole(users.getRole());
		List<Menu> menus = menuService.selectMember(menu);
		setAttr("menus", menus);
		setAttr("role", role);
		setAttr("babyName", babyName);
		setAttr("nickName", name);
		setAttr("isOK", true);
		if("家长".equals(role)){
			boolean res = sendBindedInfo(students , formId, name,users);
			if(res){
				System.out.println(name+"绑定"+babyName+"成功，发送服务通知成功！");
			}else{
				System.out.println(name+"绑定"+babyName+"成功，发送服务通知失败！");
			}
		}
		renderJson();
	}
	
	/**
	 * 微信登录
	 */
	@SuppressWarnings("unchecked")
	public void wxLogin() {
		
		System.out.println("Start getSessionKey");
		String code = getPara("code");
		String userInfo = getPara("userInfo");
		if (code == null || "".equals(code)) {
			setAttr("isOK", false);
			setAttr("erroMsg", "code 为空");
			renderJson();
			return;
		}

		Map<String,String> json = getOpenId(code);
		if(json == null)
			return;
		
		String openId = json.get("openid");
		System.out.println("openId==" + openId);
		// (1) 获得sessionkey
		String sessionKey = json.get("session_key");
		System.out.println("sessionKey==" + sessionKey);
		
		User user = new User();
		user.setOpenid(openId);
		List<User> userList = service.selectMember(user);
		if (userList == null || userList.size() != 1) {
			System.out.println("openId不存在，插入数据库");
			Map<String,Object> jsonUser = (Map<String, Object>) JSONObject.parse(userInfo.trim()); 
//			String nickName = jsonUser.get("nickName").toString();
			String avatarUrl = jsonUser.get("avatarUrl").toString();
			String gender = jsonUser.get("gender").toString();
//			user.setName(nickName);
			user.setGender(Integer.parseInt(gender));
			user.setAvatarUrl(avatarUrl);
			user.setBinded(false);
			System.out.println(user.toJson());
			if(!service.addMember(user)){
				setAttr("isOK", false);
				setAttr("erroMsg", "openId插入数据库失败");
				renderJson();
				return;
			}
			System.out.println("插入数据库成功");
			setAttr("binded", false);
		}else{
			User users = userList.get(0);
			setAttr("binded", users.getBinded());
			if(users.getBinded()){
				Menu menu = new Menu();
				menu.setChannel("小程序");
				menu.setRole(users.getRole());
				List<Menu> menus = menuService.selectMember(menu);
				setAttr("menus", menus);
				setAttr("role", users.getRole());
				setAttr("babyName", users.getBabyName());
				setAttr("nickName", users.getName());
			}
		}
		
		// (2) 得到sessionkey以后存到缓存
		// (3) 首先根据openId，取出来之前存的openId对应的sessionKey的值。
		String oldSeesionKey = CacheKit.get("kindy20", openId);
		if (oldSeesionKey != null && !"".equals(oldSeesionKey)) {
			System.out.println("oldSeesionKey==" + oldSeesionKey);
			// (4) 删除之前openId对应的缓存
			CacheKit.remove("kindy20", oldSeesionKey);
		}
		// (5) 开始缓存新的sessionKey： key --> uuid， value --> sessionObj
		//key值采用不会重复的uuid
		String rsession = UUID.randomUUID().toString();
		JSONObject sessionObj = new JSONObject();
		sessionObj.put("openId", openId);
		sessionObj.put("sessionKey", sessionKey);
		CacheKit.put("kindy20", rsession, sessionObj.toJSONString());
		// (6) 开始缓存新的openId与session对应关系 ： key --> openId , value --> rsession
		CacheKit.put("kindy20", openId, rsession);
		String newOpenId = CacheKit.get("kindy20", openId);
		String newrSession = CacheKit.get("kindy20", rsession);
		System.out.println("新的openId==" + newOpenId);
		System.out.println("新的newrSession==" + newrSession);
		// (7) 把新的sessionKey返回给小程序
		setAttr("rdSessionKey", rsession);
		setAttr("isOK", true);

		Paras paras1 = parasService.selectMember("minReserveWeekCount");
		int index_weekCount = ToolClass.getWeekCount(new Date())-Integer.parseInt(paras1.getValue());
		if(index_weekCount == -1){
			index_weekCount = 0;
		}
		String week = ToolClass.getWeekOfDate(new Date());
		String[] weeksub = Arrays.copyOfRange(ToolClass.weeks, ToolClass.getIndex(ToolClass.weeks, week), 7);
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < weeksub.length; i++) {
			Date date1 = ToolClass.getDate(date, ToolClass.getWeekCount(date), ToolClass.getIndex(ToolClass.weeks, weeksub[i]));
			weeksub[i] = weeksub[i]+"("+sdf.format(date1)+")";
		}
		setAttr("index_weekCount", index_weekCount);
		setAttr("weeks", weeksub);
		setAttr("isOK", true);
		renderJson();
		
	}
	
	@SuppressWarnings("unchecked")
	public String getAccessTtoken(){
		// 首先根据key=access_token，取出来之前存的access_token对应的值。
		String oldAccess_token = CacheKit.get("access_token", "access_token");
		if (oldAccess_token != null && !"".equals(oldAccess_token)) {
			System.out.println("oldAccess_token==" + oldAccess_token);
			//直接返回
			return oldAccess_token;
		}
		
		String url = "https://api.weixin.qq.com/cgi-bin/token";
		String httpUrl = url + "?appid=" + ToolClass.APPID + "&secret=" + ToolClass.SECRETAPP + "&grant_type=client_credential";
		String ret = HttpKit.get(httpUrl);
		if (ret == null || "".equals(ret)) {
			return null;
		}
		Map<String,String> json = (Map<String, String>) JSONObject.parse(ret.trim()); 
		String access_token = json.get("access_token");
		System.out.println("access_token==" + access_token);
		
		// 开始缓存access_token
		CacheKit.put("access_token", "access_token", access_token);
		
		return access_token;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,String> getOpenId(String code){
		String url = "https://api.weixin.qq.com/sns/jscode2session";
		String httpUrl = url + "?appid=" + ToolClass.APPID + "&secret=" + ToolClass.SECRETAPP + "&js_code=" + code
				+ "&grant_type=authorization_code";
		String ret = HttpKit.get(httpUrl);
		if (ret == null || "".equals(ret)) {
			return null;
		}
		Map<String,String> json = (Map<String, String>) JSONObject.parse(ret.trim()); 
		return json;
	}

	@SuppressWarnings("unchecked")
	public boolean sendModelMsg(String access_token,String data){
		String url = "https://api.weixin.qq.com/cgi-bin/message/wxopen/template/send";
		String httpUrl = url + "?access_token=" + access_token;
		String ret = HttpKit.post(httpUrl, data);
		if (ret == null || "".equals(ret)) {
			return false;
		}
		Map<String,Object> json = (Map<String, Object>) JSONObject.parse(ret.trim());
		int errcode = (int)json.get("errcode");
		if(errcode != 0){
			System.out.println("-------"+json.toString());
			return false;
		}
		return true;
	}
	
	/**
	 * 发送约课成功消息
	 * @param babyName
	 * @param id
	 * @param formId
	 * @param user 
	 * @return
	 */
	private boolean sendBindedInfo(Student students, String formId, String operator, User users) {
		Student subStudent = null;
		if("子用户".equals(students.getMainUserFlag())){
			//子用户信息
			subStudent = students;
			//获取主用户信息
			Student student = new Student();
			student.setName(students.getMainUserName());
			student.setPhone(users.getPhone());
			List<Student> studentList = stuService.selectMember(student);
			if (studentList == null || studentList.size() != 1) {
				return false;
			} 
			students = studentList.get(0);
			students.setName(subStudent.getName());
		}
		
		String access_token = getAccessTtoken();
		if(access_token == null || "".equals(access_token))
			return false;
		User user = new User();
		user.setBabyName(students.getName());
		user.setName(operator);
		List<User> userList = service.selectMember(user);
		if (userList == null || userList.size() != 1) {
			return false;
		} 
		user = userList.get(0);
		String openId = user.getOpenid();
		System.out.println("openId==" + openId);
		JSONObject reqData = new JSONObject();
		reqData.put("touser", openId);
		reqData.put("template_id", ToolClass.bindedModelID);
//		reqData.put("page", "userinfo");
		reqData.put("form_id", formId);
		JSONObject data = new JSONObject();
		JSONObject keyword1 = new JSONObject();
		keyword1.put("value", students.getName());
		data.put("keyword1", keyword1);
		JSONObject keyword2 = new JSONObject();
		keyword2.put("value", students.getVipType());
		data.put("keyword2", keyword2);
		JSONObject keyword3 = new JSONObject();
		keyword3.put("value", students.getVipCardNo());
		data.put("keyword3", keyword3);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
//		String beginDate = sdf.format(students.getBeginDate());
//		JSONObject keyword4 = new JSONObject();
//		keyword4.put("value", beginDate);
//		data.put("keyword4", keyword4);
		String endDate = sdf.format(students.getEndDate());
		JSONObject keyword4 = new JSONObject();
		keyword4.put("value", endDate);
		data.put("keyword4", keyword4);
		JSONObject keyword5 = new JSONObject();
		String remainCourseCount = "无限制";
		if("课时卡".equals(students.getVipType())){
			remainCourseCount = students.getRemainCourseCount()+"节课";
		}
		keyword5.put("value", remainCourseCount);
		data.put("keyword5", keyword5);
		
		reqData.put("data", data);
		return sendModelMsg(access_token,reqData.toString());
	}
	
	/**
	 * 行内切换enable状态
	 */
	public void toggleBinded(){
		Ret ret = service.doToggleBinded(getParaToInt(0));
		 renderJson(ret);
	}
	
}
