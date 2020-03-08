package cn.edu.fudan.se.pom.handler;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.util.ExecuteCmd;
import cn.edu.fudan.se.util.DBUtil;
import cn.edu.fudan.se.util.FileUtil;
import cn.edu.fudan.se.util.JsonFileUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PomParserWithCommit {
	
	public static String PROJECT_DIR;
	public static String COMMITS_PATH;
	public static String REPO_PATH;
	public static Map<Integer,String> projs = new HashMap();
	 
	public static void getUpdateCommitsOfProject() {
		List<Integer> projIds = new ArrayList<>();		
		try {
			ResultSet prs = DBUtil.query("SELECT distinct(project_id) FROM `lib_update`");
			while(prs.next()) {
				int projectId = prs.getInt("project_id");
				projIds.add(projectId);
			}
			System.out.println(projIds.size());
			JSONArray result = new JSONArray();
			for(int projectId : projIds) {
				System.out.println(projectId);
				List<String> commits = new ArrayList<>();
				ResultSet rs = DBUtil.query("SELECT distinct(prev_commit) FROM `lib_update` where `project_id`= " + projectId + " union SELECT distinct(curr_commit) FROM `lib_update` where `project_id`= " + projectId);
				while (rs.next()) {
					String commitId = rs.getString("prev_commit");
					commits.add(commitId);
				}	
				String path = null;
				ResultSet project = DBUtil.query("SELECT * FROM `project` where `id` = " + projectId);
				while(project.next()) {
					 String url = project.getString("url");
					 path = url.replace("https://github.com/", "");
					 break;
				}
				JSONObject obj = new JSONObject();
				obj.put("id", projectId);
				obj.put("name", path);
//				JSONArray array = JSONArray.fromObject(commits)parseArray(JSON.toJSONString(commits));
				JSONArray array = JSONArray.fromObject(commits);
				obj.put("commits", array);
				result.add(obj);
			}
			JsonFileUtil.save("D:/data/commits.txt", result);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public static void main(String[] args) {
//		getUpdateCommitsOfProject();
		
		PROJECT_DIR = "F:/";
		COMMITS_PATH = "D:/data/commits.txt";
		REPO_PATH = "D:/data/repo/";
		String resultPath = "D:/data/result/";
		String pomFilePath = "F:/GP/pom/";
		String unsolvedPomFilePath = "F:/GP/pom_unsolved/";
		String serverIp = "http://127.0.0.1:8080";

		int id = Integer.parseInt(args[0]);
		String commit = args[1];
		parserForCommit(id,commit,resultPath,pomFilePath,unsolvedPomFilePath,serverIp);
//		group();
	}
	
	public static void group() {
		String str = JsonFileUtil.readJsonFile("D:/data/commits.txt");
		JSONArray carray = JSONArray.fromObject(str);
		System.out.println(carray.size());
//		for(int i=0;i<carray.size();i++) {
//			JSONObject obj = carray.getJSONObject(i);
//			int project = obj.getInt("id");
//			JSONArray commits = obj.getJSONArray("commits");
//			for(int c=0;c<commits.size();c++) {
//				String commit = commits.getString(c);
//				System.out.println(commit);
//				String cmd = "java -jar pom.jar "+project+" "+commit;
//				FileUtil.appendFile("D:\\data\\batch\\"+project+".sh", cmd);
//			}
//		}
	}
	
	public static void parserForCommit(int project,String commit,String resultPath,String pomFilePath,String unsolvedPomFilePath,String serverIp) {
		if(new File(resultPath+project+"_"+commit+".txt").exists())
			return;
		JSONArray unParsedPom = new JSONArray();
		String content = JsonFileUtil.readJsonFile("proj_in_usage.txt");
		JSONArray array = JSONArray.fromObject(content);
		for(int i =0;i<array.size();i++) {
			JSONObject obj = array.getJSONObject(i);
			int projectId = obj.getInt("id");
			String localAddr = obj.getString("local_addr");
//			if(localAddr.startsWith("F:/wangying/projects_last_unzips/"))
//				localAddr = "C:/"+localAddr.replace("F:/wangying/projects_last_unzips/","");
			
//			localAddr = PROJECT_DIR + localAddr.replace("F:/wangying/projects_last_unzips/", "").replace("C:/", "").replace("D:/", "")
//					.replace("E:/", "").replace("F:/", "").replace("gradle_maven200_500/", "").replace("gradle_maven500/", "").replace("maven200_500/", "").replace("maven500/", "").replace("gradle200_500/", "").replace("gradle500/", "");
			
			
			localAddr = PROJECT_DIR + localAddr.replace("F:/wangying/projects_last_unzips/","").replace("C:/","").replace("D:/","").replace("E:/","").replace("F:/","");
			
			projs.put(projectId, localAddr);
		}
		
		String projectPath = projs.get(project);
		if(projectPath == null) {
			ResultSet rs = DBUtil.query("SELECT url FROM `project` where `id`= " + project);
			try {
				while (rs.next()) {
					String url = rs.getString("url");
					url = url.replace("https://github.com/", "").replace("/", "__fdse__");
					if(new File("F:/gradle_maven200_500/"+url).exists())
						projectPath = "F:/gradle_maven200_500/"+url;
					else if(new File("F:/gradle_maven500/"+url).exists())
						projectPath = "F:/gradle_maven500/"+url;
					else if(new File("F:/gradle200_500/"+url).exists())
						projectPath = "F:/gradle200_500/"+url;
					else if(new File("F:/gradle__500/"+url).exists())
						projectPath = "F:/gradle_500/"+url;
					else if(new File("F:/maven200_500/"+url).exists())
						projectPath = "F:/maven200_500/"+url;
					else if(new File("F:/maven500/"+url).exists())
						projectPath = "F:/maven500/"+url;
					break;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		if(projectPath == null) {
			FileUtil.appendLine("C:/Users/yw/Desktop/commit_pom/null.txt", project+" "+commit);
			return;
		}
		System.out.println("localPath : "+projectPath);
		System.out.println("id : "+project);
		System.out.println("commit : "+commit);		
		boolean success = ExecuteCmd.checkout(projectPath, commit);
		
		PomModel pomModel = new PomModel(resultPath,pomFilePath,unsolvedPomFilePath,serverIp);
		JSONObject newPro = new JSONObject();		
		newPro.put("id", project);
		newPro.put("name", projectPath.substring(projectPath.lastIndexOf("/")));
		newPro.put("commit", commit);
		pomModel.handleProjectPom(projectPath, newPro, commit);				
		for (Map.Entry<String, String[]> pomLib : pomModel.getUnParsedPomTypeLibs().entrySet()) {
			String[] lib = pomLib.getKey().split(" ");
			JSONObject pomObj = new JSONObject();
			pomObj.put("groupId", lib[0]);
			pomObj.put("artifactId", lib[1]);					
			String version = pomLib.getValue()[0];
			pomObj.put("version", version);	
			pomObj.put("module", pomLib.getValue()[1]);	
			pomObj.put("id", project);	
			unParsedPom.add(pomObj);
			writeRepo(project,pomModel.repositories,REPO_PATH);
		}
		System.out.println();
	}

	public static void parserForCommit(String resultPath,String pomFilePath,String unsolvedPomFilePath,String serverIp) {
		JSONArray unParsedPom = new JSONArray();
		String content = JsonFileUtil.readJsonFile("proj_in_usage.txt");
		JSONArray array = JSONArray.fromObject(content);
		for(int i =0;i<array.size();i++) {
			JSONObject obj = array.getJSONObject(i);
			int projectId = obj.getInt("id");
			String localAddr = obj.getString("local_addr");
//			if(localAddr.startsWith("F:/wangying/projects_last_unzips/"))
//				localAddr = "C:/"+localAddr.replace("F:/wangying/projects_last_unzips/","");
			
//			localAddr = PROJECT_DIR + localAddr.replace("F:/wangying/projects_last_unzips/", "").replace("C:/", "").replace("D:/", "")
//					.replace("E:/", "").replace("F:/", "").replace("gradle_maven200_500/", "").replace("gradle_maven500/", "").replace("maven200_500/", "").replace("maven500/", "").replace("gradle200_500/", "").replace("gradle500/", "");
			
			
			localAddr = PROJECT_DIR + localAddr.replace("F:/wangying/projects_last_unzips/","").replace("C:/","").replace("D:/","").replace("E:/","").replace("F:/","");
			
			projs.put(projectId, localAddr);
		}
		
		String str = JsonFileUtil.readJsonFile(COMMITS_PATH);
		JSONArray carray = JSONArray.fromObject(str);
		for(int i=0;i<carray.size();i++) {
			JSONObject obj = carray.getJSONObject(i);
			int project = obj.getInt("id");
			if(project != 4) {
				continue;
			}
			String projectPath = projs.get(project);
			System.out.println(project);
			System.out.println(projectPath);
			JSONArray commits = obj.getJSONArray("commits");
			for(int c=0;c<commits.size();c++) {
				String commit = commits.getString(c);
				System.out.println(commit);
				if(new File(resultPath+project+"_"+commit+".txt").exists())
					continue;
				boolean success = ExecuteCmd.checkout(projectPath, commit);
				System.out.println("localPath : "+projectPath);
				System.out.println("id : "+project);
				PomModel pomModel = new PomModel(resultPath,pomFilePath,unsolvedPomFilePath,serverIp);
				pomModel.handleProjectPom(projectPath, obj, commit);				
				for (Map.Entry<String, String[]> pomLib : pomModel.getUnParsedPomTypeLibs().entrySet()) {
					String[] lib = pomLib.getKey().split(" ");
					JSONObject pomObj = new JSONObject();
					pomObj.put("groupId", lib[0]);
					pomObj.put("artifactId", lib[1]);					
					String version = pomLib.getValue()[0];
					pomObj.put("version", version);	
					pomObj.put("module", pomLib.getValue()[1]);	
					pomObj.put("id", project);	
					unParsedPom.add(pomObj);
					writeRepo(project,pomModel.repositories,REPO_PATH);
				}
				System.out.println();
//				break;
			}
		}
	}
	
	public static void writeRepo(int projectId,List<String> repos,String path) {
		for(String repo : repos){
			FileUtil.appendLine(path+projectId+".txt", repo);
        }
		
// 		File f=new File(path+projectId+".txt");
//		BufferedWriter bw;
//		try {
//			bw = new BufferedWriter(new FileWriter(f));
//			for(String repo : repos){
//	            bw.write(repo);
//	            bw.newLine();
//	        }
//	        bw.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}	
	}
}
