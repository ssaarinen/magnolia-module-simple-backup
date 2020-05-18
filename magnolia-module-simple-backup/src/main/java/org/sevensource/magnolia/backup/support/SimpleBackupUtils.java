package org.sevensource.magnolia.backup.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SimpleBackupUtils {
	private static final Logger logger = LoggerFactory.getLogger(SimpleBackupUtils.class);

	private SimpleBackupUtils() {}

	public static Path createDirectory(Path path) {
		if(path.toFile().exists()) {
			throw new IllegalArgumentException("Directory " + path.toString() + " already exists");
		}

		try {
			return Files.createDirectory(path);
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot create directory", e);
		}
	}
}
