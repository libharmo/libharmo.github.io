package refdiff.core2;

import org.eclipse.jgit.revwalk.RevCommit;
import refdiff.core2.rm2.analysis.RefDiffConfig;
import refdiff.core2.rm2.analysis.RefDiffConfigImpl;
import refdiff.core2.rm2.analysis.StructuralDiffHandler;
import refdiff.core2.rm2.model.SDModel;
import refdiff.core2.rm2.model.refactoring.SDRefactoring;

import java.util.ArrayList;
import java.util.List;

public class RefDiff implements refdiff.core2.api.GitRefactoringDetector {

//    public static void main(String[] args) throws Exception {
//        if (args.length != 2) {
//            throw new IllegalArgumentException("Usage: RefDiff <git-repo-folder> <commit-SHA1>");
//        }
//        final String folder = args[0];
//        final String commitId = args[1];
//
//        GitService gitService = new GitServiceImpl();
//        try (Repository repo = gitService.openRepository(folder)) {
//            GitRefactoringDetector detector = new RefDiff();
//            detector.detectAtCommit(repo, commitId, new refdiff.core2.api.RefactoringHandler() {
//                @Override
//                public void handle(RevCommit commitData, List<? extends refdiff.core2.api.Refactoring> refactorings) {
//                    if (refactorings.isEmpty()) {
//                        System.out.println("No refactorings found in commit " + commitId);
//                    } else {
//                        System.out.println(refactorings.size() + " refactorings found in commit " + commitId + ": ");
//                        for (Refactoring ref : refactorings) {
//                            System.out.println("  " + ref);
//                        }
//                    }
//                }
//                @Override
//                public void handleException(String commit, Exception e) {
//                    System.err.println("Error processing commit " + commitId);
//                    e.printStackTrace(System.err);
//                }
//            });
//        }
//    }

    /**
     * Detect refactorings performed in the specified commit. 
     * 
//     * @param repository A git repository (from JGit library).
//     * @param commitId The SHA key that identifies the commit.
     * @return A list with the detected refactorings. 
     */
    public List<SDRefactoring> detect(String a, String b) {
        List<SDRefactoring> result = new ArrayList<>();
        refdiff.core2.rm2.analysis.GitHistoryStructuralDiffAnalyzer sda = new refdiff.core2.rm2.analysis.GitHistoryStructuralDiffAnalyzer(config);
        sda.detect(a, b, new refdiff.core2.rm2.analysis.StructuralDiffHandler() {
            @Override
            public void handle(RevCommit commitData, refdiff.core2.rm2.model.SDModel sdModel) {
                result.addAll(sdModel.getRefactorings());
            }
        });
        return result;
    }

    private refdiff.core2.rm2.analysis.RefDiffConfig config;

    public RefDiff() {
        this(new RefDiffConfigImpl());
    }

    public RefDiff(RefDiffConfig config) {
        this.config = config;
    }

    private final class HandlerAdpater extends StructuralDiffHandler {
        private final refdiff.core2.api.RefactoringHandler handler;

        private HandlerAdpater(refdiff.core2.api.RefactoringHandler handler) {
            this.handler = handler;
        }

        @Override
        public void handle(RevCommit commitData, SDModel sdModel) {
            handler.handle(commitData, sdModel.getRefactorings());
        }

        @Override
        public void handleException(String commitId, Exception e) {
            handler.handleException(commitId, e);
        }
        
    }
//
//    @Override
//    public void detectAtCommit(Repository repository, String commitId, RefactoringHandler handler) {
//        refdiff.core2.rm2.analysis.GitHistoryStructuralDiffAnalyzer sda = new GitHistoryStructuralDiffAnalyzer(config);
//        sda.detectAtCommit(repository, commitId, new HandlerAdpater(handler));
//    }

    @Override
    public String getConfigId() {
        return config.getId();
    }
}
