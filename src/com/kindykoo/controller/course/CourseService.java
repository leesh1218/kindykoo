package com.kindykoo.controller.course;

import java.util.Date;
import java.util.List;

import com.jfinal.kit.Ret;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.kindykoo.common.model.Course;
import com.kindykoo.controller.course.CourseService;

public class CourseService {

	public static final CourseService me = new CourseService();
	private static final Course dao = new Course().dao();
	
	/**
	 * 分页查询
	 * @param pageNumber
	 * @param pageSize
	 * @param keyword	关键词查询
	 * @param enable	是否启用
	 * @return
	 */
	public Page<Course> paginate(int pageNumber, int pageSize, String keyword, String type){
		StringBuilder where = new StringBuilder();
		if(StrKit.notBlank(type)){
			where.append(" type = '").append(type).append("'");
		}
		if(StrKit.notBlank(keyword)){
			if(where.length() > 0){
				where.append(" and ");
			}
			where.append("(")
			.append("name like '%").append(keyword).append("%' or ")
			.append("remarks like '%").append(keyword).append("%'")
			.append(")");
		}
		if(where.length()>0){
			where.insert(0, "from course where");
			where.append(" order by id asc");
		}else{
			where.append("from course order by id asc");
		}
		return dao.paginate(pageNumber, pageSize, "select * ", where.toString());
	}

	/**
	 * form提交处理
	 * @param course
	 * @return
	 */
	public boolean doSubmit(Course course) {
		if(course.getId() == null || course.getId() <= 0){
			return save(course);
		}else{
			return update(course);
		}
	}

	/**
	 * 修改数据
	 * @param course
	 * @return
	 */
	private boolean update(Course course) {
		course.setUpdateTime(new Date());
		course.update();
		return true;
	}

	/**
	 * 新增数据
	 * @param course
	 * @return
	 */
	private boolean save(Course course) {
		course.setEnable(true);
		course.save();
		return true;
	}

	/**
	 * 查找指定id的数据
	 * @param id
	 * @return
	 */
	public Course getByID(Integer id) {
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
		int count = Db.update("update course set enable = (!enable), updateTime = NOW() where id = ?", id);
		if(count==1){
			return Ret.ok();
		}else{
			return Ret.fail();
		}
	}

	public Course selectMember(Course course) {
		List<Course> courses = dao.find("select id,name,minAge,maxAge,enable from course where name='"+course.getName()+"'");
		if(courses == null || courses.size() == 0)
			return null;
		else
			return courses.get(0);
	}
	
	public List<Course> selectMember() {
		return dao.find("select * from course");
	}

	public List<Course> getAllCourse() {
		List<Course> courses = dao.find("select name from course where enable=true");
		return courses;
	}

	public List<Course> getCourseType() {
		List<Course> courses = dao.find("select DISTINCT(type) from course where enable=true");
		return courses;
	}
}
