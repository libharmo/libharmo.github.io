package cn.edu.fudan.se.pom.handler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.util.FastJsonUtil;
import cn.edu.fudan.se.util.FileUtil;
import cn.edu.fudan.se.util.JsonFileUtil;

public class PomCoordinate {
	
	public static void main(String[] args) {
		getAllModules();
	}
	
	public static void getAllModules() {
		String whole = JsonFileUtil.readJsonFile("E:/data/200_plus.txt");
		JSONArray array = JSONArray.parseArray(whole);
		System.out.println(array.size());
		for (int i = 0; i < array.size(); i++) {
			JSONObject obj = array.getJSONObject(i);
			String name = obj.getString("name");
			int id = obj.getIntValue("id");
			String projPath = "C:/projects_unzips/" + name;
			if(new File(projPath).exists()) {
				System.out.println(id + " : " + projPath);
				String[] pomFiles = FileUtil.getAllPomFiles(projPath);
				JSONArray poms = new JSONArray();
				for(String pomFile : pomFiles) {
					pomFile = pomFile.replace("\\", "/").replace("pom.xml", "");
					if(pomFile.endsWith("/"))
						pomFile = pomFile.substring(0,pomFile.length()-1);
					poms.add(pomFile);
//					System.out.println(pomFile);
				}
				FastJsonUtil.writeJson("E:/data/RQ1/api_call/modules/" + id + ".txt", poms, true);;
			}	
		}
	}

	public static void parsePomForOneProj(String outputPath, int projectId, String projectPath) {
		Map<String, String> pomMap = new HashMap<String, String>();
		String[] pomFiles = FileUtil.getAllPomFiles(projectPath);
		System.out.println("pom file size : "+pomFiles.length);
		if(pomFiles.length > 0) {
			int count = 0;
			for(String pomFile : pomFiles) {
				if(pomFile.endsWith("pom.xml")) {					
					count ++;
					pomFile = pomFile.replace("\\", "/");
					System.out.println(pomFile);
					Model model = PomFileReader.parsePomFileToModel(projectPath+"/"+pomFile);
					if(model != null) {						
						String artifactId = model.getArtifactId();						
						String packaging = model.getPackaging();
						
						String groupId = model.getGroupId();
						if(groupId == null) {
							if(model.getParent() != null) {
								groupId = model.getParent().getGroupId();
							}
						}
						String version = model.getVersion();
						if(version == null) {
							if(model.getParent() != null) {
								version = model.getParent().getVersion();
							}
						}
						if (groupId != null && groupId.contains("${")) {
							groupId = parseProperty(groupId,model,projectPath,pomFile);
						}				
						if (artifactId != null && artifactId.contains("${")) {
							artifactId = parseProperty(artifactId,model,projectPath,pomFile);
						}				
						if (version != null && version.contains("${")) {	
							version = parseProperty(version,model,projectPath,pomFile);
						}
						if (packaging != null && packaging.contains("${")) {	
							packaging = parseProperty(packaging,model,projectPath,pomFile);
						}			
						String key = groupId + ":" + artifactId + ":" + packaging + ":" + version;
						pomMap.put(key, pomFile);
					}
				}
			}
		}
		JSONObject mapObj = JSONObject.parseObject(JSON.toJSONString(pomMap));
		FastJsonUtil.writeJson(outputPath + projectId + ".txt", mapObj, true);
	}
	
	public static String parseProperty(String name,Model model,String projectPath, String path) {
		Pattern el = Pattern.compile("\\$\\{(.*?)\\}");
		boolean matched = true;
		while (name != null && matched) {
			Matcher m = el.matcher(name);// el表达式
			matched = false;
			while (m.find()) {
				matched = true;
				if (m.group(1).equals("project.groupId") || m.group(1).equals("pom.groupId")) {
					String projectGroupId = model.getGroupId();
			        for (Parent parent = model.getParent(); projectGroupId == null && model.getParent() != null;parent = model.getParent())
			        	projectGroupId = parent.getGroupId();
					if (projectGroupId != null) {
						String newName = name.replace(m.group(0), projectGroupId);
						if(name.equals(newName))
							matched = false;
						else
							name = newName;
					}
					else
						matched = false;
				} else if (m.group(1).equals("project.version") || m.group(1).equals("pom.version")) {
					String projectVersion = model.getVersion();
			        for (Parent parent = model.getParent(); projectVersion == null && model.getParent() != null;parent = model.getParent())
			        	projectVersion = parent.getVersion();
					if (projectVersion != null) {
						String newName = name.replace(m.group(0), projectVersion);
						if(name.equals(newName) || newName.contains(name))
							matched = false;
						else
							name = newName;
					}
					else
						matched = false;
				}
				else if (m.group(1).equals("project.artifactId") || m.group(1).equals("pom.artifactId")) {
					if (model.getArtifactId() != null) {
						String newName = name.replace(m.group(0), model.getArtifactId());						
						if(name.equals(newName))
							matched = false;
						else
							name = newName;
					}
					else
						matched = false;
				}
				else if (m.group(1).equals("project.packaging") || m.group(1).equals("pom.packaging")) {
					if (model.getPackaging() != null) {
						String newName = name.replace(m.group(0), model.getPackaging());						
						if(name.equals(newName))
							matched = false;
						else
							name = newName;
					}
					else {
						name = "jar";
						matched = false;
					}
				}
				else if (m.group(1).equals("project.parent.version") || m.group(1).equals("parent.version")) {
					if(model.getParent() != null && model.getParent().getVersion() != null)
						name = name.replace(m.group(0), model.getParent().getVersion());
					else
						matched = false;
				}
				else if (m.group(1).equals("project.parent.groupId") || m.group(1).equals("parent.groupId")) {
					if(model.getParent() != null && model.getParent().getGroupId() != null)
						name = name.replace(m.group(0), model.getParent().getGroupId());
					else
						matched = false;
				}
				else if (m.group(1).equals("project.parent.artifactId") || m.group(1).equals("parent.artifactId")) {
					if(model.getParent() != null && model.getParent().getArtifactId() != null)
						name = name.replace(m.group(0), model.getParent().getArtifactId());
					else
						matched = false;
				}
				else if (m.group(1).equals("project.prerequisites.maven")) {
					if(model.getPrerequisites() != null && model.getPrerequisites().getMaven() != null)
						name = name.replace(m.group(0), model.getPrerequisites().getMaven());
					else
						matched = false;
				}
				else if (model.getProperties() != null && model.getProperties().getProperty(m.group(1)) != null) {
					String newName = name.replace(m.group(0), model.getProperties().getProperty(m.group(1)));
					if(name.equals(newName))
						matched = false;
					else
						name = newName;
				}
				else if (model.getParent() != null) {
					String newName = parseFromParent(model, name, m, projectPath, path);
					if(name.equals(newName))
						matched = false;
					else
						name = newName;
				}
				else {
					matched = false;
				}
					
			}
		}
		return name;
	}
	
	public static String parseFromParent(Model model, String name, Matcher m, String projectPath, String path) {
		// 处理parent
		String parentGroupId = model.getParent().getGroupId();
		String parentArtifactId = model.getParent().getArtifactId();
		String parentVersion = model.getParent().getVersion();

		String parentPath = model.getParent().getRelativePath().replace("\\", "/");
		if (parentPath.equals(""))
			return name;
		else {
			if (!(parentPath.endsWith("pom.xml") || parentPath.endsWith(".xml"))) {
				if (!parentPath.endsWith("/"))
					parentPath += "/";
				parentPath += "pom.xml";
			}

			if (parentPath.startsWith("/"))
				parentPath = parentPath.substring(1);
			String prefix = path;

			if (prefix.endsWith(".xml")) {
				int lastPathSeparatorIndex = prefix.lastIndexOf("/");
				if (lastPathSeparatorIndex < 0) {
					prefix = "";
				} else
					prefix = prefix.substring(0, lastPathSeparatorIndex);
				if (prefix.endsWith("/") || prefix.endsWith("\\"))
					prefix = prefix.substring(0, prefix.length() - 1);
				String parentWholePath = prefix + "/" + parentPath;
				if (prefix.equals("")) {
					parentWholePath = parentPath;
				}
				System.out.println("parent: " + projectPath + "/" + parentWholePath);
				if (new File(projectPath + "/" + parentWholePath).exists() && isParent(
						projectPath + "/" + parentWholePath, parentGroupId, parentArtifactId, parentVersion)) {
					Model tempModel = PomFileReader.parsePomFileToModel(projectPath + "/" + parentWholePath);
					if(tempModel != null) {
						if (tempModel.getProperties() != null
								&& tempModel.getProperties().getProperty(m.group(1)) != null) {
							String newName = name.replace(m.group(0), tempModel.getProperties().getProperty(m.group(1)));
							return newName;
						}
						else {
							if(tempModel.getParent() != null)
								return parseFromParent(tempModel, name, m, projectPath, parentWholePath);
						}
					}					
				}
			} else if (prefix.endsWith(".pom")) {
				return name;
			}
		}
		return name;
	}
	
	public static boolean isParent(String pomPath,String groupId,String artifactId,String version) {	
		String completePath = pomPath;
		Model model = PomFileReader.parsePomFileToModel(completePath);	
		if(model == null)
			return false;
		
		String parentGroupId = model.getGroupId();
		String parentArtifactId = model.getArtifactId();
		String parentVersion = model.getVersion();
//		System.out.println(parentGroupId +" "+parentArtifactId+" "+parentVersion);
//		System.out.println(groupId +" "+artifactId+" "+version);
		if(parentGroupId != null && groupId != null && !parentGroupId.equals(groupId))
			return false;
		if(parentArtifactId != null && artifactId != null && !parentArtifactId.equals(artifactId))
			return false;
		if(parentVersion != null && version != null && !parentVersion.equals(version) && !version.equals("@project.version@"))
//		if(parentVersion != null && version != null && !parentVersion.equals(version))
			return false;
		return true;
	}	
}
