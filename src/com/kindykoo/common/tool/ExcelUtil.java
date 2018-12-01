package com.kindykoo.common.tool;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * excel 导入导出类
 * @author leeshua
 * @version 1.0
 */
public class ExcelUtil {

	/**
	 * 数据导出
	 * @param title 表头标题
	 * @param field 数据库字段名
	 * @param data 要导出的数据
	 * @param file 导出文件
	 */
	public static boolean exportData(String[] title,  String[] field, List<HashMap<String,Object>> data, File file){
		
		boolean b = true;
		//1.创建工作簿对象
		WritableWorkbook wb = null;
		try {
			wb = jxl.Workbook.createWorkbook(file);
			//2.创建工作表对象
			WritableSheet sheet = wb.createSheet("ReserveCourse", 0);
			
			//3.添加表头
			Label label = null;
			for (int i = 0; i < title.length; i++) {
				label = new Label(i, 0, title[i]);
				sheet.addCell(label);
			}
			
			//4.添加数据
			HashMap<String, Object> map = null;
			for (int i = 0; i < data.size(); i++) {
				map = data.get(i);
				for (int j = 0; j < field.length; j++) {
					label = new Label(j, i+1, String.valueOf(map.get(field[j])));
					sheet.addCell(label);
				}
			}
			
			//5.写入文件并关闭文件流
			wb.write();
			wb.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			b = false;
		}catch (WriteException e){
			e.printStackTrace();
			b = false;
		}
		return b;
	}
}
