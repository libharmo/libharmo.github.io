package cn.edu.fudan.se.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import cn.edu.fudan.se.util.DBUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.util.JsonFileUtil;
import cn.edu.fudan.se.util.MyException;

public class FileDbDataFlow {
//	private static String SAVE_PATH = "C:/Users/yw/Desktop/test/";
//	private static String SAVE_PATH = "F:/GP/output_RQ2/";
//	private static String SAVE_PATH = "D:/data/api_output/";
//	private static String SAVE_PATH = "F:/wangying/api_output/";
	private static String SAVE_PATH = "E:/api_output/";
	private static int count = 0;
	
	public static void updateApiInterface() {
		int count = 0;
		String sql = "INSERT INTO api_interface(id,lib_id) VALUES ";
		try {
			for(int i = 120000;i<=140000;i++) {
				System.out.println(i);
				int class_id = -1,version_type_id = -1,version_id = -1,library_id = -1;
				ResultSet rs2 = DBUtil.query("SELECT * FROM api_interface WHERE id = " + i);
				while (rs2.next()) {
					class_id = rs2.getInt("class_id");					
					break;
				}	
				rs2 = DBUtil.query("SELECT * FROM api_classes WHERE id = " + class_id);
				while (rs2.next()) {
					version_type_id = rs2.getInt("version_type_id");					
					break;
				}
				rs2 = DBUtil.query("SELECT * FROM version_types WHERE type_id = " + version_type_id);
				while (rs2.next()) {
					version_id = rs2.getInt("version_id");					
					break;
				}
				rs2 = DBUtil.query("SELECT * FROM library_versions WHERE id = " + version_id);
				while (rs2.next()) {
					library_id = rs2.getInt("library_id");
					sql += "("+i+","+library_id+"),";
					count++;
					if(count == 5000) {										
						if(sql.endsWith(","))
							sql = sql.substring(0, sql.length()-1);
						sql += " on duplicate key update lib_id=values(lib_id)";
						System.out.println(sql);
						DBUtil.update(sql);
						
						count = 0;
						sql = "INSERT INTO api_interface(id,lib_id) VALUES ";
					}
					break;
				}
			}
			if(sql.endsWith(",")) {
				sql = sql.substring(0, sql.length()-1);
				System.out.println(sql);
				sql += " on duplicate key update lib_id=values(lib_id)";
				DBUtil.update(sql);
			}	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void updateApiCall() {
		int count = 0;
		String sql = "INSERT INTO api_call(id,lib_id) VALUES ";
		try {
			for(int i = 310000;i<=320000;i++) {
				System.out.println(i);
				int api_id,class_id,version_type_id,version_id,library_id;
				ResultSet rs1 = DBUtil.query("SELECT * FROM api_call where id = " + i);
				while (rs1.next()) {
					api_id = rs1.getInt("api_id");
					ResultSet rs2 = DBUtil.query("SELECT * FROM api_interface WHERE id = " + api_id);
					while (rs2.next()) {
						class_id = rs2.getInt("class_id");
						ResultSet rs3 = DBUtil.query("SELECT * FROM api_classes WHERE id = " + class_id);
						while (rs3.next()) {
							version_type_id = rs3.getInt("version_type_id");
							ResultSet rs4 = DBUtil.query("SELECT * FROM version_types WHERE type_id = " + version_type_id);
							while (rs4.next()) {
								version_id = rs4.getInt("version_id");
								ResultSet rs5 = DBUtil.query("SELECT * FROM library_versions WHERE id = " + version_id);
								while (rs5.next()) {
									library_id = rs5.getInt("library_id");
									sql += "("+i+","+library_id+"),";
									count++;
									if(count == 5000) {										
										if(sql.endsWith(","))
											sql = sql.substring(0, sql.length()-1);
										sql += " on duplicate key update lib_id=values(lib_id)";
										System.out.println(sql);
										DBUtil.update(sql);
										
										count = 0;
										sql = "INSERT INTO api_call(id,lib_id) VALUES ";
									}
									break;
								}
								break;
							}
							break;
						}
						break;
					}
					break;
				}									
			}
			if(sql.endsWith(",")) {
				sql = sql.substring(0, sql.length()-1);
				System.out.println(sql);
				sql += " on duplicate key update lib_id=values(lib_id)";
				DBUtil.update(sql);
			}	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void sqlToDb(String path) {
		int index = 0;
		String sql = "INSERT INTO api_call(project_id,api_id,count,file_name) VALUES ";
		try {
			Scanner in = new Scanner(new File(path));
			while (in.hasNextLine()) {
				String str = in.nextLine();
				if(!str.startsWith("INSERT")) {
					continue;
				}
//				count ++;
				String[] apiCall = str.split("VALUES");
				if(apiCall.length == 2) {
					String values = apiCall[1].trim();					
					index++;
					sql += values+",";					
					System.out.println(index);	
					
					if(index == 5000) {
						if(sql.endsWith(","))
							sql = sql.substring(0, sql.length()-1);
						System.out.println(sql);
						DBUtil.update(sql);
						index = 0;		
						sql = "INSERT INTO api_call(project_id,api_id,count,file_name) VALUES ";
					}	
				}
				else {
					throw new MyException("--------------------------length error:"+str);
				}
							
			}
			if(sql.endsWith(",")) {
				sql = sql.substring(0, sql.length()-1);
				System.out.println(sql);
				DBUtil.update(sql);
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (MyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}		
	}
	
	public static void extractApi(String libPath,String outputPath,String metaPath) {
		String whole = JsonFileUtil.readJsonFile(metaPath);
		JSONArray array = JSONArray.parseArray(whole);
		for (int a = 0; a < array.size(); a++) {
			JSONObject obj = array.getJSONObject(a);
			int index = obj.getInteger("id");
			String name = obj.getString("lib");
			name = name.substring(0, name.length()-4)+"_decompile";
//			if(index <= end && index>=start) {
			File file = new File(libPath+name);
			System.out.println(file);
			if(file.exists()){
				JarAnalyzer ja = new JarAnalyzer();		
				ja.enterSave(libPath, name, outputPath);
			}				
//			}
		}
	}
	
	public static void extractApiInDecompilePath(String decompileLibPath,String outputPath) {
		int count = 0;
		File dir = new File(decompileLibPath);
		File[] fileList = dir.listFiles();
		boolean skip = true;
		for(File file:fileList) {
			String fileName = file.getName();			
//			if(fileName.startsWith("t")) {
//				skip = false;
//			}
//			if(skip)
//				continue;
			String outputFile= outputPath + fileName+".txt";
//			System.out.println(outputFile);
			File result = new File(outputFile);
//			if(!result.exists()) {
			if(fileName.equals("aalto-xml-1.0.0_decompile")) {
				count ++;
				System.out.println("+++++++++++++++++++++ "+fileName);
				JarAnalyzer ja = new JarAnalyzer();		
				ja.enterSave(decompileLibPath, fileName, outputPath);
			}	
		}
		System.out.println(count);
	}
	
	public static void meta(String dir) {
		int index = 0;
		JSONArray array = new JSONArray();
		File or = new File(dir);
		File[] files = or.listFiles();
		if (files != null) {
			for (File file : files) {
				String fileName = file.getName();
				if(fileName.endsWith(".jar")) {
					ResultSet rs = DBUtil.query("SELECT * FROM `version_types` where `jar_package_url`= '" + fileName+"'");
					try {
						boolean find = false;
						while (rs.next()) {
							find = true;
							int id = rs.getInt("type_id");
							if(id > 1000) {
								index++;
								JSONObject obj = new JSONObject();
								obj.put("id", index);
								//jackage_url.substring(0, jackage_url.length()-4)+"_decompile"
								obj.put("lib", fileName);
								array.add(obj);
							}
						}
						if(!find) {
							index++;
							JSONObject obj = new JSONObject();
							obj.put("id", index);
							//jackage_url.substring(0, jackage_url.length()-4)+"_decompile"
							obj.put("lib", fileName);
							array.add(obj);
						}
						System.out.println(fileName.endsWith(".jar"));
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}				
			}								
		}
		System.out.println(array.size());
//		JsonFileUtil.save("meta.txt", array);
	}
	
	public static void readJarApi() {
		for(int typeId= 1981;typeId<=2000;typeId++) {
			System.out.println("+++++++++++++++++++++++ "+typeId);
			ResultSet rs = DBUtil.query("SELECT * FROM `version_types` where `type_id`= " + typeId);
			try {
				while (rs.next()) {
					String jarName  = rs.getString("jar_package_url");
					if(jarName.endsWith(".jar")) {
						String dirName = jarName.substring(0, jarName.length()-4)+"_decompile.txt";
						System.out.println(dirName);
						File file = new File(SAVE_PATH + dirName);
						if(file.exists()) {
							String whole = JsonFileUtil.readJsonFile(SAVE_PATH + file.getName());
							JSONArray array = JSONArray.parseArray(whole);											
							for (int a = 0; a < array.size(); a++) {
								JSONObject clazz = array.getJSONObject(a);
								String className = clazz.getString("name");
								
								String sql = "INSERT INTO api_classes(version_type_id,class_name) VALUES ("+ typeId + ",\'" + className + "\')";
								DBUtil.update(sql);
								System.out.println(sql);
								int classId=-1;
								ResultSet lrs = DBUtil.query("select LAST_INSERT_ID()");
								if (lrs.next())
									classId = lrs.getInt(1);
								JSONArray apis = clazz.getJSONArray("api");
								sql = "INSERT INTO api_interface(class_id,name,remark) VALUES ";
								for (int i = 0; i < apis.size(); i++) {
									JSONObject api = apis.getJSONObject(i);
									String name = api.getString("name");
									String remark = api.getString("remark");
									if(i != apis.size()-1) {
										sql += "("+ classId + ",\'" + name + "\', \'"+remark+"\'),";
									}
									else
										sql += "("+ classId + ",\'" + name + "\', \'"+remark+"\')";
//									sql = "INSERT INTO api_interface(class_id,name,remark) VALUES ("+ classId + ",\'" + name + "\', \'"+remark+"\')";
//									DBUtil.update(sql);
//									System.out.println(sql);									
								}
								DBUtil.update(sql);
							}
						}
						else 
							System.out.println("not exists");
					}						
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			System.out.println();
		}
	}
	
	public static void readJarApiByBatch() {
		int index = 0;
		PreparedStatement statement = null;
		try {
			statement = DBUtil.getConnection().prepareStatement("INSERT INTO api_interface(class_id,name,remark) VALUES(?,?,?)");
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// 163104 53027
//		for(int typeId = 55462;typeId <= 100000;typeId++) {
//		for(int typeId = 23466;typeId <= 50000;typeId++) {
		for(int typeId = 106404;typeId <= 150000;typeId++) {
			boolean parsed = false;
			ResultSet test = DBUtil.query("SELECT * FROM `api_classes` where `version_type_id`= " + typeId);
			try {
				while (test.next()) {
					parsed = true;
					break;
				}
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			if(parsed)
				continue;
			
			System.out.println("+++++++++++++++++++++++ "+typeId);
			ResultSet rs = DBUtil.query("SELECT * FROM `version_types` where `type_id`= " + typeId);
			try {
				while (rs.next()) {
					String jarName  = rs.getString("jar_package_url");
					if(jarName.endsWith(".jar")) {
						String dirName = jarName.substring(0, jarName.length()-4)+"_decompile.txt";
						System.out.println(dirName);
						File file = new File(SAVE_PATH + dirName);
						if(file.exists()) {
							String whole = JsonFileUtil.readJsonFile(SAVE_PATH + file.getName());
							JSONArray array = JSONArray.parseArray(whole);					
							for (int a = 0; a < array.size()-1; a++) {
								JSONObject clazz = array.getJSONObject(a);								
								String className = clazz.getString("name");								
								String sql = "INSERT INTO api_classes(version_type_id,class_name) VALUES ("+ typeId + ",\'" + className + "\')";
								DBUtil.update(sql);
								System.out.println(sql);
								int classId=-1;
								ResultSet lrs = DBUtil.query("select LAST_INSERT_ID()");
								if (lrs.next())
									classId = lrs.getInt(1);
								JSONArray apis = clazz.getJSONArray("api");
								for (int i = 0; i < apis.size(); i++) {
									JSONObject api = apis.getJSONObject(i);
									String name = api.getString("name");
									String remark = api.getString("remark");
									if(!remark.equals("Method"))
										continue;
									index++;
									statement.setInt(1,classId);
									statement.setString(2,name);
									statement.setString(3,remark);
									statement.addBatch();  //增加批处理
									if(index == 5000) {
										statement.executeBatch();  //执行批处理
										DBUtil.getConnection().commit();  //提交事务
										index = 0;
									}	
								}								
							}
						}
						else 
							System.out.println("not exists");
					}						
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			try {
				statement.executeBatch();
				DBUtil.getConnection().commit();  //提交事务
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  //执行批处理
			
			System.out.println();
		}
	}
	
	public static void storeReleaseTimeByBatch(String path) {
		int index = 0;
		try {
			PreparedStatement statement= DBUtil.getConnection().prepareStatement("INSERT INTO lib_release_time(lib_name,time) VALUES(?,?)");
			Scanner in = new Scanner(new File(path));
			while (in.hasNextLine()) {
				String str = in.nextLine();
				String[] libInfo = str.split("\\+\\+\\+");
				if(libInfo.length == 5) {
					String key = libInfo[1]+" "+libInfo[2]+" "+libInfo[3];
					ResultSet rs = DBUtil.query("SELECT * FROM `lib_release_time` where `lib_name`= '" + key+"'");
					boolean exist = false;
					while (rs.next()) {
						exist = true;
						break;
					}	
					if(!exist) {
						index++;
						statement.setString(1,key);
						statement.setString(2,libInfo[4]);
						statement.addBatch();  //增加批处理
						System.out.println(index);

					}
					if(index == 5000) {
						statement.executeBatch();  //执行批处理
						DBUtil.getConnection().commit();  //提交事务
						index = 0;						
					}	
				}
				else {
					System.out.println("--------------------------length error:"+str);
				}
							
			}
			statement.executeBatch();  //执行批处理
			DBUtil.getConnection().commit();  //提交事务
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
	}
	
	public static void testFastJson() {
//		try {
//			JSONReader jsonReader = new JSONReader(
//					new FileReader(new File(SAVE_PATH + "payara-embedded-all-4.1.2.173_decompile.txt")));
//			jsonReader.startArray();// ---> [
//
//			while (jsonReader.hasNext()) {
//				jsonReader.startObject();
//				while (jsonReader.hasNext()) {
//					String objKey = jsonReader.readString();
//					String objVal = jsonReader.readObject().toString();
//					System.out.println("key: " + objKey + ", value: " + objVal);
//				}
//				jsonReader.endObject();
//			}
//			jsonReader.endArray();// ---> ]
//			jsonReader.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		
		
		File file = new File(SAVE_PATH + "payara-embedded-all-4.1.2.173_decompile.txt");
			String whole = JsonFileUtil.readJsonFile(SAVE_PATH + file.getName());
			JSONArray array = JSONArray.parseArray(whole);
			for(int i = 0; i<array.size();i++) {
				System.out.println(array.get(i).toString());
			}
	}
	
	public static void main(String[] args) {
//		JarAnalyzer ja = new JarAnalyzer();		
//		ja.enterSave("E:/GP/test/decomplie/", "hazelcast-3.5.3_decompile", "E:/GP/test/output/");
//		extractApiInDecompilePath("F:/wangying/d.data.copy/new_update_jar_decomplie/","F:/wangying/d.data.copy/new_update_jar_api/");
		extractApiInDecompilePath("C:/third_party_libs_decomplie/","C:/third_party_libs_api/");
		//		File apiCallDir = new File("D:/data/api_call_output/");
//		String[] fileList = apiCallDir.list();
//		for(String fileName : fileList) {
////			System.out.println(fileName);
//			if(fileName.endsWith(".txt"))
//				fileName = fileName.substring(0,fileName.length()-4);
////			System.out.println(fileName);
//			String[] nameArray = fileName.split("_");
//			if(nameArray.length != 2) {
//				try {
//					throw new MyException("length != 2:" + fileName);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					System.exit(0);
//				}
//			}
//			int projectId = Integer.parseInt(nameArray[0]);
//			int fileId = Integer.parseInt(nameArray[1]);
//			if(projectId > 2 && projectId != 130) {
//				sqlToDb("D:/data/api_call_output/" + fileName + ".txt");
//			}
//		}
////		System.out.println(count);
//		System.out.println(fileList.length);
//		for(int i = 79;i<=1400;i++) {
//			String path = "F:\\GP\\apicall\\undone\\"+i+".txt";
//			File file = new File(path);
//			if(file.exists())
//				sqlToDb(path);
//		}
//		updateApiCall();
//		updateApiInterface();
//		readJarApiByBatch();
//		testFastJson();
//		String whole = JsonFileUtil.readJsonFile(SAVE_PATH + "jaxb-xalan-1.5_decompile.txt");
//		JSONArray array = JSONArray.fromObject(whole);					
//		JSONObject clazz = array.getJSONObject(array.size()-1);								
//		System.out.println(clazz.getInt("nullFileCount")+"/"+clazz.getInt("totalFileCount"));
		//output-10-11.txt output-11-11.5 output11.5-12 output-12-14 output14 output16 output18 output-20-22 output22+ output-8-10
//		storeReleaseTimeByBatch("F:\\GP\\output_RQ3\\new\\output69387+.txt");
//		meta("F:/GP/lib/");
		//"F:/GP/decompile/" "C:/Users/yw/Desktop/test/" "meta.txt" 1 1
//		if(args.length == 3) {
//			String libPath = args[0];
//			String outputPath = args[1];
//			String metaPath = args[2];
//			extractApi(libPath,outputPath,metaPath);
//		}
//		else
//			System.out.println("Illegal args");		
	}
}
