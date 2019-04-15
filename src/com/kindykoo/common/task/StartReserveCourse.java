package com.kindykoo.common.task;


import com.kindykoo.common.model.Paras;
import com.kindykoo.controller.courseTable.CourseTableService;
import com.kindykoo.controller.logs.LogsService;
import com.kindykoo.controller.paras.ParasService;

public class StartReserveCourse  implements Runnable {

	private static ParasService parasService = ParasService.me;
	private static CourseTableService courseTableService = CourseTableService.me;
	
	@Override
	public void run() {
		String info = "";
//		int weekCount = ToolClass.getWeekCount(new Date());
		Paras tmpparas = parasService.selectMember("currentWeekCount");
		int weekCount = Integer.parseInt(tmpparas.getValue());
		Paras paras = parasService.selectMember("maxReserveWeekCount");
		int maxWeekCount = Integer.parseInt(paras.getValue());
		
		//启用所有课程的预约
		String sql = "update courseTable set enable=1 where stuNumber < maxNumber and enable=0 and weekCount >= "+weekCount+" and weekCount <= "+maxWeekCount;
		info = courseTableService.initWeekReserveCount(weekCount,sql);
		LogsService.insert("StartReserveCourse "+info);
	}

}
