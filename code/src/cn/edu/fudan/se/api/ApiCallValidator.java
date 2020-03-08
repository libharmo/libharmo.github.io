package cn.edu.fudan.se.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import cn.edu.fudan.se.util.DBUtil;
import cn.edu.fudan.se.util.AstParserUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.symbolsolver.javaparser.Navigator;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;

import cn.edu.fudan.se.util.FastJsonUtil;
import cn.edu.fudan.se.util.FileUtil;
import cn.edu.fudan.se.util.JsonFileUtil;

public class ApiCallValidator {
	protected List<String> libLocalPaths;
	private List<String> importList;
	protected List<String> contextList;
//	private List<Integer> libList;
	protected Map<String,Integer> allClasses = new HashMap<>();
	protected String proPath;
	protected int projectId;
//	protected String OUTPUT_PATH = "H:/wangying/api_call_output/";	
	protected CombinedTypeSolver typeSolver;
	private Map<Integer,Integer> apiCallCount;
	int allApiCallCount = 0;
		
	protected static String CALL_PATH;
//	protected static String CALL_FILE_PATH;
	protected static String PROJECT_DIR;
	protected static String LIB_PATH;
	protected static String LIB_LIST;

	public ApiCallValidator(int id,String path) {
		this.projectId = id;
		this.proPath = path;
		getLibsUsedByProj();
//		getAllLibPaths();
		this.contextList = new ArrayList<>();
		getContext(this.proPath);
		this.typeSolver = new CombinedTypeSolver(ProjectAnalyzer.createJarTypeSolverList(this.libLocalPaths,this.contextList));	
		for(String jar : this.libLocalPaths) {
			System.out.println(jar);
		}
	}
		
	public ApiCallValidator() {
	}

	private void getLibsUsedByProj() {
		this.libLocalPaths = new ArrayList<>();
//		ResultSet rs = DBUtil.query("SELECT * FROM `project_lib_usage` where `project_id`=" + this.projectId);
//		try {
//			while (rs.next()) {
//				int versionTypeId = rs.getInt("version_type_id");
//				ResultSet trs = DBUtil.query("SELECT * FROM `version_types` where `type_id`=" + versionTypeId);
//				while (trs.next()) {
//					String packageUrl = trs.getString("jar_package_url");
//					if(packageUrl.endsWith(".jar"))
//						this.libLocalPaths.add(LIB_PATH+packageUrl);
//				}
//				ResultSet crs = DBUtil.query("SELECT * FROM `api_classes` where `version_type_id`=" + versionTypeId);
//				while (crs.next()) {
//					String className = crs.getString("class_name");
//					int classId = crs.getInt("id");		
//					this.allClasses.put(className, classId);		
//				}
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			System.exit(0);
//		}
//		String dirPath = "lib_list/";
//		String dirPath = "C:/lib_list/";
		String dirPath = LIB_LIST;
//		System.out.println(dirPath+this.projectId+".json" + " " + new File(dirPath+this.projectId+".json").exists());
		if(new File(dirPath+this.projectId+".json").exists()) {
			String content = JsonFileUtil.readJsonFile(dirPath+this.projectId+".json");
			JSONArray array = JSON.parseArray(content);
			for(int i=0;i<array.size();i++) {
//				System.out.println(array.getString(i));
				if(array.getString(i).endsWith(".jar") && new File(LIB_PATH + array.getString(i)).exists())
					this.libLocalPaths.add(LIB_PATH+array.getString(i));
			}
		}
		
	}

	protected void getContext(String dir) {
		File or = new File(dir);
		File[] files = or.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					String path = file.getAbsolutePath();
					if(path.endsWith("\\src\\test\\java")||path.endsWith("\\src\\main\\java")) {
						if(!this.contextList.contains(path))
							this.contextList.add(path);						
					}
					getContext(file.getAbsolutePath());
				}
			}
		}
	}	

	
//	private List<String> getContextForOneFile(String path) {
//		List<String> result = new ArrayList<>();
//		for (String context: this.contextList) {
//			if(path.startsWith(context)) 
//				result.add(context);
//		}
//		return result;
//	}
	
	public Map<String,Integer> getAllApisOfLibUsedByOneFile() {
		Map<String,Integer> apis = new HashMap<>();
		for (Map.Entry<String, Integer> clazz : this.allClasses.entrySet()) {
			String className = clazz.getKey();
			if(isImported(className)) {
				int classId = clazz.getValue();
				ResultSet irs = DBUtil.query("SELECT * FROM `api_interface` where `class_id`=" + classId);
				System.out.println(className + " " + classId);
				try {
					while (irs.next()) {
						String apiName = irs.getString("name");
						int apiId = irs.getInt("id");
						apis.put(apiName, apiId);
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
		}
		return apis;
	}	
	
//	private Map<String,Integer> getApisOfOneLib(int typeId) {
//		Map<String,Integer> apis = new HashMap<>();
//		ResultSet rs = DBUtil.query("SELECT * FROM `api_classes` where `version_type_id`=" + typeId);
//		try {
//			while (rs.next()) {
//				String className = rs.getString("class_name");
//				if(isImported(className)) {
////					System.out.println(className);
//					int classId = rs.getInt("id");
//					ResultSet irs = DBUtil.query("SELECT * FROM `api_interface` where `class_id`=" + classId);
//					while (irs.next()) {
//						String apiName = irs.getString("name");
//						int apiId = irs.getInt("id");
//						apis.put(apiName, apiId);
//					}
//				}				
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return apis;
//	}
	
//	public Map<String,Integer> getAllApisOfLibUsedByOneFile() {
//		Map<String,Integer> apis = new HashMap<>();
//		for(int libId:this.libList) {
//			apis.putAll(getApisOfOneLib(libId));
//		}
//		return apis;
//	}	
	
	protected void getImportListOfFile(String filePath) {
		this.importList = new ArrayList<String>();
		CompilationUnit cu = AstParserUtil.getCompilationUnit(filePath);
		if(cu == null)
			return;
		List<ImportDeclaration> imports = Navigator.findAllNodesOfGivenClass(cu, ImportDeclaration.class);
		for(ImportDeclaration im:imports){
			String type = im.getNameAsString();
			this.importList.add(type);
		}		
	}
	
	protected boolean isImported(String apiClass) {
		boolean imported = false;
		for(String im:this.importList) {
			if(apiClass.startsWith(im) || im.startsWith(apiClass)) {
				imported = true;
				break;
			}
		}
		return imported;
	}	
	
	
	private void printApiList(Map<String,Integer> apis) {
		for (Map.Entry<String, Integer> api : apis.entrySet()) {
			System.out.println(api.getKey());
		}
	}
	
//	public void validateApiCall(int projectId) {		
//		getImportListOfFile("C:/Users/yw/Desktop/projects/ActionBarSherlock/actionbarsherlock/src/com/actionbarsherlock/ActionBarSherlock.java");
//		Map<String,Integer> apis = getAllApisOfLibUsedByOneFile(projectId);
//		System.out.println(apis.size());
//		ProjectAnalyzer pa = new ProjectAnalyzer();
//		List<String> call = pa.analyseOneFile("C:/Users/yw/Desktop/projects/ActionBarSherlock/actionbarsherlock/src/com/actionbarsherlock/ActionBarSherlock.java", projectId);
////		for(int i = 0; i < call.size();i++) {
////			if(apis.containsKey(call.get(i))) {
////				System.out.println(call.get(i));
////			}
////		}
//	}
	private void findApiCall(Map<String,Integer> apis,List<String> call) {
		apiCallCount = new HashMap<>();
		for(int i = 0; i < call.size();i++) {
			if(apis.containsKey(call.get(i))) {
				int apiId = apis.get(call.get(i));
				if(this.apiCallCount.containsKey(apiId)) {
					int num = this.apiCallCount.get(apiId);
					this.apiCallCount.replace(apiId,num+1);
				}
				else
					this.apiCallCount.put(apiId, 1);
				System.out.println(call.get(i)+" "+apiId);
			}
		}
	}
	
    private void apiCallPersistence(String filePath) {
    	for (Map.Entry<Integer, Integer> api : this.apiCallCount.entrySet()) {
    		String sql = "INSERT INTO api_call(project_id,api_id,count,file_name) VALUES ("+ this.projectId + "," + api.getKey() + ", "+api.getValue()+",'"+filePath+"')";
			DBUtil.update(sql);
		}
	}
    
    private void apiCallToFile(String output,String filePath) {
    	FileWriter writer = null;  
        try {     
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件     
            writer = new FileWriter(output);     
            for (Map.Entry<Integer, Integer> api : this.apiCallCount.entrySet()) {
        		String sql = "INSERT INTO api_call(project_id,api_id,count,file_name) VALUES ("+ this.projectId + "," + api.getKey() + ", "+api.getValue()+",'"+filePath+"')";
        		writer.write(sql+"\r\n");   
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

    public void browse(List<File> fileList,String dir){
    	File or = new File(dir);
		File[] files = or.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile() && file.getName().endsWith(".java")) {
					fileList.add(file);
				}else if (file.isDirectory()) {
					browse(fileList,file.getAbsolutePath());
				}
			}
		}
    }
//    public void validate(int fileId,File file){
//		String absolutePath = file.getAbsolutePath();
//		String filePath = absolutePath.replace("\\", "/");
//		if(filePath.startsWith(this.proPath)) 
//			filePath = filePath.substring(this.proPath.length());
//		System.out.println("----------------------"+absolutePath);
//		getImportListOfFile(absolutePath);
//		Map<String,Integer> apis = getAllApisOfLibUsedByOneFile();
//		System.out.println(apis.size());
////		for(Map.Entry<String, Integer> entry : apis.entrySet()) {
////			System.out.println(entry.getKey() + " " + entry.getValue());
////		}
//		if(apis.size() > 0) {
//			ProjectAnalyzer pa = new ProjectAnalyzer();
//			List<String> call = pa.analyseOneFile(absolutePath, this.typeSolver);
//			int totalCount = pa.getTotalCount();
//			int errorCount = pa.getErrorCount();			
//			findApiCall(apis,call);// new HashMap
//			apiCallToFile(OUTPUT_PATH + this.projectId + "_" +fileId+".txt",filePath);
//			System.out.println("totalCount : "+totalCount);
//			System.out.println("errorCount : "+errorCount);
//			FileUtil.appendFile(OUTPUT_PATH + this.projectId + "_" +fileId+".txt", totalCount+"");
//			FileUtil.appendFile(OUTPUT_PATH + this.projectId + "_" +fileId+".txt", errorCount+"");
//		}	
//    }
    public void appendCall(int fileId,File file, String savePath){
		String absolutePath = file.getAbsolutePath();
		String filePath = absolutePath.replace("\\", "/");
		if(filePath.startsWith(this.proPath)) 
			filePath = filePath.substring(this.proPath.length());
		System.out.println("----------------------"+absolutePath);
		ProjectAnalyzer pa = new ProjectAnalyzer();
		List<String> call = pa.analyseOneFile(absolutePath, this.typeSolver);
		int totalCount = pa.getTotalCount();
		int errorCount = pa.getErrorCount();		
		call.add(errorCount+" " +totalCount);	
		call.add(fileId+"");
//		JSONArray array= JSONArray.parseArray(JSON.toJSONString(call));
//		FastJsonUtil.writeJson(savePath, array);
		FileUtil.appendLine(savePath, JSON.toJSONString(call));
		System.out.println("totalCount : "+totalCount);
		System.out.println("errorCount : "+errorCount);	
    }
    
    public void call(int fileId,File file, String savePath){
		String absolutePath = file.getAbsolutePath();
		String filePath = absolutePath.replace("\\", "/");
		if(filePath.startsWith(this.proPath)) 
			filePath = filePath.substring(this.proPath.length());
		System.out.println("----------------------"+absolutePath);
		ProjectAnalyzer pa = new ProjectAnalyzer();
		List<String> call = pa.analyseOneFile(absolutePath, this.typeSolver);
		int totalCount = pa.getTotalCount();
		int errorCount = pa.getErrorCount();		
		call.add(errorCount+" " +totalCount);	
		JSONArray array= JSONArray.parseArray(JSON.toJSONString(call));
		FastJsonUtil.writeJson(savePath, array, false);
		System.out.println("totalCount : "+totalCount);
		System.out.println("errorCount : "+errorCount);	
    }

    public void callWithParent(int fileId,File file, String savePath){
		String absolutePath = file.getAbsolutePath();
		String filePath = absolutePath.replace("\\", "/");
		if(filePath.startsWith(this.proPath))
			filePath = filePath.substring(this.proPath.length());
		System.out.println("----------------------"+absolutePath);
		CallInMethodDeclaration cmd = new CallInMethodDeclaration();
		cmd.analyseOneFileInMethod(absolutePath, this.typeSolver);
//		int errorCountInParent = cmd.errorCountInParent;
//		int errorCountOutParent = cmd.errorCountOutParent;
//		int totalCountInParent = cmd.totalCountInParent;
//		int totalCountOutParent = cmd.totalCountOutParent;
		JSONObject obj = new JSONObject();
		obj.put("path", filePath);
		obj.put("callOutParent", JSONArray.parseArray(JSON.toJSONString(cmd.exprOutParent)));
		obj.put("otherDeclaration", JSONArray.parseArray(JSON.toJSONString(cmd.otherDeclaration)));
		obj.put("callInParent", JSONObject.parseObject(JSON.toJSONString(cmd.exprInParent)));
		obj.put("errorCountInParent", cmd.errorCountInParent);
		obj.put("errorCountOutParent", cmd.errorCountOutParent);
		obj.put("totalCountInParent", cmd.totalCountInParent);
		obj.put("totalCountOutParent", cmd.totalCountOutParent);
		FastJsonUtil.writeJsonString(savePath, obj.toJSONString());
		System.out.println("errorCountInParent : "+cmd.errorCountInParent);
		System.out.println("errorCountOutParent : "+cmd.errorCountOutParent);
		System.out.println("totalCountInParent : "+cmd.totalCountInParent);
		System.out.println("totalCountOutParent : "+cmd.totalCountOutParent);
//		System.out.println("totalCount : "+cmd.getTotalCount());
//		System.out.println("errorCount : "+cmd.getErrorCount());
    }

	public void validateFile(String dir) {
		File or = new File(dir);
		File[] files = or.listFiles();
//		Arrays.sort(files);
		if (files != null) {
			for (File file : files) {
				if (file.isFile() && file.getName().endsWith(".java")) {
//	60					&& file.getAbsolutePath().equals("F:\\GP\\high_quality_repos\\apache\\hive\\hcatalog\\webhcat\\svr\\src\\test\\java\\org\\apache\\hive\\hcatalog\\templeton\\TestWebHCatE2e.java")) {
					//63 F:\GP\high_quality_repos\apache\hbase\hbase-endpoint\src\test\java\org\apache\hadoop\hbase\coprocessor\TestBatchCoprocessorEndpoint.java
//43					F:\GP\high_quality_repos\yusuke\twitter4j\twitter4j-async\src\main\java\twitter4j\AsyncTwitterImpl.java
//	4				&& file.getAbsolutePath().equals("F:\\GP\\high_quality_repos\\netty\\netty\\testsuite\\src\\main\\java\\io\\netty\\testsuite\\transport\\socket\\DatagramConnectNotExistsTest.java")) {
					String absolutePath = file.getAbsolutePath();
					String filePath = absolutePath.replace("\\", "/");
					if(filePath.startsWith(this.proPath)) 
						filePath = filePath.substring(this.proPath.length());
					
					System.out.println("----------------------"+absolutePath);
//					boolean skip = false;
//					ResultSet irs = DBUtil.query("SELECT * FROM `api_call` where `project_id`=" + this.projectId +" and `file_name` = '"+filePath+"'");
//					try {
//						while (irs.next()) {
//							skip = true;
//						}
//					} catch (SQLException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}	
//					if(skip)
//						continue;
					getImportListOfFile(absolutePath);
					Map<String,Integer> apis = getAllApisOfLibUsedByOneFile();
					System.out.println(apis.size());
					if(apis.size() > 0) {
						ProjectAnalyzer pa = new ProjectAnalyzer();
						List<String> call = pa.analyseOneFile(absolutePath, this.typeSolver);
						findApiCall(apis,call);
						apiCallToFile(this.projectId+".txt",filePath);
//						apiCallPersistence(filePath);
					}					
				}		
				else if (file.isDirectory()) {
					validateFile(file.getAbsolutePath());
				}
			}
		}
	}	
	
	public static void readFile(String path) {
		String proLocalPath = "F:/GP/high_quality_repos/";
//		String proLocalPath = "E:/high_quality_repos/";
		int index = 0;
		Map<Integer,String> projects = new HashMap<>();
		try {
			Scanner in = new Scanner(new File(path));
			while (in.hasNextLine()) {
				String str = in.nextLine();
				String absolutePath = null;
				if(str.startsWith("../data/prior_repository/")) {
					absolutePath = proLocalPath +str.substring(25);
					File file = new File(absolutePath);
					if(file.exists()) {
						projects.put(index, absolutePath);
					}
				}					
				else 
					index = Integer.parseInt(str);				
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		//218-250
		for(int i = 69;i <= 69;i++) {
			if(projects.containsKey(i)) {
				System.out.println("-------------------projectId:"+i);
				ApiCallValidator apiCallValidator = new ApiCallValidator(i,projects.get(i));
				apiCallValidator.validateFile(projects.get(i));
			}			
		}
	}
	
//	public static void collectProjs(int num1,int num2,int id) {
//		String content = JsonFileUtil.readJsonFile("proj_in_usage.txt");
//		JSONArray array = JSONArray.parseArray(content);
////		System.out.println(array.size());
//		for(int i =0;i<array.size();i++) {
//			JSONObject obj = array.getJSONObject(i);
//			int projectId = obj.getInteger("id");
//			String localAddr = obj.getString("local_addr");
////			if(localAddr.startsWith("F:/wangying/projects_last_unzips/"))
////				localAddr = "C:/"+localAddr.replace("F:/wangying/projects_last_unzips/","");
//			
//			localAddr = PROJECT_DIR + localAddr.replace("F:/wangying/projects_last_unzips/","").replace("C:/","").replace("D:/","").replace("E:/","").replace("F:/","");
//
////			if(projectId >= 20) {
//			if(projectId == id) {
//				System.out.println(projectId + " " + localAddr);
//				if(new File(localAddr).exists()) {
//					ApiCallValidator apiCallValidator = new ApiCallValidator(projectId, localAddr +"/");
//					List<File> fileList = new ArrayList<File>();
//					apiCallValidator.browse(fileList, localAddr +"/");
//					
//					for (int f = num1; f < num2; f++) {
//						if(!new File(OUTPUT_PATH + projectId + "_" +f+".txt").exists()) {
//							File tmp = fileList.get(f);
//							System.out.println("File " + f + " : " + tmp.getAbsolutePath());
//							apiCallValidator.validate(f, tmp);
//						}						
//					}					
//				}
//				else{
//					try {
//						throw new Exception();
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//						System.exit(0);
//					}
//				}				
//			}
//		}
//	}
	
	public static void extractCalls(int num1,int num2,int id) {
		String content = JsonFileUtil.readJsonFile("proj_in_usage.txt");
		JSONArray array = JSONArray.parseArray(content);
//		System.out.println(array.size());
		for(int i =0;i<array.size();i++) {
			JSONObject obj = array.getJSONObject(i);
			int projectId = obj.getInteger("id");
			String localAddr = obj.getString("local_addr");
			if(localAddr.startsWith("F:/wangying/projects_last_unzips/"))
				localAddr = "C:/"+localAddr.replace("F:/wangying/projects_last_unzips/","");
			
//			localAddr = PROJECT_DIR + localAddr.replace("F:/wangying/projects_last_unzips/","").replace("C:/","").replace("D:/","").replace("E:/","").replace("F:/","");

//			localAddr = PROJECT_DIR + localAddr.replace("F:/wangying/projects_last_unzips/", "").replace("C:/", "").replace("D:/", "")
//					.replace("E:/", "").replace("F:/", "").replace("gradle_maven200_500/", "").replace("gradle_maven500/", "").replace("maven200_500/", "").replace("maven500/", "").replace("gradle200_500/", "").replace("gradle500/", "");
			
//			if(projectId >= 20) {
			if(projectId == id) {
//				System.out.println(projectId + " " + localAddr);
				if(new File(localAddr).exists()) {
					System.out.println(projectId + " " + localAddr);
					ApiCallValidator apiCallValidator = new ApiCallValidator(projectId, localAddr +"/");
					List<File> fileList = new ArrayList<File>();
					apiCallValidator.browse(fileList, localAddr +"/");
					
					for (int f = num1; f < num2; f++) {
						if(!new File(CALL_PATH + projectId + "_" +f+".txt").exists()) {
							File tmp = fileList.get(f);
							System.out.println("File " + f + " : " + tmp.getAbsolutePath());
//							if(tmp.getAbsolutePath().equals("C:\\gradle500\\aol__fdse__cyclops-react\\cyclops\\src\\jmh\\java\\cyclops\\reactiveSeq\\Combinations.java")) {
							apiCallValidator.call(f, tmp, CALL_PATH + projectId + "_" +f+".txt");
//							}
						}						
					}	
					
//					FileUtil.appendFile(CALL_FILE_PATH + projectId + ".txt", num1+ "_" +num2 + ":" + AstParser.errorFile+" " + AstParser.totalFile);
				}
//				else{
//					try {
//						throw new Exception();
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//						System.exit(0);
//					}
//				}				
			}
		}
	}
	
	public static void extractCallsWithParent(int num1,int num2,int id) {
		String content = JsonFileUtil.readJsonFile("proj_in_usage.txt");
		JSONArray array = JSONArray.parseArray(content);
//		System.out.println(array.size());
		for(int i =0;i<array.size();i++) {
			JSONObject obj = array.getJSONObject(i);
			int projectId = obj.getInteger("id");
			String localAddr = obj.getString("local_addr");
			if(localAddr.startsWith("F:/wangying/projects_last_unzips/"))
				localAddr = "C:/"+localAddr.replace("F:/wangying/projects_last_unzips/","");

//			localAddr = PROJECT_DIR + localAddr.replace("F:/wangying/projects_last_unzips/","").replace("C:/","").replace("D:/","").replace("E:/","").replace("F:/","");

//			localAddr = PROJECT_DIR + localAddr.replace("F:/wangying/projects_last_unzips/", "").replace("C:/", "").replace("D:/", "")
//					.replace("E:/", "").replace("F:/", "").replace("gradle_maven200_500/", "").replace("gradle_maven500/", "").replace("maven200_500/", "").replace("maven500/", "").replace("gradle200_500/", "").replace("gradle500/", "");

//			if(projectId >= 20) {
			if(projectId == id) {
//				System.out.println(projectId + " " + localAddr);
				if(new File(localAddr).exists()) {
					System.out.println("=============== " + projectId + " " + localAddr);
					ApiCallValidator apiCallValidator = new ApiCallValidator(projectId, localAddr +"/");
					List<File> fileList = new ArrayList<File>();
					apiCallValidator.browse(fileList, localAddr +"/");
					FileUtil.writeFlie("file_id.txt", "");
					for (int f = num1; f < num2; f++) {
						FileUtil.appendLine("file_id.txt", f+"");
						if(!new File(CALL_PATH + projectId + "_" +f+".txt").exists()) {
							File tmp = fileList.get(f);
							System.out.println("File " + f + " : " + tmp.getAbsolutePath());
//							if(tmp.getAbsolutePath().equals("C:\\gradle500\\aol__fdse__cyclops-react\\cyclops\\src\\jmh\\java\\cyclops\\reactiveSeq\\Combinations.java")) {
							apiCallValidator.callWithParent(f, tmp, CALL_PATH + projectId + "_" +f+".txt");
//							}
						}
					}
//					FileUtil.appendFile(CALL_FILE_PATH + projectId + ".txt", num1+ "_" +num2 + ":" + AstParser.errorFile+" " + AstParser.totalFile);
				}
			}
		}
	}
	
	public static void main(String[] args) {
////		readFile("C:/Users/yw/Desktop/pro.txt");
//		ApiCallValidator apiCallValidator = new ApiCallValidator(60,"D:/Workspace/wymain/hive/");
////		apiCallValidator.validateFile("D:/Workspace/wymain/hive/");
//		List<File> fileList = new ArrayList<File>();
//		apiCallValidator.browse(fileList, "D:/Workspace/wymain/hive/");
//		
//		for(int i =1782;i<fileList.size();i++){
//			System.out.println("File: "+i);
//			File tmp = fileList.get(i);
//			apiCallValidator.validate(i,tmp);
//		}
		
//		CALL_PATH = "D:/data/call_with_parent/call/";
////		CALL_FILE_PATH = "D:/data/call_with_parent/call_file_count/";
//		PROJECT_DIR = "F:/";
//		LIB_PATH = "C:/third_party_libs/";
		

		PROJECT_DIR = args[3].replace("\r", "").replace("\n", "");
		LIB_PATH = args[4].replace("\r", "").replace("\n", "");
		CALL_PATH = args[5].replace("\r", "").replace("\n", "");
		LIB_LIST = args[6].replace("\r", "").replace("\n", "");
		
		int projectId = Integer.parseInt(args[0]);
		int num1 = Integer.parseInt(args[1]);
		int num2 = Integer.parseInt(args[2]);
		 
		
//		collectProjs(num1,num2,projectId);
		//20 3938 3939 D:/data/call/ D:/data/call_file_count/ F:/
//		extractCalls(num1,num2,projectId);
//		extractCallsWithParent(3924,3925,20);
		extractCallsWithParent(num1,num2,projectId);
		
//		collectProjs();
//		group();
//		part();
	}
	
	public static void part() {
		String content = JsonFileUtil.readJsonFile("H:/wangying/中间过程统计数据/jars.txt");
		JSONArray array = JSONArray.parseArray(content);
//		System.out.println(array.size());
		for(int i =0;i<array.size();i+=60) {
			JSONObject obj = array.getJSONObject(i+59);
			int projectId = obj.getInteger("id");
			System.out.println(projectId);
		}
	}
	
	public static void group() {
		String content = JsonFileUtil.readJsonFile("proj_in_usage.txt");
		JSONArray array = JSONArray.parseArray(content);
		for(int i =0;i<array.size();i++) {
			JSONObject obj = array.getJSONObject(i);
			int projectId = obj.getInteger("id");
			String localAddr = obj.getString("local_addr");
			if(localAddr.startsWith("F:/wangying/projects_last_unzips/"))
				localAddr = "C:/"+localAddr.replace("F:/wangying/projects_last_unzips/","");
			
//			localAddr = PROJECT_DIR + localAddr.replace("F:/wangying/projects_last_unzips/","").replace("C:/","").replace("D:/","").replace("E:/","").replace("F:/","");

//			if(projectId > 0 && projectId <= 6625) {
//			if(projectId == id) {
				System.out.println(projectId + " " + localAddr);
				if(new File(localAddr).exists()) {
					List<File> fileList = new ArrayList<File>();
					getFileList(fileList, localAddr +"/");
					System.out.println(fileList.size());
					
					for (int f = 0; f < fileList.size(); f+=100) {
						int start = f;
						int end = f+100;
						if(end>fileList.size())
							end = fileList.size();		
						String str = "java -jar apicallvalidator.jar "+projectId+" "+start+" "+end;
						FileUtil.appendLine("F:\\rq1_batch\\"+projectId+".sh", str);
					}				
				}
				else{
					try {
						throw new Exception();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.exit(0);
					}
				}	
//			}
		}
	}
	
	public static void getFileList(List<File> fileList,String dir){
    	File or = new File(dir);
		File[] files = or.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile() && file.getName().endsWith(".java")) {
					fileList.add(file);
				}else if (file.isDirectory()) {
					getFileList(fileList,file.getAbsolutePath());
				}
			}
		}
    }
}