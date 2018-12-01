package com.kindykoo.controller.user;

import java.util.List;

import com.jfinal.kit.Ret;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.kindykoo.common.model.User;

public class UserService {
	public static final UserService me = new UserService();
	private static final User dao = new User().dao();

	/**
	 * 查找数据是否已存在
	 * @param user
	 * @return
	 */
	public List<User> selectMember(User user) {
		StringBuilder where = new StringBuilder();
		if(StrKit.notBlank(user.getOpenid())){
			where.append(" openid = '").append(user.getOpenid()).append("'");
		}
		if(StrKit.notBlank(user.getBabyName())){
			if(where.length()>0){
				where.append(" and ");
			}
			where.append(" babyName = '").append(user.getBabyName()).append("'");
		}
		if(StrKit.notBlank(user.getPhone())){
			if(where.length()>0){
				where.append(" and ");
			}
			where.append(" phone = '").append(user.getPhone()).append("'");
		}
		if(StrKit.notBlank(user.getName())){
			if(where.length()>0){
				where.append(" and ");
			}
			where.append(" name = '").append(user.getName()).append("'");
		}
		if(StrKit.notBlank(user.getRole())){
			if(where.length()>0){
				where.append(" and ");
			}
			where.append(" role = '").append(user.getRole()).append("'");
		}
		if(user.getBinded() != null){
			if(where.length()>0){
				where.append(" and ");
			}
			where.append(" binded = ").append(user.getBinded());
		}
		where.insert(0, "select * from user where ");
		System.out.println("User.selectMember.where="+where.toString());
		return dao.find(where.toString());
	}

	/**
	 * 新增数据
	 * @param user
	 * @return
	 */
	public boolean addMember(User user) {
		return user.save();
	}

	/**
	 * 更新数据
	 * @param user
	 * @return
	 */
	public boolean updMember(User user) {
		return user.update();
	}

	public Page<User> paginate(int pageNumber, Integer pageSize, String keyword, Boolean binded) {
		StringBuilder where = new StringBuilder();
		if(binded != null){
			where.append(" binded = ").append(binded);
		}
		if(StrKit.notBlank(keyword)){
			if(binded != null){
				where.append(" and ");
			}
			where.append("(")
			.append("babyName like '%").append(keyword).append("%' or ")
			.append("name like '%").append(keyword).append("%'")
			.append(")");
		}
		if(where.length()>0){
			where.insert(0, "from user where");
			where.append(" order by id asc");
		}else{
			where.append("from user order by id desc");
		}
		return dao.paginate(pageNumber, pageSize, "select * ", where.toString());
	}

	public Ret doToggleBinded(Integer id) {
		int count = Db.update("update user set binded = false, updateTime = NOW() where id = ?", id);
		if(count==1){
			return Ret.ok();
		}else{
			return Ret.fail();
		}
	}

}
