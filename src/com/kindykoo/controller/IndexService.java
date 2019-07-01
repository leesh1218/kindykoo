package com.kindykoo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.plugin.activerecord.Db;
import com.kindykoo.common.model.Paras;
import com.kindykoo.controller.paras.ParasService;

public class IndexService {
	
	public static final IndexService me = new IndexService();
	private static ParasService parasService = ParasService.me;
	

	public Map<String, Integer> summaryQry() {
		
		Paras tmpparas = null;
		
		tmpparas = parasService.selectMember("currentWeekCount");
		int weekCount = Integer.parseInt(tmpparas.getValue());
		tmpparas = parasService.selectMember("maxReserveWeekCount");
		int maxWeekCount = Integer.parseInt(tmpparas.getValue());
		tmpparas = parasService.selectMember("minReserveWeekCount");
		int minWeekCount = Integer.parseInt(tmpparas.getValue());
		
		//2019-07-01 修复查询出去年同样周数约课记录的问题
		Paras paraDate = parasService.selectMember("currentDate");
		String currentDateStr = paraDate.getValue();
		
		String sql_1 = "select sum(weekReserveCount) from student";
		String sql_2 = "select sum(disableCourseCount) as id from student";
		String sql_3 = "select sum(stuNumber) as id from courseTable where weekCount="+weekCount;
		String sql_4 = "select sum(stuNumber) as id from courseTable where weekCount>="+minWeekCount+" and weekCount<="+maxWeekCount;
		String sql_5 = "select count(*) as id from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and status='已预约' and weekCount="+weekCount;
		String sql_6 = "select count(*) as id from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and status='上课中' and weekCount="+weekCount;
		String sql_7 = "select count(*) as id from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and status='未确认' and weekCount="+weekCount;
		String sql_8 = "select count(*) as id from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and status='已确认' and weekCount="+weekCount;
		String sql_9 = "select count(*) as id from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and status='已预约' and weekCount>="+minWeekCount+" and weekCount<="+maxWeekCount;
		String sql_10 = "select count(*) as id from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and status='未确认' and weekCount>="+minWeekCount+" and weekCount<="+maxWeekCount;
		String sql_11 = "select count(*) as id from reserveCourse where date > STR_TO_DATE('"+currentDateStr+"','%Y-%m-%d') and status='已确认' and weekCount>="+minWeekCount+" and weekCount<="+maxWeekCount;
		
		Map<String,Integer> maps = new HashMap<>();
		List<Object> sql_1_str = Db.query(sql_1);
		int weekReserveCount_sum = Integer.parseInt(sql_1_str.get(0).toString());
		List<Object>  sql_2_str = Db.query(sql_2);
		int disableCourseCount_sum = Integer.parseInt(sql_2_str.get(0).toString());
		List<Object>  sql_3_str = Db.query(sql_3);
		int stuNumber_sum = Integer.parseInt(sql_3_str.get(0).toString());
		List<Object>  sql_4_str = Db.query(sql_4);
		int stuNumber_sumAll = Integer.parseInt(sql_4_str.get(0).toString());
		List<Object>  sql_5_str = Db.query(sql_5);
		int reserveCourse_sum = Integer.parseInt(sql_5_str.get(0).toString());
		List<Object>  sql_6_str = Db.query(sql_6);
		int reserveCourse_sum_ing = Integer.parseInt(sql_6_str.get(0).toString());
		List<Object>  sql_7_str = Db.query(sql_7);
		int reserveCourse_sum_ed = Integer.parseInt(sql_7_str.get(0).toString());
		List<Object>  sql_8_str = Db.query(sql_8);
		int reserveCourse_sum_confirmed = Integer.parseInt(sql_8_str.get(0).toString());
		List<Object>  sql_9_str = Db.query(sql_9);
		int reserveCourse_sumAll = Integer.parseInt(sql_9_str.get(0).toString());
		List<Object>  sql_10_str = Db.query(sql_10);
		int reserveCourse_sum_edAll = Integer.parseInt(sql_10_str.get(0).toString());
		List<Object>  sql_11_str = Db.query(sql_11);
		int reserveCourse_sum_confirmedAll = Integer.parseInt(sql_11_str.get(0).toString());
		
		maps.put("weekReserveCount_sum", weekReserveCount_sum);
		maps.put("disableCourseCount_sum", disableCourseCount_sum);
		maps.put("stuNumber_sum", stuNumber_sum);
		maps.put("stuNumber_sumAll", stuNumber_sumAll);
		maps.put("reserveCourse_sum", reserveCourse_sum);
		maps.put("reserveCourse_sum_ing", reserveCourse_sum_ing);
		maps.put("reserveCourse_sum_ed", reserveCourse_sum_ed);
		maps.put("reserveCourse_sum_confirmed", reserveCourse_sum_confirmed);
		maps.put("reserveCourse_sumAll", reserveCourse_sumAll);
		maps.put("reserveCourse_sum_edAll", reserveCourse_sum_edAll);
		maps.put("reserveCourse_sum_confirmedAll", reserveCourse_sum_confirmedAll);
		
		return maps;
	}
	
	

}
