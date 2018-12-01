package com.kindykoo.common.task;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.ehcache.CacheKit;
import com.kindykoo.common.tool.ToolClass;
import com.kindykoo.controller.logs.LogsService;

public class UpdateAccessToken  implements Runnable{

	@Override
	public void run() {
		String oldAccess_token = CacheKit.get("access_token", "access_token");
		if (oldAccess_token != null && !"".equals(oldAccess_token)) {
			// 删除之前openId对应的缓存
			CacheKit.remove("access_token", oldAccess_token);
		}
		String url = "https://api.weixin.qq.com/cgi-bin/token";
		String httpUrl = url + "?appid=" + ToolClass.APPID + "&secret=" + ToolClass.SECRETAPP + "&grant_type=client_credential";
		String ret = HttpKit.get(httpUrl);
		Map<String,String> json = (Map) JSONObject.parse(ret.trim()); 
		String access_token = json.get("access_token");
//		long expires_in = Integer.parseInt(json.get("expires_in"));
		
		// 开始缓存access_token
		CacheKit.put("access_token", "access_token", access_token);
		
		System.out.println("oldAccess_token==" + oldAccess_token);
		System.out.println("newAccess_token==" + access_token);
//		System.out.println("expires_in=" + expires_in);
		String info = "UpdateAccessToken";
		LogsService.insert(info);
	}

}
