package refdiff.core2.rm2.model.refactoring;

import refdiff.core2.api.RefactoringType;
import refdiff.core2.rm2.model.SDType;

public class SDMoveAndRenameClass extends SDMoveClass {

    public SDMoveAndRenameClass(SDType typeBefore, SDType typeAfter) {
        super(RefactoringType.MOVE_RENAME_CLASS, typeBefore, typeAfter);
    }

}
