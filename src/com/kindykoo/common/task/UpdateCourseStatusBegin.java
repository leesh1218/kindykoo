package com.kindykoo.common.task;

import com.kindykoo.controller.courseTable.CourseTableService;
import com.kindykoo.controller.logs.LogsService;
import com.kindykoo.controller.reserveCourse.ReserveCourseService;

/**
 * 定时更新课程状态（上课中，未确认）
 * @author leeshua
 *
 */
public class UpdateCourseStatusBegin implements Runnable {
	private static ReserveCourseService reserveCourseService = ReserveCourseService.me;
	private static CourseTableService courseTableService = CourseTableService.me;

	@Override
	public void run() {
		String info = "UpdateCourseStatusBegin";
		String sql1 = "update reserveCourse set STATUS='上课中' where date<=now() and STATUS='已预约'";
		int count1 = reserveCourseService.UpdateReserveCourseStatus(sql1);
		String sql2 = "update courseTable set STATUS='上课中' where date<=now() and STATUS='未上课'";
		int count2 = courseTableService.UpdateCourseTableStatus(sql2);
		System.out.println(info+" count1="+count1+" count2="+count2);
		if(count1 > 0 || count2 > 0){
			LogsService.insert(info+" count1="+count1+" count2="+count2);
		}
	}

}
