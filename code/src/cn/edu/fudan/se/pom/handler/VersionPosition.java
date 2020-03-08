package cn.edu.fudan.se.pom.handler;

import com.alibaba.fastjson.annotation.JSONField;

public class VersionPosition {
	private String rawVersion;
	private String declarePosition;
	private boolean isProperty;
	private String versionPosition;
	@JSONField(name="propertyPosition")
	private String propertyPosition;
	@JSONField(name="propertyName")
	private String propertyName;
	@JSONField(name="propertyValue")
	private String propertyValue;
	
	
	
	public String getRawVersion() {
		return rawVersion;
	}

	public void setRawVersion(String rawVersion) {
		this.rawVersion = rawVersion;
	}

	public boolean getIsProperty() {
		return isProperty;
	}

	public void setIsProperty(boolean isProperty) {
		this.isProperty = isProperty;
	}

	public String getVersionPosition() {
		return versionPosition;
	}

	public void setVersionPosition(String versionPosition) {
		this.versionPosition = versionPosition;
	}

	public String getPropertyPosition() {
		return propertyPosition;
	}

	public void setPropertyPosition(String propertyPosition) {
		this.propertyPosition = propertyPosition;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getPropertyValue() {
		return propertyValue;
	}

	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}

	public void setDeclarePosition(String declarePosition) {
		this.declarePosition = declarePosition;
	}

	public String getDeclarePosition() {
		return declarePosition;
	}

	public VersionPosition(String rawVersion,String declarePosition,boolean isProperty,String versionPosition,String propertyPosition,String propertyName,String propertyValue) {
		this.rawVersion = rawVersion;
		this.declarePosition = declarePosition;
		this.isProperty = isProperty;
		this.versionPosition = versionPosition;
		this.propertyPosition = propertyPosition;
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
	}
	
	
}
