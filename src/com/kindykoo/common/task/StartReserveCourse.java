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
		System.out.println("StartReserveCourse "+info);
		LogsService.insert("StartReserveCourse "+info);
		
		//更新initWeekFlag为0-未执行
		int tmpInitWeekFlagCount = parasService.updateInitWeekFlag(0);
		if(tmpInitWeekFlagCount == 1) {//更新成功
			info = "weekCount="+weekCount+" update initWeekFlag success and initWeekFlag=0";
		}else {
			info = "weekCount="+weekCount+" update initWeekFlag fail and initWeekFlag=1";
		}
		System.out.println("StartReserveCourse "+info);
		LogsService.insert("StartReserveCourse "+info);
	}

}
