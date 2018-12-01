package com.kindykoo.controller.course;



import java.util.List;

import com.jfinal.core.Controller;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.kindykoo.common.model.Course;
import com.kindykoo.controller.course.CourseService;

public class CourseController extends Controller {

	private static CourseService service = CourseService.me;

	/**
	 * 直接访问course进入list.jsp
	 */
	public void index() {
		List<Course> courseTypes = service.getCourseType();
		setAttr("courseTypes", courseTypes);
		render("index.html");
	}
	
	/**
	 * 查询数据
	 */
	public void list(){
		Integer pageSize = getParaToInt("limit", 10);
		int offset = getParaToInt("offset", 0);
		String keyword = getPara("keywords").trim();
		String type = getPara("type");
		
		int pageNumber = offset/pageSize+1;
		Page<Course> coursePageData = service. paginate(pageNumber, pageSize, keyword, type);
		setAttr("total", coursePageData.getTotalRow());
		setAttr("rows", coursePageData.getList());
		renderJson(); 
	}
	
	/**
	 * 编辑时获取所选id的所以字段的信息
	 */
	public void edit(){
		Integer id = getParaToInt(0);
		if(id != null && id>0){
			setAttr("course", service.getByID(id));
		}
		List<Course> courseTypes = service.getCourseType();
		setAttr("courseTypes", courseTypes);
		render("form.html");
	}
	
	/**
	 * form表单提交
	 */
	public void submit(){
		Course course = getModel(Course.class, "course");
		boolean success = service.doSubmit(course);
		if(success){
			render("/WEB-INF/view/admin/common/success.html");
		}else{
			setAttr("infoMsg", "操作失败！");
			render("/WEB-INF/view/admin/common/error.html");
		}
	}
	
	/**
	 * 行内编辑处理部分
	 */
	public void save(){
		Course course = getModel(Course.class, "course");
		boolean success = service.doSubmit(course);
		renderJson(success?Ret.ok():Ret.fail());
	}
	
	/**
	 * 删除
	 */
	public void delete(){
		Ret ret = service.deleteById(getParaToInt(0));
		 renderJson(ret);
	}
	
	/**
	 * 行内切换enable状态
	 */
	public void toggleEnable(){
		Ret ret = service.doToggleEnable(getParaToInt(0));
		 renderJson(ret);
	}
}
