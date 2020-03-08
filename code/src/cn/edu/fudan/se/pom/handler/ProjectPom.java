package cn.edu.fudan.se.pom.handler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import cn.edu.fudan.se.net.Client;
import cn.edu.fudan.se.util.JsonFileUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ProjectPom {
	protected Properties properties = new Properties();
	protected JSONArray dependenciesArray = new JSONArray();
	protected String projectGroupId;
	protected String projectVersion;
	protected String parentProjectGroupId;
	protected String parentProjectArtifactId;
	protected String parentProjectVersion;
	protected int projectId;
	protected boolean hasNoPom = false;
//	private Map<String,String[]> libVerPairs= new HashMap<>();
	protected Map<String,Map<String,String[]>> libVerPairs= new HashMap<>();
	protected Map<String,Map<String,String[]>> libVerPairsInPom= new HashMap<>();
	protected Map<String,String[]> unParsedPomTypeLibs= new HashMap<>();
//	String pomFilePath = "F:/GP/pom/";
	String pomFilePath = "D:/GP/pom/";
		
//	public void handleProjectPom(String projectPath, JSONObject project) {
//		File f=new File(projectPath);
//		if (!f.exists()) {
//			System.out.println("path : "+projectPath+" not exists");
//			return;
//		}
//        File list[] = f.listFiles();
//        for(int l=0;l<list.length;l++) {
//        	if(list[l].isDirectory()){          		
//        		this.dependenciesArray.add(project);
//        		this.projectId = project.optInt("id");
//        		parsePomFile(projectPath + "/" + list[l].getName(),null);
//        		if(!hasNoPom) {
//        			libVerPairsToArray();
//            		JsonFileUtil.save("result/"+this.projectId+".txt", this.dependenciesArray);
//        		}        		
//        	}
//        }		      
//        
//	}
	
	public Map<String, String[]> getUnParsedPomTypeLibs() {
		return unParsedPomTypeLibs;
	}

	public void handleProjectPom(String projectPath, JSONObject project) {
		this.dependenciesArray.add(project);
		this.projectId = project.optInt("id");
		parsePomFile(projectPath,null,false,false);
		if(!hasNoPom) {
			libVerPairsToArray();
    		JsonFileUtil.save("prev_result/"+this.projectId+".txt", this.dependenciesArray);
		}     	      
        
	}
	
	public void parsePomFile(String path,String module,boolean isPomLib,boolean isParent) {
		String pomPath = null;
		if(isPomLib)
			pomPath = path;
		else {
			if(module == null)
				pomPath = path + "/" + "pom.xml";
			else
				pomPath = path +module+ "/" + "pom.xml";
		}		
		File file = new File(pomPath);
		if (!file.exists()) {
			if(module == null) {
				hasNoPom = true;
				System.out.println("path : "+pomPath+" not exists");
			}				
			return;
		}
		MavenXpp3Reader reader = new MavenXpp3Reader();

		Model model = null;
		try {
			FileReader fr = new FileReader(pomPath); 
			model = reader.read(fr);
			fr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			System.out.println("error id : "+projectId);
			e.printStackTrace();
			return;
		}       		

		if(model == null)
			return;		
//		System.out.println("++++++++++++++++++++++++++++"+pomPath);
		readModel(model,path,module,isPomLib,isParent);		
		
	}
	
	public void parsePomFile(InputStream inputStream,String path,String module,boolean isPomLib,boolean isParent) {	
		MavenXpp3Reader reader = new MavenXpp3Reader();

		Model model = null;
		try {
			model = reader.read(inputStream);
			inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			System.out.println("XmlPullParserException : error id : "+projectId);
//			e.printStackTrace();
			return;
		}       		

		if(model == null)
			return;		
//		System.out.println("++++++++++++++++++++++++++++"+pomPath);
		readModel(model,path,module,isPomLib,isParent);		
		
	}
	
	public void readModel(Model model,String path,String module,boolean isPomLib,boolean isParent) {
		//处理parent
		if(module == null && model.getParent() != null && !new File(model.getParent().getRelativePath()).exists()) {			
			String parentGroupId = model.getParent().getGroupId();
			String parentArtifactId = model.getParent().getArtifactId();
			String parentVersion = model.getParent().getVersion();
//			String newPath = "pom/" + parentGroupId+" "+parentArtifactId+" "+parentVersion+".pom";
//			File file = new File(newPath);			
//			if (file.exists()) {
//				parsePomFile(newPath, null, true,true);
//			} else {
//				String[] value = { parentVersion, null };
//				this.unParsedPomTypeLibs.put(parentGroupId + " " + parentArtifactId, value);
//			}
//			System.out.println("++++++++++++++++++++++++++++"+parentGroupId+" "+parentArtifactId+" "+parentVersion);
			handlePomType(parentGroupId,parentArtifactId,parentVersion,module,true,true);
		}
		
		if (model.getGroupId() != null && this.projectGroupId == null && !isPomLib && !isParent)
			this.projectGroupId = model.getGroupId();
		if (model.getVersion() != null && this.projectVersion == null && !isPomLib && !isParent)
			this.projectVersion = model.getVersion();		
		if (model.getGroupId() != null && isParent)
			this.parentProjectGroupId = model.getGroupId();	
		if (model.getArtifactId() != null && isParent)
			this.parentProjectArtifactId = model.getArtifactId();	
		if (model.getVersion() != null && isParent)
			this.parentProjectVersion = model.getVersion();	
		if(model.getProperties() != null)
			this.properties.putAll(model.getProperties());
		List<Dependency> dependencies = model.getDependencies();
		parseDependencyFromList(dependencies,module,"Dependency",model,isPomLib); 

		if (model.getDependencyManagement() != null) {
			dependencies = model.getDependencyManagement().getDependencies();
			parseDependencyFromList(dependencies,module,"DependencyManagement",model,isPomLib);
		}

//		if (model.getBuild() != null) {
//			List<Plugin> plugins = model.getBuild().getPlugins();
//			parseDependencyFromList(plugins,module,"Plugin");
//		}
//
//		if (model.getBuild() != null && model.getBuild().getPluginManagement() != null) {
//			List<Plugin> plugins = model.getBuild().getPluginManagement().getPlugins();
//			parseDependencyFromList(plugins,module,"PluginManagement");
//		}	
		
		List<String> modules = model.getModules();
		for(int m = 0;m < modules.size();m++) {
//			parsePomFile(path + "/"+modules.get(m));
			if(module == null)
				parsePomFile(path,"/"+modules.get(m),false,false);
			else
				parsePomFile(path, module + "/"+modules.get(m),false,false);
		}		
	}
	
	public void parseDependencyFromList(List list,String module,String information,Model model,boolean isPomLib) {
		for (int i = 0; i < list.size(); i++) {
			String groupId,artifactId,version,type = null;
			if(list.get(i) instanceof Plugin) {
				groupId = ((Plugin) list.get(i)).getGroupId();
				artifactId = ((Plugin) list.get(i)).getArtifactId();
				version = ((Plugin) list.get(i)).getVersion();
			}
			else if (list.get(i) instanceof Dependency) {
				groupId = ((Dependency) list.get(i)).getGroupId();
				artifactId = ((Dependency) list.get(i)).getArtifactId();
				version = ((Dependency) list.get(i)).getVersion();
				type = ((Dependency) list.get(i)).getType();
			}
			else 
				continue;	
			if (groupId != null && groupId.contains("${"))
				groupId = parseProperty(groupId,model);
			if (artifactId != null && artifactId.contains("${"))
				artifactId = parseProperty(artifactId,model);
			if (version != null && version.contains("${")) {				
				version = parseProperty(version,model);			
			}																
			
			if(type != null) {
				if(type.equals("pom")) {
					handlePomType(groupId,artifactId,version,module,true,false);
				}
				else if(!type.equals("jar")){
					if(isPomLib)
						addToLibVerPairs(this.libVerPairsInPom, groupId +" "+artifactId+" "+type,version,module,information);
					else
						addToLibVerPairs(this.libVerPairs,groupId +" "+artifactId+" "+type,version,module,information);
				}
			}
			if (isPomLib)
				addToLibVerPairs(this.libVerPairsInPom, groupId + " " + artifactId, version, module, information);
			else
				addToLibVerPairs(this.libVerPairs, groupId + " " + artifactId, version, module, information);
//			if(type !=null && !type.equals("jar") && !type.equals("pom"))
//				System.out.println(type);
//			System.out.println(groupId);
//			System.out.println(artifactId);
//			System.out.println(version);
//
//			System.out.println();
		}
	}
	
	public String parseProperty(String name,Model model) {
		// String el = "\\$\\{[^\\}]+\\}";
		// Pattern el = Pattern.compile("\\$\\{([^\\}]+)\\}");
		Pattern el = Pattern.compile("\\$\\{(.*?)\\}");
		boolean matched = true;
//		if (name != null) {
		while (name != null && matched) {
			Matcher m = el.matcher(name);// el表达式
			matched = false;
			while (m.find()) {
				matched = true;
				if (m.group(1).equals("project.groupId")) {
					if (this.projectGroupId != null)
						name = name.replace(m.group(0), this.projectGroupId);
					else if(this.parentProjectGroupId != null)
						name = name.replace(m.group(0), this.parentProjectGroupId);
					else
						matched = false;
				} else if (m.group(1).equals("project.version")) {
					if (this.projectVersion != null)
						name = name.replace(m.group(0), this.projectVersion);
					else if(this.parentProjectVersion != null)
						name = name.replace(m.group(0), this.parentProjectVersion);
					else
						matched = false;
				}
				else if (m.group(1).equals("project.parent.version")) {
					if(model.getParent() != null && model.getParent().getVersion() != null)
						name = name.replace(m.group(0), model.getParent().getVersion());
					else
						matched = false;
				}
				else if (m.group(1).equals("project.parent.groupId")) {
					if(model.getParent() != null && model.getParent().getGroupId() != null)
						name = name.replace(m.group(0), model.getParent().getGroupId());
					else
						matched = false;
				}
				else if (m.group(1).equals("project.parent.artifactId")) {
					if(model.getParent() != null && model.getParent().getArtifactId() != null)
						name = name.replace(m.group(0), model.getParent().getArtifactId());
					else
						matched = false;
				}
				else if (this.properties != null && this.properties.getProperty(m.group(1)) != null) {
					name = name.replace(m.group(0), this.properties.getProperty(m.group(1)));
				}
				else
					matched = false;
			}
		}
		return name;
	}
	
	protected void handlePomType(String groupId,String artifactId,String version,String module,boolean isPomLib,boolean isParent) {
		if(groupId == null || artifactId == null || version == null) {
			String[] value = { version, module };
			this.unParsedPomTypeLibs.put(groupId + " " + artifactId, value);
			return;
		}
		String newPath = pomFilePath + groupId+" "+artifactId+" "+version+".pom";
		File file = new File(newPath);
		if (file.exists())
			parsePomFile(newPath,null,true,isParent);
//		if(groupId == null || artifactId == null || version == null) {
//			String[] value = { version, module };
//			this.unParsedPomTypeLibs.put(groupId + " " + artifactId, value);
//			return;
//		}
////	    String content = Client.post("http://10.222.147.212:8080", groupId, artifactId, version);
////		String content = Client.post("http://192.168.1.107:8080", groupId, artifactId, version);
//		String content = Client.post("http://localhost:8080", groupId, artifactId, version);
//		if (content != null && content.length() > 0) {
//		    InputStream is = new ByteArrayInputStream(content.getBytes());  
//			parsePomFile(is,"", null,isPomLib,isParent);
//		} else {			
//			String[] value = { version, module };
//			this.unParsedPomTypeLibs.put(groupId + " " + artifactId, value);
//		}
	}
	
	protected void addToLibVerPairs(Map<String,Map<String,String[]>> pairs,String lib,String version,String module,String information) {
		if(pairs.containsKey(lib)) {
			if(pairs.get(lib).containsKey(null)) {
				String[] value = {module,information};
				pairs.get(lib).remove(null);
				pairs.get(lib).put(version, value);
			}
			else if(version == null) {			
			}
			else if(!pairs.get(lib).containsKey(version)) {		
				System.out.println(lib + " version:"+version+"  "+ pairs.get(lib).keySet().toString());
				String[] value = {module,information};
				pairs.get(lib).put(version, value);
			}
		}
		else {
			String[] value = {module,information};
			Map<String,String[]> dep = new HashMap<>();
			dep.put(version, value);
			pairs.put(lib, dep);
		}
	}
	
	private void libVerPairsToArray() {
		for (Map.Entry<String, Map<String,String[]>> entry : this.libVerPairs.entrySet()) {
			String libStr = entry.getKey();
			String[] lib = libStr.split(" ");
			for (Map.Entry<String, String[]> value : entry.getValue().entrySet()) {
				JSONObject obj = new JSONObject();
				obj.put("groupId", lib[0]);
				obj.put("artifactId", lib[1]);
				if(lib.length == 3) {
					obj.put("type", lib[2]);
//					System.out.println(lib[2]);
				}	
				String version = value.getKey();
				obj.put("version", version);
				obj.put("module", value.getValue()[0]);
				obj.put("information", value.getValue()[1]);
				if(version == null && this.libVerPairsInPom.get(libStr) != null) {
					for (Map.Entry<String, String[]> pomLib : this.libVerPairsInPom.get(libStr).entrySet()) {
						if(pomLib.getKey() != null) {
							obj.put("version", pomLib.getKey());
//							obj.put("module", pomLib.getValue()[0]);
//							obj.put("information", pomLib.getValue()[1]);
							break;
						}
					}
				}			
				this.dependenciesArray.add(obj);
//				System.out.println(obj.get("groupId"));
//				System.out.println(obj.get("artifactId"));
//				System.out.println(obj.get("version"));
//				System.out.println(obj.get("type"));
//				System.out.println(obj.get("module"));
//				System.out.println(obj.get("information"));
	
//				System.out.println();
			}			
		}
	}
}
