package cn.edu.fudan.se.pom.handler;

import cn.edu.fudan.se.util.DirUtil;
import cn.edu.fudan.se.util.FileUtil;
import cn.edu.fudan.se.util.JsonFileUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.maven.model.*;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PomTreeGeneratorModel {
	//	protected List<List<Properties>> propertiesInProfile = new ArrayList<>();
	//	List<Properties> propertiesList = new ArrayList<>();
//	int currPropertiesListLevel = 0;
	//	protected List<Map<String,Map<String,String>>> allLibVerPairs= new ArrayList<>();
//	protected Map<String,Map<String,String>> allLibVerPairs= new HashMap<>();
//	protected Map<String, Map<String, String>> libVerPairs = new HashMap<>();
	//	protected Map<String,Map<String,String>> libVerPairsInPom= new HashMap<>();
//	protected Map<String,Map<String,String>> libVerPairsInManagement= new HashMap<>();
//	protected Map<String,Map<String,String>> libVerPairsInProfile= new HashMap<>();
//	protected Map<String, String[]> unParsedPomTypeLibs = new HashMap<>();
	//	protected List<String> repositories = new ArrayList<>();
	private Map<String, Properties> propertiesInFile = new HashMap<String, Properties>();
	public JSONArray resultJSON = new JSONArray();
	public Set<String> allPomFiles = new HashSet<>();
	protected Properties properties = new Properties();
	protected JSONArray dependenciesArray = new JSONArray();
	protected int projectId;
	protected String projectPath;
	protected String module = null;
	//	private String resultPath = "curr_result/";
//	private String resultPath = "E:/data/curr_result_add/";
	private String RESULT_PATH;
	protected String POM_FILE_PATH;

	public PomTreeGeneratorModel(String resultPath, String pomFilePath) {
		this.RESULT_PATH = resultPath;
		this.POM_FILE_PATH = pomFilePath;
	}

	public void handleProjectPom(String projectPath, JSONObject project) {
		this.dependenciesArray.add(project);
		this.projectId = project.optInt("id");

		this.projectPath = projectPath;
		if (!new File(projectPath).exists()) {
			System.out.println("path : " + projectPath + " not exists");
			return;
		}
		String[] pomFiles = FileUtil.getAllPomFiles(projectPath);
		if (pomFiles.length <= 0) {
			return;
		}
		for (String pomFile : pomFiles) {
			if (!pomFile.endsWith("pom.xml")) {
				continue;
			}
			pomFile = pomFile.replace("\\", "/");
			System.out.println("Handle Pom:" + pomFile);
			Model tempModel = PomFileReader.parsePomFileToModel(this.projectPath + "/" + pomFile);
			readModel(tempModel, pomFile, "local");
		}
	}
	private List<String> fileList = new ArrayList<String>();

	public void readModel(Model model, String pomPath, String type) {
//		String temp = pomPath;
//		if(pomPath.startsWith("D:/Workspace/WyRepos/pom/")){
//			temp = temp.replace("D:/Workspace/WyRepos/pom/","");
//		}

		allPomFiles.add(pomPath+"__fdse__"+type);
		if (model == null) {
			return;
		}

		if(model.getProperties() != null && !this.fileList.contains(pomPath)) {
			this.fileList.add(pomPath);
			this.propertiesInFile.put(pomPath, model.getProperties());
		}
		if (model.getParent() != null) {
			String parentGroupId = model.getParent().getGroupId();
			String parentArtifactId = model.getParent().getArtifactId();
			String parentVersion = model.getParent().getVersion();
			String parentPath = model.getParent().getRelativePath().replace("\\", "/");
			if (parentPath.equals("")) {
				nextModel(parentGroupId,parentArtifactId,parentVersion,pomPath,type,"remote");
//				String name = handlePomType(parentGroupId, parentArtifactId, parentVersion);
//				String newPath = this.POM_FILE_PATH + name;
//				if (name != null) {
//					System.out.println(pomPath + "----->");
//					System.out.println("------>" + newPath);
//					JSONObject jo = new JSONObject();
//					jo.put("A", pomPath);
//					jo.put("parent", name);
//					jo.put("type-A", type);
//					jo.put("type-parent", "remote");
//					resultJSON.add(jo);
//					Model tempModel = PomFileReader.parsePomFileToModel(newPath);
//					readModel(tempModel, newPath, "remote");
//				}
			} else {
				if (!(parentPath.endsWith("pom.xml") || parentPath.endsWith(".xml"))) {
					if (!parentPath.endsWith("/"))
						parentPath += "/";
					parentPath += "pom.xml";
				}

				if (parentPath.startsWith("/"))
					parentPath = parentPath.substring(1);
				String prefix = pomPath;

				if (prefix.endsWith("pom.xml")) {
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

					String parentWholePath2 = DirUtil.trimPath(parentWholePath);
					String parentAbsPath = this.projectPath + "/" + parentWholePath2;
					System.out.println("parent: " + parentAbsPath);
					if (new File(parentAbsPath).exists() && isParent(parentAbsPath, parentGroupId, parentArtifactId, parentVersion)) {
						System.out.println(pomPath + "----->" + parentWholePath2);
						JSONObject jo = new JSONObject();
						jo.put("A", pomPath);
						jo.put("parent", parentWholePath2);
						jo.put("type-A", type);
						jo.put("type-parent", "local");
						resultJSON.add(jo);
						Model tempModel = PomFileReader.parsePomFileToModel(parentAbsPath);
						readModel(tempModel, parentWholePath2, "local");
					} else {
						nextModel(parentGroupId,parentArtifactId,parentVersion,pomPath,type,"repo");
//						String name = handlePomType(parentGroupId, parentArtifactId, parentVersion);
//						String newPath = this.POM_FILE_PATH + name;
//						if (name != null) {
//							System.out.println(pomPath + "----->" + newPath);
//							JSONObject jo = new JSONObject();
//							jo.put("A", pomPath);
//							jo.put("parent", name);
//							jo.put("type-A", type);
//							jo.put("type-parent", "repo");
//							resultJSON.add(jo);
//							Model tempModel = PomFileReader.parsePomFileToModel(newPath);
//							readModel(tempModel, newPath, "repo");
//						}
					}
				} else if(prefix.endsWith(".pom")){
					nextModel(parentGroupId,parentArtifactId,parentVersion,pomPath,type,"remote");
//					String name = handlePomType(parentGroupId,parentArtifactId,parentVersion);
//					String newPath = this.POM_FILE_PATH + name;
//					if (name != null) {
//						System.out.println(pomPath + "----->" + newPath);
//						JSONObject jo = new JSONObject();
//						jo.put("A", pomPath);
//						jo.put("parent", name);
//						jo.put("type-A", type);
//						jo.put("type-parent", "remote");
//						resultJSON.add(jo);
//						Model tempModel = PomFileReader.parsePomFileToModel(newPath);
//						readModel(tempModel, newPath, "remote");
//					}
				}
			}
		}

//		if(!isPomLib) {
			List<Dependency> dependencies = model.getDependencies();
			parseDependencyFromList(pomPath,dependencies,false,model,type);
//		}
//		if(isPomLib || isParent) {
			if (model.getDependencyManagement() != null) {
				dependencies = model.getDependencyManagement().getDependencies();
				parseDependencyFromList(pomPath,dependencies,true,model,type);
			}
//		}
	}

	public void nextModel(String parentGroupId,String parentArtifactId,String parentVersion,String pomPath,String type,String typeParent){
		String name = handlePomType(parentGroupId,parentArtifactId,parentVersion);
		String newPath = this.POM_FILE_PATH + name;
		if (name != null) {
			System.out.println(pomPath + "----->" + newPath);
			JSONObject jo = new JSONObject();
			jo.put("A", pomPath);
			jo.put("parent", name);
			jo.put("type-A", type);
			jo.put("type-parent", typeParent);
			resultJSON.add(jo);
			Model tempModel = PomFileReader.parsePomFileToModel(newPath);
			readModel(tempModel, newPath, "remote");
		}
	}

	public void parseDependencyFromList(String path,List list,boolean isDependencyManagement,Model model,String type2) {
		for (int i = 0; i < list.size(); i++) {
			String groupId,artifactId,version,type,classifier = null;
			if (!(list.get(i) instanceof Dependency)) {
				continue;
			}
			groupId = ((Dependency) list.get(i)).getGroupId();
			artifactId = ((Dependency) list.get(i)).getArtifactId();
			version = ((Dependency) list.get(i)).getVersion();
			type = ((Dependency) list.get(i)).getType();
			classifier = ((Dependency) list.get(i)).getClassifier();

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
			if(newType != null) {
				if(newType.equals("pom")) {
					String scope = ((Dependency) list.get(i)).getScope();
					if(!isDependencyManagement ||(isDependencyManagement && scope != null && scope.equals("import"))) {
						String name = handlePomType(result[4+0],result[4+1],newVersion);
						String newPath = this.POM_FILE_PATH + name;
						if (name != null) {
							System.out.println(path + "----->");
							System.out.println("------>" + newPath);
							JSONObject jo = new JSONObject();
							jo.put("A", path);
							jo.put("parent", newPath);
							jo.put("type-A", type2);
							jo.put("type-parent", "remote-import");
							resultJSON.add(jo);
							Model tempModel = PomFileReader.parsePomFileToModel(newPath);
							readModel(tempModel, newPath, "remote-import");
						}
					}
//					else if() {
//						System.out.println("DependencyManagement:    " + pomFilePath + result[0]+" "+result[1]+" "+newVersion+".pom");
//						handlePomType(result[4+0],result[4+1],newVersion);
//					}
				}
			}
		}
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
					for(Map.Entry<String, Properties> entry: this.propertiesInFile.entrySet()) {
						String path = entry.getKey();
						Properties p = entry.getValue();
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
//					else if(isPomLib)
//						addToLibVerPairs(this.libVerPairsInPom, key,newVersion, vp, true);
//					else
//						addToLibVerPairs(this.libVerPairsInManagement,key,newVersion, vp, true);
//				else if(!newType.equals("jar")){
//					if(!isDependencyManagement) {
//						if(this.libsToLocate.contains(key)) {
////							System.out.println(key);
//							addToLibVerPairs(this.libVerPairs,key,newVersion, vp, false);
//						}
//					}
//					else if(isPomLib)
//						addToLibVerPairs(this.libVerPairsInPom, key,newVersion, vp, true);
//					else
//						addToLibVerPairs(this.libVerPairsInManagement,key,newVersion, vp, true);
//				}

//			if(!isDependencyManagement) {
//				if(this.libsToLocate.contains(key)) {
////					System.out.println(key);
//					addToLibVerPairs(this.libVerPairs,key,newVersion, vp, false);
//				}
//			}
//			else if(isPomLib)
//				addToLibVerPairs(this.libVerPairsInPom,key,newVersion, vp, true);
//			else
//				addToLibVerPairs(this.libVerPairsInManagement,key,newVersion, vp, true);



	protected String handlePomType(String groupId, String artifactId, String version) {
		if (groupId == null || artifactId == null || version == null) {
//			String[] value = {version, this.module};
//            this.unParsedPomTypeLibs.put(groupId + " " + artifactId, value);
			return null;
		}
		String newPath = this.POM_FILE_PATH + groupId + " " + artifactId + " " + version + ".pom";
//		System.out.println("++++ try: "+newPath);
		String name = groupId + " " + artifactId + " " + version + ".pom";
		File file = new File(newPath);
		if (file.exists()) {
			return name;
		}
		System.err.println("pom file not exists in pom repo");
		return null;
	}
	public boolean isParentTrueFlag;
	public boolean isParent(String pomPath, String groupId, String artifactId, String version) {
		if(isParentTrueFlag){
			return true;
		}
//			System.out.println("isParent:"+pomPath);
		Model tempModel = PomFileReader.parsePomFileToModel(pomPath);
		if (tempModel == null) {
			return false;
		}
		if (artifactId.equals(tempModel.getArtifactId())) {
			return true;
		}
		return false;
	}

	public JSONObject toJSONObject() {
		JSONObject jo = new JSONObject();
		jo.put("relation",this.resultJSON);
		JSONArray jaa = new JSONArray();
		jaa.addAll(this.allPomFiles);
		jo.put("pom",jaa);
		return jo;
	}
}