package com.kindykoo.controller.student;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.Ret;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kindykoo.common.model.ReserveCourse;
import com.kindykoo.common.model.Student;
import com.kindykoo.common.model.User;
import com.kindykoo.controller.logs.LogsService;
import com.kindykoo.controller.reserveCourse.ReserveCourseService;
import com.kindykoo.controller.user.UserService;

public class StudentService {
	public static final StudentService me = new StudentService();
	private static final ReserveCourseService reserveCourseService = ReserveCourseService.me;
	private static final UserService userService = UserService.me;
	private static final Student dao = new Student().dao();
	private static final int WeekMaxCount = 4;
	
	/**
	 * 分页查询
	 * @param pageNumber
	 * @param pageSize
	 * @param keyword	关键词查询
	 * @param enable	是否启用
	 * @param vipType 
	 * @param mainUserFlag 
	 * @return
	 */
	public Page<Student> paginate(int pageNumber, int pageSize, String keyword, Boolean enable, String mainUserFlag, String vipType){
		StringBuilder where = new StringBuilder();
		if(enable != null){
			where.append(" enable = ").append(enable);
		}
		if(StrKit.notBlank(keyword)){
			if(where.length() > 0){
				where.append(" and ");
			}
			where.append(" (")
			.append("name like '%").append(keyword).append("%' or ")
			.append("nickName like '%").append(keyword).append("%' or ")
			.append("contact like '%").append(keyword).append("%' or ")
			.append("phone like '%").append(keyword).append("%' or ")
			.append("mainUserName like '%").append(keyword).append("%' ")
			.append(")");
		}
		if(StrKit.notBlank(mainUserFlag)){
			if(where.length() > 0){
				where.append(" and ");
			}
			where.append(" mainUserFlag = '").append(mainUserFlag).append("' ");
		}
		if(StrKit.notBlank(vipType)){
			if(where.length() > 0){
				where.append(" and ");
			}
			where.append(" vipType = '").append(vipType).append("' ");
		}
		if(where.length()>0){
			where.insert(0, "from student where ");
			where.append(" order by weekReserveCount desc");
		}else{
			where.append("from student order by id desc");
		}
		return dao.paginate(pageNumber, pageSize, "select * ", where.toString());
	}

	/**
	 * form提交处理
	 * @param phoneChange 
	 * @param teacher
	 * @return
	 */
	@Before(Tx.class)
	public boolean doSubmit(Student student, String oldPhone) {
		student.setUpdateTime(new Date());
		if(student.getId() == null || student.getId() <= 0){
			return save(student);
		}else{
			String newPhone = student.getPhone();
			if(!oldPhone.equals(newPhone)) {//手机号改变，需要同步修改约课记录表和用户表
				//修改约课记录表
				ReserveCourse reserveCourse = new ReserveCourse();
				reserveCourse.setStudentName(student.getName());
				reserveCourse.setPhone(oldPhone);
				List<ReserveCourse> reserveCourses = reserveCourseService.selectMember(reserveCourse, "doSubmit_phone");
				if(reserveCourses != null && reserveCourses.size() > 0) {
					List<ReserveCourse> list = new ArrayList<>();
					for (ReserveCourse reserveCourse2 : reserveCourses) {
						reserveCourse2.setPhone(newPhone);
						list.add(reserveCourse2);
					}
					Db.batchUpdate(list, list.size());
					String info = "oldPhone="+oldPhone+" newPhone="+student.getPhone()+" ReserveCourse batch update "+list.size()+"条记录";
					LogsService.insert(info);
				}
				//修改用户表
				User user = new User();
				user.setBabyName(student.getName());
				user.setPhone(oldPhone);
				List<User> users = userService.selectMember(user);
				if(users != null && users.size() > 0) {
					List<User> list = new ArrayList<>();
					for (User user2 : users) {
						user2.setPhone(newPhone);
						list.add(user2);
					}
					Db.batchUpdate(list, list.size());
					String info = "oldPhone="+oldPhone+" newPhone="+newPhone+" user batch update "+list.size()+"条记录";
					LogsService.insert(info);
				}
			}
			return update(student);
		}
	}

	/**
	 * 修改数据
	 * @param teacher
	 * @return
	 */
	public boolean update(Student student) {
		if("课时卡".equals(student.getVipType())){
			student.setCourseCount(student.getVipCourseCount()+student.getPresentCourseCount());
			student.setRemainCourseCount(student.getCourseCount()-student.getUseCourseCount());
			if(student.getRemainCourseCount() <= 0){
				student.setEnable(false);
			}
		}
		student.setUpdateTime(new Date());
		return student.update();
	}

	/**
	 * 新增数据
	 * @param teacher
	 * @return
	 */
	private boolean save(Student student) {
		student.setRegisterDate(new Date());
		student.setUseCourseCount(0);
		student.setWeekMaxCount(StudentService.WeekMaxCount);
		student.setWeekReserveCount(0);
		student.setEnable(true);
		if("主用户".equals(student.getMainUserFlag())){
			student.setDisableCourseCount(0);
			if("课时卡".equals(student.getVipType())){
				student.setWeekMaxCount(99);//无意义，代表不限制周约课次数
				student.setActivityCourseCount(0);
				student.setCourseCount(student.getVipCourseCount()+student.getPresentCourseCount());
				student.setRemainCourseCount(student.getCourseCount()-student.getUseCourseCount());
			}
		}else if("子用户".equals(student.getMainUserFlag())){
			if("课时卡".equals(student.getVipType())){
				student.setWeekMaxCount(99);//无意义，代表不限制周约课次数
			}
			student.setVipType(null);
		}else{
			System.out.println("主用户标志不对");
			return false;
		}
		student.save();
		return true;
	}

	/**
	 * 查找指定id的数据
	 * @param id
	 * @return
	 */
	public Student getByID(Integer id) {
		return dao.findById(id);
	}

	/**
	 * 删除数据
	 * @param id
	 * @return
	 */
	public Ret deleteById(Integer id) {
		boolean success = dao.deleteById(id);
		if(success){
			return Ret.ok();
		}else{
			return Ret.fail();
		}
	}

	/**
	 * 切换enable状态
	 * @param id
	 * @return
	 */
	public Ret doToggleEnable(Integer id) {
		int count = Db.update("update student set enable = (!enable), updateTime = NOW() where id = ?", id);
		if(count==1){
			return Ret.ok();
		}else{
			return Ret.fail();
		}
	}

	public List<Student> selectMember(Student student) {
		String sql = "";
		if(student.getPhone() != null && !"".equals(student.getPhone())){
			if(student.getMainUserName()!=null && !"".equals(student.getMainUserName())){
				sql = "select * from student where name='"+student.getMainUserName()+"' and phone= '"+student.getPhone()+"'";
			}else{
				sql = "select * from student where name='"+student.getName()+"' and phone= '"+student.getPhone()+"'";
			}
		}else if("checkMainUserName".equals(student.getRemarks())){
			sql = "select * from student where name='"+student.getName()+"' and mainUserFlag = '主用户'";
		}else if("checkBabyName".equals(student.getRemarks())){
			sql = "select * from student where name='"+student.getName()+"' and enable = true";
		}else{
			sql = "select * from student where name='"+student.getName()+"'";
		}
		return dao.find(sql);
	}
	
	/**
	 * 
	 * @param student
	 * @return
	 */
	public List<Student> selectMemberTemp(Student student) {
		String sql = "select * from student where (name='"+student.getName()+"' or mainUserName='"+student.getName()+"') and phone= '"+student.getPhone()+"'";
		return dao.find(sql);
	}
	
	public List<Student> selectMemberTest(Student student) {
			String sql = "select * from student where name='"+student.getName()+"'";
			return dao.find(sql);
	}
	
	public List<Student> selectMember() {
		String sql = "select * from student order by id asc";
		List<Student> students = dao.find(sql);
		if(students == null || students.size() == 0)
			return null;
		else
			return students;
	}
	
	public List<Student> selectMemberTest() {
		String sql = "select * from student where mainUserFlag='主用户' order by id asc";
		List<Student> students = dao.find(sql);
		if(students == null || students.size() == 0)
			return null;
		else
			return students;
	}
	
	//由出生日期获得年龄  
    public  int getAge(Date birthDay) throws Exception {  
        Calendar cal = Calendar.getInstance();  
  
        if (cal.before(birthDay)) {  
            throw new IllegalArgumentException(  
                    "The birthDay is before Now.It's unbelievable!");  
        }  
        int yearNow = cal.get(Calendar.YEAR);  
        int monthNow = cal.get(Calendar.MONTH);  
        cal.setTime(birthDay);  
  
        int yearBirth = cal.get(Calendar.YEAR);  
        int monthBirth = cal.get(Calendar.MONTH);  
  
        int age = (yearNow - yearBirth)*12;  
  
        if (monthNow <= monthBirth) {  
        	age = age-(monthBirth-monthNow);  
        }else{  
            age = age + (monthNow-monthBirth);  
        }  
      
        return age;  
    }

    /**
     * 初始化周预约课时数
     * @param sql1
     * @param sql2
     * @param sql3
     * @param sql6 
     * @param sql5 
     * @param sql4 
     * @param sql66 
     * @param sql62 
     * @param sql55 
     * @param sql52 
     * @param sql44 
     */
	public String initWeekReserveCount(int weekCount,String sql1, String sql2, String sql22, String sql3, String sql33, String sql4, String sql44, String sql5, String sql55, String sql6, String sql66) {
		int count1 = Db.update(sql1);
		int count2 = Db.update(sql2);
		int count22 = Db.update(sql22);
		int count3= Db.update(sql3);
		int count33= Db.update(sql33);
		int count4= Db.update(sql4);
		int count44= Db.update(sql44);
		int count5= Db.update(sql5);
		int count55= Db.update(sql55);
		int count6= Db.update(sql6);
		int count66= Db.update(sql66);
		String info = "weekCount="+weekCount+" count1="+count1+" count2="+count2+" count22="+count22+" count3="+count3+" count33="+count33+" count4="+count4+" count44="+count44+" count5="+count5+" count55="+count55+" count6="+count6+" count66="+count66;
		System.out.println(info);
		return info;
	}

}
