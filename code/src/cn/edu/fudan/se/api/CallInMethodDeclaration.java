package cn.edu.fudan.se.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cn.edu.fudan.se.util.AstParserUtil;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparser.Navigator;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.UnsolvedSymbolException;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;


public class CallInMethodDeclaration extends ProjectAnalyzer{
	protected int totalCountInParent = 0;
	protected int errorCountInParent = 0;
	
	protected int totalCountOutParent = 0;
	protected int errorCountOutParent = 0;
	
	protected List<String> exprOutParent = new ArrayList<>();
	protected Map<String,List<String>> exprInParent = new HashMap<>();
	protected List<String> otherDeclaration = new ArrayList<>();
	
	public void analyseOneFileInMethod(String path,CombinedTypeSolver typeSolver) {
//		TypeSolver[] solvers = createJarTypeSolverList(getLibPathUsedByProj(projectId));
		CompilationUnit cu = AstParserUtil.getCompilationUnit(path);
		if(cu != null) {
			callExpr(cu,typeSolver);
		}		
	}

	public void callExpr(CompilationUnit cu,CombinedTypeSolver typeSolver) {
//		List<String> exprList = new ArrayList<>();	
		String packageName = "";
		PackageDeclaration pd = cu.getPackageDeclaration().orElse(null);
		if(pd != null)
			packageName = pd.getNameAsString() + ".";
		
		List<FieldAccessExpr> fieldAccessExpr = Navigator.findAllNodesOfGivenClass(cu, FieldAccessExpr.class);
		List<MethodCallExpr> methodCalls = Navigator.findAllNodesOfGivenClass(cu, MethodCallExpr.class);
		List<ClassOrInterfaceType> classOrInterfaceType = Navigator.findAllNodesOfGivenClass(cu, ClassOrInterfaceType.class);
		List<ExplicitConstructorInvocationStmt> explicitConstructorInvocationStmt = Navigator.findAllNodesOfGivenClass(cu, ExplicitConstructorInvocationStmt.class);
		for(MethodCallExpr methodCallExpr:methodCalls){
			// get parent
			String parentDeclaration = getParentName(methodCallExpr,packageName);
//			System.out.println("+++++++++++++++++++" + parentDeclaration);
			if(parentDeclaration == null)
				this.totalCountOutParent ++;
			else
				this.totalCountInParent ++;
//			this.totalCount ++;
			try {
				ResolvedMethodDeclaration rmd= JavaParserFacade.get(typeSolver).solve(methodCallExpr, true).getCorrespondingDeclaration();
				String signature = rmd.getQualifiedSignature();	
//				System.out.println(rmd.getReturnType().toString());
				saveExprToList(parentDeclaration, signature);
//				exprList.add(signature);
				System.out.println("======="+signature);										
//				System.out.println(parent.getClass());
			} 
			catch (UnsolvedSymbolException | UnsupportedOperationException | IllegalArgumentException | StackOverflowError  | NoClassDefFoundError e) {
//				this.errorCount ++;
				if(parentDeclaration == null)
					this.errorCountOutParent ++;
				else
					this.errorCountInParent ++;
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
//				this.errorCount ++;
				if(parentDeclaration == null)
					this.errorCountOutParent ++;
				else
					this.errorCountInParent ++;
				// TODO Auto-generated catch block				
				System.out.println("RuntimeException:"+e.getMessage());
//				e.printStackTrace();
			}			
		}
		for(FieldAccessExpr fae : fieldAccessExpr){	
//			System.out.println("======="+fae);
			String parentDeclaration = getParentName(fae,packageName);
//			System.out.println("+++++++++++++++++++" + parentDeclaration);
			boolean hasParent = (parentDeclaration == null)?false:true;	
			
			String field = fae.getNameAsString();
			String qualifier = getQualifier(fae, typeSolver,hasParent);
			if(qualifier != null) {
				field = qualifier + "." + field;
//				exprList.add(field);
				saveExprToList(parentDeclaration, field);
			}
			System.out.println("======="+field);
		}
		for(ExplicitConstructorInvocationStmt ecis:explicitConstructorInvocationStmt){
			String parentDeclaration = getParentName(ecis,packageName);
//			System.out.println("+++++++++++++++++++" + parentDeclaration);
			if(parentDeclaration == null)
				this.totalCountOutParent ++;
			else
				this.totalCountInParent ++;
			
//			this.totalCount ++;
			try {
				ResolvedConstructorDeclaration rmd= JavaParserFacade.get(typeSolver).solve(ecis, true).getCorrespondingDeclaration();
				String signature = rmd.getQualifiedSignature();
//				exprList.add(signature);
				saveExprToList(parentDeclaration, signature);
				System.out.println("======="+signature);
			}catch (UnsolvedSymbolException | UnsupportedOperationException | IllegalArgumentException | StackOverflowError  | NoClassDefFoundError e) {
//				this.errorCount ++;
				if(parentDeclaration == null)
					this.errorCountOutParent ++;
				else
					this.errorCountInParent ++;
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
//				this.errorCount ++;
				if(parentDeclaration == null)
					this.errorCountOutParent ++;
				else
					this.errorCountInParent ++;
				// TODO Auto-generated catch block				
				System.out.println("RuntimeException:"+e.getMessage());
			}
		}
		
		List<MethodDeclaration> methodDeclarations = Navigator.findAllNodesOfGivenClass(cu, MethodDeclaration.class);	
//		System.out.println(methodDeclarations.size());
		for(MethodDeclaration md:methodDeclarations){
			String declaration = getQualifiedMethodDeclaration(md,packageName);
//			System.out.println("+++++++++++++++++++" + declaration);
			if(!exprInParent.containsKey(declaration)) {
//				if(otherDeclaration.contains(declaration))			
//				System.out.println("+++++++++++++++++++" + declaration);
				otherDeclaration.add(declaration);
			}		
		}
//		System.out.println(otherDeclaration.size());
//		return exprList;
	}
	
	private String getQualifiedMethodDeclaration(CallableDeclaration cd,String packageName) {
		String qualifier = "";
		Node parent = cd.getParentNode().orElse(null);
        while (parent != null) {
        	String add = null;
            if(ClassOrInterfaceDeclaration.class.isAssignableFrom(parent.getClass())) {
            	add = ((ClassOrInterfaceDeclaration)parent).getNameAsString();
            }
            else if(EnumConstantDeclaration.class.isAssignableFrom(parent.getClass())) {
            	add = ((EnumConstantDeclaration)parent).getNameAsString();
            }
            else if(EnumDeclaration.class.isAssignableFrom(parent.getClass())) {
            	add = ((EnumDeclaration)parent).getNameAsString();
            }
            if(add != null) {
            	if(qualifier.equals(""))
            		qualifier = add;
            	else
            		qualifier = add + "." + qualifier;
            }
            parent = parent.getParentNode().orElse(null);
        }
        
        String declaration = cd.getDeclarationAsString(false, false, false);
		String name = cd.getNameAsString();
		int index = declaration.indexOf(name);
		declaration = declaration.substring(index);
		if(qualifier.equals(""))
			return packageName + declaration;
//      return Optional.of(classType.cast(parent));
        return packageName + qualifier + "." + declaration;
	}
	
	private void saveExprToList(String parentDeclaration,String expr) {
		if(parentDeclaration == null)
			exprOutParent.add(expr);
		else {
			if(exprInParent.containsKey(parentDeclaration))
				exprInParent.get(parentDeclaration).add(expr);
			else {
				List<String> newList = new ArrayList<>();
				newList.add(expr);
				exprInParent.put(parentDeclaration, newList);
			}
		}
	}
	
	private String getQualifier(FieldAccessExpr fae,CombinedTypeSolver typeSolver,boolean hasParent) {
//		this.totalCount ++;
		if(!hasParent)
			this.totalCountOutParent ++;
		else
			this.totalCountInParent ++;
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
//			this.errorCount ++;
			if(!hasParent)
				this.errorCountOutParent ++;
			else
				this.errorCountInParent ++;
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
			if(!hasParent)
				this.errorCountOutParent ++;
			else
				this.errorCountInParent ++;
//			this.errorCount ++;
			// TODO Auto-generated catch block				
			System.out.println("RuntimeException:"+e.getMessage());
		}
		return qualifier;
	}
	
	private String getParentName(Node node,String packageName) {
		String parentDeclaration = null;
		Optional<MethodDeclaration> parent = node.getAncestorOfType(MethodDeclaration.class);
		if(parent.isPresent()) {
			return getQualifiedMethodDeclaration(parent.get(),packageName);
//			parentDeclaration = parent.get().getSignature().asString();
		}
		else {
			Optional<ConstructorDeclaration> newParent = node.getAncestorOfType(ConstructorDeclaration.class);
			if(newParent.isPresent()) {
				return getQualifiedMethodDeclaration(newParent.get(),packageName);
//				parentDeclaration = newParent.get().getSignature().asString();
			}
		}
		return parentDeclaration;
	}
	
}
