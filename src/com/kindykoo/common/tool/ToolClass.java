package com.kindykoo.common.tool;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
/**
 * 工具类
 */
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

public class ToolClass {
	
	public static final String reserveModelID = "4SXTtXN29Miak_Y75vJMlFNlCQOSpkm2ezKliumTHRA";
	public static final String enableModelID = "UXNlnMN67fZ9vXAP5pte9wB_nOUgQInvTrXYOh3Tuto";
	public static final String bindedModelID = "AAaL6eGiAlrXTWndGz6LoD5Lx5RNDTdWp1GeYXtARw0";
	public static final String APPID = "wx29e6ab5ea5e3d179";
	public static final String SECRETAPP = "82b295a6fd80158167ee808c37e2c932";
	public static final String BATHPATH = "https://www.koalabear.ac.cn/";
	public static final String EXPORTPATH = "export/";
	public static final int maxFixedNum = 6;
	public static final int maxStudentNum = 10;
	public static final int courseTime = 45;
	public static String []  weeks = {"星期一", "星期二", "星期三","星期四","星期五","星期六","星期日"};
	
	/**
	 * 得到传入时间在一年中的周数
	 * @param date
	 * @return
	 */
	public static int getWeekCount(Date date){
		  
		Calendar calendar = Calendar.getInstance();  
		calendar.setFirstDayOfWeek(Calendar.MONDAY);  
		calendar.setTime(date);  
		  
		return calendar.get(Calendar.WEEK_OF_YEAR); 
	}
	
	/**
	 * 将月份年龄转换为"2岁3个月"格式
	 * @param age
	 * @return
	 */
	public static String getStringAge(int age){
		String str = "";
		int age_year = (int) Math.floor(age/12);
		if(age<12){
			str = new StringBuilder(age).append("个月").toString();
		}else if(age%12!=0){
			str = new StringBuilder().append(age_year+"岁").append(age%12).append("个月").toString();
		}else{
			str = new StringBuilder().append(age_year+"岁").toString();
		}
		return str;
	}
	
	public static Date getDateAndTime(Date date, Date time){
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd ,HH:mm:ss"); 
		String str = new StringBuilder(StringUtils.substringBefore(sdf1.format(date), ",")).append(StringUtils.substringAfter(sdf1.format(time), ",")).toString();
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		try {
			return sdf2.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 提到指定星期的日期
	 * @param week
	 * @return
	 */
	public static Date getDate(Date date,int weekCount,int week){
		Calendar calendar=Calendar.getInstance(Locale.CHINA);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.setTimeInMillis(date.getTime());
		calendar.set(Calendar.WEEK_OF_YEAR, weekCount);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		calendar.add(Calendar.DATE, week);
		return calendar.getTime();
	}
	
	/**
	 * 获取传入日期的星期
	 * @param date
	 * @return
	 */
	public static String getWeekOfDate(Date date) {  
        String[] weekDays = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };  
        Calendar cal = Calendar.getInstance();  
        cal.setTime(date);  
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;  
        if (w < 0)  
            w = 0;  
        return weekDays[w];  
    }
	
	/**
	 * 得到指定字符串在字符串数组中的序号
	 * @param strs
	 * @param str
	 * @return
	 */
	public static int getIndex(String[] strs, String str){
		List<String> list = new ArrayList<String>();
        for(int i=0; i<strs.length; i++){
        	list.add(strs[i]);
        }
        return list.indexOf(str);
	}

}
