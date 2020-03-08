package cn.edu.fudan.se.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class JsonFileUtil {



	public static void save(String savePath, JSONArray array) {
		try {
			FileWriter f = new FileWriter(savePath);
			f.write(array.toString());// 写入
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void save(String savePath, JSONObject obj) {
		try {
			FileWriter f = new FileWriter(savePath);
			f.write(obj.toString(4));// 写入
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public static void save(String savePath, com.alibaba.fastjson.JSONObject obj) {
		try {
			FileWriter f = new FileWriter(savePath);
			f.write(obj.toJSONString());// 写入
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void save(String savePath, com.alibaba.fastjson.JSONArray obj) {
		try {
			FileWriter f = new FileWriter(savePath);
			f.write(obj.toJSONString());// 写入
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static void save(String savePath, JSONArray array, int indent) {
		try {
			FileWriter f = new FileWriter(savePath);
			f.write(array.toString(4));// 写入
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void save(String savePath, JSONObject obj, int indent) {
		try {
			FileWriter f = new FileWriter(savePath);
			f.write(obj.toString(4));// 写入
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}





	
	public static String readJsonFile(String path) {
		String whole = "";
		try {
			Scanner in = new Scanner(new File(path));
			while (in.hasNextLine()) {
				String str = in.nextLine();
				whole += str.trim();
//				System.out.println("enter");
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return whole;
	
	}
	
	public static JSONObject readJsonFileAsObject(String path) {
		String s = readJsonFile(path);
		JSONObject jo = JSONObject.fromObject(s);
		return jo;
		
	}
	public static JsonObject readGsonFileAsObject(String path){
		String s = readJsonFile(path);
		JsonObject returnData = new JsonParser().parse(s).getAsJsonObject();
		return returnData;
	}

	public static JsonArray readGsonFileAsArray(String path){
		String s = readJsonFile(path);
		JsonArray returnData = new JsonParser().parse(s).getAsJsonArray();
		return returnData;
	}
	
	public static JSONArray readJsonFileAsArray(String path) {
		String s = readJsonFile(path);
		JSONArray jo = JSONArray.fromObject(s);
		return jo;
		
	}
}



