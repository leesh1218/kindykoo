
/**
 * 初始化
 */
$(document).ready(function() {
	setCurrentNavMenu();
});

/**
 * 设置当前导航菜单
 */
function setCurrentNavMenu() {
	var pathName = location.pathname;
	if (pathName == '/') {
		// $("#main_menu [home='true']").addClass("active");
		// $("#main_menu a[href='/']").addClass("active");
		$("#main_menu li:first-child a").addClass("active");
		return ;
	}
	
	// jquery 的 each 内 return false 相当于 break; return ture 相当于 continue;
	$("#main_menu a[href]").each(function(index, element) {
		var href = $(element).attr("href");
		if (href == '/' || href == 'https://jfinal.com/' || href == 'https://www.jfinal.com/') {
			return true;		// continue;
		}
		
		if (pathName.indexOf(href) != -1) {
			doSetCurrentNavMenu(href);
			return false;	// break;
		}
		
		var fullPn = "https://jfinal.com" + pathName;
		if (fullPn.indexOf(href) != -1) {
			doSetCurrentNavMenu(href);
			return false;
		}
		
		fullPn = "https://www.jfinal.com" + pathName;
		if (fullPn.indexOf(href) != -1) {
			doSetCurrentNavMenu(href);
			return false;
		}
	});
}

function doSetCurrentNavMenu(href) {
	var currentMenu = $("#main_menu a[href='" + href + "']");
	currentMenu.addClass("active");
}

/**
 * 采用问号挂参的方式，为 a 链接追加 returnUrl 参数
 */
function appendReturnUrl(target) {
	var returnUrl;
	var currentUrl = location.pathname;
	if (currentUrl.indexOf("/login") != 0 && currentUrl.indexOf("/reg") != 0) {
		returnUrl = "?returnUrl=" + currentUrl;
		var link = $(target);
		link.attr("href", link.attr("href") + returnUrl);
	}
	//else {
	//	if (location.search) {
	//		returnUrl =  location.search;
	//	} else {
	//		return ;
	//	}
	//}
	//var link = $(target);
	//link.attr("href", link.attr("href") + returnUrl);
}

/**
 * 退出登录
 */
function logout() {
	if (confirm('确定要退出登录？')) {
		location.href = '/logout';
	} 
}

/**
 * textarea 高度根据内容自适应
 * 
 * textarea 不要设置 margin 值，否则 IE 下的 scrollHeight 会包含该值，用外部嵌套div来布局
 * @param ele 必须是 textarea，并且在外部需要将 overflow 设置为 hidden
 * @param minHeight 最小高度值
 */
function autoHeight(ele, minHeight) {
	minHeight = minHeight || 16;
	// ele.style.height = minHeight + "px";
	if (ele.style.height) {
		ele.style.height = (parseInt(ele.style.height) - minHeight) + "px";
	}
	ele.style.height = ele.scrollHeight + "px";

	// 返回了: 29  30 30，后两个始终比前一个大一个 px，经测试前都就是少了一个px的border-bottom
	// alert(ele.clientHeight + " : " +ele.scrollHeight + " : " + ele.offsetHeight);
	// 或许这个 currHeight 留着有点用
	// ele.currHeight = ele.style.height;
}

//share、feedback 详情页回复功能
function reply(url, articleId, map) {
	if (map.isLoading) {
		return ;
	}

	$.ajax(url, {
		type: "POST"
		, cache: false
		, dataType: "json"
		, data: {
			articleId: articleId,
			replyContent: $('#replyContent').val()
		}
		, beforeSend: function() {
			map.isLoading = true;
			map.submit_btn.hide();
			map.submit_loading.show();
		}
		, success: function(ret) {
			if (ret.state == "ok") {
				// var replyContent = $('#replyContent');
				// replyContent.val("");
				// 数据清空后，高度重置一下，注意高度与 css 文件中保持一致
				// replyContent.css({height:"30px"});
				// 插入刚刚回复的内容 replyItem
				// TODO 考虑用 news feed 模块的定位方案来改进一下，更优雅
				// $(".jf-reply-list > li:last-child").before(ret.replyItem);	// jfinal.com-3.0
				
				var replyContent = map.submit_btn.parent().find("textarea");
				replyContent.val("");
				// 数据清空后，高度重置一下，注意高度与 css 文件中保持一致
				replyContent.css({height:"30px"});
				map.submit_btn.closest("div.jf-common-item").before(ret.replyItem);
			} else {
				showReplyErrorMsg(ret.msg);
			}
		}
		, complete: function() {
			map.submit_loading.hide();
			map.submit_btn.show();
			map.isLoading = false;
		}
	});
}

/**
 * share、feedback 详情页回复链接的 at 功能
 * 将 @at 填充到回复 textarea 中
 */
function atAndReply(nickName) {
	var replyContent = $('#replyContent');
	var content = replyContent.val() + "@" + nickName + " ";
	replyContent.val(content);
}

/**
 * share、feedback 详情页回复错误信息提示框，需要引入 layer.js
 *  news feed 模块的 replyNewsFeed(...) 也用到此方法，在演化时注意
  */
function showReplyErrorMsg(msg) {
	layer.msg(msg, {
			shift: 6
			, shade: 0.4
			, time: 2000
			// , offset: "140px"
			, closeBtn: 1
			, shadeClose: true
			,maxWidth: "1000"
		}, function () {}
	);
}

/**
 * ajax GET 请求封装，提供了一些默认参数
 */
function ajaxGet(url, options) {
	var defaultOptions = {
		type: "GET"
		, cache: false      // 经测试设置为 false 时，ajax 请求会自动追加一个参数 "&_=nnnnnnnnnnn"
		, dataType: "json"  // "json" "text" "html" "jsonp"，如果设置为"html"，其中的script会被执行
		// , data: {}
		// , timeout: 9000     // 毫秒
		// , beforeSend: function(XHR) {}
		, success: function(ret){
			if (ret.state == "ok") {
				alert(ret.msg ? ret.msg : "操作成功");
			} else {
				alert("操作失败：" + (ret.msg ? ret.msg : "请告知管理员！"));
			}
		}
		, error: function(XHR, msg) {
			showReplyErrorMsg(msg); // 默认调用
		}
		// , complete: function(XHR, msg){} // 请求成功与失败都调用
	};
	// 用户自定义参数覆盖掉默认参数
	for(var o in options) {
		defaultOptions[o] = options[o];
	}

	$.ajax(url, defaultOptions);
}

/**
 * 确认对话框层，点击确定才真正操作
 * @param msg 对话框的提示文字
 * @param actionUrl 点击确认后请求到的目标 url
 * @param options jquery $.ajax(...) 方法的 options 参数
 */
function confirmAjaxGet(msg, actionUrl, options) {
	layer.confirm(msg, {
		icon: 0
		, title:''                                      // 设置为空串时，title消失，并自动切换关闭按钮样式，比较好的体验
		, shade: 0.4
		, offset: "139px"
	}, function(index) {                                // 只有点确定后才会回调该方法
		// location.href = operationUrl;                // 操作是一个 GET 链接请求，并非 ajax
		// 替换上面的 location.href 操作，改造成 ajax 请求。后端用 renderJson 更方便，不需要知道 redirect 到哪里
		ajaxGet(actionUrl, options);
		layer.close(index);                             // 需要调用 layer.close(index) 才能关闭对话框
	});
}

// share、feedback 详情页 reply 删除功能
function deleteReply(deleteBtn, url) {
	confirmAjaxGet("删除后无法恢复，确定要删除？", url, {
		success: function(ret) {
			if (ret.state == "ok") {
				// $(deleteBtn).parents(".jf-reply-list li").remove();
				// $(deleteBtn).closest("li").remove();	// jfinal.com-3.0
				$(deleteBtn).closest("div.jf-common-item").remove();
			}
		}
	});
}

// 添加好友功能，用于关注/粉丝列表页面
function addFriend(btn, friendId) {
	layer.msg("正在加载，请稍后！", {icon: 16, offset: '100px'});
	ajaxGet("/friend/add?friendId=" + friendId, {
		success: function(ret) {
			if (ret.state == "ok") {
				setFriendBtn($(btn), friendId, ret.friendRelation);
			} else {
				showReplyErrorMsg(ret.msg);
			}
		}
		, complete: function(XHR, msg){
			layer.closeAll();
		}
	});
}
// 删除好友功能，用于关注/粉丝列表页面
function deleteFriend(btn, friendId) {
	confirmAjaxGet("取消关注后，此人的动态消息将不会出现在你的首页，确定要操作？", "/friend/delete?friendId=" + friendId, {
		success: function(ret) {
			if (ret.state == "ok") {
				setFriendBtn($(btn), friendId, ret.friendRelation);
			} else {
				showReplyErrorMsg(ret.msg);
			}
		}
	});
}

/**
 * 用于关注/粉丝列表页面
 * friendRelation 含义
 * 0： accountId 与 friendId 无任何关系
 * 1： accountId 关注了 friendId
 * 2： friendId 关注了 accountId
 * 3： accountId 与 friendId 互相关注
 */
function setFriendBtn(btn, friendId, friendRelation) {
	if (friendRelation == 0) {
		btn.attr("onclick", "addFriend(this," + friendId + ");");
		btn.text("+关注");
	} else if (friendRelation == 1) {
		btn.attr("onclick", "deleteFriend(this," + friendId + ");");
		btn.text("取消关注");
	} else if (friendRelation == 2) {
		btn.attr("onclick", "addFriend(this," + friendId + ");");
		btn.text("+关注");
	} else if (friendRelation == 3) {
		btn.attr("onclick", "deleteFriend(this," + friendId + ");");
		btn.text("取消互粉");
	}
}

/**
 * 用于个人空间用户头像下方的关注/取消关注功能
 */
function handleFriend(thisBtn, isAdd, friendId) {
	var layerIndex = layer.msg("正在加载，请稍后！", {icon: 16, offset: '100px'});
	var url = isAdd ? "/friend/add?friendId=" + friendId : "/friend/delete?friendId=" + friendId;
	ajaxGet(url, {
		success: function(ret) {
			if (ret.state == "ok") {
                var parent = $(thisBtn).parent();
                var link;
				if (ret.friendRelation == 0) {
                    // link = "未关注<a href='javascript:void(0);' onclick='handleFriend(this, true, " + friendId + ");'>关注</a>";
					link = "<a href='javascript:void(0);' onclick='handleFriend(this, true, " + friendId + ");' class='btn btn-outline-grey'><i class='fa fa-heart-o'></i>关注</a>";
				} else if (ret.friendRelation == 1) {
                    // link = "已关注<a href='javascript:void(0);' onclick='handleFriend(this, false, " + friendId + ");'>取消</a>";
					link = "<a href='javascript:void(0);' onclick='handleFriend(this, false, " + friendId + ");' class='btn btn-outline-grey'><i class='fa fa-heart text-danger'></i>已关注</a>";
				} else if (ret.friendRelation == 2) {
                    // link = "粉丝<a href='javascript:void(0);' onclick='handleFriend(this, true, " + friendId + ");'>+关注</a>";
                    link = "<a href='javascript:void(0);' onclick='handleFriend(this, true, " + friendId + ");' class='btn btn-outline-grey'><i class='fa fa-heart-o'></i> + 关注</a>";
				} else {
                    // link = "互相关注<a href='javascript:void(0);' onclick='handleFriend(this, false, " + friendId + ");'>取消</a>";
					link = "<a href='javascript:void(0);' onclick='handleFriend(this, false, " + friendId + ");' class='btn btn-outline-grey'><i class='fa fa-heart text-danger'></i>互相关注</a>";
				}
                parent.html(link);
			} else {
				showReplyErrorMsg(ret.msg);
			}
		}
		, complete: function(XHR, msg){
			layer.close(layerIndex);
		}
	});
}

/**
 * 点赞
 */
function doLike(refType, refId, isAdd, options) {
    var url = "/like?refType=" + refType + "&refId=" + refId;
    if (isAdd != null) {
        url = url + "&isAdd=" + isAdd;
    }
    ajaxGet(url, options);
}

/**
 * 点赞
 */
function like(refType, refId, map) {
    if (map.isLoading) {
        return ;
    } else {
        map.isLoading = true;
    }

    doLike(refType, refId, map.isAdd, {
        success: function(ret){
            if (ret.state == "ok") {
                var btn = map.btn;
                var next = btn.next();
                var num = next.text();
                num = parseInt(num);
                if (isNaN(num)) {
                    num = 0;
                }
                if (map.isAdd) {
                    num = num + 1;
                    btn.addClass("active");
                    map.isAdd = false;
                } else {
                    num = num - 1;
                    btn.removeClass("active");
                    map.isAdd = true;
                }
                if (num == 0) {
                    num = "";
                }
                next.text(num);
            } else {
                showReplyErrorMsg(ret.msg);
            }
	        map.isLoading = false;  // 重置 isLoading，允许点击时提交请求
        }
    });
}

/**
 * 收藏
 */
function doFavorite(refType, refId, isAdd, options) {
    var url = "/favorite?refType=" + refType + "&refId=" + refId;
    if (isAdd != null) {
        url = url + "&isAdd=" + isAdd;
    }
    ajaxGet(url, options);
}

/**
 * 收藏
 */
function favorite(refType, refId, map) {
    if (map.isLoading) {
        return ;
    } else {
        map.isLoading = true;
    }

    doFavorite(refType, refId, map.isAdd, {
        success: function(ret){
            if (ret.state == "ok") {
                var btn = map.btn;
                var next = btn.next();
                var num = next.text();
                num = parseInt(num);
                if (isNaN(num)) {
                    num = 0;
                }
                if (map.isAdd) {
                    num = num + 1;
                    btn.addClass("active");
                    map.isAdd = false;
                } else {
                    num = num - 1;
                    btn.removeClass("active");
                    map.isAdd = true;
                }
                if (num == 0) {
                    num = "";
                }
                next.text(num);
            } else {
                showReplyErrorMsg(ret.msg);
            }
	        map.isLoading = false;  // 重置 isLoading，允许点击时提交请求
        }
    });
}

/**
 * 非 ajax action 请求之前确认，确定则执行，否则放弃
 * 个人空间的分享、项目、反馈的删除用到此函数
 */
function confirmAction(msg, actionUrl) {
	layer.confirm(msg, {
		icon: 0
		, title:''						// 设置为空串时，title消失，并自动切换关闭按钮样式，比较好的体验
		, shade: 0.4
		, offset: "139px"
	}, function(index) {				// 只有点确定后才会回调该方法
		location.href = actionUrl;		// 通过普通的 url 请求调用后端逻辑
		layer.close(index);				// 需要调用 layer.close(index) 才能关闭对话框
	});
}

/**
 * 刷新验证码
 */
function updateCaptcha(img, inputId) {
	var src = "/login/captcha?v=" + Math.random();
	if (img.src) {		// 支持传入 this
		img.src = src;
	} else {			// 支持传入 jquery 对象
		img.attr("src", src);
	}
	$("#" + inputId).val("");
}

/**
 * 搜索
 */
function search() {
    var e = document.getElementById("q");
    if (e.value == "") {
        return false;
    }
    
    // var t = "https://cn.bing.com/search?q=site:jfinal.com%20" + e.value;
    // var t = "https://www.baidu.com/s?ie=utf-8&wd=site:jfinal.com%20" + e.value;
    var t = "https://www.baidu.com/s?ie=utf-8&wd=site:jfinal.com%20" + e.value;
    return -1 < navigator.userAgent.indexOf("iPad")
    	|| -1 < navigator.userAgent.indexOf("iPod")
    	|| -1 < navigator.userAgent.indexOf("iPhone")
    	? location.href = t
    	: window.open(t, "_blank"), false;
}

/**
 * 点击文档菜单
 */
function clickDocMenu(event) {
	// console.info(event);
	event.preventDefault();	// 取代 return false 防止页面跳转
	
	docLoadingLayer();
	
	var menu = event.target.id;
	var url = "/doc/ajaxContent/" + menu;
	$.ajax(url, {
		type: "GET"
		, cache: false
		, dataType: "html"
		, success: function(ret) {
			// 关闭所有加载层
			layer.closeAll('loading');
			
			// 替换掉右侧文档内容
			$("#ajaxContainer").replaceWith(ret);
			// pre 标签中的源代码添加样式
			$("pre").addClass("prettyprint linenums");
			prettyPrint();
			
			// 设置当前被选中菜单
			var $a = $(event.target);
			$a.parent().parent().parent().find("a").removeClass("active");
			$a.addClass("active");
			
			// 改变地址栏 url 并添加新的历史记录
			window.history.pushState(null, "", "/doc/" + menu);
		}
	});
}

/**
 * 文档加载时的弹出层动画
 */
function docLoadingLayer() {
	// 清空右侧文档
	$("#ajaxContainer .jf-doc-contentbox").empty();
	
	// 弹出加载层，风格可以传入0-2
	layer.load(0, {
		shade: false
		// shade: 0.1
		, offset: '190px'	// 只定义top坐标，水平保持居中
		, time: 0			// 0 为不自动关闭窗口
	});
}

/**
 * 表单序列化成 json 字符串，便于在 ajax post 请求中使用 json 数据，例如：
 * 
	$.ajax({
		type: "post",
		url: "http://localhost/action",
		contentType: 'application/json; charset=UTF-8',	// 关键参数
		dataType: 'json',
		data: formToJsonString("表单id"),
		success: function(ret) {
		}
	});
	
 */
function formToJsonString(formId) {
	var paramArray = $('#' + formId).serializeArray();
	// 表单参数转 json 对象
	var jsonObj = {};
	$(paramArray).each(function() {
		jsonObj[this.name] = this.value;  
	});
	
	// json 对象转 json 字符串
	return JSON.stringify(jsonObj);
}


