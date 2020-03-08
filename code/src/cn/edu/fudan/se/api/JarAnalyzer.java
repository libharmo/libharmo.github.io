package cn.edu.fudan.se.api;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import cn.edu.fudan.se.util.DBUtil;
import cn.edu.fudan.se.util.AstParserUtil;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.symbolsolver.javaparser.Navigator;

import cn.edu.fudan.se.util.JsonFileUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JarAnalyzer {
	
	private JSONArray clazzs = new JSONArray();
//	private static String SAVE_PATH = "C:/Users/yw/Desktop/test/";
//	private static String INPUT_FILE_PATH = "C:/Users/yw/Desktop/test/";
	private static String DECOMPILED_LIB_PATH = "F:/GP/decompile/";
	private boolean go = false;		
	int index = 0;
	PreparedStatement statement = null;
	int count = 0;
	int nullFileCount = 0;
	int totalFileCount = 0;

	private void parseFile(int versionTypeId,String dir,String pathPrefix) {
		File or = new File(dir);
		File[] files = or.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile() && file.getName().endsWith(".java") && !file.getName().contains("$")) {
					String absolutePath = file.getAbsolutePath();
					absolutePath = absolutePath.substring(0, absolutePath.length()-5);
					if(absolutePath.replace("\\", "/").startsWith(pathPrefix.replace("\\", "/"))) {
						String apiClass = absolutePath.substring(pathPrefix.length()+1).replace("/", ".").replace("\\", ".");						
//						if(go) {
						System.out.println("+++++++++++++++++++++++++++ "+apiClass);
						try {
							Map<String,String> apis = getApi(AstParserUtil.getCompilationUnit(file.getAbsolutePath()),apiClass);
							apiPersistence(versionTypeId,apiClass,apis);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						catch (OutOfMemoryError e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	
//						}
//						if(apiClass.equals("org.sqlite.SQLite")) {
//							go =true;
//						}
					}
					
				} 
				else if (file.isDirectory()) {
					parseFile(versionTypeId,file.getAbsolutePath(),pathPrefix);
				}
			}
		}
	}
	
	private void parseFile(String dir,String pathPrefix) {
		File or = new File(dir);
		File[] files = or.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile() && file.getName().endsWith(".java") && !file.getName().contains("$")) {
//						&& file.getName().equals("OperationRunnerImpl.java") BackupAwareEntryProcessor.java
					String absolutePath = file.getAbsolutePath();
					absolutePath = absolutePath.substring(0, absolutePath.length()-5);
					if(absolutePath.replace("\\", "/").startsWith(pathPrefix.replace("\\", "/"))) {
						this.totalFileCount ++;
						String apiClass = absolutePath.substring(pathPrefix.length()+1).replace("/", ".").replace("\\", ".");						
						System.out.println("+++++++++++++++++++++++++++ "+apiClass);
						try {
							CompilationUnit cu = AstParserUtil.getCompilationUnit(file.getAbsolutePath());
							if(cu == null) {
								this.nullFileCount ++;
								continue;
							}
							Map<String,String> apis = getApi(cu,apiClass);
							JSONObject clazz = apiPersistence(apiClass,apis);
							if(clazz != null) {
								clazzs.add(clazz);
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						catch (OutOfMemoryError e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}							
					}
					
				} 
				else if (file.isDirectory()) {
					parseFile(file.getAbsolutePath(),pathPrefix);
				}
			}
		}
	}
	
	public JSONObject apiPersistence(String apiClass,Map<String,String> apis) {	
		if(apis.size() == 0)
			return null;
		JSONArray array = new JSONArray();
		for (Map.Entry<String, String> entry : apis.entrySet()) {
			JSONObject obj = new JSONObject();
			obj.put("name", entry.getKey());
			obj.put("remark", entry.getValue());
			array.add(obj);
		}
		JSONObject clazz = new JSONObject();
		clazz.put("name", apiClass);
		clazz.put("api", array);
		return clazz;
	}
	
	public JSONObject apiPersistenceWithHashCode(String apiClass,Map<String,String> apis) {	
		if(apis.size() == 0)
			return null;
		JSONArray array = new JSONArray();
		for (Map.Entry<String, String> entry : apis.entrySet()) {
			JSONObject obj = new JSONObject();
			obj.put("name", entry.getKey());
			String str = entry.getValue();
			String[] data = str.split("--");
			obj.put("hashcode", data[1]);
			obj.put("remark", data[0]);
			array.add(obj);
		}
		JSONObject clazz = new JSONObject();
		clazz.put("name", apiClass);
		clazz.put("api", array);
		return clazz;
	}
	
	private void apiPersistence(int versionTypeId,String apiClass,Map<String,String> apis) {
		if(apis.size() == 0)
			return;
//		ResultSet rrs = DBUtil.query("SELECT * FROM `api_classes` where `version_type_id`=" + versionTypeId);
//		try {
//			while (rrs.next()) {
//				return;
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		String sql = "INSERT INTO api_classes(version_type_id,class_name) VALUES ("+ versionTypeId + ",\'" + apiClass + "\')";
		System.out.println(sql);
		DBUtil.update(sql);
		int classId=-1;
		ResultSet rs = DBUtil.query("select LAST_INSERT_ID()");
		try {
			if (rs.next())
				classId = rs.getInt(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (Map.Entry<String, String> entry : apis.entrySet()) {
			count++;
			
			try {
				System.out.println(classId);
				statement.setInt(1,classId);			
				statement.setString(2,entry.getKey());
				statement.setString(3,entry.getValue());
				statement.addBatch();  //增加批处理
				
				if(count == 5000) {
					statement.executeBatch();  //执行批处理
					DBUtil.getConnection().commit();  //提交事务
					count = 0;
				}	
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
//			sql = "INSERT INTO api_interface(class_id,name,remark) VALUES ("+ classId + ",\'" + entry.getKey() + "\', \'"+entry.getValue()+"\')";
//			System.out.println(sql);
//			DBUtil.update(sql);
		}
	}
	
	private void printList(Map<String,String> apis) {
		for (Map.Entry<String, String> entry : apis.entrySet()) {
			System.out.println(entry.getKey());
		}
	}
	
	public Map<String,String> getApiWithHashCode(CompilationUnit cu,String importClass) throws IOException {
		Map<String,String> apis = new HashMap<>();		
		if(cu ==null)
			return apis;
		String clazz = importClass.substring(importClass.lastIndexOf(".")+1);
		apis.put(clazz, "ClassOrInterface--"+String.valueOf(clazz.hashCode()));
		List<FieldDeclaration> fieldDeclarations = Navigator.findAllNodesOfGivenClass(cu, FieldDeclaration.class);
		List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarations = Navigator.findAllNodesOfGivenClass(cu, ClassOrInterfaceDeclaration.class);
		List<ConstructorDeclaration> constructorDeclarations = Navigator.findAllNodesOfGivenClass(cu, ConstructorDeclaration.class);
		List<MethodDeclaration> methodDeclarations = Navigator.findAllNodesOfGivenClass(cu, MethodDeclaration.class);
		for(FieldDeclaration fd:fieldDeclarations){
			if(fd.isPublic()) {
				List<Node> children= fd.getChildNodes();
				for(Node c:children) {
					if(c instanceof VariableDeclarator) {
						String field = ((VariableDeclarator)c).getNameAsString();
						if(!apis.containsKey(field))
							apis.put(field, "Field--"+String.valueOf(fd.hashCode()));
					}
				}
			}
		}
		for(MethodDeclaration md:methodDeclarations){
			if(md.isPublic()) {
				String name = md.getNameAsString();
				String method = md.getDeclarationAsString(false, false, false);
				int index = method.indexOf(name);
				method = method.substring(index);
				if(!apis.containsKey(method))
					apis.put(method, "Method--"+String.valueOf(md.hashCode()));
			}
		}
		for(ConstructorDeclaration cd:constructorDeclarations){
			if(cd.isPublic()) {
				String constructor = cd.getDeclarationAsString(false, false, false);
				if(!apis.containsKey(constructor))
					apis.put(constructor, "Constructor--"+String.valueOf(cd.hashCode()));
//				System.out.println(constructor);
			}	
		}
		for(ClassOrInterfaceDeclaration cid:classOrInterfaceDeclarations){
			if(cid.isPublic()) {
				String classOrInterface = cid.getNameAsString();
				if(!apis.containsKey(classOrInterface))
					apis.put(classOrInterface, "ClassOrInterface--"+String.valueOf(cid.hashCode()));
			}
		}
		return apis;
	}
	
	public Map<String,String> getApi(CompilationUnit cu,String importClass) throws IOException {
		Map<String,String> apis = new HashMap<>();		
		if(cu ==null)
			return apis;
		String clazz = importClass.substring(importClass.lastIndexOf(".")+1);
		apis.put(clazz, "ClassOrInterface");
		List<FieldDeclaration> fieldDeclarations = Navigator.findAllNodesOfGivenClass(cu, FieldDeclaration.class);
		List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarations = Navigator.findAllNodesOfGivenClass(cu, ClassOrInterfaceDeclaration.class);
		List<ConstructorDeclaration> constructorDeclarations = Navigator.findAllNodesOfGivenClass(cu, ConstructorDeclaration.class);
		List<MethodDeclaration> methodDeclarations = Navigator.findAllNodesOfGivenClass(cu, MethodDeclaration.class);
		for(FieldDeclaration fd:fieldDeclarations){
			if(fd.isPublic()) {
//				System.out.println("================= " + fd);
//				List<Node> children= fd.getChildNodes();
//				for(Node c:children) {
//					if(c instanceof VariableDeclarator) {
//						String field = ((VariableDeclarator)c).getNameAsString();
//						if(!apis.containsKey(field))
//							apis.put(field, "Field");
//					}
//				}
			}
		}
		for(MethodDeclaration md:methodDeclarations){
			if(md.isPublic()) {
				String name = md.getNameAsString();
				String method = md.getDeclarationAsString(false, false, false);
//				md.
//				ResolvedMethodDeclaration rmd= JavaParserFacade.get(typeSolver).solve(methodCallExpr).getCorrespondingDeclaration();
//				String signature = rmd.getQualifiedSignature();	
				System.out.println("================= " + method);
//				int index = method.indexOf(name);
//				method = method.substring(index);
//				if(!apis.containsKey(method))
//					apis.put(method, "Method");
			}
		}
		for(ConstructorDeclaration cd:constructorDeclarations){
			if(cd.isPublic()) {
				String constructor = cd.getDeclarationAsString(false, false, false);
				System.out.println("================= " + constructor);
//				if(!apis.containsKey(constructor))
//					apis.put(constructor, "Constructor");
			}	
		}
		for(ClassOrInterfaceDeclaration cid:classOrInterfaceDeclarations){
			if(cid.isPublic()) {
//				System.out.println("================= " + cid);
//				String classOrInterface = cid.getNameAsString();
//				if(!apis.containsKey(classOrInterface))
//					apis.put(classOrInterface, "ClassOrInterface");
			}
		}
		return apis;
	}
	
	public void enterParseById(int typeId) {
		ResultSet rs = DBUtil.query("SELECT * FROM `version_types` where `type_id`=" + typeId);
		try {
			statement = DBUtil.getConnection().prepareStatement("INSERT INTO api_interface(class_id,name,remark) VALUES(?,?,?)");
			while (rs.next()) {
				String jackage_url = rs.getString("jar_package_url");
				if(jackage_url.endsWith(".jar")) {
					String path = DECOMPILED_LIB_PATH+jackage_url.substring(0, jackage_url.length()-4)+"_decompile";
					File file = new File(path);
					if(!file.exists()) {
						System.out.println("package : " + path +"  not exists");
						return;
					}
					enterParse(typeId,path);
				}
			}
			statement.executeBatch();  //执行批处理
			DBUtil.getConnection().commit();  //提交事务
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	public void enterParse(int versionTypeId,String decompiledLibPath) {			
		parseFile(versionTypeId,decompiledLibPath,decompiledLibPath);
	}
	
	public void enterSave(String dirPath,String decompiledLibName,String savePath) {			
		parseFile(dirPath + decompiledLibName,dirPath + decompiledLibName);
		
		JSONObject cntObj = new JSONObject();
		cntObj.put("nullFileCount", this.nullFileCount);
		cntObj.put("totalFileCount", this.totalFileCount);
		clazzs.add(cntObj);
		System.out.println("ccccccccccccccc  " + this.nullFileCount + "/" + this.totalFileCount);
		
		JsonFileUtil.save(savePath + decompiledLibName+".txt", clazzs);
	}
	
//	public static void main(String[] args) {
//		for(int i = 1981;i<=1981;i++) {
//			JarAnalyzer ja = new JarAnalyzer();		
//			System.out.println(i);
//			ja.enterParseById(i);
//		}
//	}
}
