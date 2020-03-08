package refdiff.core.rm2.model.refactoring;

import refdiff.core.api.RefactoringType;
import refdiff.core.rm2.model.SDMethod;

public class SDPushDownMethod extends SDMoveMethod {

    public SDPushDownMethod(SDMethod methodBefore, SDMethod methodAfter) {
        super(RefactoringType.PUSH_DOWN_OPERATION, methodBefore, methodAfter);
    }

}
