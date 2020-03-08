package apidiff2.internal.visitor;

import apidiff2.internal.util.UtilTools;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.EnumDeclaration;

import java.util.ArrayList;

public class EnumDeclarationVisitor extends ASTVisitor{
	private ArrayList<EnumDeclaration> acessibleEnums = new ArrayList<EnumDeclaration>();
	private ArrayList<EnumDeclaration> nonAcessibleEnums = new ArrayList<EnumDeclaration>();

	public ArrayList<EnumDeclaration> getAcessibleEnums() {
		return acessibleEnums;
	}

	public ArrayList<EnumDeclaration> getNonAcessibleEnums() {
		return nonAcessibleEnums;
	}

	@Override
	public boolean visit(EnumDeclaration node){
		if(UtilTools.isVisibilityProtected(node) || UtilTools.isVisibilityPublic(node)){
			this.acessibleEnums.add(node);
		}
		else{
			this.nonAcessibleEnums.add(node);
		}
		return super.visit(node);
	}

}
