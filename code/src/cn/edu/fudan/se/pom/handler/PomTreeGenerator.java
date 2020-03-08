package cn.edu.fudan.se.pom.handler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import cn.edu.fudan.se.util.DBUtil;

import cn.edu.fudan.se.util.JsonFileUtil;
import cn.edu.fudan.se.util.MyException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PomTreeGenerator {

	public static void main(String[] args) {
//		runPomParser("");
		runPomParserWY();
	}

	public static void runPomParser(String file) {
		String rootPath = "C:/Users/huangkaifeng/Desktop/";
		file = rootPath + "pomtreeinput.json";
		String resultPath = rootPath + "tree/";
		String pomFilePath = "D:/Workspace/WyRepos/pom/";
		String unsolvedPomFilePath = "D:/Workspace/WyRepos/pom_unsolved/";
		String serverIp = "http://127.0.0.1:8080";
//		JSONArray unParsedPom = new JSONArray();
		JSONArray array = JsonFileUtil.readJsonFileAsArray(file);
		System.out.println(array.size());
		Set<Integer> s = new HashSet<>();
		for (int i = 0; i < array.size(); i++) {
			JSONObject obj = array.getJSONObject(i);
			String localPath = obj.optString("local_addr");
			localPath = "I:/projects/" + localPath;
			obj.put("local_addr", localPath);
			int id = obj.optInt("id");
			if(id==1753 || id == 2890){
				continue;
			}
//			if(id!=602){
//				continue;
//			}
			s.add(id);
			System.out.println("++++++++++++++++++++++localPath : " + id + "   " + localPath);
			if (new File(resultPath + id + ".json").exists())
				continue;
			PomTreeGeneratorModel pomModel = new PomTreeGeneratorModel(resultPath, pomFilePath);
			pomModel.isParentTrueFlag = true;
			if(id ==2923|| id==3323 || id ==1383){
				pomModel.isParentTrueFlag = false;
			}
			pomModel.handleProjectPom(localPath, obj);
			JSONObject joo = pomModel.toJSONObject();
			JsonFileUtil.save(resultPath + id + ".json", joo, 4);
//			break;
		}
		System.out.println(s.size());
	}
	
	public static void runPomParserWY() {
		String rootPath = "E:/data/multiversion/";
		String resultPath = rootPath + "tree/";
		String pomFilePath = rootPath + "pom/";
		
		Map<String,String> projMap = LocateDependencyPosition.getProjMap();

		
		Set<Integer> s = new HashSet<>();
		String[] files = new File(rootPath + "output").list();
		for(String file : files) {
			int id = Integer.parseInt(file.replace(".txt", ""));
			String localPath = "C:/projects_unzips/" + projMap.get("" + id);
			JSONObject obj = new JSONObject();
			obj.put("local_addr", localPath);
			obj.put("id", id);
			
			s.add(id);
			System.out.println("++++++++++++++++++++++localPath : " + id + "   " + localPath);
			if (new File(resultPath + id + ".json").exists())
				continue;
			PomTreeGeneratorModel pomModel = new PomTreeGeneratorModel(resultPath, pomFilePath);
			pomModel.isParentTrueFlag = true;
			if(id ==2923|| id==3323 || id ==1383){
				pomModel.isParentTrueFlag = false;
			}
			pomModel.handleProjectPom(localPath, obj);
			JSONObject joo = pomModel.toJSONObject();
			JsonFileUtil.save(resultPath + id + ".json", joo, 4);
		}
		System.out.println(s.size());
	}

}
