/**
 * 后台管理使用js
 */
$(function(){
	$("#admin_nav li").on("click",function(){
		$("#admin_nav li.active").removeClass("active");
		$(this).addClass("active");
	});
});
