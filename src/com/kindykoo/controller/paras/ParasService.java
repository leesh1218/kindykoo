package com.kindykoo.controller.paras;

import java.util.List;

import com.jfinal.kit.Ret;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.kindykoo.common.model.Paras;

public class ParasService {
	public static final ParasService me = new ParasService();
	private static final Paras dao = new Paras().dao();
	
	/**
	 * 查询参数值
	 * @param courseTable
	 * @return
	 */
	public Paras selectMember(String name) {
		List<Paras> list = dao.find("select * from paras "
				+ "where enable = true and name='"+name+"'");
		if(list == null || list.size() == 0)
			return null;
		else
			return list.get(0);
	}
	
	/**
	 * 分页查询
	 * @param pageNumber
	 * @param pageSize
	 * @param keyword	关键词查询
	 * @param enable	是否启用
	 * @return
	 */
	public Page<Paras> paginate(int pageNumber, int pageSize, String keyword, Boolean enable){
		StringBuilder where = new StringBuilder();
		if(enable != null){
			where.append(" enable = ").append(enable);
		}
		if(StrKit.notBlank(keyword)){
			if(where.length() > 0){
				where.append(" and ");
			}
			where.append(" (")
			.append("name like '%").append(keyword).append("%' ")
//			.append("desc like '%").append(keyword).append("%'")
			.append(")");
		}
		if(where.length()>0){
			where.insert(0, "from paras where");
			where.append(" order by name asc");
		}else{
			where.append("from paras order by name asc");
		}
		return dao.paginate(pageNumber, pageSize, "select * ", where.toString());
	}

	/**
	 * 每四周更新参数
	 * @param reserveCourseTypePara 
	 * @param minWeekCount 
	 * @param maxWeekCount 
	 * @param minWeekCount 
	 * @param maxWeekCount 
	 * @param string
	 * @param string2
	 * @return
	 */
	public int updateWeekCount(int reserveCourseTypePara, int maxWeekCount, int minWeekCount) {
		int count2 = 0;
		String sql = "update paras set value=value+4 where name='minReserveWeekCount' or name='maxReserveWeekCount'";
		if(reserveCourseTypePara == 1) {//1-单周放课
			sql = "update paras set value='"+(maxWeekCount+1)+"' where name='minReserveWeekCount' or name='maxReserveWeekCount'";
		}else if(maxWeekCount - minWeekCount ==0) {//上一阶段是单周约课
			sql = "update paras set value='"+(maxWeekCount+1)+"' where name='minReserveWeekCount'";
			String sql2 = "update paras set value=value+4 where name='maxReserveWeekCount'";
			count2 = Db.update(sql2);
		}
		
		int count = Db.update(sql);
		return count2+count;
	}

	public int updateCurrentWeekCount(int weekCount) {
		String sql = "update paras set value="+(weekCount+1)+" where name='currentWeekCount'";
		int count = Db.update(sql);
		return count;
	}

	public int updateCurrentDate(String date) {
		String sql = "update paras set value='"+date+"' where name='currentDate'";
		int count = Db.update(sql);
		return count;
	}
	
	/**
	 * 查找指定id的数据
	 * @param id
	 * @return
	 */
	public Paras getByID(Integer id) {
		return dao.findById(id);
	}
	
	public Ret doToggleEnable(Integer id) {
		int count = Db.update("update paras set enable = (!enable) where id = ?", id);
		if(count==1){
			return Ret.ok();
		}else{
			return Ret.fail();
		}
	}
	/**
	 * 修改数据
	 * @param course
	 * @return
	 */
	private boolean update(Paras paras) {
		paras.update();
		return true;
	}

	/**
	 * 新增数据
	 * @param course
	 * @return
	 */
	private boolean save(Paras paras) {
		paras.setEnable(true);
		paras.save();
		return true;
	}

	public boolean doSubmit(Paras paras) {
		if(paras.getId() == null || paras.getId() <= 0){
			return save(paras);
		}else{
			if("reserveCourseType".equals(paras.getName().trim()) && !"0".equals(paras.getValue()) && !"1".equals(paras.getValue())) {//1-单周放课，0-放课四周
				return false;
			}
			return update(paras);
		}
	}

	public List<Paras> selectMember(Paras paras) {
		String sql = "select * from paras where name='"+paras.getName()+"' and enable = true";
		return dao.find(sql);
	}

	public int updateInitWeekFlag(int initWeekFlag) {
		String sql = "update paras set value="+initWeekFlag+" where name='initWeekFlag'";
		int count = Db.update(sql);
		return count;
	}
}
