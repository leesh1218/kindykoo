package com.kindykoo.common.task;

import com.kindykoo.controller.courseTable.CourseTableService;
import com.kindykoo.controller.logs.LogsService;
import com.kindykoo.controller.reserveCourse.ReserveCourseService;

/**
 * 定时更新课程状态（上课中，未确认）
 * @author leeshua
 *
 */
public class UpdateCourseStatusEnd implements Runnable {

	private static ReserveCourseService reserveCourseService = ReserveCourseService.me;
	private static CourseTableService courseTableService = CourseTableService.me;

	@Override
	public void run() {
		String info = "UpdateCourseStatusEnd";
		String sql1 = "update reserveCourse set STATUS='未确认' where date<=date_sub(now(), interval 45 minute) and STATUS='上课中'";
		int count1 = reserveCourseService.UpdateReserveCourseStatus(sql1);
		String sql2 = "update courseTable set STATUS='未确认',allowFixed=0,enable=0 where date<=date_sub(now(), interval 45 minute) and STATUS='上课中'";
		int count2 = courseTableService.UpdateCourseTableStatus(sql2);
		System.out.println(info+" count1="+count1+" count2="+count2);
		if(count1 > 0 || count2 > 0){
			LogsService.insert(info+" count1="+count1+" count2="+count2);
		}
	}
}
