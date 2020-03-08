package cn.edu.fudan.se.effort.bean;

import java.util.List;

/**
 * 在两个jar包内比较文件的结果
 */
public class JarFileResult {

    private List<String> deleteFileList;
    private List<String> addFileList;
    private List<String> unionFileList;

    public List<String> getDeleteFileList() {
        return deleteFileList;
    }

    public void setDeleteFileList(List<String> deleteFileList) {
        this.deleteFileList = deleteFileList;
    }

    public List<String> getAddFileList() {
        return addFileList;
    }

    public void setAddFileList(List<String> addFileList) {
        this.addFileList = addFileList;
    }

    public JarFileResult() {
    }

    public JarFileResult(List<String> deleteFileList, List<String> addFileList, List<String> unionFileList) {

        this.deleteFileList = deleteFileList;
        this.addFileList = addFileList;
        this.unionFileList = unionFileList;
    }

    public List<String> getUnionFileList() {

        return unionFileList;
    }

    public void setUnionFileList(List<String> unionFileList) {
        this.unionFileList = unionFileList;
    }
}
