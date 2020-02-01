package org.sevensource.magnolia.backup.descriptor;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;


public class WorkspaceDescriptor {
	@XmlAttribute
	private String name;
	
	@XmlElement(name = "file")
	private final List<WorkspaceItemDescriptor> workspaceItems = new ArrayList<>();
	
	public WorkspaceDescriptor(String workspace) {
		this.name = workspace;
	}
	
	private WorkspaceDescriptor() {
		// needed for JAXB
	}
	
	public String getWorkspace() {
		return name;
	}
	
	public List<WorkspaceItemDescriptor> getWorkspaceItems() {
		return workspaceItems;
	}
	
	public void addWorkspaceItem(WorkspaceItemDescriptor item) {
		this.workspaceItems.add(item);
	}
}