package cn.edu.fudan.se.pom.handler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import cn.edu.fudan.se.util.DBUtil;

import cn.edu.fudan.se.util.JsonFileUtil;
import cn.edu.fudan.se.util.MyException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PomParser {
	
	public static void main(String[] args) {
//		String[] files = getAllPomFiles("F:\\GP\\high_quality_repos\\JakeWharton\\ActionBarSherlock");
//		PomModel projectPom= new PomModel();
//		projectPom.handleProjectPom("F:\\GP\\high_quality_repos\\JakeWharton\\ActionBarSherlock", new JSONObject());
//		readProjectFile(args[0]);
		runPomParser(args[0]);
		//		getMavenProj();
//		readProjPaths();
//		parseOneProj();
	}
	
	public static void parseOneProj() {
//		String resultPath = "D:/data/rq3_result/";
		String resultPath = "D:/data/test_result/";
		String pomFilePath = "F:/GP/pom/";
		String unsolvedPomFilePath = "F:/GP/pom_unsolved/";
		String serverIp = "http://127.0.0.1:8080";
		
		String projectPath = "E:/Activiti";
		int projectId = -1;
		String url = "https://github.com/Activiti/Activiti";
		ResultSet prs = DBUtil.query("SELECT * FROM `project` where `url` = '" + url + "'");
		try {
			while(prs.next()) {
				projectId = prs.getInt("id");
				break;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("localPath : "+projectPath);
		System.out.println("id : "+projectId);
		JSONArray unParsedPom = new JSONArray();
		PomModel pomModel = new PomModel(resultPath,pomFilePath,unsolvedPomFilePath,serverIp);
		JSONObject newPro = new JSONObject();		
		newPro.put("id", projectId);
		newPro.put("name", projectPath.substring(projectPath.lastIndexOf("/")));
		pomModel.handleProjectPom(projectPath, newPro);				
		for (Map.Entry<String, String[]> pomLib : pomModel.getUnParsedPomTypeLibs().entrySet()) {
			String[] lib = pomLib.getKey().split(" ");
			JSONObject pomObj = new JSONObject();
			pomObj.put("groupId", lib[0]);
			pomObj.put("artifactId", lib[1]);					
			String version = pomLib.getValue()[0];
			pomObj.put("version", version);	
			pomObj.put("module", pomLib.getValue()[1]);	
			pomObj.put("id", projectId);	
			unParsedPom.add(pomObj);
			writeRepo(projectId,pomModel.repositories,"D:/data/test_repo/");
		}
		System.out.println();
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
	
	public static void readProjPaths() {
		String resultPath = "D:/data/rq3_result/";
		String pomFilePath = "F:/GP/pom/";
		String unsolvedPomFilePath = "F:/GP/pom_unsolved/";
		String serverIp = "http://127.0.0.1:8080";
		
		String projPath = "I:/RQ3/projects";
		String[] files = new File(projPath).list();
//		int count = 0;
		for(String name:files) {
//			System.out.println("------------- "+name);
			if(name.contains("__fdse__")) {
//				System.out.println(name);
				String projectPath = projPath + "/" + name;
				int projectId = -1;
				String url = "https://github.com/" + name.replace("__fdse__","/");
				ResultSet prs = DBUtil.query("SELECT * FROM `project` where `url` = '" + url + "'");
				try {
					while(prs.next()) {
						projectId = prs.getInt("id");
						break;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(new File(resultPath+projectId+".txt").exists() || projectId == 148 || projectId == 1105 || projectId == 2075
						)
					continue;
				System.out.println("localPath : "+projectPath);
				System.out.println("id : "+projectId);
				JSONArray unParsedPom = new JSONArray();
				PomModel pomModel = new PomModel(resultPath,pomFilePath,unsolvedPomFilePath,serverIp);
				JSONObject newPro = new JSONObject();		
				newPro.put("id", projectId);
				newPro.put("name", projectPath.substring(projectPath.lastIndexOf("/")));
				pomModel.handleProjectPom(projectPath, newPro);				
				for (Map.Entry<String, String[]> pomLib : pomModel.getUnParsedPomTypeLibs().entrySet()) {
					String[] lib = pomLib.getKey().split(" ");
					JSONObject pomObj = new JSONObject();
					pomObj.put("groupId", lib[0]);
					pomObj.put("artifactId", lib[1]);					
					String version = pomLib.getValue()[0];
					pomObj.put("version", version);	
					pomObj.put("module", pomLib.getValue()[1]);	
					pomObj.put("id", projectId);	
					unParsedPom.add(pomObj);
					writeRepo(projectId,pomModel.repositories,"D:/data/rq3_repo/");
				}
				System.out.println();
			}
//				count ++;
//			System.out.println(name);
		}
//		System.out.println(count);
	}

	public static void getMavenProj() {
		String sql = "SELECT * FROM project WHERE (`type` = 'maven' or `type` = 'maven-gradle') and `stars` > 200";
		ResultSet rs = DBUtil.query(sql);
		JSONArray array = new JSONArray();
		try {
			while(rs.next()) {
				int projectId = rs.getInt("id");
				String url = rs.getString("url");
				url = url.replace("https://github.com/", "").replace("/", "__fdse__");
				String name = url;
				if(new File("C:/gradle200_500/"+url).exists())
					url = "C:/gradle200_500/"+url;
				else if(new File("C:/gradle500/"+url).exists())
					url = "C:/gradle500/"+url;
				else if(new File("D:/gradle_maven200_500/"+url).exists())
					url = "D:/gradle_maven200_500/"+url;
				else if(new File("D:/gradle_maven500/"+url).exists())
					url = "D:/gradle_maven500/"+url;
				else if(new File("E:/maven200_500/"+url).exists())
					url = "E:/maven200_500/"+url;
				else if(new File("E:/maven500/"+url).exists())
					url = "E:/maven500/"+url;
				else
					throw new MyException("not in local : " + url + "( " + projectId + ")");
				JSONObject obj = new JSONObject();
				obj.put("id", projectId);
				obj.put("local_addr", url);
				obj.put("name", name);
				array.add(obj);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(array.size());
		JsonFileUtil.save("maven_or_both.txt", array);
	}

	public static void runPomParser(String file) {
		String resultPath = "C:/data/result/";
		String pomFilePath = "C:/data/pom/";
		String unsolvedPomFilePath = "C:/data/pom_unsolved/";
		String serverIp = "http://127.0.0.1:8080";

		JSONArray unParsedPom = new JSONArray();
		String whole = JsonFileUtil.readJsonFile(file);		
		JSONArray array = JSONArray.fromObject(whole);
		System.out.println(array.size());
		int count = 0;
		int[] intarray = {108, 1103, 1148, 1240, 1261, 138, 1421, 145, 1471, 1490, 15, 1542, 1571, 1593, 1594, 1599, 1610, 164, 1674, 1723, 1733, 1779, 1780, 1843, 1944, 1981, 1982, 2056, 23, 2336, 2606, 2609, 2714, 2737, 28, 288, 2895, 2917, 2957, 2969, 2985, 3038, 3081, 3160, 3212, 328, 3315, 3327, 3329, 3469, 3475, 350, 3533, 359, 3697, 370, 381, 3814, 3824, 3903, 3945, 3964, 4012, 4239, 4274, 4284, 4414, 448, 452, 4744, 478, 4951, 50, 5079, 5145, 5348, 5368, 5399, 5460, 5463, 5502, 5525, 588, 596, 681, 686, 699, 80, 834, 921, 937, 945, 95};
		Arrays.sort(intarray);
//		System.out.println(intarray.length);
		
		for (int i = 0; i < array.size(); i++) {
			JSONObject obj = array.getJSONObject(i);
			String localPath = obj.optString("local_addr");
			int id = obj.optInt("id");
//			if (id == 1943 || id == 2755) {
			int index = Arrays.binarySearch(intarray, id) ;
			if (index > -1 && id == 3160) {
				System.out.println("localPath : " + localPath);
				System.out.println("id : " + id);
				if(new File(resultPath+id+".txt").exists())
					continue;
				PomModel pomModel = new PomModel(resultPath,pomFilePath,unsolvedPomFilePath,serverIp);
				pomModel.handleProjectPom(localPath, obj);
				for (Map.Entry<String, String[]> pomLib : pomModel.getUnParsedPomTypeLibs().entrySet()) {
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
				// readPom(localPath,obj);
				System.out.println();
				writeRepo(id, pomModel.repositories, "C:/data/repo_solve/");
			}
		}
		JsonFileUtil.save("pom.txt", unParsedPom);
	}

	public static void readProjectFile(String file) {
		JSONArray unParsedPom = new JSONArray();
		String whole = JsonFileUtil.readJsonFile(file);
		JSONArray array = JSONArray.fromObject(whole);
		System.out.println(array.size());
//		for (int i = 0; i < array.size(); i++) {
//			JSONObject obj = array.getJSONObject(i);
//			String localPath = obj.optString("local_addr");
//			int id = obj.optInt("id");
//			//- 19
//			//7.25
//			// 19(not exist)
//			// 285 329 436 579 705 837 1082 1152 1316 1340 1637 1761 2208 2266 2629 2671 3473 3806 4282 4348 4390 5060 5205 5408 5517(getPropertiesWithAllProfiles outofmemory)
//			// 1943 2755 网络
//			//7.26
//			//7.29
//			//8.5 145 359 448 2208 2957* 3160* 3824 4348 5205 5408 5460 5502
//			if(id == 1943 || id == 2755) {
////			if(id > 5502 && id != 4274 && id != 5079) {
////			if(id > 1340 && id != 19 && id != 83 && id != 148 && id != 759 && id != 941 && id != 957) {
//				System.out.println("localPath : "+localPath);
//				System.out.println("id : "+id);
//				PomModel pomModel= new PomModel();
//				localPath = localPath.replace("../data/prior_repository/", "");
//				String[] splitedPath = localPath.split("/");
//				if(splitedPath.length == 2) {
//					localPath = splitedPath[0] + "__fdse__" + splitedPath[1];
//				}
//				localPath = "H:/wangying/projects_last_unzips/maven500/" + localPath;
//				System.out.println(localPath);
////				pomModel.handleProjectPom(localPath.replace("../data/prior_repository", "F:/GP/high_quality_repos"), obj);
//				pomModel.handleProjectPom(localPath, obj);
//				for (Map.Entry<String, String[]> pomLib : pomModel.getUnParsedPomTypeLibs().entrySet()) {
//					String[] lib = pomLib.getKey().split(" ");
//					JSONObject pomObj = new JSONObject();
//					pomObj.put("groupId", lib[0]);
//					pomObj.put("artifactId", lib[1]);
//					String version = pomLib.getValue()[0];
//					pomObj.put("version", version);
//					pomObj.put("module", pomLib.getValue()[1]);
//					pomObj.put("id", id);
//					unParsedPom.add(pomObj);
//					writeRepo(id,pomModel.repositories,"E:/data/repo_solve/");
////					writeRepo(id,pomModel.repositories,"repo/");
//				}
////				readPom(localPath,obj);
//				System.out.println();
//			}
//		}
//		JsonFileUtil.save("pom.txt", unParsedPom);
	}

	public static void writeRepo(int projectId,List<String> repos,String path) {
 		File f=new File(path+projectId+".txt");
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(f));
			for(String repo : repos){
	            bw.write(repo);
	            bw.newLine();
	        }
	        bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}

