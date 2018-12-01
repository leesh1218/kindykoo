package com.kindykoo.controller.logs;

import com.jfinal.plugin.activerecord.Db;
import com.kindykoo.common.model.Logs;

public class LogsService {

	public static final LogsService me = new LogsService();
	private static final Logs dao = new Logs().dao();
	
	public static void insert(String info) {
		String sql = "insert into logs (`time`,`desc`) values (NOW(),'"+info+"')";
		Db.update(sql);
	}
	
}
