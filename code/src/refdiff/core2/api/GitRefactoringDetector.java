package refdiff.core2.api;

import refdiff.core2.rm2.model.refactoring.SDRefactoring;

import java.util.List;

/**
 * Detect refactorings in a git commit.
 * 
 */
public interface GitRefactoringDetector {

	/**
	 * Detect refactorings performed in the specified commit. 
	 * 
//	 * @param repository A git repository (from JGit library).
//	 * @param commitId The SHA key that identifies the commit.
//	 * @param handler A handler object that is responsible to process the detected refactorings.
	 */
	List<SDRefactoring> detect(String a, String b);

	/**
	 * @return An ID that represents the current configuration for the algorithm in use.
	 */
	String getConfigId();
}
