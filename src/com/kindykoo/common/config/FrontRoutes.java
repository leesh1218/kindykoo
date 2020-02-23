package com.kindykoo.common.config;

import com.jfinal.config.Routes;
import com.kindykoo.controller.Index2Controller;

/**
 * 前台路由
 */
public class FrontRoutes extends Routes {

	public void config() {
		setBaseViewPath("/WEB-INF/view");
		add("/", Index2Controller.class,"/index");
	}
}
