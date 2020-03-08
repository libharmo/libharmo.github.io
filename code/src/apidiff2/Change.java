package apidiff2;

import apidiff2.enums.Category;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.Serializable;

/**
 * Information about the change.
 * @author aline
 */
public class Change implements Serializable {
	
	public Change(){
		
	}
	
	public Change(final String path, final String struture, final apidiff2.enums.Category category, final Boolean isBreakingChange, final String description){
		this.path = path;
		this.element = struture;
		this.category = category;
		this.description = description;
		this.breakingChange = isBreakingChange;
	}
	
	public Change(final String path, final String struture, final apidiff2.enums.Category category, final Boolean isBreakingChange){
		this.path = path;
		this.element = struture;
		this.category = category;
		this.breakingChange = isBreakingChange;
		this.description = "";
	}
	
	public Change(final String path, final String struture, final apidiff2.enums.Category category) {
		this.path = path;
		this.element = struture;
		this.category = category;
	}
	

	private String path;

	private String element;

	private apidiff2.enums.Category category;

	private Boolean breakingChange;

	private String description;
	
	private Boolean javadoc;

	private Boolean deprecated;

	private RevCommit revCommit;

	/**
	 * @return Element path (i.e, java.util.ArrayList).
	 */
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	/**
	 * @return Struture name (i.e., public void setName(String)).
	 */
	public String getElement() {
		return element;
	}

	public void setElement(String element) {
		this.element = element;
	}
	
	/**
	 * @return If it is breaking change (BC) is true, otherwise is false.
	 */
	public Boolean isBreakingChange() {
		return breakingChange;
	}

	public void setBreakingChange(Boolean breakingChange) {
		this.breakingChange = breakingChange;
	}
	
	/**
	 * @return Description about the change.
	 */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return If element has JavaDoc is true, otherwise is false.
	 */
	public Boolean containsJavadoc() {
		return this.javadoc;
	}

	public void setJavadoc(Boolean javadoc) {
		this.javadoc = javadoc;
	}

	/**
	 * @return If element is deprecated is true, otherwise is false.
	 */
	public Boolean isDeprecated() {
		return this.deprecated;
	}

	public void setDeprecated(Boolean depreciated) {
		this.deprecated = depreciated;
	}

	/**
	 * @return Change category.For more details {@link apidiff2.enums.Category}
	 */
	public apidiff2.enums.Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	/**
	 * @return Information about commit (i.e., author, email, commit hash, date).
	 */
	public RevCommit getRevCommit() {
		return revCommit;
	}

	public void setRevCommit(RevCommit revCommit) {
		this.revCommit = revCommit;
	}
	
}
