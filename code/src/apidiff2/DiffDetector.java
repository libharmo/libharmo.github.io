package apidiff2;

import apidiff2.enums.Classifier;

import java.util.List;

public interface DiffDetector {
	
	/**
	 * Analyzing changes performed in specific commit
	 * @param commitId - SHA key
	 * @param classifier - Classifier for packages
	 * @return Result - Detected changes
	 * @throws Exception - Exception during process
	 */
	public Result detectChangeAtCommit(String commitId, apidiff2.enums.Classifier classifier) throws Exception;

	/**
	 * Analyzing changes performed in several commits
	 * @param branch - branch name (i.e., "master")
	 * @param classifiers - Classifier for packages
	 * @return Result - Detected changes
	 * @throws Exception - Exception during process
	 */
	public Result detectChangeAllHistory(String branch, List<Classifier> classifiers) throws Exception;

	/**
	 * Analyzing changes performed in several commits
	 * @param branch - branch name (i.e., "master")
	 * @param classifier - Classifier for packages
	 * @return - Detected changes
	 * @throws Exception - Exception during process
	 */
	public Result detectChangeAllHistory(String branch, apidiff2.enums.Classifier classifier) throws Exception;

	/**
	 * AAnalyzing changes performed in several commits
	 * @param classifiers
	 * @return - Detected changes
	 * @throws Exception - Exception during process
	 */
	public Result detectChangeAllHistory(List<Classifier> classifiers) throws Exception;

	/**
	 * Analyzing changes performed in several commits
	 * @param classifier - Classifier for packages
	 * @return Result - Detected changes
	 * @throws Exception
	 */
	public Result detectChangeAllHistory(apidiff2.enums.Classifier classifier) throws Exception;

	/**
	 * Fetching new commits from a repository
	 * @param classifiers - Classifier for packages
	 * @return Result - Detected changes
	 * @throws Exception - Exception during process
	 */
	public Result fetchAndDetectChange(List<Classifier> classifiers) throws Exception;
	
	/**
	 * Fetching new commits from a repository
	 * @param classifier - Classifier for packages
	 * @return Result - Detected changes
	 * @throws Exception - Exception during process
	 */
	public Result fetchAndDetectChange(Classifier classifier) throws Exception;
	
}
