package com.kindykoo.controller.courseTable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.Ret;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kindykoo.common.model.CourseTable;
import com.kindykoo.common.model.ReserveCourse;
import com.kindykoo.common.model.User;
import com.kindykoo.common.tool.ToolClass;
import com.kindykoo.controller.courseTable.CourseTableService;
import com.kindykoo.controller.logs.LogsService;
import com.kindykoo.controller.reserveCourse.ReserveCourseService;

public class CourseTableService {

	public static final CourseTableService me = new CourseTableService();
	private static final ReserveCourseService reserveCourseService = ReserveCourseService.me;
	private static final CourseTable dao = new CourseTable().dao();
	
	/**
	 * 分页查询
	 * @param pageNumber
	 * @param pageSize
	 * @param keyword	关键词查询
	 * @param enable	是否启用
	 * @param allowFixed 
	 * @return
	 */
	public Page<CourseTable> paginate(int pageNumber, int pageSize, String courseTime, String course, String weekCount, String week, Boolean enable,Boolean allowFixed, String minReserveWeekCount,String maxReserveWeekCount){
		StringBuilder where = new StringBuilder();
		if(enable != null){
			where.append(" enable = ").append(enable);
		}
		if(allowFixed != null){
			if(where.length() > 0){
				where.append(" and ");
			}
			where.append(" allowFixed = ").append(allowFixed);
		}
		if(StrKit.notBlank(weekCount)){
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
		if(where.length()>0){
			where.insert(0, " from courseTable where");
			where.append(" order by stuNumber desc");
		}else{
			where.append(" from courseTable where weekCount<= ").append(maxReserveWeekCount).append(" and weekCount>= ").append(minReserveWeekCount).append(" order by stuNumber desc");
		}
		return dao.paginate(pageNumber, pageSize, "select * ", where.toString());
	}

	/**
	 * form提交处理
	 * @param courseTable
	 * @param teacherStatus 
	 * @return
	 */
	@Before(Tx.class)
	public boolean doSubmit(CourseTable courseTable, String teacherStatus) {
		boolean res = false;
		if(courseTable.getId() == null || courseTable.getId() <= 0){
			res = save(courseTable);
		}else{
			if(!"".equals(teacherStatus)) {//主教或助教改变，需要同步修改约课记录表
				ReserveCourse reserveCourse = new ReserveCourse();
				reserveCourse.setWeekCount(courseTable.getWeekCount());
				reserveCourse.setWeek(courseTable.getWeek());
				reserveCourse.setCourseTime(courseTable.getCourseTime());
				reserveCourse.setCourse(courseTable.getCourse());
				List<ReserveCourse> reserveCourses = reserveCourseService.selectMember(reserveCourse, "doSubmit");
				if(reserveCourses != null && reserveCourses.size() > 0) {
					List<ReserveCourse> list = new ArrayList<>();
					String teacher1 = courseTable.getTeacher1();
					String teacher2 = courseTable.getTeacher2();
					for (ReserveCourse reserveCourse2 : reserveCourses) {
						boolean change = false;
						if(!teacher1.equals(reserveCourse2.getTeacher1())) {
							reserveCourse2.setTeacher1(teacher1);
							change = true;
						}
						if(!teacher2.equals(reserveCourse2.getTeacher2())) {
							reserveCourse2.setTeacher2(teacher2);
							change = true;
						}
						if(change) {
							list.add(reserveCourse2);
						}
					}
					if(list.size() > 0) {
						Db.batchUpdate(list, list.size());
						String info = "teacher1="+teacher1+" teacher2="+teacher2+" batch update "+list.size()+"条记录";
						LogsService.insert(info);
					}
				}
			}
			res = update(courseTable);
		}
		return res;
	}

	/**
	 * 修改数据
	 * @param courseTable
	 * @return
	 */
	public boolean update(CourseTable courseTable) {
		courseTable.setUpdateTime(new Date());
		courseTable.update();
		return true;
	}
	
	/**
	 * 根据条件更新
	 * @param courseTable
	 * @return
	 */
	public boolean updateMore(CourseTable courseTable,String flag) {
		String sql = "";
		if("ageUpdate".equals(flag)){
			sql = "update courseTable set minAge="+courseTable.getMinAge()+",maxAge="+courseTable.getMaxAge()+" where course='"+courseTable.getCourse()+"'";
		}
		int count = Db.update(sql);
		if(count == -1)
			return false;
		System.out.println("course = "+courseTable.toJson()+" and ageUpdate " +count+ " 条记录");
		return true;
	}

	/**
	 * 新增数据
	 * @param courseTable
	 * @return
	 */
	private boolean save(CourseTable courseTable) {
		courseTable.setAllowFixed(true);
		courseTable.setFixedNum(0);
//		courseTable.setMaxFixedNum(ToolClass.maxFixedNum);
		courseTable.setEnable(true);
		courseTable.setStuNumber(0);
		courseTable.setStatus("未上课");
		courseTable.save();
		return true;
	}

	/**
	 * 查找指定id的数据
	 * @param id
	 * @return
	 */
	public CourseTable getByID(Integer id) {
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
		int count = Db.update("update courseTable set enable = (!enable), updateTime = NOW() where id = ?", id);
		if(count==1){
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
	public Ret doToggleAllowFixed(Integer id) {
		int count = Db.update("update courseTable set allowFixed = (!allowFixed), updateTime = NOW() where id = ?", id);
		if(count==1){
			return Ret.ok();
		}else{
			return Ret.fail();
		}
	}

	/**
	 * 小程序查询课程
	 * @param qryType查询类型（按时间、课程、教师）
	 * @param weekCount周数
	 * @param para//第二个参数
	 * @return
	 */
	public List<CourseTable> getCourseTableList(String qryType, String para,String courseTime,User user,String[] weeksub) {

		List<CourseTable> courseTableList = null;
		int weekCount = ToolClass.getWeekCount(new Date());
//		if(weekCount == 26){
//			weekCount++;
//		}
		StringBuilder where = new StringBuilder("select * from courseTable where weekCount = "+weekCount);
		if("家长".equals(user.getRole())){
			where.append(" and minAge <= "+user.getBabyAge()+" and maxAge >= "+user.getBabyAge());
			where.append(" and date> now() ");
		}
		if(!"".equals(courseTime)){
			where.append(" and courseTime = '"+courseTime+"'");
		}
		if("time".equals(qryType)){
			where.append(" and week = '").append(para).append("'");
		}else{//qryType为course
			where.append(" and course like '%").append(para).append("%'");
		}
		where.append(" order by date asc");
		courseTableList = dao.find(where.toString());
		return courseTableList;
	}

	/**
	 * 小程序查询课程详情
	 * @param id
	 * @return
	 */
	public CourseTable getCourseTable(String id) {
		return dao.findById(id);
	}
	
	/**
	 * 固定课程时查询未来几周的课程
	 * @param courseTable
	 * @return
	 */
	public List<CourseTable> selectMember(CourseTable courseTable, int maxReserveWeekCount) {
		return dao.find("select * from courseTable "
				+ "where allowFixed=true and enable = true and week='"+courseTable.getWeek()+"' and courseTime='"+courseTable.getCourseTime()+"' and course='"+courseTable.getCourse()+"' and weekCount >= "+courseTable.getWeekCount()+" and weekCount <= "+maxReserveWeekCount+" order by weekCount asc");
	}
	
	/**
	 * 查询课表
	 * @param courseTable
	 * @return
	 */
	public List<CourseTable> selectMember(CourseTable courseTable,String flag) {
		String sql = "";
		if("copy".equals(flag)){
			sql = "select * from courseTable where weekCount ="+courseTable.getWeekCount();
		}else if("myConfirmCourses".equals(flag)){
			if(courseTable != null && !"".equals(courseTable.getTeacher2())){
				sql = "select * from courseTable where teacher2 ='"+courseTable.getTeacher2()+"' and weekCount= "+courseTable.getWeekCount()+" and week = '"+courseTable.getWeek()+"'";
			}else{
				sql = "select * from courseTable where status in ('未确认','未上课','上课中') and teacher1 ='"+courseTable.getTeacher1()+"' and weekCount= "+courseTable.getWeekCount()+" and week = '"+courseTable.getWeek()+"'";
			}
		}else if("myConfirmCourses2".equals(flag)){
				sql = "select * from courseTable where status='已确认' and teacher1 ='"+courseTable.getTeacher1()+"' and weekCount= "+courseTable.getWeekCount()+" and week = '"+courseTable.getWeek()+"'";
		}else if("courseTableCondition".equals(flag)){
			sql = "select * from courseTable where stuNumber >= maxNumber and updateTime>date_sub(now(), interval 12 hour)";
		}else if("courseTableFixedCondition".equals(flag)){
			sql = "select * from courseTable where fixedNum >= maxFixedNum and updateTime>date_sub(now(), interval 12 hour)";
		}
		List<CourseTable> list = dao.find(sql);
		if(list == null || list.size() == 0)
			return null;
		else
			return list;
	}

	public List<CourseTable> getCourseTimeLists(String qryType, String para, User user) {
		int weekCount = ToolClass.getWeekCount(new Date());
//		if(weekCount == 26){
//			weekCount++;
//		}
		List<CourseTable> list = null;
		StringBuilder where = new StringBuilder("select distinct(courseTime),beginTime from courseTable "
					+ "where weekCount = "+weekCount);
		if("家长".equals(user.getRole())){
			where.append(" and minAge <= "+user.getBabyAge()+" and maxAge >= "+user.getBabyAge());
		}
		if("time".equals(qryType)){
			if(!"".equals(para)){
				where.append(" and week = '"+para).append("'");
			}
		}else{
			where.append(" and course like '%").append(para).append("%'");
		}
		where.append(" and date> now() order by courseTime asc");
		list = dao.find(where.toString());
		if(list == null || list.size() == 0)
			return null;
		else
			return list;
	}
	
	/**
     * 更新课表状态
     * @param sql
     */
	public int UpdateCourseTableStatus(String sql) {
		int count = Db.update(sql);
		return count;
	}

	public String initWeekReserveCount(int weekCount, String sql) {
		int count= Db.update(sql);
		String info = "CourseTableService.initWeekReserveCount weekCount="+weekCount+" count="+count;
		System.out.println(info);
		return info;
	}
}
