package cn.edu.fudan.se.pom.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Profile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.pom.handler.PomFileReader;
import cn.edu.fudan.se.pom.handler.PomModel;
import cn.edu.fudan.se.util.JsonFileUtil;

public class UnifyMultiVersions {
	
	private static String TWO_HUNDRED_PLUS = "E:/data/200_plus.txt";
	private static String PROJECT_DIR = "C:/projects_unzips_test/";
	
	private Properties properties = new Properties(); 
	
	public static void main(String[] args) {
//		addProperty("E:\\data\\multiversion\\unify\\pom.xml");
		UnifyMultiVersions uv = new UnifyMultiVersions();
		uv.unify();
	}	
	
	public void unify() {
		Map<String,String> map = getProjPath();
		
		String content = JsonFileUtil.readJsonFile("E:\\data\\multiversion\\unify\\actionresult.json");
		Map<String,Map<String, Map<String,JSONArray>>> wholeMap = (Map<String,Map<String, Map<String,JSONArray>>>)JSONObject.parse(content);
		for(Map.Entry<String, Map<String, Map<String,JSONArray>>> fileMap : wholeMap.entrySet()) {
			String file = fileMap.getKey();
			if(!file.equals("1098.txt"))
				continue;
			System.out.println("+++++++++++++++++++++++++++++" + file);
			String projectPath = PROJECT_DIR + map.get(file.replace(".txt", ""));
			for(Map.Entry<String, Map<String,JSONArray>> jarMAP : fileMap.getValue().entrySet()) {
				String jarName = jarMAP.getKey();
				System.out.println("---------------------------" + jarName);
				String[] jarNameArray = jarName.split("__fdse__");
				ArrayList<String> modifiedVersions = new ArrayList<String>();
				for(Map.Entry<String, JSONArray> subTreeMAP : jarMAP.getValue().entrySet()) {
					System.out.println(subTreeMAP.getKey());
//					System.out.println(subTreeMAP.getValue());
					JSONArray subTree = subTreeMAP.getValue();
					for(int i = 0;i < subTree.size() - 1;i ++) {
						// 需要保存改过的version！！！！！！！！！！！！！！！！
						JSONObject obj = subTree.getJSONObject(i);
//						System.out.println(obj);
						String versionPosition = projectPath + "/" + obj.getString("versionPosition");
//						String declarePosition = projectPath + "/" + obj.getString("declarePosition");
//						boolean isProperty = obj.getBoolean("isProperty");
						String rawVersion = obj.getString("rawVersion");
						String newVersion = obj.getString("action_update_version_symbol");
						String key = versionPosition + "__fdse__" + rawVersion + "__fdse__" + newVersion;
						if(!modifiedVersions.contains(key)) {
							modifyVersion(projectPath, versionPosition, newVersion, rawVersion, jarNameArray);
							modifiedVersions.add(key);
						}
					}
					JSONObject obj = subTree.getJSONObject(subTree.size() - 1);
					String pomPosition = projectPath + "/" + obj.getString("action_update_define_pos");
					String property = obj.getString("action_update_define_version_value");
					String[] propertyArray = property.split("=");
					addProperty(pomPosition, propertyArray[0], propertyArray[1]);
				}
			}
		}
	}
	
	public void modifyVersion(String projectPath, String versionPosition, String newVersion, String rawVersion, String[] jarNameArray) {
		Model model = PomFileReader.parsePomFileToModel(versionPosition);
		if(model == null) {
			// !!!!!!!!!!!!!!!!!!!!!!!!!! 失败
			return;
		}
		if(model.getProperties() != null)
			PomModel.putAllToProperties(this.properties, model.getProperties());	
  		
		List<Dependency> dependencies = model.getDependencies();
		List<Dependency> toModify = parseDependencyFromList(dependencies,false,model,rawVersion,jarNameArray); 
		if (model.getDependencyManagement() != null) {
			dependencies = model.getDependencyManagement().getDependencies();
			toModify.addAll(parseDependencyFromList(dependencies,true,model,rawVersion,jarNameArray));
		}	
		if(toModify.size() > 0) {
			for(Dependency d : toModify) {
				System.out.println(versionPosition + " : " + d.getGroupId() + " " + d.getArtifactId() + " " + d.getVersion() + " " + d.getType() + " " + d.getClassifier());
				d.setVersion(newVersion);
			}
			PomFileWriter.writeModelToPomFile(new File(versionPosition), model);
			System.out.println("Write to " + versionPosition);
			System.out.println("==================================================");
		}
		else {
			System.err.println(versionPosition + ":" + jarNameArray[0] + " " + jarNameArray[1] + " " + jarNameArray[2] + " " + rawVersion);
			System.exit(0);
		}
	}
	
	public List<Dependency> parseDependencyFromList(List list,boolean isDependencyManagement,Model model,String targetVersion,String[] jarNameArray) {
		List<Dependency> toModify = new ArrayList<Dependency>();
		for (int i = 0; i < list.size(); i++) {
			String groupId,artifactId,version,type,classifier = null;
			if (list.get(i) instanceof Dependency) {
				groupId = ((Dependency) list.get(i)).getGroupId();
				artifactId = ((Dependency) list.get(i)).getArtifactId();
				version = ((Dependency) list.get(i)).getVersion();
				type = ((Dependency) list.get(i)).getType();
				classifier = ((Dependency) list.get(i)).getClassifier();
			}
			else 
				continue;								
			
			String[] result = getPropertyValue(model,this.properties,groupId,artifactId,type,classifier);
			if(result[2] == null)
				result[2] = "jar";
			if(result[0].equals(jarNameArray[0]) && result[1].equals(jarNameArray[1]) && result[2].equals(jarNameArray[2]) && version != null && targetVersion.equals(version)) {
				if(jarNameArray.length == 3 && result[3] == null) {
					toModify.add((Dependency) list.get(i));
				}
				else if(jarNameArray.length == 4 && result[3] != null && result[3].equals(jarNameArray[3])) {
					toModify.add((Dependency) list.get(i));
				}
			}
		}
		return toModify;
	}
	
	public String[] getPropertyValue(Model model,Properties properties,String groupId,String artifactId,String type,String classifier) {
		if (groupId != null && groupId.contains("${")) {
			groupId = parseProperty(groupId,model,properties);
		}				
		if (artifactId != null && artifactId.contains("${")) {
			artifactId = parseProperty(artifactId,model,properties);
		}				
		if (type != null && type.contains("${")) {	
			type = parseProperty(type,model,properties);
		}			
		if (classifier != null && classifier.contains("${")) {	
			classifier = parseProperty(classifier,model,properties);
		}
		String[] result = {groupId,artifactId,type,classifier};
		return result;
	}
	
	public String parseProperty(String name,Model model,Properties property) {
		Pattern el = Pattern.compile("\\$\\{(.*?)\\}");
		boolean matched = true;
		while (name != null && matched) {
			Matcher m = el.matcher(name);// el表达式
			matched = false;
			while (m.find()) {
				matched = true;
				if (m.group(1).equals("project.groupId") || m.group(1).equals("pom.groupId") || m.group(1).equals("groupId")) {
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
				} else if (m.group(1).equals("project.version") || m.group(1).equals("pom.version") || m.group(1).equals("version")) {
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
				else if (m.group(1).equals("project.artifactId") || m.group(1).equals("pom.artifactId") || m.group(1).equals("artifactId")) {
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
				else if (m.group(1).equals("project.packaging") || m.group(1).equals("pom.packaging") || m.group(1).equals("packaging")) {
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
				else if (property != null && property.getProperty(m.group(1)) != null) {
					String newName = name.replace(m.group(0), property.getProperty(m.group(1)));
					if(name.equals(newName))
						matched = false;
					else
						name = newName;
				}
				else
					matched = false;
			}
		}
		return name;
	}
	
	public static void addProperty(String pomFile, String propertyName, String propertyValue) {
		Model model = PomFileReader.parsePomFileToModel(pomFile);
		if(model != null) {
			Properties properties = model.getProperties();
//			for (String key : properties.stringPropertyNames()) {
//	            System.out.println(key + "=" + properties.getProperty(key));
//	        }
			model.addProperty(propertyName, propertyValue);
			PomFileWriter.writeModelToPomFile(new File(pomFile), model);
		}
		
	}
	
	public static Map<String,String> getProjPath() {
		Map<String,String> map = new HashMap<String,String>();
		String content = JsonFileUtil.readJsonFile(TWO_HUNDRED_PLUS);
		JSONArray array = JSONArray.parseArray(content);
		for(int i = 0; i < array.size(); i ++) {
			JSONObject obj = array.getJSONObject(i);
			int projectId = obj.getInteger("id");
			String name = obj.getString("name");
			map.put(""+projectId, name);
		}
		return map;
	}
}
