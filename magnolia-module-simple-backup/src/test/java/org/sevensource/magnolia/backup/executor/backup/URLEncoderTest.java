package org.sevensource.magnolia.backup.executor.backup;

import info.magnolia.importexport.command.JcrExportCommand;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Collections;

public class URLEncoderTest {

	@Test
	public void testCreateBackupFilename() {
		BackupExecutor executor = new BackupExecutor(Collections.emptyList(), Paths.get("."), null);

		String nodePath = "/München/That's-us";
		String filename = executor.createBackupFilename("dummyworkspace", nodePath);
		Assertions.assertThat(filename).isEqualTo("dummyworkspace.m%c3%bcnchen.that%27s-us.xml");
	}
}
