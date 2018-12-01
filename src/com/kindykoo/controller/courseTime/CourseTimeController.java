package com.kindykoo.controller.courseTime;


import com.jfinal.core.Controller;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.kindykoo.common.model.CourseTime;
import com.kindykoo.controller.courseTime.CourseTimeService;

public class CourseTimeController extends Controller {

	private static CourseTimeService service = CourseTimeService.me;

	/**
	 * 直接访问teacher进入list.jsp
	 */
	public void index() {
		render("index.html");
	}
	
	/**
	 * 查询数据
	 */
	public void list(){
		Integer pageSize = getParaToInt("limit", 10);
		int offset = getParaToInt("offset", 0);
		String keyword = getPara("keywords").trim();
		Boolean enable = getParaToBoolean("enable");
		int pageNumber = offset/pageSize+1;
		Page<CourseTime> teacherPageData = service. paginate(pageNumber, pageSize, keyword, enable);
		setAttr("total", teacherPageData.getTotalRow());
		setAttr("rows", teacherPageData.getList());
		renderJson(); 
	}
	
	/**
	 * 编辑时获取所选id的所以字段的信息
	 */
	public void edit(){
		Integer id = getParaToInt(0);
		if(id != null && id>0){
			setAttr("courseTime", service.getByID(id));
		}
		render("form.html");
	}
	
	/**
	 * form表单提交
	 */
	public void submit(){
		CourseTime courseTime = getModel(CourseTime.class, "courseTime");
		boolean success = service.doSubmit(courseTime);
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
		CourseTime courseTime = getModel(CourseTime.class, "courseTime");
		boolean success = service.doSubmit(courseTime);
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
