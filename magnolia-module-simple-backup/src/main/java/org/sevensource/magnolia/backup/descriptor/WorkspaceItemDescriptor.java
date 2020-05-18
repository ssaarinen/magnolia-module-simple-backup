package org.sevensource.magnolia.backup.descriptor;

import javax.xml.bind.annotation.XmlAttribute;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WorkspaceItemDescriptor {
	@XmlAttribute
	private String filePath;

	@XmlAttribute
	private String nodePath;

	public WorkspaceItemDescriptor(Path filePath, String nodePath) {
		this.filePath = filePath.toString();
		this.nodePath = nodePath;
	}

	private WorkspaceItemDescriptor() {
		// needed for JAXB
	}

	public Path getFilePath() {
		return Paths.get(filePath);
	}

	public String getNodePath() {
		return nodePath;
	}
}
