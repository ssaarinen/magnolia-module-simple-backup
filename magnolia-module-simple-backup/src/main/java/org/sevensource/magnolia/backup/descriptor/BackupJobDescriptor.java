package org.sevensource.magnolia.backup.descriptor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "mgnl-simplebackup")
class BackupJobDescriptor {

	@XmlElement(name = "workspace")
	private final List<WorkspaceDescriptor> workspaces = new ArrayList<>();

	public WorkspaceDescriptor getWorkspaceDescriptor(String workspace) {
		return workspaces
			.stream()
			.filter(w -> w.getWorkspace().equals(workspace))
			.findFirst()
			.orElse(null);
	}

	public void addWorkspace(String workspace) {
		if(getWorkspaceDescriptor(workspace) == null) {
			this.workspaces.add(new WorkspaceDescriptor(workspace));
		}
	}

	public List<WorkspaceDescriptor> getWorkspaces() {
		return workspaces;
	}
}
