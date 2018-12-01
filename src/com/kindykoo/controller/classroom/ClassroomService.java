package com.kindykoo.controller.classroom;

import java.util.Date;
import java.util.List;

import com.jfinal.kit.Ret;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.kindykoo.common.model.Classroom;
import com.kindykoo.controller.classroom.ClassroomService;

public class ClassroomService {

	public static final ClassroomService me = new ClassroomService();
	private static final Classroom dao = new Classroom().dao();
	
	/**
	 * 分页查询
	 * @param pageNumber
	 * @param pageSize
	 * @param keyword	关键词查询
	 * @param enable	是否启用
	 * @return
	 */
	public Page<Classroom> paginate(int pageNumber, int pageSize, String keyword, Boolean enable){
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
			.append("remarks like '%").append(keyword).append("%'")
			.append(")");
		}
		if(where.length()>0){
			where.insert(0, "from classroom where");
			where.append(" order by id desc");
		}else{
			where.append("from classroom order by id desc");
		}
		return dao.paginate(pageNumber, pageSize, "select id,name,updateTime,remarks,enable", where.toString());
	}

	/**
	 * form提交处理
	 * @param classroom
	 * @return
	 */
	public boolean doSubmit(Classroom classroom) {
		if(classroom.getId() == null || classroom.getId() <= 0){
			return save(classroom);
		}else{
			return update(classroom);
		}
	}

	/**
	 * 修改数据
	 * @param classroom
	 * @return
	 */
	private boolean update(Classroom classroom) {
		classroom.setUpdateTime(new Date());
		classroom.update();
		return true;
	}

	/**
	 * 新增数据
	 * @param classroom
	 * @return
	 */
	private boolean save(Classroom classroom) {
		classroom.setEnable(true);
		classroom.save();
		return true;
	}

	/**
	 * 查找指定id的数据
	 * @param id
	 * @return
	 */
	public Classroom getByID(Integer id) {
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
		int count = Db.update("update classroom set enable = (!enable), updateTime = NOW() where id = ?", id);
		if(count==1){
			return Ret.ok();
		}else{
			return Ret.fail();
		}
	}

	public List<Classroom> getAllClassroom() {
		List<Classroom> classrooms = dao.find("select name from classroom where enable=true");
		return classrooms;
	}
}
