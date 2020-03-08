package cn.edu.fudan.se.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class HandleProjects {
	public static void parseProjectsPath() {
		JSONArray array = new JSONArray();
		ResultSet highQualityRs = DBUtil.query("SELECT repository_id FROM `repository_high_quality`");
		try {
			while (highQualityRs.next()) {
				int repositoryId = highQualityRs.getInt("repository_id");
				ResultSet releasesIndexRs = DBUtil.query("SELECT release_id FROM `prior_releases_index` where repository_id = " + repositoryId);
				while (releasesIndexRs.next()) {
					int releaseId = releasesIndexRs.getInt("release_id");
					ResultSet releasesRs = DBUtil.query("SELECT * FROM `releases` where id = " + releaseId);
					while (releasesRs.next()) {
						String localAddr = releasesRs.getString("local_addr");
						String prefix =  "releases" + ((releaseId + 9999) / 10000) + "_unzip";
						String[] split = localAddr.split("/");
						String actualAddr = "../data/" + prefix + "/" + split[1] + "/" + split[2] + "/" + split[3].replace(".zip", "");
						JSONObject obj = new JSONObject();
						obj.put("id", releasesRs.getInt("id"));
						obj.put("release_id", releasesRs.getString("release_id"));
						obj.put("repository_id", releasesRs.getInt("repository_id"));
						obj.put("name", releasesRs.getString("name"));
						System.out.println(releasesRs.getTimestamp("created_at"));
						obj.put("created_at", releasesRs.getTimestamp("created_at"));
						obj.put("local_addr", actualAddr);						
						array.add(obj);
					}				
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(array.size());
		JsonFileUtil.save("projects.txt", array);
	}
	
	public static void getProjectsRepo() {
		JSONArray array = new JSONArray();
		ResultSet highQualityRs = DBUtil.query("SELECT * FROM `repository_high_quality`");
		try {
			while (highQualityRs.next()) {
//				int repositoryId = highQualityRs.getInt("repository_id");
				String localAddr = highQualityRs.getString("repos_addr").replace("home/fdse", "..");				
				JSONObject obj = new JSONObject();
				obj.put("id", highQualityRs.getInt("id"));
				obj.put("repository_id", highQualityRs.getInt("repository_id"));
				obj.put("url", highQualityRs.getString("url"));
				obj.put("stars", highQualityRs.getInt("stars"));
				obj.put("commit_count", highQualityRs.getInt("commit_count"));
				obj.put("sizes", highQualityRs.getInt("sizes"));
				obj.put("fork", highQualityRs.getInt("fork"));
				obj.put("local_addr", localAddr);						
				array.add(obj);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(array.size());
		JsonFileUtil.save("projects_repo.txt", array);
	}
	
	public static void saveProjectsPath() {
		FileWriter writer = null;  
        try {     
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件     
            writer = new FileWriter("pro.txt", true);   
            String whole = JsonFileUtil.readJsonFile("projects_repo.txt");		
    		JSONArray array = JSONArray.fromObject(whole);
    		for (int i = 0; i < array.size(); i++) {
    			JSONObject obj = array.getJSONObject(i);
    			String localPath = obj.optString("local_addr");
    			int id = obj.optInt("id");	           
            	File file = new File("curr_result/"+id+".txt");
            	if(file.exists()) {
            		writer.write(id+"\r\n");
            		writer.write(localPath+"\r\n");
            	}            		
            }
            
               
        } catch (IOException e) {     
            e.printStackTrace();     
        } finally {     
            try {     
                if(writer != null){  
                    writer.close();     
                }  
            } catch (IOException e) {     
                e.printStackTrace();     
            }     
        } 
	}
	
	public static void gradleProjects(String path) {
		FileWriter writer = null;  
        try {     
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件     
            writer = new FileWriter("pro_gradle.txt", true);   
            File baseDir = new File(path);
    		File[] list = baseDir.listFiles();
    		for(File li : list) {
    			if(li.isDirectory()) {
    				File[] pjs = li.listFiles();
    				for(File pj : pjs) {
    					if(pj.isDirectory()) {
    						writer.write(pj.getName()+"\r\n");
    					}
    				}
    			}
    		}
               
        } catch (IOException e) {     
            e.printStackTrace();     
        } finally {     
            try {     
                if(writer != null){  
                    writer.close();     
                }  
            } catch (IOException e) {     
                e.printStackTrace();     
            }     
        } 
		
	}
	
	public static void antProjects() {
		String whole = JsonFileUtil.readJsonFile("projects_repo.txt");		
		JSONArray array = JSONArray.fromObject(whole);		
		FileWriter writer = null;  
        try {     
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件     
            writer = new FileWriter("pro_ant.txt", true);  
            for (int i = 0; i < array.size(); i++) {
    			JSONObject obj = array.getJSONObject(i);
    			String localPath = obj.optString("local_addr");
    			int id = obj.optInt("id");	
    			if(id > 12) {
    				System.out.println("localPath : "+localPath);
    				System.out.println("id : "+id);
    				File file = new File(localPath);
    				if(file.exists()) {
    					String[] ivyFiles = getAllIvyFiles(localPath);
        				if(ivyFiles.length > 0) {
        					writer.write(localPath+"\r\n");
        				}
        				System.out.println();
    				}
    				
    			}		
    		}          
        } catch (IOException e) {     
            e.printStackTrace();     
        } finally {     
            try {     
                if(writer != null){  
                    writer.close();     
                }  
            } catch (IOException e) {     
                e.printStackTrace();     
            }     
        } 
		
	}
	
	private static final String[] INCLUDE_ALL_IVYS = new String[]{"**\\ivy.xml"};
	
	public static String[] getAllIvyFiles(String projectPath) {
		final DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(projectPath);
        scanner.setIncludes(INCLUDE_ALL_IVYS);
        scanner.scan();
		return scanner.getIncludedFiles();
	}
	
	public static void totalProjects() {
//		List<String> projs = new ArrayList<String>();
		Map<String,String> projs = new HashMap<String,String>();
		String whole = JsonFileUtil.readJsonFile("pro_gradle.txt");		
		JSONArray array = JSONArray.fromObject(whole);		
		int count = 0;
		
		for(int i=0;i<array.size();i++) {
//			count++;
			String temp = array.getString(i);
			if(temp.startsWith("home/fdse/data/prior_repository/")) {
				temp = temp.substring(32, temp.length());
				System.out.println(temp);
//				if(!projs.contains(temp))
//					projs.add(temp);
				projs.put(temp, "");
			}			
		}
		try {
			Scanner in = new Scanner(new File("pro.txt"));
			while (in.hasNextLine()) {
				String str = in.nextLine();
				String temp = null;
				if(str.startsWith("../data/prior_repository/")) {
					count++;
					temp = str.substring(25);
					System.out.println(temp);
//					if(!projs.contains(temp))
//						projs.add(temp);
					projs.put(temp, "");
				}								
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(projs.size());
		System.out.println(count);
//		File file = new File("pro.txt");
	}
	public static void main(String[] args) throws IOException, XmlPullParserException {
//		HandleProjects.getProjectsRepo();
//		HandleProjects.saveProjectsPath();
//		gradleProjects("H://wangying/gradleProject");
//		antProjects();
		totalProjects();
	}
}
