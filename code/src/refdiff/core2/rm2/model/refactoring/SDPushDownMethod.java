package refdiff.core2.rm2.model.refactoring;

import refdiff.core2.api.RefactoringType;
import refdiff.core2.rm2.model.SDMethod;

public class SDPushDownMethod extends SDMoveMethod {

    public SDPushDownMethod(SDMethod methodBefore, SDMethod methodAfter) {
        super(RefactoringType.PUSH_DOWN_OPERATION, methodBefore, methodAfter);
    }

}
