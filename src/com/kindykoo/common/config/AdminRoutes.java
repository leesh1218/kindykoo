package com.kindykoo.common.config;

import com.jfinal.config.Routes;
import com.kindykoo.controller.Index2Controller;
import com.kindykoo.controller.IndexController;
import com.kindykoo.controller.classroom.ClassroomController;
import com.kindykoo.controller.course.CourseController;
import com.kindykoo.controller.courseTable.CourseTableController;
import com.kindykoo.controller.courseTime.CourseTimeController;
import com.kindykoo.controller.paras.ParasController;
import com.kindykoo.controller.reserveCourse.ReserveCourseController;
import com.kindykoo.controller.student.StudentController;
import com.kindykoo.controller.teacher.TeacherController;
import com.kindykoo.controller.user.UserController;

public class AdminRoutes extends Routes {

	@Override
	public void config() {
		// TODO Auto-generated method stub
		setBaseViewPath("WEB-INF/view/admin");
		add("/admin", IndexController.class,"/index");
		add("/admin/student", StudentController.class,"/student"); 
		add("/admin/teacher", TeacherController.class,"teacher"); 
		add("/admin/course", CourseController.class,"/course"); 
		add("/admin/courseTime", CourseTimeController.class,"/courseTime"); 
		add("/admin/classroom", ClassroomController.class,"/classroom"); 
		add("/admin/courseTable", CourseTableController.class,"/courseTable"); 
		add("/admin/reserveCourse", ReserveCourseController.class,"/reserveCourse"); 
		add("/admin/login", UserController.class,"/login"); 
		add("/admin/user", UserController.class,"/user"); 
		add("/admin/paras", ParasController.class,"/paras"); 
	}

}
