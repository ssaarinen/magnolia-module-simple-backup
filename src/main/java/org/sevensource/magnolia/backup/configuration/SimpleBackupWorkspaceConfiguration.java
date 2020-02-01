package org.sevensource.magnolia.backup.configuration;

public class SimpleBackupWorkspaceConfiguration {
	
	private String workspace;
	private String path = "/";
	private boolean split = false;

	public String getWorkspace() {
		return workspace;
	}

	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public boolean isSplit() {
		return split;
	}
	
	public void setSplit(boolean split) {
		this.split = split;
	}
}