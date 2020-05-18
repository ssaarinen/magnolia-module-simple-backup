package org.sevensource.magnolia.backup.configuration;

import java.util.List;

public class SimpleBackupJobConfiguration {
	private String name;
	private boolean enabled = true;
	private String backupPath;

	private List<SimpleBackupWorkspaceConfiguration> workspaces;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<SimpleBackupWorkspaceConfiguration> getWorkspaces() {
		return workspaces;
	}
	public void setWorkspaces(List<SimpleBackupWorkspaceConfiguration> workspaces) {
		this.workspaces = workspaces;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getBackupPath() {
		return backupPath;
	}
	public void setBackupPath(String backupPath) {
		this.backupPath = backupPath;
	}
}
