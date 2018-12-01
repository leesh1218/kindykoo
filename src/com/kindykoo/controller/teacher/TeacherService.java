package com.kindykoo.controller.teacher;

import java.util.Date;
import java.util.List;

import com.jfinal.kit.Ret;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.kindykoo.common.model.Course;
import com.kindykoo.common.model.Student;
import com.kindykoo.common.model.Teacher;
import com.kindykoo.controller.teacher.TeacherService;

public class TeacherService {

	public static final TeacherService me = new TeacherService();
	private static final Teacher dao = new Teacher().dao();
	
	/**
	 * 分页查询
	 * @param pageNumber
	 * @param pageSize
	 * @param keyword	关键词查询
	 * @param enable	是否启用
	 * @return
	 */
	public Page<Teacher> paginate(int pageNumber, int pageSize, String keyword, Boolean enable){
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
			.append("age like '%").append(keyword).append("%' or ")
			.append("phone like '%").append(keyword).append("%' or ")
			.append("sex like '%").append(keyword).append("%' or ")
			.append("email like '%").append(keyword).append("%'")
			.append(")");
		}
		if(where.length()>0){
			where.insert(0, "from teacher where");
			where.append(" order by id desc");
		}else{
			where.append("from teacher order by id desc");
		}
		return dao.paginate(pageNumber, pageSize, "select id,name,age,sex,phone,email,entryDate,remarks,enable", where.toString());
	}

	/**
	 * form提交处理
	 * @param teacher
	 * @return
	 */
	public boolean doSubmit(Teacher teacher) {
		if(teacher.getId() == null || teacher.getId() <= 0){
			return save(teacher);
		}else{
			return update(teacher);
		}
	}

	/**
	 * 修改数据
	 * @param teacher
	 * @return
	 */
	private boolean update(Teacher teacher) {
		teacher.setUpdateTime(new Date());
		teacher.update();
		return true;
	}

	/**
	 * 新增数据
	 * @param teacher
	 * @return
	 */
	private boolean save(Teacher teacher) {
		teacher.setEnable(true);
		teacher.save();
		return true;
	}

	/**
	 * 查找指定id的数据
	 * @param id
	 * @return
	 */
	public Teacher getByID(Integer id) {
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
		int count = Db.update("update teacher set enable = (!enable), updateTime = NOW() where id = ?", id);
		if(count==1){
			return Ret.ok();
		}else{
			return Ret.fail();
		}
	}

	public List<Teacher> getAllTeacher() {
		List<Teacher> teachers = dao.find("select name from teacher where enable=true");
		return teachers;
	}

	public Teacher selectMember(Teacher teacher) {
		String sql = "";
		if(teacher.getPhone()!=null && !"".equals(teacher.getPhone())){
			sql = "select * from teacher where name ='"+teacher.getName()+"' and phone ='"+teacher.getPhone()+"'";
		}else{
			sql = "select * from teacher where name ='"+teacher.getName()+"'";
		}
		List<Teacher> teachers = dao.find(sql);
		if(teachers == null || teachers.size() == 0)
			return null;
		else
			return teachers.get(0);
	}
}
