package cn.edu.fudan.se.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import cn.edu.fudan.se.util.DBUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;

import cn.edu.fudan.se.util.ExecuteCmd;
import cn.edu.fudan.se.util.FastJsonUtil;
import cn.edu.fudan.se.util.FileUtil;
import cn.edu.fudan.se.util.JsonFileUtil;
import cn.edu.fudan.se.util.MyException;

public class ApiCallUpdate extends ApiCallValidator {
	private Map<String, String> apiWithClasses = new HashMap();
	private List<String> apiCallList = new ArrayList<>();
	public static Map<Integer, String> projs = new HashMap();
	public static String FINAL_OUTPUT = "F:/wangying/api_call_update_output/";

	// public static String PROJECT_LIB_OF_COMMIT = "E:/data/proj_update_lib/";
	// public static String JAR_PATH = "F:/wangying/lib_all/";
	// public static String API_PATH = "E:/api_output/";
	// protected static String PROJECT_DIR = "F:/";
	// public static String API_CALL_OUTPUT = "D:/data/api_call_update_output/";
	// public static String PROJ_LIB_LIST = "C:/lib_list/";

	public static String JAR_PATH;
	protected static String PROJECT_DIR;
	public static String API_CALL_OUTPUT;
	public static String PROJ_LIB_LIST;

	public static String PROJECT_LIB_OF_COMMIT;
	public static String API_PATH;

	protected int error = 0;
	protected int total = 0;

	public List<String> getApiCall() {
		return this.apiCallList;
	}

	public void initApiCall() {
		this.apiCallList = new ArrayList<>();
	}

	public ApiCallUpdate(int id, String path) {
		super();
		this.projectId = id;
		this.proPath = path;
		this.contextList = new ArrayList<>();
		getContext(this.proPath);
		// for(String context:this.contextList)
		// System.out.println(context);
	}

	public ApiCallUpdate(int id, String path, String commit) {
		super();
		this.projectId = id;
		this.proPath = path;
		this.contextList = new ArrayList<>();
		getContext(this.proPath);
		// for(String context:this.contextList)
		// System.out.println(context);
		getLibsUsedByProj(commit);
		this.typeSolver = new CombinedTypeSolver(
				ProjectAnalyzer.createJarTypeSolverList(this.libLocalPaths, this.contextList));
		for(String jar : this.libLocalPaths) {
			System.out.println(jar);
		}

	}

	private void getLibsUsedByProj(String commit) {
		this.libLocalPaths = new ArrayList<>();
		// String dirPath = "C:/lib_list/";
		if (new File(PROJ_LIB_LIST + this.projectId + "_" + commit + ".txt").exists()) {
			String content = JsonFileUtil.readJsonFile(PROJ_LIB_LIST + this.projectId + "_" + commit + ".txt");
			JSONArray array = JSON.parseArray(content);
			for (int i = 0; i < array.size(); i++) {
				// System.out.println(array.getString(i));
				if(array.getString(i).endsWith(".jar") && new File(JAR_PATH + array.getString(i)).exists())
					this.libLocalPaths.add(JAR_PATH + array.getString(i));
			}
		}
//		else {
//			System.out.println("error:" + PROJ_LIB_LIST + this.projectId + "_" + commit + ".txt");
//			System.exit(0);
//		}
	}

	public void handleProjById() {
		Map<String, List<Integer>> parsingLibOfCommit = new HashMap<>();
		String projectPath = null;

		try {
			ResultSet trs = DBUtil.query("SELECT distinct(prev_commit) FROM `lib_update` where `project_id`= " + projectId
					+ " union SELECT distinct(curr_commit) FROM `lib_update` where `project_id`= " + projectId);
			while (trs.next()) {
				String commitId = trs.getString("prev_commit");
				ResultSet libTrs = DBUtil.query("SELECT * FROM `lib_update` where `prev_commit`= '" + commitId
						+ "' or `curr_commit`='" + commitId + "' and `project_id` = " + projectId);
				List<Integer> libs = new ArrayList<>();
				while (libTrs.next()) {
					int versionTypeId = libTrs.getInt("lib_id");
					if (!libs.contains(versionTypeId)) {
						libs.add(versionTypeId);
					}
					break;
				}
				if (libs.size() > 0)
					parsingLibOfCommit.put(commitId, libs);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(parsingLibOfCommit.size());

		if (projectPath != null) {
			for (Map.Entry<String, List<Integer>> entry : parsingLibOfCommit.entrySet()) {
				String commitId = entry.getKey();
				List<Integer> libs = entry.getValue();
				boolean success = ExecuteCmd.checkout(projectPath, commitId);
				if (success) {
					for (int versionTypeId : libs) {
						getClassesOfOneLib(versionTypeId);
						validateFile(this.proPath, versionTypeId);
						// this.apiCall = this.apiCall;
					}
				}
			}
		}
	}

	public void getClassesOfOneLib(int versionTypeId) {
		this.libLocalPaths = new ArrayList<>();
		this.allClasses = new HashMap<String, Integer>();

		ResultSet trs = DBUtil.query("SELECT * FROM `version_types` where `type_id`=" + versionTypeId);
		try {
			while (trs.next()) {
				String packageUrl = trs.getString("jar_package_url");
				if (packageUrl.endsWith(".jar")) {
					this.libLocalPaths.add(this.JAR_PATH + packageUrl);
					break;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ResultSet crs = DBUtil.query("SELECT * FROM `api_classes` where `version_type_id`=" + versionTypeId);
		try {
			while (crs.next()) {
				String className = crs.getString("class_name");
				int classId = crs.getInt("id");
				this.allClasses.put(className, classId);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.typeSolver = new CombinedTypeSolver(
				ProjectAnalyzer.createJarTypeSolverList(this.libLocalPaths, this.contextList));
	}

	public void initLibs(String jarName) {
		this.error = 0;
		this.total = 0;
		apiCallList = new ArrayList<>();
		this.libLocalPaths = new ArrayList<>();
		this.libLocalPaths.add(this.JAR_PATH + jarName);
		this.typeSolver = new CombinedTypeSolver(
				ProjectAnalyzer.createJarTypeSolverList(this.libLocalPaths, this.contextList));
	}

	public Map<String, Integer> getAllApisOfLibUsedByOneFile() {
		Map<String, Integer> apis = new HashMap<>();
		for (Map.Entry<String, Integer> clazz : this.allClasses.entrySet()) {
			String className = clazz.getKey();
			if (isImported(className)) {
				int classId = clazz.getValue();
				ResultSet irs = DBUtil
						.query("SELECT * FROM `api_interface` where `remark` = 'Method' and `class_id`=" + classId);
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

	public Map<String, String> getAllApisOfLibUsedByOneFile(String jarName) {
		Map<String, String> result = new HashMap<>();
		String filePath = API_PATH + jarName.replace(".jar", "_decompile.txt");
		if (new File(filePath).exists()) {
			String whole = JsonFileUtil.readJsonFile(filePath);
			JSONArray array = JSONArray.parseArray(whole);
			for (int a = 0; a < array.size() - 1; a++) {
				JSONObject clazz = array.getJSONObject(a);
				String className = clazz.getString("name");
				if (isImported(className)) {
					JSONArray apis = clazz.getJSONArray("api");
					for (int i = 0; i < apis.size(); i++) {
						JSONObject api = apis.getJSONObject(i);
						String name = api.getString("name");
						String remark = api.getString("remark");
						if (!remark.equals("Method"))
							continue;
						result.put(name, className);
					}
				}
			}
		}
		return result;
	}

	public void validateFile(String dir, int versionTypeId) {
		// 获取第三方库class列表
		File or = new File(dir);
		File[] files = or.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile() && file.getName().endsWith(".java")) {
					String absolutePath = file.getAbsolutePath();
					String filePath = absolutePath.replace("\\", "/");
					if (filePath.startsWith(this.proPath))
						filePath = filePath.substring(this.proPath.length());

					// System.out.println("----------------------"+absolutePath);
					getImportListOfFile(absolutePath);
					Map<String, Integer> apis = getAllApisOfLibUsedByOneFile();
					// System.out.println(apis.size());
					if (apis.size() > 0) {
						System.out.println("----------------------" + absolutePath);
						ProjectAnalyzer pa = new ProjectAnalyzer();
						List<String> call = pa.analyseOneFile(absolutePath, this.typeSolver);
						findApiCall(apis, call);
						// apiCallToFile(this.projectId+".txt",filePath);
						// apiCallPersistence(filePath);
					}
				} else if (file.isDirectory()) {
					validateFile(file.getAbsolutePath(), versionTypeId);
				}
			}
		}
	}

	public void validateFile(String dir, String jarName) {
		// 获取第三方库class列表
		File or = new File(dir);
		File[] files = or.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile() && file.getName().endsWith(".java")) {
					String absolutePath = file.getAbsolutePath();
					String filePath = absolutePath.replace("\\", "/");
					if (filePath.startsWith(this.proPath))
						filePath = filePath.substring(this.proPath.length());

					// System.out.println("----------------------"+absolutePath);
					getImportListOfFile(absolutePath);
					Map<String, String> apis = getAllApisOfLibUsedByOneFile(jarName);
					// System.out.println(apis.size());
					if (apis.size() > 0) {
						System.out.println("----------------------" + absolutePath);
						ProjectAnalyzer pa = new ProjectAnalyzer();
						List<String> call = pa.analyseOneFile(absolutePath, this.typeSolver);
						this.error += pa.getErrorCount();
						this.total += pa.getTotalCount();
						System.out.println("++++++++++++++++++++++ " + this.error + " / " + this.total);
						findApiCall2(apis, call);
					}
				} else if (file.isDirectory()) {
					validateFile(file.getAbsolutePath(), jarName);
				}
			}
		}
	}

	private void findApiCall(Map<String, Integer> apis, List<String> call) {
		// apiCallCount = new HashMap<>();
		for (int i = 0; i < call.size(); i++) {
			if (apis.containsKey(call.get(i))) {
				String apiCall = call.get(i);
				int apiId = apis.get(call.get(i));
				// System.out.println("apiId : "+apiId);
				String className = "";
				try {
					ResultSet irs = DBUtil.query("SELECT * FROM `api_interface` where `id`=" + apiId);
					int apiClassId = -1;
					while (irs.next()) {
						apiClassId = irs.getInt("class_id");
						break;
					}
					ResultSet crs = DBUtil.query("SELECT * FROM `api_classes` where `id`=" + apiClassId);

					while (crs.next()) {
						className = crs.getString("class_name");
						break;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!className.equals(""))
					apiCall = className + "." + apiCall;
				if (!this.apiCallList.contains(apiCall)) {
					this.apiCallList.add(apiCall);
				}
				System.out.println(apiCall);
				System.out.println(this.apiCallList.size());
			}
		}
	}

	private void findApiCall2(Map<String, String> apis, List<String> call) {
		// apiCallList = new ArrayList<>();
		for (int i = 0; i < call.size(); i++) {
			if (apis.containsKey(call.get(i))) {
				String apiCall = call.get(i);
				String className = apis.get(call.get(i));
				if (!className.equals(""))
					apiCall = className + "." + apiCall;
				if (!this.apiCallList.contains(apiCall)) {
					this.apiCallList.add(apiCall);
				}
				System.out.println(apiCall);
				System.out.println(this.apiCallList.size());
			}
		}
	}

	public static void handleEntry(int entryId, int projectId, String prevCommit, String currCommit, int prevTypeId,
			int currTypeId) {
		JSONArray prevJson = null, currJson = null;
		if (projs.containsKey(projectId)) {
			String projectPath = projs.get(projectId);
			ApiCallUpdate apiCallUpdate = new ApiCallUpdate(4, projectPath + "/");
			// apiCallUpdate.updateData();
			boolean success = ExecuteCmd.checkout(projectPath, prevCommit);
			if (success) {
				apiCallUpdate.getClassesOfOneLib(prevTypeId);
				apiCallUpdate.validateFile(projectPath + "/", prevTypeId);
				List<String> prevCall = apiCallUpdate.getApiCall();
				System.out.println("====================" + prevCall.size());
				prevJson = (JSONArray) JSONArray.toJSON(prevCall);
			}

			success = ExecuteCmd.checkout(projectPath, currCommit);
			if (success) {
				apiCallUpdate.initApiCall();
				apiCallUpdate.getClassesOfOneLib(currTypeId);
				apiCallUpdate.validateFile(projectPath + "/", currTypeId);
				List<String> currCall = apiCallUpdate.getApiCall();
				System.out.println("====================" + currCall.size());
				currJson = (JSONArray) JSONArray.toJSON(currCall);
			}

			String jar1 = null, jar2 = null;
			try {
				String sql = "SELECT * FROM version_types where type_id = " + prevTypeId;
				ResultSet rs1 = DBUtil.query(sql);
				while (rs1.next()) {
					jar1 = rs1.getString("jar_package_url");
					break;
				}
				sql = "SELECT * FROM version_types where type_id = " + currTypeId;
				ResultSet rs2 = DBUtil.query(sql);
				while (rs2.next()) {
					jar2 = rs2.getString("jar_package_url");
					break;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			JSONObject obj = new JSONObject();
			obj.put("prev_jar", jar1);
			obj.put("curr_jar", jar2);
			obj.put("prev_api_call_list", prevJson);
			obj.put("curr_api_call_list", currJson);
			FastJsonUtil.writeJson(API_CALL_OUTPUT + entryId + ".txt", obj, false);
		} else {
			try {
				throw new MyException("project id : " + projectId);
			} catch (MyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(0);
			}
		}

	}

	public static void getProjs() {
		String sql = "SELECT distinct project_id from lib_update where prev_type_id is not null and curr_type_id is not null";
		ResultSet rs = DBUtil.query(sql);
		try {
			while (rs.next()) {
				int projectId = rs.getInt("project_id");
				System.out.println(" project id: " + projectId);
				FileUtil.appendLine("proj_in_lib_update.txt", "" + projectId);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void getCommitOfProjs() {
		ArrayList<Integer> projIds = new ArrayList<>();
		Scanner in;
		try {
			in = new Scanner(new File("proj_in_lib_update.txt"));
			while (in.hasNextLine()) {
				String str = in.nextLine();
				int projectId = Integer.parseInt(str);
				// System.out.println(projectId);
				projIds.add(projectId);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(projIds.size());

		for (int i = 0; i < projIds.size(); i++) {
			int count = 0;
			int projectId = projIds.get(i);
			if (projectId <= 1469)
				continue;
			System.out.println("projectId :" + projectId);
			Map<String, List> result = new HashMap<>();
			ResultSet trs = DBUtil.query("SELECT distinct(prev_commit) FROM `lib_update` where `project_id`= " + projectId
					+ " and `prev_type_id` is not null and `curr_type_id` is not null union SELECT distinct(curr_commit) FROM `lib_update` where `project_id`= "
					+ projectId + " and `prev_type_id` is not null and `curr_type_id` is not null");
			try {
				while (trs.next()) {
					String commitId = trs.getString("prev_commit");
					System.out.println(commitId);
					List<Integer> libs = new ArrayList<>();

					ResultSet libTrs1 = DBUtil
							.query("SELECT * FROM lib_update where prev_commit= '" + commitId + "' and project_id = "
									+ projectId + " and prev_type_id is not null and curr_type_id is not null");
					while (libTrs1.next()) {
						int versionTypeId = libTrs1.getInt("prev_type_id");
						if (!libs.contains(versionTypeId)) {
							libs.add(versionTypeId);
						}
					}
					ResultSet libTrs2 = DBUtil
							.query("SELECT * FROM lib_update where curr_commit='" + commitId + "' and project_id = "
									+ projectId + " and prev_type_id is not null and curr_type_id is not null");
					while (libTrs2.next()) {
						int versionTypeId = libTrs2.getInt("curr_type_id");
						if (!libs.contains(versionTypeId)) {
							libs.add(versionTypeId);
						}
					}

					if (libs.size() > 0) {
						result.put(commitId, libs);
						count += libs.size();
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			JSONObject json = (JSONObject) JSONObject.toJSON(result);
			FastJsonUtil.writeJson(PROJECT_LIB_OF_COMMIT + projectId + ".txt", json, false);
			System.out.println(projectId + " : " + count);
			// break;
		}
	}

	public static void handleOneProj(int projectId) {
		String projectPath = projs.get(projectId);
		String whole = JsonFileUtil.readJsonFile(PROJECT_LIB_OF_COMMIT + projectId + ".txt");
		Map<String, Object> libs = JSONObject.parseObject(whole);
		for (Map.Entry<String, Object> entry : libs.entrySet()) {

			String commitId = entry.getKey();
			// if(commitId.equals("78c949f282f0eedff079a41f222834ac42457548")) {
			System.out.println("=============== " + projectId + " : " + commitId);
			JSONArray ja = (JSONArray) entry.getValue();
			List<Integer> typeIds = JSONObject.parseArray(ja.toJSONString(), Integer.class);

			boolean success = ExecuteCmd.checkout(projectPath, commitId);
			ApiCallUpdate apiCallUpdate = new ApiCallUpdate(projectId, projectPath + "/");

			if (success) {
				// for(int typeId : typeIds) {
				for (int i = 0; i < typeIds.size(); i++) {
					int typeId = typeIds.get(i);
					// if(typeId == 13827) {
					if (new File(API_CALL_OUTPUT + projectId + "_" + commitId + "_" + typeId + ".txt").exists())
						continue;
					System.out.println("=============== " + projectId + " : " + commitId + " : " + typeId);
					String jarName = null;
					try {
						String sql = "SELECT * FROM version_types where type_id = " + typeId;
						ResultSet rs1 = DBUtil.query(sql);
						while (rs1.next()) {
							jarName = rs1.getString("jar_package_url");
							break;
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String decompilePath = API_PATH + jarName.replace(".jar", "_decompile.txt");
					if (new File(decompilePath).exists()) {
						apiCallUpdate.initLibs(jarName);
						apiCallUpdate.validateFile(projectPath + "/", jarName);
						List<String> call = apiCallUpdate.getApiCall();
						JSONArray callJson = (JSONArray) JSONArray.toJSON(call);
						JSONObject obj = new JSONObject();
						obj.put("jar_name", jarName);
						obj.put("api_call_list", callJson);
						obj.put("error", apiCallUpdate.error);
						obj.put("total", apiCallUpdate.total);
						FastJsonUtil.writeJson(API_CALL_OUTPUT + projectId + "_" + commitId + "_" + typeId + ".txt",
								obj, false);
					} else {
						FileUtil.appendLine("unfind-lib.txt", decompilePath);
					}
					// }

				}
				// break;
			}
			// }
		}
	}

	public static void handleOneProj(int projectId, String commit) {
		String projectPath = projs.get(projectId);
		String whole = JsonFileUtil.readJsonFile(PROJECT_LIB_OF_COMMIT + projectId + ".txt");
		Map<String, Object> libs = JSONObject.parseObject(whole);
		for (Map.Entry<String, Object> entry : libs.entrySet()) {

			String commitId = entry.getKey();
			if (commitId.equals(commit)) {
				System.out.println("=============== " + projectId + " : " + commitId);
				JSONArray ja = (JSONArray) entry.getValue();
				List<Integer> typeIds = JSONObject.parseArray(ja.toJSONString(), Integer.class);

				boolean success = ExecuteCmd.checkout(projectPath, commitId);
				ApiCallUpdate apiCallUpdate = new ApiCallUpdate(projectId, projectPath + "/");

				if (success) {
					// for(int typeId : typeIds) {
					for (int i = 0; i < typeIds.size(); i++) {
						int typeId = typeIds.get(i);
						// if(typeId == 13827) {
						if (new File(API_CALL_OUTPUT + projectId + "_" + commitId + "_" + typeId + ".txt").exists())
							continue;
						System.out.println("=============== " + projectId + " : " + commitId + " : " + typeId);
						String jarName = null;
						try {
							String sql = "SELECT * FROM version_types where type_id = " + typeId;
							ResultSet rs1 = DBUtil.query(sql);
							while (rs1.next()) {
								jarName = rs1.getString("jar_package_url");
								break;
							}
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						String decompilePath = API_PATH + jarName.replace(".jar", "_decompile.txt");
						if (new File(decompilePath).exists()) {
							apiCallUpdate.initLibs(jarName);
							apiCallUpdate.validateFile(projectPath + "/", jarName);
							List<String> call = apiCallUpdate.getApiCall();
							JSONArray callJson = (JSONArray) JSONArray.toJSON(call);
							JSONObject obj = new JSONObject();
							obj.put("jar_name", jarName);
							obj.put("api_call_list", callJson);
							obj.put("error", apiCallUpdate.error);
							obj.put("total", apiCallUpdate.total);
							FastJsonUtil.writeJson(API_CALL_OUTPUT + projectId + "_" + commitId + "_" + typeId + ".txt",
									obj, false);
						} else {
							FileUtil.appendLine("unfind-lib.txt", decompilePath);
						}
						// }

					}
					// break;
				}
			}
		}
	}

	public static void extractCallsWithoutDb(int projectId, String commit, int num1, int num2) {
		String projectPath = projs.get(projectId);
		if (!new File(projectPath).exists()) {
			try {
				throw new MyException("project path not exists : " + projectPath);
			} catch (MyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(0);
			}
		}

		System.out.println("=============== " + projectId + " : " + commit);

		boolean success = ExecuteCmd.checkout(projectPath, commit);
		// ApiCallUpdate apiCallUpdate = new ApiCallUpdate(projectId,
		// projectPath + "/");

		if (success) {

			if (new File(projectPath).exists()) {
				System.out.println(projectId + " " + commit + " " + projectPath);
				// ApiCallValidator apiCallValidator = new
				// ApiCallValidator(projectId, localAddr +"/");
				ApiCallUpdate apiCallUpdate = new ApiCallUpdate(projectId, projectPath + "/", commit);
				List<File> fileList = new ArrayList<File>();
				apiCallUpdate.browse(fileList, projectPath + "/");

				FileUtil.writeFlie("file_id.txt", "");
				for (int f = num1; f < num2; f++) {	
					FileUtil.appendLine("file_id.txt", f+"");
					if (!new File(API_CALL_OUTPUT + projectId + "_" + commit + "_" + f + ".txt").exists()) {
						File tmp = fileList.get(f);
						System.out.println("File " + f + " : " + tmp.getAbsolutePath());
						// if(tmp.getAbsolutePath().equals("C:\\gradle500\\aol__fdse__cyclops-react\\cyclops\\src\\jmh\\java\\cyclops\\reactiveSeq\\Combinations.java"))
						// {
						apiCallUpdate.appendCall(f, tmp, API_CALL_OUTPUT + projectId + "_" + commit + ".txt");
						// }
					}					
				}
//				FileUtil.appendFile(CALL_FILE_PATH + projectId + "_" + commit + ".txt",
//						num1 + "_" + num2 + ":" + AstParserUtil.errorFile + " " + AstParserUtil.totalFile);
			}
		}
	}

	public static void handleOneProjWithoutDb(int projectId, String commit) {
		String projectPath = projs.get(projectId);
		if (!new File(projectPath).exists()) {
			try {
				throw new MyException("project path not exists : " + projectPath);
			} catch (MyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(0);
			}
		}
		String whole = JsonFileUtil.readJsonFile(PROJECT_LIB_OF_COMMIT + projectId + ".txt");
		Map<String, Object> libs = JSONObject.parseObject(whole);
		for (Map.Entry<String, Object> entry : libs.entrySet()) {

			String commitId = entry.getKey();
			if (commitId.equals(commit)) {
				System.out.println("=============== " + projectId + " : " + commitId);
				JSONObject ja = (JSONObject) entry.getValue();
				Map<String, Object> jarMap = JSONObject.parseObject(ja.toJSONString());

				boolean success = ExecuteCmd.checkout(projectPath, commitId);
				ApiCallUpdate apiCallUpdate = new ApiCallUpdate(projectId, projectPath + "/");

				if (success) {
					for (Map.Entry<String, Object> jar : jarMap.entrySet()) {
						String typeId = jar.getKey();
						if (new File(API_CALL_OUTPUT + projectId + "_" + commitId + "_" + typeId + ".txt").exists())
							continue;
						System.out.println("=============== " + projectId + " : " + commitId + " : " + typeId);
						String jarName = (String) jar.getValue();
						String decompilePath = API_PATH + jarName.replace(".jar", "_decompile.txt");
						System.out.println(decompilePath);
						if (new File(decompilePath).exists()) {
							apiCallUpdate.initLibs(jarName);
							apiCallUpdate.validateFile(projectPath + "/", jarName);
							List<String> call = apiCallUpdate.getApiCall();
							JSONArray callJson = (JSONArray) JSONArray.toJSON(call);
							JSONObject obj = new JSONObject();
							obj.put("jar_name", jarName);
							obj.put("api_call_list", callJson);
							obj.put("error", apiCallUpdate.error);
							obj.put("total", apiCallUpdate.total);
							FastJsonUtil.writeJson(API_CALL_OUTPUT + projectId + "_" + commitId + "_" + typeId + ".txt",
									obj, false);
						} else {
							FileUtil.appendLine("unfind-lib.txt", decompilePath);
						}
					}
				}
			}
		}
	}

	public static void extractUpdateApiCalls() {
		String sql = "SELECT id,project_id,prev_commit,curr_commit,prev_type_id,curr_type_id from lib_update where prev_type_id is not null and curr_type_id is not null";
		ResultSet rs = DBUtil.query(sql);
		try {
			while (rs.next()) {
				int entryId = rs.getInt("id");
				int projectId = rs.getInt("project_id");
				if (projectId >= 21 && projectId <= 50) {
					System.out.println(projectId);
					String prevCommit = rs.getString("prev_commit");
					String currCommit = rs.getString("curr_commit");
					int prevTypeId = rs.getInt("prev_type_id");
					int currTypeId = rs.getInt("curr_type_id");
					String prevPath = "F:/wangying/api_call_update_output/" + projectId + "_" + prevCommit + "_"
							+ prevTypeId + ".txt";
					String currPath = "F:/wangying/api_call_update_output/" + projectId + "_" + currCommit + "_"
							+ currTypeId + ".txt";
					if (new File(prevPath).exists() && new File(currPath).exists()) {

						String prevContent = JsonFileUtil.readJsonFile(prevPath);
						JSONObject prevJson = JSONObject.parseObject(prevContent);
						String prevJar = prevJson.getString("jar_name");
						JSONArray prevList = prevJson.getJSONArray("api_call_list");

						String currContent = JsonFileUtil.readJsonFile(currPath);
						JSONObject currJson = JSONObject.parseObject(currContent);
						String currJar = currJson.getString("jar_name");
						JSONArray currList = currJson.getJSONArray("api_call_list");

						JSONObject obj = new JSONObject();
						obj.put("prev_jar", prevJar);
						obj.put("curr_jar", currJar);
						obj.put("prev_api_call_list", prevList);
						obj.put("curr_api_call_list", currList);
						FastJsonUtil.writeJson("F:/wangying/final_output/" + entryId + ".txt", obj, false);
					}
				}

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// public static void group(int projectId) {
	// String whole = JsonFileUtil.readJsonFile(PROJECT_LIB_OF_COMMIT +
	// projectId + ".txt");
	// Map<String, Object> libs = JSONObject.parseObject(whole);
	// for(Map.Entry<String, Object> entry:libs.entrySet()) {
	//
	// String commitId = entry.getKey();
	// String str = "java -jar apicallupdate.jar "+projectId+" " + commitId + "
	// $a $b $c $d $e";
	//// FileUtil.appendFile("C:\\Users\\yw\\Desktop\\api_call_update\\batch"+projectId+".sh",
	// str);
	// FileUtil.appendFile("C:\\Users\\yw\\Desktop\\api_call_update\\new\\batch"+projectId+".sh",
	// str);
	// }
	// }

	public static void group(int projectId, String outputDir) {
		// if(new File("H:\\api_call_update\\batch\\"+projectId+".sh").exists())
		if (new File(outputDir + projectId + ".sh").exists())
			return;
		String path = PROJECT_LIB_OF_COMMIT + projectId + ".txt";
		if (new File(path).exists()) {
			String projectPath = projs.get(projectId);
			System.out.println(projectPath);
			System.out.println(projectPath + " " +new File(projectPath).exists());
			if (new File(projectPath).exists()) {
				System.out.println(projectId + " " + projectPath);
				String whole = JsonFileUtil.readJsonFile(path);
				Map<String, Object> libs = JSONObject.parseObject(whole);
				System.out.println(libs.size());
				for (Map.Entry<String, Object> entry : libs.entrySet()) {
					String commitId = entry.getKey();
					boolean success = ExecuteCmd.checkout(projectPath, commitId);
					if (success) {
						List<File> fileList = new ArrayList<File>();
						getFileList(fileList, projectPath + "/");
						System.out.println(fileList.size());

						for (int f = 0; f < fileList.size(); f += 100) {
							int start = f;
							int end = f + 100;
							if (end > fileList.size())
								end = fileList.size();
							String str = "java -jar apicallupdate.jar " + projectId + " " + commitId + " " + start + " "
									+ end;
							// FileUtil.appendFile("H:\\api_call_update\\batch\\"+projectId+".sh",
							// str);
							FileUtil.appendLine(outputDir + projectId + ".sh", str);
						}
					}
					// String str = "java -jar apicallupdate.jar "+projectId+" "
					// + commitId + " $a $b $c $d $e";
					// FileUtil.appendFile("C:\\Users\\yw\\Desktop\\api_call_update\\new\\batch"+projectId+".sh",
					// str);
				}
			}
		}

	}

	public static void getJarNameUsedByProj() {
		String content = JsonFileUtil.readJsonFile("proj_in_usage.txt");
		JSONArray array = JSONArray.parseArray(content);
		for (int i = 0; i < array.size(); i++) {
			JSONObject obj = array.getJSONObject(i);
			int projectId = obj.getInteger("id");
			String localAddr = obj.getString("local_addr");
			// if(localAddr.startsWith("F:/wangying/projects_last_unzips/"))
			// localAddr =
			// "C:/"+localAddr.replace("F:/wangying/projects_last_unzips/","");

			localAddr = localAddr.replace("F:/wangying/projects_last_unzips/", "").replace("C:/", "").replace("D:/", "")
					.replace("E:/", "").replace("F:/", "").replace("gradle_maven200_500/", "")
					.replace("gradle_maven500/", "").replace("maven200_500/", "").replace("maven500/", "")
					.replace("gradle200_500/", "").replace("gradle500/", "");

			// System.out.println(localAddr);

			projs.put(projectId, localAddr);
		}

		Map<String, List> result = new HashMap();
		String[] files = new File("E:/data/proj_update_lib").list();
		for (String fileName : files) {
			int projectId = Integer.parseInt(fileName.replace(".txt", ""));
			String projectPath = projs.get(projectId);
			System.out.println(projectId + " : " + projectPath);
			List<String> jars = new ArrayList();
			String whole = JsonFileUtil.readJsonFile("E:/data/proj_update_lib/" + fileName);
			Map<String, Object> libs = JSONObject.parseObject(whole);
			for (Map.Entry<String, Object> entry : libs.entrySet()) {
				JSONArray ja = (JSONArray) entry.getValue();
				List<Integer> typeIds = JSONObject.parseArray(ja.toJSONString(), Integer.class);
				for (int i = 0; i < typeIds.size(); i++) {
					int typeId = typeIds.get(i);
					String jarName = null;
					try {
						String sql = "SELECT * FROM version_types where type_id = " + typeId;
						ResultSet rs1 = DBUtil.query(sql);
						while (rs1.next()) {
							jarName = rs1.getString("jar_package_url");
							break;
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (!jars.contains(jarName)) {
						jars.add(jarName);
					}
				}
			}

			result.put(projectPath, jars);
		}
		JSONObject obj = (JSONObject) JSONObject.toJSON(result);
		FastJsonUtil.writeJson("jars.txt", obj, false);
	}

	public static void parseTypeIdToName() {
		String[] files = new File("E:/data/proj_update_lib").list();
		for (String fileName : files) {
			if (!new File("E:/data/lib/" + fileName).exists()) {
				Map<String, Map> result = new HashMap();
				String whole = JsonFileUtil.readJsonFile("E:/data/proj_update_lib/" + fileName);
				Map<String, Object> libs = JSONObject.parseObject(whole);
				for (Map.Entry<String, Object> entry : libs.entrySet()) {
					String commit = entry.getKey();
					JSONArray ja = (JSONArray) entry.getValue();
					List<Integer> typeIds = JSONObject.parseArray(ja.toJSONString(), Integer.class);
					Map<Integer, String> typeName = new HashMap();
					for (int i = 0; i < typeIds.size(); i++) {
						int typeId = typeIds.get(i);
						String jarName = null;
						try {
							String sql = "SELECT * FROM version_types where type_id = " + typeId;
							ResultSet rs1 = DBUtil.query(sql);
							while (rs1.next()) {
								jarName = rs1.getString("jar_package_url");
								break;
							}
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						typeName.put(typeId, jarName);
					}
					result.put(commit, typeName);
				}
				JSONObject obj = (JSONObject) JSONObject.toJSON(result);
				FastJsonUtil.writeJson("E:/data/lib/" + fileName, obj, false);
			}
		}
	}

	public static void main(String[] args) {
		// getJarNameUsedByProj();
		// parseTypeIdToName();

		// getCommitOfProjs();

		// extractUpdateApiCalls();

//		 JAR_PATH = args[4].replace("\r", "").replace("\n", "");
//		 PROJECT_DIR = args[5].replace("\r", "").replace("\n", "");
//		 API_CALL_OUTPUT = args[6].replace("\r", "").replace("\n", "");
//		 CALL_FILE_PATH = args[7].replace("\r", "").replace("\n", "");
//		 PROJ_LIB_LIST = args[8].replace("\r", "").replace("\n", "");
//		// PROJECT_LIB_OF_COMMIT = args[4].replace("\r", "").replace("\n","");
//		// API_PATH = args[5].replace("\r", "").replace("\n", "");

		// PROJECT_DIR = "F:/";
		// PROJECT_LIB_OF_COMMIT = "H:/proj_update_lib/";

		PROJECT_DIR = args[0].replace("\r", "").replace("\n", "");
		PROJECT_LIB_OF_COMMIT = args[1].replace("\r", "").replace("\n", "");
		String outputDir = args[2].replace("\r", "").replace("\n", "");

		String content = JsonFileUtil.readJsonFile("all_proj.json");
		JSONArray array = JSONArray.parseArray(content);
		for (int i = 0; i < array.size(); i++) {
			JSONObject obj = array.getJSONObject(i);
			int projectId = obj.getInteger("id");
			String localAddr = obj.getString("local_addr");
//			 if(localAddr.startsWith("F:/wangying/projects_last_unzips/"))
//			 localAddr = localAddr.replace("F:/wangying/projects_last_unzips/","");

			// localAddr = PROJECT_DIR +
			// localAddr.replace("F:/wangying/projects_last_unzips/",
			// "").replace("C:/", "").replace("D:/", "")
			// .replace("E:/", "").replace("F:/",
			// "").replace("gradle_maven200_500/",
			// "").replace("gradle_maven500/", "").replace("maven200_500/",
			// "").replace("maven500/", "").replace("gradle200_500/",
			// "").replace("gradle500/", "");

			localAddr = PROJECT_DIR + localAddr;

			// localAddr = PROJECT_DIR +
			// localAddr.replace("F:/wangying/projects_last_unzips/","").replace("C:/","").replace("D:/","").replace("E:/","").replace("F:/","");

			projs.put(projectId, localAddr);
		}

		// handleOneProj(34);
		// int[] projs = {1107, 1109, 1213, 130, 138, 180, 193, 197, 2, 20, 205,
		// 223, 258, 262, 270, 271, 279, 30, 34, 346, 347, 351, 359, 38, 388, 4,
		// 446, 447, 556, 591, 6, 654, 660, 68, 692, 709, 797, 8, 84, 966};
		 int[] projs = {3555, 3586, 3588, 3597, 3606, 3609, 3615, 3625, 3631, 3640, 3648, 3673, 3683, 3717, 3729, 3741, 3743, 3747, 3757, 3778, 3798, 3828, 3834, 3847, 3852, 3879, 3887, 3893, 3928, 3964, 3970, 3974, 4002, 4012, 4034, 4038, 4043, 4069, 4087, 4114, 4124, 4132, 4161, 4178, 4237, 4241, 4257, 4284, 4288, 4315, 4321, 4343, 4461, 4475, 4487, 4491, 4535, 4564, 4574, 4605, 4662, 4672, 4674, 4676, 4725, 4744, 4760, 4774, 4803, 4848, 4941, 4951, 5005, 5042, 5075, 5089, 5128, 5145, 5156, 5348, 5368, 5412, 5414, 5422, 5432, 5463, 5480, 5486, 5502, 5533};
		 for(int i=0;i<projs.length;i++) {
			 group(projs[i], outputDir);
		 }
//		int i = Integer.valueOf(args[3]);
//		// for(int i=0;i<5540;i++) {
//		group(i, outputDir);
		// }
		// group(2);

//		 int projectId = Integer.parseInt(args[0]);
//		 String commitId = args[1];
//		 int num1 = Integer.parseInt(args[2]);
//		 int num2 = Integer.parseInt(args[3]);
//		
//		 extractCallsWithoutDb(projectId,commitId,num1,num2);

		// System.out.println(PROJECT_LIB_OF_COMMIT);
		// System.out.println(JAR_PATH);
		// System.out.println(API_PATH);
		// System.out.println(PROJECT_DIR);
		// System.out.println(API_CALL_OUTPUT);
		// handleOneProjWithoutDb(projectId,commitId);
		// handleOneProj(projectId,commitId);

		// handleOneProjWithoutDb(2,"297c7fd64d856498e3ee0e357a4454e6c6f2c990");

		// for(int i = 132;i<=5600;i++) {
		// if(new File(PROJECT_LIB_OF_COMMIT + i + ".txt").exists())
		// group(i);
		// }

		// for(int i = 0; i< 5000;i++) {
		// if(projs.containsKey(i) ) {
		// if(i>4)
		// handleOneProj(i);
		// }
		// }

		//
		// for(int i=0;i<projs.size();i++) {
		// int projectId = projIds.get(i);
		// String projectPath = projs.get(projectId);
		// ApiCallUpdate apiCallUpdate = new ApiCallUpdate(projectId,
		// projectPath + "/");
		// apiCallUpdate.handleProjById();
		// }

		// System.out.println(projs.size());
		// String sql = "SELECT distinct project_id where prev_type_id is not
		// null and curr_type_id is not null";
		// String sql = "SELECT
		// id,project_id,prev_commit,curr_commit,prev_type_id,curr_type_id from
		// lib_update where id = 835";
		// ResultSet rs = DBUtil.query(sql);
		// try {
		// while(rs.next()) {
		// int entryId = rs.getInt("id");
		// int projectId = rs.getInt("project_id");
		// String prevCommit = rs.getString("prev_commit");
		// String currCommit = rs.getString("curr_commit");
		// int prevTypeId = rs.getInt("prev_type_id");
		// int currTypeId = rs.getInt("curr_type_id");
		// System.out.println("++++++++++++ entry id: "+entryId + " project id:
		// "+ projectId);
		// handleEntry(entryId,projectId,
		// prevCommit,currCommit,prevTypeId,currTypeId);
		// }
		// } catch (SQLException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}
}
