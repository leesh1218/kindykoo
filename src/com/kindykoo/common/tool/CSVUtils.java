package com.kindykoo.common.tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

public class CSVUtils {
	// 写csv文件 传参数文件名 csv文件表头 需要写入的数据
	public static Boolean writeCsvFile(String fileName, String[] header, List<List<String>> records) {
		BufferedWriter bufferedWriter = null;
		CSVPrinter csvPrinter = null;
		try {
			// 创建文件及文件夹
			File file = new File(fileName);
			String folderName = file.getParent();
			if (StringUtils.isNotBlank(folderName)) {
				File folder = new File(folderName);
				if (!(folder.exists() && folder.isDirectory())){
					folder.mkdirs();
				}
			}
			// 初始化FileWriter object
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "gbk"),
					1024);
			// 初始化 CSVPrinter
			csvPrinter = new CSVPrinter(bufferedWriter, CSVFormat.DEFAULT.withHeader(header));
			for (List<String> record : records) {
				for (String col : record) {
					csvPrinter.print(col);
				}
				csvPrinter.println();	// 换行
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bufferedWriter.flush();
				bufferedWriter.close();
				csvPrinter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	// 读取csv文件 传参数 文件 表头
	public static List<List<String>> readCsvFile(String fileName, String[] header) {
		BufferedReader bufferReader = null;
		CSVParser parser = null;
		List<List<String>> records = null;
		try {
			// 初始化FileReader object
			bufferReader = new BufferedReader(new InputStreamReader(getInputStreamFromUrl(fileName), "gbk"));
			// 初始化 CSVParser object
			parser = new CSVParser(bufferReader, CSVFormat.DEFAULT.withHeader(header));
			// Parsing into memory
			List<CSVRecord> list = parser.getRecords();
			// 初始化返回的records
			records = new ArrayList<List<String>>();
			// 遍历
			for (CSVRecord item : list) {
				if( null != header && item.getRecordNumber() == 1) //不返回header
					continue;
				List<String> record = new ArrayList<String>();
				for (String col : item) {
					record.add(col.trim());
				}
				records.add(record);
			}
			list.clear();
		} catch (Exception e) {
			//e.printStackTrace();
			return null;
		} finally {
			try {
				if( null != bufferReader){
					bufferReader.close();
				}
				if (null != parser){
					parser.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return records;
	}

	/**
	 * 通过流读取csv文件
	 * @param is
	 * @param header
	 * @return
	 */
	public static List<List<String>> readCsvFile(InputStream is, String[] header) {
		BufferedReader bufferReader = null;
		CSVParser parser = null;
		List<List<String>> records = null;
		try {
			// 初始化FileReader object
			bufferReader = new BufferedReader(new InputStreamReader(is, "gbk"));
			// 初始化 CSVParser object
			parser = new CSVParser(bufferReader, CSVFormat.DEFAULT.withHeader(header));
			// Parsing into memory
			List<CSVRecord> list = parser.getRecords();
			// 初始化返回的records
			records = new ArrayList<List<String>>();
			// 遍历
			for (CSVRecord item : list) {
				if( null != header && item.getRecordNumber() == 1) //不返回header
					continue;
				List<String> record = new ArrayList<String>();
				for (String col : item) {
					record.add(col.trim());
				}
				records.add(record);
			}
			list.clear();
		} catch (Exception e) {
			//e.printStackTrace();
			return null;
		} finally {
			try {
				if( null != bufferReader){
					bufferReader.close();
				}
				if (null != parser){
					parser.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return records;
	}
	
	/** 
     * 通过文件url返回InputStream 
     * @param path
     * @return 
     */  
    public static InputStream getInputStreamFromUrl(String path) {  
        //URL url = null;  
        InputStream is =null;
        if (null == path )
        	return is;
        if (path.matches("^([hH][tT]{2}[pP]://|[hH][tT]{2}[pP][sS]://)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\/])+$"))
        {
            try {
            	//path = URLDecoder.decode(path, "utf-8");
            	URL url = new URL(path);  
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();//利用HttpURLConnection对象,我们可以从网络中获取网页数据.  
                conn.setDoInput(true);  
                conn.connect();  
                is = conn.getInputStream(); //得到网络返回的输入流  
            } catch (IOException e) {  
                //e.printStackTrace();  
            }
        }else{
        	try {
				is = new FileInputStream(path);
			} catch (FileNotFoundException e) {
				//e.printStackTrace();
			}
        }
        return is;  
    }

	/**
	 * 文件保存
	 * @param is 文件流
	 * @param savePath 保存的路径
	 */
	public static void saveFile(InputStream is, String savePath) {

		try {
			File fp = new File(new File(savePath).getParent());
			if (!fp.exists())
				fp.mkdirs();
			FileOutputStream out = new FileOutputStream(savePath);
			// InputStream is = new FileInputStream(file);
			try {
				byte[] buffer = new byte[4 * 1024];
				int read;
				while ((read = is.read(buffer)) > 0) {
					out.write(buffer, 0, read);
				}
			} catch (Exception e) {
				throw new RuntimeException();
			} finally {
				if (null != is)
					is.close();
				if (null != out)
					out.close();
			}
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}
}
