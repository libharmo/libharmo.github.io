package cn.edu.fudan.se.pom.handler;

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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import cn.edu.fudan.se.util.FastJsonUtil;
import cn.edu.fudan.se.util.FileUtil;
import cn.edu.fudan.se.util.MyException;

public class LocateDependencyPosition {
	static String MV_DIR = "E:/data/multiversion/";
	static String PROJ_DIR = "C:/projects_unzips/";
	private static String POM_FILE_PATH = "E:/data/multiversion/pom/";
	
	private static String OUTPUT_DIR = "E:/data/multiversion/output/";
	
	private String projectPath;
	private String module = null;
//	private Properties properties = new Properties();
	private List<String> libsToLocate;
	
	private List<String> fileList = new ArrayList<String>();
	private Map<String, Properties> propertiesInFile = new HashMap<String, Properties>();
			
	private Map<String,Map<String,VersionPosition>> libVerPairs= new HashMap<>();
	private Map<String,Map<String,VersionPosition>> libVerPairsInPom= new HashMap<>();
	private Map<String,Map<String,VersionPosition>> libVerPairsInManagement= new HashMap<>();
	
	
	public static void main(String[] args) {
		Map<String, String> projMap = getProjMap();
		
		String content = FileUtil.read(MV_DIR + "mv_info_maven_total.txt");
		Map<String, Map<String,List<String>>> map = (Map)JSON.parse(content);
//		System.out.println(map.size());
		for(Map.Entry<String, Map<String,List<String>>> entry: map.entrySet()) {
			
			int projectId = Integer.parseInt(entry.getKey());			
//			if(new File(OUTPUT_DIR + projectId + ".txt").exists() || projectId == 3323 || projectId == 2923)
//				continue;
//			if(projectId != 1107)
//				continue;
			System.out.println("+++++++++++++++++++++" + projectId);
			String projectPath = PROJ_DIR + projMap.get(projectId + "");
//			if(!new File(projectPath).exists()) {
//				System.err.println("not exists : " + projectPath);
//				System.exit(0);
//			}
			
			Map<String,List<String>> pomMap = entry.getValue();
			JSONObject projObj = new JSONObject();
			for(Map.Entry<String,List<String>> pomEntry: pomMap.entrySet()) {
				String pomPath = pomEntry.getKey();				
				System.out.println(pomPath);
				List<String> newLibs = new ArrayList<String>();
				List<String> libs = pomEntry.getValue();
				for(String lib : libs) {
					String[] libArray = lib.split("__fdse__");
					String key = libArray[0] + "__fdse__" + libArray[1] + "__fdse__" + libArray[2];
					String version = libArray[3];
					if(libArray.length == 5) {
						key += "__fdse__" + libArray[3];
						version = libArray[4];
//						System.out.println(key);
//						System.out.println(version);
					}
					newLibs.add(key);
				}
//				
				Model tempModel = PomFileReader.parsePomFileToModel(projectPath+"/"+pomPath);
				if(tempModel != null) {
					LocateDependencyPosition ldp = new LocateDependencyPosition(projectPath);
					JSONObject libsInOnePom = ldp.parseOnePom(tempModel, pomPath, newLibs);
					projObj.put(pomPath, libsInOnePom);
//					break;
				}					
			}
			FastJsonUtil.writeJson(OUTPUT_DIR + projectId + ".txt", projObj, true);
		}
	}
	
	public LocateDependencyPosition(String projPath) {
		this.projectPath = projPath;
	}
	
	public JSONObject parseOnePom(Model model, String pomPath, List<String> libsToLocate) {
		this.module = module;
		this.libsToLocate = libsToLocate;
		fileList = new ArrayList<String>();
		propertiesInFile = new HashMap<String, Properties>();
//		this.properties = new Properties();
		this.libVerPairs= new HashMap<>();
		this.libVerPairsInPom= new HashMap<>();
		this.libVerPairsInManagement= new HashMap<>();
		
//		for(String temp:this.libsToLocate) {
//			System.out.println(temp);
//		}
		
		readModel(model,pomPath,false,false);	
				
		
		while(!getVersionFromPomLibOrParent(model)) {						
		}		
		JSONObject libs = JSONObject.parseObject(JSON.toJSONString(this.libVerPairs,SerializerFeature.WriteMapNullValue));
		return libs;
	}
	
	private boolean getVersionFromPomLibOrParent(Model model) {
		boolean result = true;
		for (Map.Entry<String, Map<String,VersionPosition>> entry : this.libVerPairs.entrySet()) {
			String libStr = entry.getKey();
			String[] lib = libStr.split("__fdse__");
			String type = lib[2];
//			if(lib.length == 3)
//				type = lib[2];
			for (Map.Entry<String, VersionPosition> value : entry.getValue().entrySet()) {
				String version = value.getKey();
				VersionPosition originVp = value.getValue();			
				if(version == null) {
//					boolean solved = false;						
					if(this.libVerPairsInPom.get(libStr) != null) {
						for (Map.Entry<String, VersionPosition> pomLib : this.libVerPairsInPom.get(libStr).entrySet()) {
							if(pomLib.getKey() != null) {								
								String newVersion = pomLib.getKey();
								VersionPosition newVp = pomLib.getValue();
								if(newVersion.contains("${")) {
									String[] temp = parseProperty(newVp.getDeclarePosition(),newVersion,model);
									newVersion = temp[0];
									newVp.setPropertyPosition(temp[1]);
									newVp.setPropertyName(temp[2]);
									newVp.setPropertyValue(temp[3]);
								}
								if(!this.libVerPairs.containsKey(newVersion)) {
									newVp.setDeclarePosition(originVp.getDeclarePosition());
									this.libVerPairs.get(libStr).put(newVersion, newVp);
									this.libVerPairs.get(libStr).remove(version);	
//									solved = true;
									if(type != null && type.equals("pom")) {
										result = false;
//										System.out.println("=================pom last result:"+lib[0]+" "+lib[1]+" "+version+" "+module);
										handlePomType(lib[0],lib[1],version,true,false);
									}
//									break;
								}								
							}
						}
					}	
//					System.out.println(this.libVerPairsInManagement.get(libStr) == null);
					if(this.libVerPairsInManagement.get(libStr) != null) {
						for (Map.Entry<String, VersionPosition> parentLib : this.libVerPairsInManagement.get(libStr).entrySet()) {
							if(parentLib.getKey() != null) {								
								String newVersion = parentLib.getKey();
								VersionPosition newVp = parentLib.getValue();
								if(newVersion.contains("${")) {
									String[] temp = parseProperty(newVp.getDeclarePosition(),newVersion,model);
									newVersion = temp[0];
									newVp.setPropertyPosition(temp[1]);
									newVp.setPropertyName(temp[2]);
									newVp.setPropertyValue(temp[3]);
								}
								if(!this.libVerPairs.containsKey(newVersion)) {
									newVp.setDeclarePosition(originVp.getDeclarePosition());
									this.libVerPairs.get(libStr).put(newVersion, newVp);
									this.libVerPairs.get(libStr).remove(version);	
									if(type != null && type.equals("pom")) {
										result = false;
//										System.out.println("=================pom last result:"+lib[0]+" "+lib[1]+" "+version+" "+module);
										handlePomType(lib[0],lib[1],version,true,false);
									}
//									break;
								}
								
							}
						}
					}	
				}
			}			
		}
		return result;
	}
	
	public static Map<String, String> getProjMap() {
		Map<String, String> projMap = new HashMap<String, String>();
		String content = FileUtil.read("E:/data/200_plus.txt");
		JSONArray array = JSONArray.parseArray(content);
		for(int i=0;i<array.size();i++) {
			JSONObject obj = array.getJSONObject(i);
			String id = obj.getString("id");
			String name = obj.getString("name");
			projMap.put(id, name);
		}
		return projMap;
	}
	
	public boolean isParent(String pomPath,String groupId,String artifactId,String version) {	
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
	
	public void readModel(Model model,String path,boolean isPomLib,boolean isParent) {				
		if(model.getProperties() != null) {
			if(this.fileList.contains(path)) {
//				try {
//					throw new MyException();
//				} catch (MyException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					System.exit(0);
//				}
			}
			else {
				this.fileList.add(path);
				this.propertiesInFile.put(path, model.getProperties());
//				System.out.println(path);
//				System.out.println(model.getProperties().getProperty("dep.testng.version"));
			}			
		}
//			putAllToProperties(this.properties, model.getProperties());	
		
		//处理parent
		if(model.getParent() != null) {	
//			System.out.println("path : "+path);						
			String parentGroupId = model.getParent().getGroupId();
			String parentArtifactId = model.getParent().getArtifactId();
			String parentVersion = model.getParent().getVersion();
			
			String parentPath = model.getParent().getRelativePath().replace("\\", "/");
			if(parentPath.equals(""))			
				handlePomType(parentGroupId,parentArtifactId,parentVersion,true,true);
			else {
				if(!(parentPath.endsWith("pom.xml") || parentPath.endsWith(".xml"))) {
//				if(!parentPath.endsWith("pom.xml")) {
					if(!parentPath.endsWith("/"))
						parentPath+="/";
					parentPath+="pom.xml";
				}				

				if(parentPath.startsWith("/"))
					parentPath = parentPath.substring(1);
				String prefix = path;
				
				if(prefix.endsWith(".xml")) {
					int lastPathSeparatorIndex = prefix.lastIndexOf("/");
					if(lastPathSeparatorIndex<0) {
						prefix = "";
					}
					else 
						prefix = prefix.substring(0, lastPathSeparatorIndex);
//						prefix = prefix.substring(0, prefix.length()-7);
					if(prefix.endsWith("/")||prefix.endsWith("\\"))
						prefix = prefix.substring(0, prefix.length()-1);
//					System.out.println(prefix);
					String parentWholePath = prefix+"/"+parentPath;	
					if(prefix.equals("")) {
						parentWholePath = parentPath;	
					}
					System.out.println("parent: "+this.projectPath+"/"+parentWholePath);
					if(new File(this.projectPath+"/"+parentWholePath).exists()
							&& isParent(this.projectPath+"/"+parentWholePath,parentGroupId,parentArtifactId,parentVersion)) {							
						Model tempModel = PomFileReader.parsePomFileToModel(this.projectPath+"/"+parentWholePath);
						if(tempModel != null) {						
							readModel(tempModel,parentWholePath,false,true);		
						}
					}						
					else
						handlePomType(parentGroupId,parentArtifactId,parentVersion,true,true);				
				}
				else if(prefix.endsWith(".pom")) {
					handlePomType(parentGroupId,parentArtifactId,parentVersion,true,true);						
				}
			}
		}
  		
		if(!isPomLib) {
			List<Dependency> dependencies = model.getDependencies();
			parseDependencyFromList(path,dependencies,false,model,isPomLib,isParent); 
		}
		if(isPomLib || isParent) {
			if (model.getDependencyManagement() != null) {
				List<Dependency> dependencies = model.getDependencyManagement().getDependencies();
				parseDependencyFromList(path,dependencies,true,model,isPomLib,isParent);
			}
		}			
	}
	
	public void parseDependencyFromList(String path,List list,boolean isDependencyManagement,Model model,boolean isPomLib,boolean isParent) {	
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
//			System.out.println(groupId+" "+artifactId+" "+version+" "+type+" "+classifier);
			
//			if(groupId.equals("org.mortbay.jetty.alpn")&&artifactId.equals("alpn-boot")) {
//				System.out.println("==============================="+this.propertiesInProfile.size());
//				for(int t = 0 ;t<this.propertiesInProfile.size();t++) {
//					List<Properties> temp = this.propertiesInProfile.get(t);
//					System.out.print(temp.size()+" ");
//				}
//				System.out.println();
//			}		
									
			String[] result = getPropertyValue(path,model,groupId,artifactId,version,type,classifier);
			boolean isProperty = false;
			if(result[0].equals("y"))
				isProperty = true;
			
			String rawVersion = version,declarePosition = path,versionPosition = path;
			String propertyPosition = result[1],propertyName = result[2],propertyValue = result[3];			
			VersionPosition vp = new VersionPosition(rawVersion,declarePosition,isProperty,versionPosition,propertyPosition,propertyName,propertyValue);			

			String newVersion = result[4+2];
			String newType = result[4+3];
			String key = result[4+0] +"__fdse__"+result[4+1];
			if(result[4+3] != null)
				key += "__fdse__"+result[4+3];
			else
				key += "__fdse__jar";
			if(result[4+4] != null)
				key += "__fdse__" + result[4+4];	
//			System.out.println(key);
			if(newType != null) {				
				if(newType.equals("pom")) {
					String scope = ((Dependency) list.get(i)).getScope();
					if(!isDependencyManagement) {
						handlePomType(result[4+0],result[4+1],newVersion,true,false);
					}
					else if(isDependencyManagement && scope != null && scope.equals("import")) {
//						System.out.println("DependencyManagement:    " + pomFilePath + result[0]+" "+result[1]+" "+newVersion+".pom");
						handlePomType(result[4+0],result[4+1],newVersion,true,false);
					}
					else if(isPomLib)
						addToLibVerPairs(this.libVerPairsInPom, key,newVersion, vp, true);
					else
						addToLibVerPairs(this.libVerPairsInManagement,key,newVersion, vp, true);
					
				}
				else if(!newType.equals("jar")){
					if(!isDependencyManagement) {
						
						if(this.libsToLocate.contains(key)) {
//							System.out.println(key);
							addToLibVerPairs(this.libVerPairs,key,newVersion, vp, false);
						}
					}	
					else if(isPomLib)
						addToLibVerPairs(this.libVerPairsInPom, key,newVersion, vp, true);
					else
						addToLibVerPairs(this.libVerPairsInManagement,key,newVersion, vp, true);
				}
			}
			if(!isDependencyManagement) {
				if(this.libsToLocate.contains(key)) {
//					System.out.println(key);
					addToLibVerPairs(this.libVerPairs,key,newVersion, vp, false);
				}
			}
			else if(isPomLib)
				addToLibVerPairs(this.libVerPairsInPom,key,newVersion, vp, true);
			else
				addToLibVerPairs(this.libVerPairsInManagement,key,newVersion, vp, true);	
		}
	}
	
	private void addToLibVerPairs(Map<String,Map<String,VersionPosition>> pairs,String lib,String version, VersionPosition vp, boolean isInPomOrDM) {
		if(pairs.containsKey(lib)) {
			if(!isInPomOrDM) {
				if(pairs.get(lib).containsKey(null)) {
					pairs.get(lib).remove(null);
					pairs.get(lib).put(version, vp);
				}
				else if(version == null) {			
				}
				else if(!pairs.get(lib).containsKey(version)) {		
//					System.out.println(lib + " version:"+version+"  "+ pairs.get(lib).keySet().toString());
					pairs.get(lib).put(version, vp);
				}
			}
			else {
				if(pairs.get(lib).containsKey(version))
					pairs.get(lib).remove(version);
				pairs.get(lib).put(version, vp);
			}
		}
		else {
			Map<String,VersionPosition> dep = new HashMap<>();
			dep.put(version, vp);
			pairs.put(lib, dep);
		}
	}
	
	public String[] getPropertyValue(String path,Model model,String groupId,String artifactId,String version,String type,String classifier) {
		String isProperty;
		String propertyPosition;
		String propertyName;
		String propertyValue;		
		if (groupId != null && groupId.contains("${")) {
			groupId = parseProperty(path,groupId,model)[0];
		}				
		if (artifactId != null && artifactId.contains("${")) {
			artifactId = parseProperty(path,artifactId,model)[0];
		}				
		if (version != null && version.contains("${")) {	
			isProperty = "y";
			String[] versionInfo = parseProperty(path,version,model);
			version = versionInfo[0];
			propertyPosition = versionInfo[1];
			propertyName = versionInfo[2];
			propertyValue = versionInfo[3];
		}
		else {
			isProperty = "n";
			propertyPosition = null;
			propertyName = null;
			propertyValue = null;
		}
		if (type != null && type.contains("${")) {	
			type = parseProperty(path,type,model)[0];
		}			
		if (classifier != null && classifier.contains("${")) {	
			classifier = parseProperty(path,classifier,model)[0];
		}
		String[] result = {isProperty,propertyPosition,propertyName,propertyValue,groupId,artifactId,version,type,classifier};
		return result;
	}
	
	public String[] parseProperty(String position,String name,Model model) {
		String propertyPosition = null;
		String propertyName = null;
		String propertyValue = null;
		
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
						
						propertyPosition = position;
						propertyName = m.group(1);
						propertyValue = projectVersion;
						
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
					if(model.getParent() != null && model.getParent().getVersion() != null) {
						propertyPosition = position;
						propertyName = m.group(1);
						propertyValue = model.getParent().getVersion();
						name = name.replace(m.group(0), model.getParent().getVersion());
					}
						
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
				else {
					boolean find = false;
					for(String path : fileList) {
						Properties p = this.propertiesInFile.get(path);
						if (p != null && p.getProperty(m.group(1)) != null) {
							String newName = name.replace(m.group(0), p.getProperty(m.group(1)));
							if(name.equals(newName))
								matched = false;
							else
								name = newName;
							
							find = true;
							propertyPosition = path;
							propertyName = m.group(1);
							propertyValue = p.getProperty(m.group(1));
							break;
						}
					}
//					for(Map.Entry<String, Properties> entry: this.propertiesInFile.entrySet()) {
//						String path = entry.getKey();
//						Properties p = entry.getValue();
//						if (p != null && p.getProperty(m.group(1)) != null) {
//							String newName = name.replace(m.group(0), p.getProperty(m.group(1)));
//							if(name.equals(newName))
//								matched = false;
//							else
//								name = newName;
//							
//							find = true;
//							propertyPosition = path;
//							propertyName = m.group(1);
//							propertyValue = p.getProperty(m.group(1));
//							break;
//						}
//					}
					if(!find)
						matched = false;
				}
//				else if (this.properties != null && this.properties.getProperty(m.group(1)) != null) {
//					String newName = name.replace(m.group(0), this.properties.getProperty(m.group(1)));
//					if(name.equals(newName))
//						matched = false;
//					else
//						name = newName;
//				}
//				else
//					matched = false;
			}
		}
//		return name;
		String[] finalResult = {name,propertyPosition,propertyName,propertyValue};
		return finalResult;
	}
	
	private void handlePomType(String groupId,String artifactId,String version,boolean isPomLib,boolean isParent) {
		if(groupId == null || artifactId == null || version == null) {
			return;
		}
		String newPath = POM_FILE_PATH + groupId+" "+artifactId+" "+version+".pom";
		System.out.println("++++ try: "+newPath);
		File file = new File(newPath);
		if (file.exists()) {
//			parsePomFileToModel(newPath,true,isParent);
			Model tempModel = PomFileReader.parsePomFileToModel(newPath);
			if(tempModel != null)
				readModel(tempModel,newPath,true,isParent);
		}
	}
}
