package refdiff.core.rm2.model.refactoring;

import refdiff.core.api.RefactoringType;
import refdiff.core.rm2.model.SDMethod;

public class SDPullUpMethod extends SDMoveMethod {

    public SDPullUpMethod(SDMethod methodBefore, SDMethod methodAfter) {
        super(RefactoringType.PULL_UP_OPERATION, methodBefore, methodAfter);
    }
}
