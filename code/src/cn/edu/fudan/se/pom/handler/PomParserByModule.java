package cn.edu.fudan.se.pom.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import cn.edu.fudan.se.util.JsonFileUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PomParserByModule {

	public static void main(String[] args) throws IOException, XmlPullParserException {
		// String pomPath = "E:/Workspace_eclipse/appserver12/pom.xml";
//		String pomPath = "C:/Users/yw/Downloads/pom.xml";
//		readPom("C:/Users/yw/Downloads",new JSONObject());
		// readData("F:/GP/result/1.txt");
//		for(int i =0;i<args.length;i++) {
//			System.out.println(args[i]);
//		}
//		 readProjectFile("project.txt");
//		HandleProjects.parseProjectsPath();
		
		readProjectFile(args[0]);
//		readPom("E:/Workspace_eclipse", new JSONObject());		
//		ProjectPom projectPom= new ProjectPom();
//		projectPom.handleProjectPom("C:/Users/yw/Desktop/projects/netty", new JSONObject());
//		for (Map.Entry<String, String[]> pomLib : projectPom.getUnParsedPomTypeLibs().entrySet()) {
//			String[] lib = pomLib.getKey().split(" ");
//			System.out.println(lib[0] +" "+lib[1]);
//		}
	}	

	public static void readData(String dataPath) {
		String whole = "";
		try {
			Scanner in = new Scanner(new File(dataPath));
			while (in.hasNextLine()) {
				String str = in.nextLine();
				whole += str;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		JSONArray array = JSONArray.fromObject(whole);
		for (int i = 0; i < array.size(); i++) {
			JSONObject obj = array.getJSONObject(i);
			System.out.println(obj.optString("groupId"));
			System.out.println(obj.optString("artifactId"));
			System.out.println(obj.optString("version"));
			System.out.println();
		}
	}

	public static void readProjectFile(String file) {
		JSONArray unParsedPom = new JSONArray();
		String whole = JsonFileUtil.readJsonFile(file);		
		JSONArray array = JSONArray.fromObject(whole);
		for (int i = 0; i < array.size(); i++) {
			JSONObject obj = array.getJSONObject(i);
			String localPath = obj.optString("local_addr");
			int id = obj.optInt("id");			
//			if(id != 584) {
			if(id == 1344) {
//				localPath = localPath.replace("repositories","repository1");
//				obj.put("local_addr", localPath);	
				System.out.println("localPath : "+localPath);
				System.out.println("id : "+id);
				ProjectPom projectPom= new ProjectPom();
				projectPom.handleProjectPom(localPath, obj);
//				projectPom.handleProjectPom(localPath.replace("../data/prior_repository", "F:/GP/high_quality_repos"), obj);
				for (Map.Entry<String, String[]> pomLib : projectPom.getUnParsedPomTypeLibs().entrySet()) {
					String[] lib = pomLib.getKey().split(" ");
					JSONObject pomObj = new JSONObject();
					pomObj.put("groupId", lib[0]);
					pomObj.put("artifactId", lib[1]);					
					String version = pomLib.getValue()[0];
					pomObj.put("version", version);	
					pomObj.put("module", pomLib.getValue()[1]);	
					pomObj.put("id", id);	
					unParsedPom.add(pomObj);
				}
//				readPom(localPath,obj);
				System.out.println();
			}		
		}
		JsonFileUtil.save("pom.txt", unParsedPom);
	}

}
