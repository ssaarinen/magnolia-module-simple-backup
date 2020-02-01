package org.sevensource.magnolia.backup.executor.backup;

import java.nio.file.Path;

final class BackupJobDefinition {
	private final Path exportPath;
	private final String workspace;
	private final String repositoryRootPath;
	private final boolean split;
	
	public BackupJobDefinition(Path exportPath, String workspace, String repositoryRootPath, boolean split) {
		this.exportPath = exportPath;
		this.workspace = workspace;
		this.repositoryRootPath = repositoryRootPath;
		this.split = split;
	}
	
	public String getWorkspace() {
		return workspace;
	}
	
	public boolean isSplit() {
		return split;
	}
	
	public Path getExportPath() {
		return exportPath;
	}
	
	public String getRepositoryRootPath() {
		return repositoryRootPath;
	}
}
