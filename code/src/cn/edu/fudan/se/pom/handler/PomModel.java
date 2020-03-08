package cn.edu.fudan.se.pom.handler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Profile;
import org.apache.maven.model.Repository;

import cn.edu.fudan.se.net.Client;
import cn.edu.fudan.se.util.FileUtil;
import cn.edu.fudan.se.util.JsonFileUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PomModel {
	protected Properties properties = new Properties();
	protected List<List<Properties>> propertiesInProfile  = new ArrayList<>();
//	List<Properties> propertiesList = new ArrayList<>();
//	int currPropertiesListLevel = 0;
	protected JSONArray dependenciesArray = new JSONArray();
	protected int projectId;
	protected List<Map<String,Map<String,String>>> allLibVerPairs= new ArrayList<>();	
//	protected Map<String,Map<String,String>> allLibVerPairs= new HashMap<>();	
	protected Map<String,Map<String,String>> libVerPairs= new HashMap<>();
	protected Map<String,Map<String,String>> libVerPairsInPom= new HashMap<>();
	protected Map<String,Map<String,String>> libVerPairsInManagement= new HashMap<>();
	protected Map<String,Map<String,String>> libVerPairsInProfile= new HashMap<>();
	protected Map<String,String[]> unParsedPomTypeLibs= new HashMap<>();
	protected List<String> repositories = new ArrayList<>();
	protected String projectPath;
	protected String module = null;
//	private String resultPath = "curr_result/";
//	private String resultPath = "E:/data/curr_result_add/";
	private String RESULT_PATH;
	protected String POM_FILE_PATH;
	protected String UNSOLVED_POM_FILE_PATH;
	protected String SERVER_IP;
//	protected String serverIp = "http://192.168.1.103:8080";
//	protected String pomFilePath = "pom/";
	
//	public Map<String, Map<String, String>> getAllLibVerPairs() {
//		return allLibVerPairs;
//	}

	public Map<String, Map<String, String>> getLibVerPairs() {
		return libVerPairs;
	}
	
	public Map<String, String[]> getUnParsedPomTypeLibs() {
		return unParsedPomTypeLibs;
	}
	
	public PomModel() {
		this.RESULT_PATH = "C:/result/";
		this.POM_FILE_PATH = "F:/GP/pom/";
		this.UNSOLVED_POM_FILE_PATH = "F:/GP/pom_unsolved/";
		this.SERVER_IP = "http://127.0.0.1:8080";
	}
	
	public PomModel(String resultPath,String pomFilePath,String unsolvedPomFilePath,String serverIp) {
		this.RESULT_PATH = resultPath;
		this.POM_FILE_PATH = pomFilePath;
		this.UNSOLVED_POM_FILE_PATH = unsolvedPomFilePath;
		this.SERVER_IP = serverIp;
	}
	
	public PomModel(String resultPath,String pomFilePath,String unsolvedPomFilePath) {
		this.RESULT_PATH = resultPath;
		this.POM_FILE_PATH = pomFilePath;
		this.UNSOLVED_POM_FILE_PATH = unsolvedPomFilePath;
	}

	public void handleProjectPom(String projectPath, JSONObject project) {
		this.dependenciesArray.add(project);
		this.projectId = project.optInt("id");
		this.projectPath = projectPath;
		if(!new File(projectPath).exists()) {
			System.out.println("path : "+projectPath+" not exists");
			return;
		}
		String[] pomFiles = FileUtil.getAllPomFiles(projectPath);
		if(pomFiles.length > 0) {
			for(String pomFile : pomFiles) {
				if(pomFile.endsWith("pom.xml")) {
					pomFile = pomFile.replace("\\", "/");
					this.properties = new Properties();
					this.propertiesInProfile  = new ArrayList<>();
//					this.currPropertiesListLevel = 0;
//					this.propertiesList = new ArrayList<>();
					this.libVerPairs= new HashMap<>();
					this.libVerPairsInPom= new HashMap<>();
					this.libVerPairsInManagement= new HashMap<>();
//					if((this.projectPath+"/"+pomFile).equals("F:/maven500/speedment__fdse__speedment/archetype-parent/speedment-archetype-mariadb/src/main/resources/archetype-resources/pom.xml")) {
					if(pomFile.endsWith("pom.xml"))
						this.module = pomFile.substring(0, pomFile.length()-7);
					if(this.module.endsWith("/"))
						this.module = this.module.substring(0, this.module.length()-1);
					Model tempModel = PomFileReader.parsePomFileToModel(this.projectPath+"/"+pomFile);
					if(tempModel != null)
						readModel(tempModel,pomFile,false,false);		
					
					while(!getVersionFromPomLibOrParent(tempModel)) {						
					}
					addToLibVerPairs(this.libVerPairs,this.allLibVerPairs);
					
//					}
				}
			}
			
//			System.out.println(this.properties.get("hadoop.version"));
			libVerPairsToArray();
//			System.out.println("save:"+"result/"+this.projectId+".txt");
    		JsonFileUtil.save(this.RESULT_PATH+this.projectId+".txt", this.dependenciesArray);
		}     	              
	}		
	
	public void handleProjectPom(String projectPath, JSONObject project, String commitId) {
		this.projectId = project.optInt("id");
		this.projectPath = projectPath;
		
//		JSONObject newPro = new JSONObject();				
//		newPro.put("id", this.projectId);
//		newPro.put("name", project.optString("name"));
//		newPro.put("commit", commitId);
//		this.dependenciesArray.add(newPro);
		
		this.dependenciesArray.add(project);
		
		if(!new File(projectPath).exists()) {
			System.out.println("path : "+projectPath+" not exists");
			return;
		}
		String[] pomFiles = FileUtil.getAllPomFiles(projectPath);
		System.out.println("pom file size : "+pomFiles.length);
		if(pomFiles.length > 0) {
			for(String pomFile : pomFiles) {
				if(pomFile.endsWith("pom.xml")) {
					pomFile = pomFile.replace("\\", "/");
					this.properties = new Properties();
					this.propertiesInProfile  = new ArrayList<>();
//					this.currPropertiesListLevel = 0;
//					this.propertiesList = new ArrayList<>();
					this.libVerPairs= new HashMap<>();
					this.libVerPairsInPom= new HashMap<>();
					this.libVerPairsInManagement= new HashMap<>();
//					if((this.projectPath+"/"+pomFile).equals("F:/maven500/speedment__fdse__speedment/archetype-parent/speedment-archetype-mariadb/src/main/resources/archetype-resources/pom.xml")) {
					if(pomFile.endsWith("pom.xml"))
						this.module = pomFile.substring(0, pomFile.length()-7);
					if(this.module.endsWith("/"))
						this.module = this.module.substring(0, this.module.length()-1);
					Model tempModel = PomFileReader.parsePomFileToModel(this.projectPath+"/"+pomFile);
					if(tempModel != null)
						readModel(tempModel,pomFile,false,false);		
					
					while(!getVersionFromPomLibOrParent(tempModel)) {						
					}
					addToLibVerPairs(this.libVerPairs,this.allLibVerPairs);
					
//					}
				}
			}
			
//			System.out.println(this.properties.get("hadoop.version"));
			libVerPairsToArray();
//			System.out.println("save:"+"result/"+this.projectId+".txt");
    		JsonFileUtil.save(this.RESULT_PATH+this.projectId+"_"+commitId+".txt", this.dependenciesArray);
		}
		else {
			JsonFileUtil.save(this.RESULT_PATH+this.projectId+"_"+commitId+".txt", this.dependenciesArray);
		}
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
		if(model.getProperties() != null)
			putAllToProperties(this.properties, model.getProperties());
//			this.properties.putAll(model.getProperties());
		// repositories in profiles
		if(model.getProfiles() != null) {
  			List<Profile> profiles = model.getProfiles();
  			List<Properties> propertiesInOneFile = new ArrayList<>();
//  			propertiesInOneFile.add(model.getProperties());
  			for(Profile pro : profiles) {  				
  				Properties profileProperties = pro.getProperties();
  				if(profileProperties != null) { 					
  					propertiesInOneFile.add(profileProperties); 
  				}
  			} 
  			if(propertiesInOneFile.size() > 0)
  				this.propertiesInProfile.add(propertiesInOneFile);
  		}		
		// repositories
		handleRepositories(model.getRepositories(),model);
		if(model.getProfiles() != null) {
			List<Profile> profiles = model.getProfiles();
  			for(Profile pro : profiles) {  				  				
  				handleRepositories(pro.getRepositories(),model);
  			} 		 			
  		}	
		
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
        
//        if(this.propertiesInProfile.size() == 0) {
//        	this.propertiesList = new ArrayList<>();
//        	this.propertiesList.add(this.properties);
//        }
//        if(this.propertiesInProfile.size() > this.currPropertiesListLevel) {
//        	this.propertiesList = getPropertiesWithAllProfiles();
//        	this.currPropertiesListLevel = this.propertiesInProfile.size();
//        }
		if(this.propertiesInProfile.size() == 0) {
        	List<Properties> firstLevel = new ArrayList<>();
        	firstLevel.add(this.properties);
        	this.propertiesInProfile.add(firstLevel);
        }
		if(this.propertiesInProfile.size() > 1) {
        	getPropertiesWithAllProfiles();
        }
		
		//处理profile
  		if(model.getProfiles() != null) {
  			List<Profile> profiles = model.getProfiles();
  			for(Profile pro : profiles) {
  				handleProfile(pro,model, isPomLib, isParent);
  			}
  		}		
  		
		List<Dependency> dependencies = model.getDependencies();
		parseDependencyFromList(dependencies,false,model,isPomLib,isParent); 
		if (model.getDependencyManagement() != null) {
			dependencies = model.getDependencyManagement().getDependencies();
			parseDependencyFromList(dependencies,true,model,isPomLib,isParent);
		}	
	}
	
	public static void putAllToProperties(Properties oldPro, Properties newPro) {
        for (Map.Entry<Object, Object> entry : newPro.entrySet()) {
        	oldPro.putIfAbsent(entry.getKey(), entry.getValue());
        }
	}
	
	public void handleProfile(Profile profile,Model model,boolean isPomLib,boolean isParent) {		
		List<Dependency> dependencies = profile.getDependencies();
		parseDependencyFromList(dependencies,false,model,isPomLib,isParent); 
		if (profile.getDependencyManagement() != null) {
			dependencies = profile.getDependencyManagement().getDependencies();
			parseDependencyFromList(dependencies,true,model,isPomLib,isParent);
		}	
	}
	
	public void handleRepositories(List<Repository> repos,Model model) {
		if(repos != null){
			for(Repository repo:repos) {
				String url = repo.getUrl();				
				if(url != null) {
					url = url.replace("\\", "/");
					if(url.contains("${")) {
						
						if(this.propertiesInProfile.size() == 0) {
				        	List<Properties> firstLevel = new ArrayList<>();
				        	firstLevel.add(this.properties);
				        	this.propertiesInProfile.add(firstLevel);
				        }
						if(this.propertiesInProfile.size() > 1) {
				        	getPropertiesWithAllProfiles();
				        }
						
						List<Properties> propertiesList = this.propertiesInProfile.get(0);
//						if(this.propertiesInProfile.size() == 0) {
//				        	this.propertiesList = new ArrayList<>();
//				        	this.propertiesList.add(this.properties);
//				        }
//				        if(this.propertiesInProfile.size() > this.currPropertiesListLevel) {
//				        	this.propertiesList = getPropertiesWithAllProfiles();
//				        	this.currPropertiesListLevel = this.propertiesInProfile.size();
//				        }
				        for(Properties properties : propertiesList) {
							url = parseProperty(url,model,properties);
							if(!url.contains("${")) 
								break;
						}
					}					
					if(url.endsWith("/"))
						url = url.substring(0, url.length()-1);
					if(!this.repositories.contains(url))
						this.repositories.add(url);
				}				
			}
		}
	}
	
	public void parseDependencyFromList(List list,boolean isDependencyManagement,Model model,boolean isPomLib,boolean isParent) {	
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
			
			List<String> keys = new ArrayList<String>();
			List<String[]> libs = new ArrayList<>();
			List<String> unParsedKeys = new ArrayList<String>();
			List<String[]> unParsedLibs = new ArrayList<>();
//			if(groupId.equals("org.mortbay.jetty.alpn")&&artifactId.equals("alpn-boot")) {
//				System.out.println("==============================="+this.propertiesInProfile.size());
//				for(int t = 0 ;t<this.propertiesInProfile.size();t++) {
//					List<Properties> temp = this.propertiesInProfile.get(t);
//					System.out.print(temp.size()+" ");
//				}
//				System.out.println();
//			}			
			
			List<Properties> propertiesList = this.propertiesInProfile.get(0);
			
			for(Properties properties : propertiesList) {
				String[] result = getPropertyValue(model,properties,groupId,artifactId,version,type,classifier);
				String key = result[0] +" "+result[1]+" "+result[2]+" "+result[3]+" "+result[4];
				
				if(!keys.contains(key) && !key.contains("${") && !key.contains(" null ")) {
					keys.add(key);
					libs.add(result);
				}
				else if (key.contains("${") || key.contains(" null ")){
					if(!unParsedKeys.contains(key)) {
						unParsedKeys.add(key);
						unParsedLibs.add(result);
					}
				}
			}
			if(keys.size() == 0) {
				keys = unParsedKeys;
				libs = unParsedLibs;
			}
			for(int idx=0;idx<libs.size();idx++) {
				String[] result = libs.get(idx);				
				String newVersion = result[2];
				String newType = result[3];
				String key = result[0] +" "+result[1];
				if(result[3] != null)
					key += " "+result[3];
				else
					key += " jar";
				if(result[4] != null)
					key += " " + result[4];
				if(newType != null) {				
					if(newType.equals("pom")) {
						String scope = ((Dependency) list.get(i)).getScope();
						if(!isDependencyManagement) {
							handlePomType(result[0],result[1],newVersion,true,false);
						}
						else if(isDependencyManagement && scope != null && scope.equals("import")) {
//							System.out.println("DependencyManagement:    " + pomFilePath + result[0]+" "+result[1]+" "+newVersion+".pom");
							handlePomType(result[0],result[1],newVersion,true,false);
						}
						else if(isPomLib)
							addToLibVerPairs(this.libVerPairsInPom, key,newVersion);
						else
							addToLibVerPairs(this.libVerPairsInManagement,key,newVersion);
						
					}
					else if(!newType.equals("jar")){
						if(!isDependencyManagement)
							addToLibVerPairs(this.libVerPairs,key,newVersion);
						else if(isPomLib)
							addToLibVerPairs(this.libVerPairsInPom, key,newVersion);
						else
							addToLibVerPairs(this.libVerPairsInManagement,key,newVersion);
					}
				}
				if(!isDependencyManagement)
					addToLibVerPairs(this.libVerPairs,key,newVersion);
				else if(isPomLib)
					addToLibVerPairs(this.libVerPairsInPom,key,newVersion);
				else
					addToLibVerPairs(this.libVerPairsInManagement,key,newVersion);	
			}
		}
	}
	
	public void getPropertiesWithAllProfiles() {
//		System.out.println("==============================="+this.propertiesInProfile.size());
//		for(int t = 0 ;t<this.propertiesInProfile.size();t++) {
//			List<Properties> temp = this.propertiesInProfile.get(t);
//			System.out.print(temp.size()+" ");
//		}
//		System.out.println();
		
		List<Properties> propertiesList = new ArrayList<Properties>();
		int levels = this.propertiesInProfile.size();
		int[] indexes = new int[levels];
		Arrays.fill(indexes, -1);
		each0(levels, 0, indexes,propertiesList);
//		System.out.println("==============================="+this.propertiesInProfile.size());
//		for(int t = 0 ;t<this.propertiesInProfile.size();t++) {
//			List<Properties> temp = this.propertiesInProfile.get(t);
//			System.out.print(temp.size()+" ");
//		}
//		System.out.println();
		this.propertiesInProfile  = new ArrayList<>();
		this.propertiesInProfile.add(propertiesList);
		
//		System.out.println("==============================="+propertiesList.size());
//		System.out.println();
//		return propertiesList;
		
	}
	
	private void each0(int levels, int nextLevel, int[] indexes,List<Properties> propertiesList) {
		if (nextLevel >= levels)
			return;
		for (int i = 0; i < this.propertiesInProfile.get(nextLevel).size(); i++) {
			indexes[nextLevel] = i;
			for (int k = indexes.length; --k > nextLevel;) {
				indexes[k] = -1;
			}
			if(nextLevel == levels - 1) {
				propertiesList.add(call(indexes));
			}				
			each0(levels, nextLevel + 1, indexes,propertiesList);
		}
	}
	
	public Properties call(int[] indexes) {
		Properties newProperties = new Properties();
//		newProperties.putAll(this.properties);
		for (int l = indexes.length - 1; l >= 0; l--) {
			int index = indexes[l];
			Properties properties = this.propertiesInProfile.get(l).get(index);
			if (properties != null) {				
				newProperties.putAll(properties);
			}
		}
		
		return newProperties;
	}
	
	public String[] getPropertyValue(Model model,Properties properties,String groupId,String artifactId,String version,String type,String classifier) {
		if (groupId != null && groupId.contains("${")) {
			groupId = parseProperty(groupId,model,properties);
		}				
		if (artifactId != null && artifactId.contains("${")) {
			artifactId = parseProperty(artifactId,model,properties);
		}				
		if (version != null && version.contains("${")) {	
			version = parseProperty(version,model,properties);
		}
		if (type != null && type.contains("${")) {	
			type = parseProperty(type,model,properties);
		}			
		if (classifier != null && classifier.contains("${")) {	
			classifier = parseProperty(classifier,model,properties);
		}
		String[] result = {groupId,artifactId,version,type,classifier};
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
				else if (this.properties != null && this.properties.getProperty(m.group(1)) != null) {
					String newName = name.replace(m.group(0), this.properties.getProperty(m.group(1)));
					if(name.equals(newName))
						matched = false;
					else
						name = newName;
				}
//				else if (this.properties != null && this.properties.containsKey(m.group(1))) {
//					List<String> value = this.properties.get(m.group(1));
//					if(value.size() < 2)
//						name = name.replace(m.group(0), value.get(0));
//					else {
//						String[] newValue = new String[value.size()];		
//						for(int i=0;i<value.size();i++) {
//							newValue[i] = name.replace(m.group(0), value.get(i));
//						}
//						return newValue;
//					}
//				}
				else
					matched = false;
			}
		}
//		String[] result = {name};
		return name;
	}
	
	protected void handlePomType(String groupId,String artifactId,String version,boolean isPomLib,boolean isParent) {
		if(groupId == null || artifactId == null || version == null) {
			String[] value = { version, this.module };
			this.unParsedPomTypeLibs.put(groupId + " " + artifactId, value);
			return;
		}
		String newPath = this.POM_FILE_PATH + groupId+" "+artifactId+" "+version+".pom";
//		System.out.println("++++ try: "+newPath);
		File file = new File(newPath);
		if (file.exists()) {
//			parsePomFileToModel(newPath,true,isParent);
			Model tempModel = PomFileReader.parsePomFileToModel(newPath);
			if(tempModel != null)
				readModel(tempModel,newPath,true,isParent);
		}
			
		else {	
			String[] value = { version, this.module };
			this.unParsedPomTypeLibs.put(groupId + " " + artifactId, value);
			// add unsolved pom
//			File unsolved= new File(this.UNSOLVED_POM_FILE_PATH + groupId+" "+artifactId+" "+version+".pom");
//			if (unsolved.exists()) {
//				String[] value = { version, this.module };
//				this.unParsedPomTypeLibs.put(groupId + " " + artifactId, value);
//				return;
//			}
//			String content = Client.post(this.SERVER_IP,"", groupId, artifactId, version);
////			file = new File(newPath);
////			if (file.exists())
////				parsePomFile(newPath,true,isParent);
//			if (content != null && content.length() > 0) {
//				InputStream is = new ByteArrayInputStream(content.getBytes());
////				parsePomFileToModel(is, isPomLib, isParent);
//				Model tempModel = PomFileReader.parsePomFileToModel(is);
//				if(tempModel != null)
//					readModel(tempModel,newPath,isPomLib,isParent);
//
////				System.out.println("++++++++++++++++++++++++++++"+pomPath);					
//
//			} else {
//				boolean solved = false;
//				System.out.println(this.repositories.size());
//				for(String repoUrl : this.repositories) {
//					System.out.println(repoUrl);
//					content = Client.post(this.SERVER_IP,repoUrl, groupId, artifactId, version);
//					if (content != null && content.length() > 0) {
//						InputStream is = new ByteArrayInputStream(content.getBytes());
//						Model tempModel = PomFileReader.parsePomFileToModel(is);
//						if(tempModel != null)
//							readModel(tempModel,newPath,isPomLib,isParent);
//						solved = true;
//						break;
//					}
//					
//				}
//				if(!solved) {
//					String[] value = { version, this.module };
//					this.unParsedPomTypeLibs.put(groupId + " " + artifactId, value);
//				}
//				
//			}			
		}
	}
	
	protected void addToLibVerPairs(Map<String,Map<String,String>> pairsOfOnePom,List<Map<String,Map<String,String>>> all) {
		if(pairsOfOnePom.size() > 0) {
			all.add(pairsOfOnePom);
		}
	}
	
	protected void addToLibVerPairs(Map<String,Map<String,String>> pairsOfOnePom,Map<String,Map<String,String>> all) {
		for (Map.Entry<String, Map<String,String>> entry : pairsOfOnePom.entrySet()) {
			String lib = entry.getKey();
			for (Map.Entry<String, String> value : entry.getValue().entrySet()) {
				String version = value.getKey();
				String versionInfo = value.getValue();
				if(all.containsKey(lib)) {
//					if(!all.get(lib).containsKey(version)) {		
//						System.out.println(lib + " version:"+version+"  "+ all.get(lib).keySet().toString());
						all.get(lib).put(version, versionInfo);
//					}
				}
				else {
					Map<String,String> dep = new HashMap<>();
					dep.put(version, versionInfo);
					all.put(lib, dep);
				}
			}
		}		
	}
	
	protected void addToLibVerPairs(Map<String,Map<String,String>> pairs,String lib,String version) {
		if(pairs.containsKey(lib)) {
			if(pairs.get(lib).containsKey(null)) {
//				String[] value = {module,information};
				pairs.get(lib).remove(null);
				pairs.get(lib).put(version, this.module);
			}
			else if(version == null) {			
			}
			else if(!pairs.get(lib).containsKey(version)) {		
//				System.out.println(lib + " version:"+version+"  "+ pairs.get(lib).keySet().toString());
				pairs.get(lib).put(version, this.module);
			}
		}
		else {
			Map<String,String> dep = new HashMap<>();
			dep.put(version, this.module);
			pairs.put(lib, dep);
		}
	}
	
	protected Map<String,Map<String,String>> unsolvedLibVerPairs(Model model) {
		Map<String,Map<String,String>> unsolvedPairs = new HashMap<>();		 
		boolean result = true;
		for (Map.Entry<String, Map<String,String>> entry : this.libVerPairs.entrySet()) {
			String libStr = entry.getKey();
			String[] lib = libStr.split(" ");
			String type = lib[2];
//			if(lib.length == 3)
//				type = lib[2];
			for (Map.Entry<String, String> value : entry.getValue().entrySet()) {
				String version = value.getKey();
				String module = value.getValue();			
				if(version == null) {
					boolean solved = false;						
					if(this.libVerPairsInPom.get(libStr) != null) {
						for (Map.Entry<String, String> pomLib : this.libVerPairsInPom.get(libStr).entrySet()) {
							if(pomLib.getKey() != null) {								
								String newVersion = pomLib.getKey();
								if(newVersion.contains("${")) {
									newVersion = parseProperty(newVersion,model,new Properties());
								}
								if(!this.libVerPairs.containsKey(newVersion)) {
									this.libVerPairs.get(libStr).put(newVersion, module);
									this.libVerPairs.get(libStr).remove(version);	
									solved = true;
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
						for (Map.Entry<String, String> parentLib : this.libVerPairsInManagement.get(libStr).entrySet()) {
							if(parentLib.getKey() != null) {								
								String newVersion = parentLib.getKey();
								if(newVersion.contains("${")) {
									newVersion = parseProperty(newVersion,model,new Properties());
								}
								if(!this.libVerPairs.containsKey(newVersion)) {
									this.libVerPairs.get(libStr).put(newVersion, module);
									this.libVerPairs.get(libStr).remove(version);	
									solved = true;
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
					if(!solved) {
						Map<String,String> newValue = new HashMap<>();
						newValue.put(version, module);
						unsolvedPairs.put(libStr, newValue);
					}
				}
			}			
		}
		return unsolvedPairs;
	}
	
	protected boolean getVersionFromPomLibOrParent(Model model) {
		boolean result = true;
		for (Map.Entry<String, Map<String,String>> entry : this.libVerPairs.entrySet()) {
			String libStr = entry.getKey();
			String[] lib = libStr.split(" ");
			String type = lib[2];
//			if(lib.length == 3)
//				type = lib[2];
			for (Map.Entry<String, String> value : entry.getValue().entrySet()) {
				String version = value.getKey();
				String module = value.getValue();			
				if(version == null) {
//					boolean solved = false;						
					if(this.libVerPairsInPom.get(libStr) != null) {
						for (Map.Entry<String, String> pomLib : this.libVerPairsInPom.get(libStr).entrySet()) {
							if(pomLib.getKey() != null) {								
								String newVersion = pomLib.getKey();
								if(newVersion.contains("${")) {
									newVersion = parseProperty(newVersion,model,new Properties());
								}
								if(!this.libVerPairs.containsKey(newVersion)) {
									this.libVerPairs.get(libStr).put(newVersion, module);
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
						for (Map.Entry<String, String> parentLib : this.libVerPairsInManagement.get(libStr).entrySet()) {
							if(parentLib.getKey() != null) {								
								String newVersion = parentLib.getKey();
								if(newVersion.contains("${")) {
									newVersion = parseProperty(newVersion,model,new Properties());
								}
								if(!this.libVerPairs.containsKey(newVersion)) {
									this.libVerPairs.get(libStr).put(newVersion, module);
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
	
	private void libVerPairsToArray() {
		for(Map<String, Map<String,String>> pairsOfOnePom: this.allLibVerPairs) {
			for (Map.Entry<String, Map<String,String>> entry : pairsOfOnePom.entrySet()) {
				String libStr = entry.getKey();
				String[] lib = libStr.split(" ");
				for (Map.Entry<String, String> value : entry.getValue().entrySet()) {
					JSONObject obj = new JSONObject();
					obj.put("groupId", lib[0]);
					obj.put("artifactId", lib[1]);
					obj.put("type", lib[2]);
					if(lib.length == 4) {
						obj.put("classifier", lib[3]);
					}	
					String version = value.getKey();
					obj.put("version", version);
					obj.put("module", value.getValue());		
					
					this.dependenciesArray.add(obj);
				}			
			}
		}
		
//		for (Map.Entry<String, Map<String,String>> entry : this.allLibVerPairs.entrySet()) {
//			String libStr = entry.getKey();
//			String[] lib = libStr.split(" ");
//			for (Map.Entry<String, String> value : entry.getValue().entrySet()) {
//				JSONObject obj = new JSONObject();
//				obj.put("groupId", lib[0]);
//				obj.put("artifactId", lib[1]);
//				obj.put("type", lib[2]);
//				if(lib.length == 4) {
//					obj.put("classifier", lib[3]);
//				}	
//				String version = value.getKey();
//				obj.put("version", version);
//				obj.put("module", value.getValue());		
//				
//				this.dependenciesArray.add(obj);
////				System.out.println(obj.get("groupId"));
////				System.out.println(obj.get("artifactId"));
////				System.out.println(obj.get("version"));
////				System.out.println(obj.get("type"));
////				System.out.println(obj.get("module"));
////				System.out.println(obj.get("information"));
//	
////				System.out.println();
//			}			
//		}
	}
}