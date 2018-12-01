package com.kindykoo.controller.paras;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
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
	 * 每四周更新参数
	 * @param string
	 * @param string2
	 * @return
	 */
	public int updateWeekCount() {
		String sql = "update paras set value=value+4 where name='minReserveWeekCount' or name='maxReserveWeekCount'";
		int count = Db.update(sql);
		return count;
	}
}
