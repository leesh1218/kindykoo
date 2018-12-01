package com.kindykoo.controller.courseTime;

import java.util.Date;
import java.util.List;

import com.jfinal.kit.Ret;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.kindykoo.common.model.CourseTime;
import com.kindykoo.controller.courseTime.CourseTimeService;

public class CourseTimeService {

	public static final CourseTimeService me = new CourseTimeService();
	private static final CourseTime dao = new CourseTime().dao();
	
	/**
	 * 分页查询
	 * @param pageNumber
	 * @param pageSize
	 * @param keyword	关键词查询
	 * @param enable	是否启用
	 * @return
	 */
	public Page<CourseTime> paginate(int pageNumber, int pageSize, String keyword, Boolean enable){
		StringBuilder where = new StringBuilder();
		if(enable != null){
			where.append(" enable = ").append(enable);
		}
		if(StrKit.notBlank(keyword)){
			if(enable != null){
				where.append(" and ");
			}
			where.append("(")
			.append("name like '%").append(keyword).append("%' or ")
			.append("time like '%").append(keyword).append("%' or ")
			.append("remarks like '%").append(keyword).append("%'")
			.append(")");
		}
		if(where.length()>0){
			where.insert(0, "from courseTime where");
			where.append(" order by id asc");
		}else{
			where.append("from courseTime order by id asc");
		}
		return dao.paginate(pageNumber, pageSize, "select * ", where.toString());
	}

	/**
	 * form提交处理
	 * @param courseTime
	 * @return
	 */
	public boolean doSubmit(CourseTime courseTime) {
		if(courseTime.getId() == null || courseTime.getId() <= 0){
			return save(courseTime);
		}else{
			return update(courseTime);
		}
	}

	/**
	 * 修改数据
	 * @param courseTime
	 * @return
	 */
	private boolean update(CourseTime courseTime) {
		courseTime.setUpdateTime(new Date());
		courseTime.update();
		return true;
	}

	/**
	 * 新增数据
	 * @param courseTime
	 * @return
	 */
	private boolean save(CourseTime courseTime) {
		courseTime.setEnable(true);
		courseTime.save();
		return true;
	}

	/**
	 * 查找指定id的数据
	 * @param id
	 * @return
	 */
	public CourseTime getByID(Integer id) {
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
		System.out.println("id="+id);
		int count = Db.update("update courseTime set enable = (!enable), updateTime = NOW() where id = ?", id);
		if(count==1){
			return Ret.ok();
		}else{
			return Ret.fail();
		}
	}

	/**
	 * 提供给CourseTableController查询courseTimeList
	 * @return
	 */
	public List<CourseTime> getCourseTimeList() {
		return dao.find("select * from courseTime");
	}

	public CourseTime selectMember(CourseTime courseTime) {
		List<CourseTime> courseTimes = dao.find("select * from courseTime where time='"+courseTime.getTime()+"'");
		if(courseTimes == null || courseTimes.size() == 0)
			return null;
		else
			return courseTimes.get(0);
	}

	public List<CourseTime> getAllCourseTime() {
		List<CourseTime> courseTimes = dao.find("select * from courseTime where enable=true");
		return courseTimes;
	}
}
