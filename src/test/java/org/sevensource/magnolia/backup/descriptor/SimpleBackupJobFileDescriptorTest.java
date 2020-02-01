package org.sevensource.magnolia.backup.descriptor;


import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.bind.JAXBException;

import org.junit.jupiter.api.Test;

class SimpleBackupJobFileDescriptorTest {

	@Test
	void relativizePath() {
		Path basePath = Paths.get("/", "var", "log");
		Path otherPath = Paths.get("/", "var", "log", "123", "test");
		
		Path relative = basePath.relativize(otherPath);
		
		assertThat(relative.toString()).isEqualTo("123/test");
	}
	
	
	@Test
	void test() throws FileNotFoundException, JAXBException {
		SimpleBackupJobFileDescriptor d = new SimpleBackupJobFileDescriptor();
		d.addWorkspace("website");
		d.addWorkspaceItem("website", "/index", Paths.get("backup_date", "website", "website.1.xml"));
		d.addWorkspaceItem("website", "/about", Paths.get("backup_date", "website", "website.2.xml"));
		d.addWorkspaceItem("website", "/through", Paths.get("backup_date", "website", "website.3.xml"));
		d.addWorkspace("config");
		d.addWorkspaceItem("config", "/", Paths.get("backup_date", "config", "website.1.xml"));
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		d.serialize(os);
		
		String output = new String(os.toByteArray(), StandardCharsets.UTF_8);
		
		
		ByteArrayInputStream is = new ByteArrayInputStream(output.getBytes());
		
		d = SimpleBackupJobFileDescriptor.deserialize(is);
		
		os = new ByteArrayOutputStream();
		d.serialize(os);
		
		String output2 = new String(os.toByteArray(), StandardCharsets.UTF_8);
		
		
		assertThat(output).isEqualTo(output2);
		
	}

}
