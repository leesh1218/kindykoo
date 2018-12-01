package com.kindykoo.controller.menu;

import java.util.List;

import com.kindykoo.common.model.Menu;

public class MenuService {
	
	public static final MenuService me = new MenuService();
	private static final Menu dao = new Menu().dao();

	
	public List<Menu> selectMember(Menu menu) {
		List<Menu> menus = dao.find("select * from menu where role='"+menu.getRole()+"' and channel='"+menu.getChannel()+"'");
		return menus;
	}
}
