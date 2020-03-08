package cn.edu.fudan.se.cfg.rq2.bean;

import java.util.*;

/**
 * jar包依赖
 */
public class DependencyItem {

    String groupId;
    String artifactId;
    String version;
    String jarName;
    String jarPath;

    Set<String> methodList;

    Map<String, List<String>> callgraph;

    List<DependencyItem> childList;

    public Set<String> getMethodList() {
        if (methodList == null) {
            methodList = new HashSet<>();
        }
        return methodList;
    }

    public DependencyItem(String groupId, String artifactId, String version, String jarName, String jarPath) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.jarName = jarName;
        this.jarPath = jarPath;
    }

    public void setMethodList(Set<String> methodList) {
        this.methodList = methodList;
    }

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }


    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }

    public DependencyItem(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        childList = new ArrayList<>();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, List<String>> getCallgraph() {
        return callgraph;
    }

    public void setCallgraph(Map<String, List<String>> callgraph) {
        this.callgraph = callgraph;
    }

    public List<DependencyItem> getChildList() {
        if (childList == null) {
            childList = new ArrayList<>();
        }
        return childList;
    }

    public void setChildList(List<DependencyItem> childList) {
        this.childList = childList;
    }

    public DependencyItem() {

    }

    public DependencyItem(String groupId, String artifactId, String version, Map<String, List<String>> callgraph, List<DependencyItem> childList) {

        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.callgraph = callgraph;
        this.childList = childList;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DependencyItem && obj.toString().equals(toString())) {
            return true;
        }
        return false;
    }
}
