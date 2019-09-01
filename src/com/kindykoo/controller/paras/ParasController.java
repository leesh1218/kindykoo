package com.kindykoo.controller.paras;

import java.util.List;

import com.jfinal.core.Controller;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.kindykoo.common.model.Paras;
import com.kindykoo.common.model.Student;

public class ParasController  extends Controller {

	private static ParasService service = ParasService.me;
	
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
		Page<Paras> parasPageData = service. paginate(pageNumber, pageSize, keyword, enable);
		setAttr("total", parasPageData.getTotalRow());
		setAttr("rows", parasPageData.getList());
		renderJson(); 
	}
	
	/**
	 * 编辑时获取所选id的所有字段的信息
	 */
	public void edit(){
		Integer id = getParaToInt(0);
		if(id != null && id>0){
			setAttr("paras", service.getByID(id));
		}
		render("form.html");
	}
	
	/**
	 * form表单提交
	 */
	public void submit(){
		Paras paras = getModel(Paras.class, "paras");
		boolean success = service.doSubmit(paras);
		if(success){
			render("/WEB-INF/view/admin/common/success.html");
		}else{
			setAttr("infoMsg", "操作失败！");
			render("/WEB-INF/view/admin/common/error.html");
		}
	}
	
	/**
	 * 校验参数名称是否存在
	 */
	public void checkName(){
		Ret ret =  Ret.fail();
		String name = getPara("name").trim();
		Paras paras = new Paras();
		paras.setName(name);
		List<Paras> parasList = service.selectMember(paras);
		if (parasList == null || parasList.size()==0) {
			ret =  Ret.ok();
		} else {
			ret =  Ret.fail();
			ret.set("infoMsg", "参数已存在！");
		}
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
