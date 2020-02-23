package com.kindykoo.common.config;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.JFinal;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.activerecord.tx.TxByMethodRegex;
import com.jfinal.plugin.cron4j.Cron4jPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.template.Engine;
import com.kindykoo.common.model._MappingKit;
import com.kindykoo.common.task.InitWeekReserveCount;
import com.kindykoo.common.task.NotifyReserveCourseCondition;
import com.kindykoo.common.task.StartReserveCourse;
import com.kindykoo.common.task.UpdateAccessToken;

public class MainConfig extends JFinalConfig {
	/**
	 * 配置JFinal常量
	 */
	@Override
	public void configConstant(Constants me) {
		//读取数据库配置文件
		PropKit.use("config.properties");
		//设置当前是否为开发模式
		me.setDevMode(PropKit.getBoolean("devMode"));
		//设置默认上传文件保存路径 getFile等使用
		me.setBaseUploadPath("upload/");
		//设置上传最大限制尺寸
		//me.setMaxPostSize(1024*1024*10);
		//设置默认下载文件路径 renderFile使用
		me.setBaseDownloadPath("download");
		//设置默认视图类型
		//me.setViewType(ViewType.JSP);
		//设置404渲染视图
		//me.setError404View("404.html");
		
		
	}
	/**
	 * 配置JFinal路由映射
	 */
	@Override
	public void configRoute(Routes me) {
		me.add(new AdminRoutes());
		me.add(new FrontRoutes());
	}
	/**
	 * 配置JFinal插件
	 * 数据库连接池
	 * ORM
	 * 缓存等插件
	 * 自定义插件
	 */
	@Override
	public void configPlugin(Plugins me) {
		//配置缓存插件
		me.add(new EhCachePlugin());
		//配置数据库连接池插件
		DruidPlugin dbPlugin=new DruidPlugin(PropKit.get("jdbcUrl"), PropKit.get("user"), PropKit.get("password"));
		dbPlugin.setInitialSize(PropKit.getInt("initialSize"));
		dbPlugin.setMaxActive(PropKit.getInt("maxActive"));
		dbPlugin.setMinIdle(PropKit.getInt("minIdle"));
		//orm映射 配置ActiveRecord插件
		ActiveRecordPlugin arp=new ActiveRecordPlugin(dbPlugin);
		arp.setShowSql(PropKit.getBoolean("devMode"));
		arp.setDialect(new MysqlDialect());
		/********在此添加数据库 表-Model 映射*********/
		_MappingKit.mapping(arp);
		//添加到插件列表中
		me.add(dbPlugin);
		me.add(arp);
		//定时任务插件
		Cron4jPlugin cp = new Cron4jPlugin();
		cp = new Cron4jPlugin("config.txt", "cron4j");
		me.add(cp);
	}
	/**
	 * 配置全局拦截器
	 */
	@Override
	public void configInterceptor(Interceptors me) {
		//me.add(new TxByMethods("save", "update"));
		me.add(new TxByMethodRegex("(trans.*)"));
	}
	/**
	 * 配置全局处理器
	 */
	@Override
	public void configHandler(Handlers me) {
		me.add(new BasePathHandler());
	}
	
	/**
	 * 配置模板引擎 
	 */
	@Override
	public void configEngine(Engine me) {
		//这里只有选择JFinal TPL的时候才用
		//配置共享函数模板
		me.setDevMode(true);
		me.addSharedFunction("/WEB-INF/view/admin/common/innerLayout.html");
		me.addSharedFunction("/WEB-INF/view/admin/common/layerFormLayout.html");
		me.addSharedFunction("/WEB-INF/view/admin/common/_layout.html");
	}
	
	public static void main(String[] args) {
		JFinal.start("WebRoot", 8080, "/");
	}

	@Override
	public void afterJFinalStart() {
		new UpdateAccessToken().run();
		new NotifyReserveCourseCondition().run();
//		new UpdateCourseStatusBegin().run();
//		new UpdateCourseStatusEnd().run();
//		new InitWeekReserveCount().run();
//		new StartReserveCourse().run();
//		new Test().test();
	}
}
