package cn.edu.fudan.se.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//import java.util.Optional;

import cn.edu.fudan.se.util.AstParserUtil;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparser.Navigator;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.UnsolvedSymbolException;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

public class ProjectAnalyzer {
	private List<String> exprList = new ArrayList<>();
	public List<String> getExprList() {
		return exprList;
	}
	
	private int cut;
	private StringBuilder typeString;
	
	protected int totalCount = 0;
	protected int errorCount = 0;
	
//	private int jarCount = 0;
//	private int notFoundJarCount = 0;
	
	public int getTotalCount() {
		return totalCount;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public List<String> analyseOneFile(String path,CombinedTypeSolver typeSolver) {
		List<String> calls = new ArrayList<>();
//		TypeSolver[] solvers = createJarTypeSolverList(getLibPathUsedByProj(projectId));
		CompilationUnit cu = AstParserUtil.getCompilationUnit(path);
		if(cu != null) {
			calls = getCallExpr(cu,typeSolver);
		}		
		return calls;
	}
		
//	private List<String> getLibPathUsedByProj(int projectId) {
//		List<String> libPaths = new ArrayList<>();
//		ResultSet rs = DBUtil.query("SELECT * FROM `project_lib_usage` where `project_id`=" + projectId);
//		try {
//			while (rs.next()) {
//				int versionTypeId = rs.getInt("version_type_id");
//				ResultSet trs = DBUtil.query("SELECT * FROM `version_types` where `type_id`=" + versionTypeId);
//				while (trs.next()) {
//					String packageUrl = trs.getString("jar_package_url");
//					if(packageUrl.endsWith(".jar"))
//						libPaths.add("F:/GP/lib/"+packageUrl);
//				}
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return libPaths;
//	}
	
	public static TypeSolver[] createJarTypeSolverList(List<String> libPaths,List<String> contextPaths) {
		List<TypeSolver> solvers = new ArrayList<>();
		solvers.add(new ReflectionTypeSolver());	
		for(String context:contextPaths)
			solvers.add(new JavaParserTypeSolver(new File(context)));
		for(String path:libPaths) {
//			System.out.println(path);
			try {
				solvers.add(JarTypeSolver.getJarTypeSolver(path));
			} catch (IOException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				System.out.println(e.getMessage());
//				System.exit(0);
//				System.out.println("-----------------error:JarTypeSolver");
			}
			catch (RuntimeException e) {
				// TODO Auto-generated catch block				
//				System.out.println("RuntimeException");
				System.out.println(e.getMessage());
//				e.printStackTrace();
//				System.exit(0);
			}
			catch (Exception e) {
				// TODO Auto-generated catch block				
				System.out.println(e.getMessage());
//				e.printStackTrace();
//				System.exit(0);
			}
		}	
		TypeSolver[] result = {};
		return solvers.toArray(result);
	}
	
	
//	public List<String> getCallExpr(CompilationUnit cu,TypeSolver[] solvers) throws IOException {
	public List<String> getCallExpr(CompilationUnit cu,CombinedTypeSolver typeSolver){
		List<String> exprList = new ArrayList<>();
//		TypeSolver myTypeSolver = new CombinedTypeSolver(new ReflectionTypeSolver(),
//				JarTypeSolver.getJarTypeSolver("jars/library1.jar"),
//				JarTypeSolver.getJarTypeSolver("jars/library2.jar"),
//				JarTypeSolver.getJarTypeSolver("jars/library3.jar"),
//				new JavaParserTypeSolver(new File("src/main/java")),
//				new JavaParserTypeSolver(new File("generated_code")));
		
//		CombinedTypeSolver typeSolver = new CombinedTypeSolver(solvers);

		List<FieldAccessExpr> fieldAccessExpr = Navigator.findAllNodesOfGivenClass(cu, FieldAccessExpr.class);
		List<MethodCallExpr> methodCalls = Navigator.findAllNodesOfGivenClass(cu, MethodCallExpr.class);
		List<ClassOrInterfaceType> classOrInterfaceType = Navigator.findAllNodesOfGivenClass(cu, ClassOrInterfaceType.class);
		List<ExplicitConstructorInvocationStmt> explicitConstructorInvocationStmt = Navigator.findAllNodesOfGivenClass(cu, ExplicitConstructorInvocationStmt.class);
		for(MethodCallExpr methodCallExpr:methodCalls){
			this.totalCount ++;
			try {
				ResolvedMethodDeclaration rmd= JavaParserFacade.get(typeSolver).solve(methodCallExpr).getCorrespondingDeclaration();
				String signature = rmd.getQualifiedSignature();	
//				System.out.println(rmd.getReturnType().toString());
				exprList.add(signature);
				System.out.println("======="+signature);
			} 
			catch (UnsolvedSymbolException | UnsupportedOperationException | IllegalArgumentException | StackOverflowError  | NoClassDefFoundError e) {
				this.errorCount ++;
				// TODO Auto-generated catch block
				if(e instanceof UnsupportedOperationException)
					System.out.println("UnsupportedOperationException:" +e.getMessage()+"("+methodCallExpr+")");
				else if(e instanceof IllegalArgumentException)
					System.out.println("IllegalArgumentException:"+e.getMessage());
				else if(e instanceof UnsolvedSymbolException)
					System.out.println("UnsolvedSymbolException:"+e.getMessage());
				else if(e instanceof NoClassDefFoundError)
					System.out.println("NoClassDefFoundError:"+e.getMessage());
				else {
					System.out.println("StackOverflowError:"+e.getMessage());
					continue;
				}
			}
			catch (RuntimeException  e) {
				this.errorCount ++;
				// TODO Auto-generated catch block				
				System.out.println("RuntimeException:"+e.getMessage());
//				e.printStackTrace();
			}			
		}
//		for(ClassOrInterfaceType ci:classOrInterfaceType){
//			this.totalCount ++;			
//			String type = ci.getNameAsString();
//			exprList.add(type);
////			System.out.println(ci.resolve());
////			ci.resolve().getQualifiedName();
//			System.out.println(JavaParserFacade.get(typeSolver).getSymbolSolver().solveType(ci).getQualifiedName());
//			
//		}
		for(FieldAccessExpr fae : fieldAccessExpr){			
//			System.out.println("============ "+fae);			
			String field = fae.getNameAsString();
			String qualifier = getQualifier(fae, typeSolver);
			if(qualifier != null) {
				field = qualifier + "." + field;
				exprList.add(field);
			}
//			System.out.println("======="+field);
			
		}
		for(ExplicitConstructorInvocationStmt ecis:explicitConstructorInvocationStmt){
			this.totalCount ++;
			try {
				ResolvedConstructorDeclaration rmd= JavaParserFacade.get(typeSolver).solve(ecis).getCorrespondingDeclaration();
				String signature = rmd.getQualifiedSignature();
				exprList.add(signature);
//				System.out.println("======="+signature);
			}catch (UnsolvedSymbolException | UnsupportedOperationException | IllegalArgumentException | StackOverflowError  | NoClassDefFoundError e) {
				this.errorCount ++;
				// TODO Auto-generated catch block
				if(e instanceof UnsupportedOperationException)
					System.out.println("UnsupportedOperationException:" +e.getMessage()+"("+ecis+")");
				else if(e instanceof IllegalArgumentException)
					System.out.println("IllegalArgumentException:"+e.getMessage());
				else if(e instanceof UnsolvedSymbolException)
					System.out.println("UnsolvedSymbolException:"+e.getMessage());
				else if(e instanceof NoClassDefFoundError)
					System.out.println("NoClassDefFoundError:"+e.getMessage());
				else {
					System.out.println("StackOverflowError:"+e.getMessage());
					continue;
				}
			}
			catch (RuntimeException  e) {
				this.errorCount ++;
				// TODO Auto-generated catch block				
				System.out.println("RuntimeException:"+e.getMessage());
			}
		}
		return exprList;
	}
	
	private String getQualifier(FieldAccessExpr fae,CombinedTypeSolver typeSolver) {
		this.totalCount ++;
		String qualifier = null;
		try {
			SymbolReference sr = JavaParserFacade.get(typeSolver).getSymbolSolver().solveType(fae.getScope().toString(), fae.getScope());
			if(sr.isSolved()) {
				qualifier = sr.getCorrespondingDeclaration().asType().getQualifiedName();
			}
			else {
				ResolvedType type = JavaParserFacade.get(typeSolver).getType(fae.getScope());
				qualifier = type.asReferenceType().getQualifiedName();
			}
		}catch (UnsolvedSymbolException | UnsupportedOperationException | IllegalArgumentException | StackOverflowError  | NoClassDefFoundError e) {
			this.errorCount ++;
			// TODO Auto-generated catch block
			if(e instanceof UnsupportedOperationException)
				System.out.println("UnsupportedOperationException:" +e.getMessage());
			else if(e instanceof IllegalArgumentException)
				System.out.println("IllegalArgumentException:"+e.getMessage());
			else if(e instanceof UnsolvedSymbolException)
				System.out.println("UnsolvedSymbolException:"+e.getMessage());
			else if(e instanceof NoClassDefFoundError)
				System.out.println("NoClassDefFoundError:"+e.getMessage());
			else {
				System.out.println("StackOverflowError:"+e.getMessage());
			}
		}
		catch (RuntimeException  e) {
			this.errorCount ++;
			// TODO Auto-generated catch block				
			System.out.println("RuntimeException:"+e.getMessage());
		}
		return qualifier;
	}
	
//	private String getQualifier(FieldAccessExpr fae,CombinedTypeSolver typeSolver) {
//		this.totalCount ++;
//		List<Node> children = fae.getChildNodes();
//		String qualifier = null;
//		for(int c=0;c<children.size()-1;c++) {
//			if(children.get(c) instanceof ThisExpr)
//				break;
////			System.out.println(children.get(c)+" : "+children.get(c).getClass());			
//			try {
//				SymbolReference sr = JavaParserFacade.get(typeSolver).getSymbolSolver().solveType(children.get(c).toString(), children.get(c));
//				if(sr.isSolved()) {
//					qualifier = sr.getCorrespondingDeclaration().asType().getQualifiedName();
//				}
//				else {
//					ResolvedType type = JavaParserFacade.get(typeSolver).getType(children.get(c));
//					qualifier = type.asReferenceType().getQualifiedName();
//				}
//			}catch (UnsolvedSymbolException | UnsupportedOperationException | IllegalArgumentException | StackOverflowError  | NoClassDefFoundError e) {
//				this.errorCount ++;
//				// TODO Auto-generated catch block
//				if(e instanceof UnsupportedOperationException)
//					System.out.println("UnsupportedOperationException:" +e.getMessage());
//				else if(e instanceof IllegalArgumentException)
//					System.out.println("IllegalArgumentException:"+e.getMessage());
//				else if(e instanceof UnsolvedSymbolException)
//					System.out.println("UnsolvedSymbolException:"+e.getMessage());
//				else if(e instanceof NoClassDefFoundError)
//					System.out.println("NoClassDefFoundError:"+e.getMessage());
//				else {
//					System.out.println("StackOverflowError:"+e.getMessage());
//					continue;
//				}
//			}
//			catch (RuntimeException  e) {
//				this.errorCount ++;
//				// TODO Auto-generated catch block				
//				System.out.println("RuntimeException:"+e.getMessage());
//			}
//			break;
//		}	
//		return qualifier;
//	}
	
	private void deleteTypeQualifier(Node type) {
		List<Node> children = type.getChildNodes();
		for(int c=0;c<children.size();c++) {
			if(children.get(c) instanceof SimpleName) {
				//int start = children.get(c).getBegin().get().column - this.cut;
				//int end = children.get(c).getEnd().get().column - this.cut;
				int start = 0;
				int end = 0;
				//todo
				if(end  < this.typeString.length() && this.typeString.charAt(end) == '.' && Character.isLowerCase(this.typeString.charAt(start-1))) {
					if((end+1 >= this.typeString.length()) || ((end+1 < this.typeString.length())&& this.typeString.charAt(end+1) != '.')) {
						this.typeString.replace(start-1, end+1, "");
						this.cut += (end - start + 2);
					}					
				}					
			}
			else
				deleteTypeQualifier(children.get(c));
		}		
	}
	
	public void test() {
//		Type ty = JavaParser.parseType("java.lang.Class<? extends java.lang.Object>...");
		
//		"java.lang.Class<? extends java.lang.Object>... aa"
		Parameter ty = JavaParser.parseParameter("java.lang.Object... a");
//		JavaParser.parse("registerImplementation(java.lang.Class<? extends com.actionbarsherlock.ActionBarSherlock>)");
//		System.out.println(ty);
		cut = 0;
		// "java.lang.Class<? extends java.lang.Object>[]"
		this.typeString = new StringBuilder("java.lang.Object...");
		deleteTypeQualifier(ty);
		System.out.println(this.typeString.toString());
	}
	
//	public static void main(String args[]) {
////		List<TypeSolver> solvers = new ArrayList<>();
////		solvers.add(new ReflectionTypeSolver());	
////		solvers.add(new MemoryTypeSolver());	
////		System.out.println(solvers.size());
////		TypeSolver[] result = {};
////		result = solvers.toArray(result);
////		System.out.println(result.length);
////		CombinedTypeSolver typeSolver = new CombinedTypeSolver(result);
//////		TypeSolver typeSolver = new ReflectionTypeSolver();
////		
////		System.out.println(String.format("== %s ==",typeSolver.solveType("TimeUnit").getQualifiedName()));
////		showReferenceTypeDeclaration(typeSolver.solveType("java.lang.Object"));
//		
//		TypeSolver typeSolver = new ReflectionTypeSolver();
//		System.out.println(typeSolver.solveType("java.lang.Object"));
//	}
}