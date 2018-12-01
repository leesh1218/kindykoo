package com.kindykoo.controller;

import com.jfinal.core.Controller;

public class IndexController extends Controller {
	public void index(){
		render("index.html");
	}
	
	public void summary(){
		render("summary.html");
	}
}
