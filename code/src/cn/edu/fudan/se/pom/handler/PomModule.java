package cn.edu.fudan.se.pom.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import cn.edu.fudan.se.util.FastJsonUtil;
import cn.edu.fudan.se.util.FileUtil;

public class PomModule {
	
	public static void main(String[] args) {
//		getModuleFromPom();
//		getModuleByLevels();
		getPomFileAndModuleCount();
	}
	
	public static void getModuleByLevels() {
//		Map<String, List<String>> result = new HashMap<String, List<String>>();
//		String[] dirs = {"F:/mv_projects170","F:/mv_projectsbu"};
//		for(String dir : dirs) {
//			String[] projs = new File(dir).list();
//			for(String proj : projs) {
//				System.out.println("+++++++++++++++++" + proj);
//				List<String> projModules = new ArrayList<String>();			
//				getChildModule(dir + "/" + proj + "/", "pom.xml", projModules);
//				result.put(proj, projModules);
//			}
//		}		
//		JSONObject obj = JSONObject.parseObject(JSON.toJSONString(result,SerializerFeature.WriteMapNullValue));
//		FastJsonUtil.writeJson("F:/modules.json", obj, true);
		
		String dir = "C:/projects_unzips";
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		String content = FileUtil.read("E:/data/200_plus_maven.txt");
		List<Map<String,Object>> mavenProjs = (List<Map<String,Object>>)JSON.parse(content);
		System.out.println(mavenProjs.size());
		for(Map<String,Object> obj : mavenProjs) {
			String proj = (String) obj.get("name");
//			System.out.println(obj.get("id") + ":" + dir + "/" + proj);
//			if(new File(dir + "/" + proj).exists()) {
//				System.err.println(obj.get("id") + ":" + dir + "/" + proj);
//				System.exit(0);
//			}
			System.out.println("+++++++++++++++++" + proj);
			List<String> projModules = new ArrayList<String>();			
			getChildModule(dir + "/" + proj + "/", "pom.xml", projModules);
			result.put(proj, projModules);
		}
		JSONObject obj = JSONObject.parseObject(JSON.toJSONString(result,SerializerFeature.WriteMapNullValue));
		FastJsonUtil.writeJson("C:/Users/yw/Desktop/modules_maven.json", obj, true);
	}
	
	public static void getChildModule(String projPath, String pomFile,List<String> projModules) {
		String pomPath = (projPath + pomFile).replace("\\", "/");
		String prefix = pomFile.replace("\\", "/");
		if(prefix.endsWith("pom.xml"))
			prefix = prefix.substring(0, prefix.length()-7);
		if(new File(pomPath).exists()) {
			System.out.println("-----------------" + pomPath);
			System.out.println(prefix);
			Model model = PomFileReader.parsePomFileToModel(pomPath);
			if(model!= null && model.getModules() != null) {
				List<String> temp = model.getModules();
				for(String t:temp) {
					projModules.add(prefix + t);
					System.out.println(prefix + t);
					getChildModule(projPath, prefix + t + "/pom.xml",projModules);
				}
//				projModules.addAll(temp);
//				System.out.println(projModules.size());
//				
			}
		}
	}

	public static void getModuleFromPom() {
//		Map<String, String> projMap = LocateDependencyPosition.getProjMap();
//		
//		String content = FileUtil.read(LocateDependencyPosition.MV_DIR + "mv_info_maven.txt");
//		Map<String, Map<String,List<String>>> map = (Map)JSON.parse(content);
//		
//		Map<String, List<String>> result = new HashMap<String, List<String>>();
//		
//		for(Map.Entry<String, Map<String,List<String>>> entry: map.entrySet()) {
//			
//			int projectId = Integer.parseInt(entry.getKey());			
//			System.out.println("+++++++++++++++++++++" + projectId);
//			String projectPath = LocateDependencyPosition.PROJ_DIR + projMap.get(projectId + "");
//			String pomPath = projectPath + "/" + "pom.xml";	
//			if(new File(pomPath).exists()) {
//				System.out.println("+++++++++++++++++++++" + pomPath);
//				Model model = PomFileReader.parsePomFileToModel(pomPath);
//				if(model.getModules() != null) {
//					result.put("" + projectId, model.getModules());
//				}
//			}
//		}
		
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		String dir = "F:/mv_projects170";
		String[] projs = new File(dir).list();
		for(String proj : projs) {
			System.out.println("+++++++++++++++++" + proj);
			List<String> projModules = new ArrayList<String>();
			
			String[] pomFiles = FileUtil.getAllPomFiles(dir + "/" + proj);
			for(String file : pomFiles) {
				file = file.replace("\\", "/");
				String pomPath = dir + "/" + proj + "/" + file;	
				pomPath = pomPath.replace("\\", "/");
				String prefix = file;
				if(prefix.endsWith("pom.xml"))
					prefix = prefix.substring(0, prefix.length()-7);
				if(new File(pomPath).exists()) {
					System.out.println("-----------------" + pomPath);
					System.out.println(file);
					System.out.println(prefix);
					Model model = PomFileReader.parsePomFileToModel(pomPath);
					if(model!= null && model.getModules() != null) {
						List<String> temp = model.getModules();
						for(String t:temp) {
							projModules.add(prefix + t);
							System.out.println(prefix + t);
						}
					}
				}
			}
			result.put(proj, projModules);
		}
		JSONObject obj = JSONObject.parseObject(JSON.toJSONString(result,SerializerFeature.WriteMapNullValue));
		FastJsonUtil.writeJson("F:/modules.json", obj, true);
	}
	
	public static void getPomFileAndModuleCount() {
		String dir = "C:/projects_unzips";
		Map<String,List<String>> modules = (Map<String,List<String>>)JSON.parse(FileUtil.read("C:/Users/yw/Desktop/modules_maven.json"));
		System.out.println(modules.size());
		List<Map<String,Object>> mavenProjs = (List<Map<String,Object>>)JSON.parse(FileUtil.read("E:/data/200_plus_maven.txt"));
		System.out.println(mavenProjs.size());
		
		Map<String, int[]> result = new HashMap<String, int[]>();
		for(Map<String,Object> obj : mavenProjs) {
			String proj = (String) obj.get("name");
			System.out.println("+++++++++++++++++" + proj);
			String[] pomFiles = FileUtil.getAllPomFiles(dir + "/" + proj);
			int fileCount = pomFiles.length;
			int moduleCount = modules.get(proj).size();
			int[] newValue = {fileCount, moduleCount};
			result.put((int)obj.get("id") + "", newValue);
			System.out.println(fileCount + " " + moduleCount);
		}
		JSONObject obj = JSONObject.parseObject(JSON.toJSONString(result,SerializerFeature.WriteMapNullValue));
		FastJsonUtil.writeJson("C:/Users/yw/Desktop/pom_module_count.json", obj, true);
	}
	
}
