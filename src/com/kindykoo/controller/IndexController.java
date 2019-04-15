package com.kindykoo.controller;

import java.util.Map;

import com.jfinal.core.Controller;

public class IndexController extends Controller {
	
	private static IndexService service = IndexService.me;
	
	public void index(){
		render("index.html");
	}
	
	public void summary(){
		Map<String,Integer> maps = service.summaryQry();
		setAttr("weekReserveCount_sum", maps.get("weekReserveCount_sum"));
		setAttr("disableCourseCount_sum", maps.get("disableCourseCount_sum"));
		setAttr("stuNumber_sum", maps.get("stuNumber_sum"));
		setAttr("stuNumber_sumAll", maps.get("stuNumber_sumAll"));
		setAttr("reserveCourse_sum", maps.get("reserveCourse_sum"));
		setAttr("reserveCourse_sum_ing", maps.get("reserveCourse_sum_ing"));
		setAttr("reserveCourse_sum_ed", maps.get("reserveCourse_sum_ed"));
		setAttr("reserveCourse_sum_confirmed", maps.get("reserveCourse_sum_confirmed"));
		setAttr("reserveCourse_sumAll", maps.get("reserveCourse_sumAll"));
		setAttr("reserveCourse_sum_edAll", maps.get("reserveCourse_sum_edAll"));
		setAttr("reserveCourse_sum_confirmedAll", maps.get("reserveCourse_sum_confirmedAll"));
		render("summary.html");
	}
}
