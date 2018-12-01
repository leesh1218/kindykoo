package com.kindykoo.common.config;

import com.jfinal.config.Routes;
import com.kindykoo.controller.IndexController;
import com.kindykoo.controller.classroom.ClassroomController;
import com.kindykoo.controller.course.CourseController;
import com.kindykoo.controller.courseTable.CourseTableController;
import com.kindykoo.controller.courseTime.CourseTimeController;
import com.kindykoo.controller.reserveCourse.ReserveCourseController;
import com.kindykoo.controller.student.StudentController;
import com.kindykoo.controller.teacher.TeacherController;
import com.kindykoo.controller.user.UserController;

public class AdminRoutes extends Routes {

	@Override
	public void config() {
		// TODO Auto-generated method stub
		setBaseViewPath("WEB-INF/view");
		add("/admin", IndexController.class);
		add("/admin/student", StudentController.class); 
		add("/admin/teacher", TeacherController.class); 
		add("/admin/course", CourseController.class); 
		add("/admin/courseTime", CourseTimeController.class); 
		add("/admin/classroom", ClassroomController.class); 
		add("/admin/courseTable", CourseTableController.class); 
		add("/admin/reserveCourse", ReserveCourseController.class); 
		add("/admin/login", UserController.class); 
		add("/admin/user", UserController.class); 
	}

}
