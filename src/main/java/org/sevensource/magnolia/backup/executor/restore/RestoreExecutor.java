package org.sevensource.magnolia.backup.executor.restore;

import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.DataTransporter;
import org.apache.commons.lang3.time.StopWatch;
import org.sevensource.magnolia.backup.descriptor.SimpleBackupJobFileDescriptor;
import org.sevensource.magnolia.backup.descriptor.WorkspaceDescriptor;
import org.sevensource.magnolia.backup.descriptor.WorkspaceItemDescriptor;
import org.sevensource.magnolia.backup.magnolia.SameJcrSessionContextDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ImportUUIDBehavior;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class RestoreExecutor {
	private static final Logger logger = LoggerFactory.getLogger(RestoreExecutor.class);

	private final Path restoreBasePath;

	public RestoreExecutor(Path restoreBasePath) {
		this.restoreBasePath = restoreBasePath;
	}


	public void run() {
		final List<WorkspaceDescriptor> workspaces = SimpleBackupJobFileDescriptor
				.deserialize(restoreBasePath)
				.getWorkspaces();

		// make sure config workspace is imported last in order for
		// Magnolia observation to kick in last
		final Iterator<WorkspaceDescriptor> it = workspaces.iterator();
		while(it.hasNext()) {
			WorkspaceDescriptor item = it.next();
			if(item.getWorkspace().equals("config")) {
				it.remove();
				workspaces.add(item);
				break;
			}
		}

		logger.warn("Restoring workspaces from {}", restoreBasePath);
		final StopWatch stopWatch = StopWatch.createStarted();

		final List<String> workspaceNames = workspaces.stream()
				.map(WorkspaceDescriptor::getWorkspace)
				.collect(Collectors.toList());

		final Context originalContext = MgnlContext.getInstance();

		final SameJcrSessionContextDecorator contextDecorator =
				new SameJcrSessionContextDecorator(originalContext, workspaceNames);

		try {
			MgnlContext.setInstance(contextDecorator);

			for (WorkspaceDescriptor ws : workspaces) {
				logger.info("Restoring workspace '{}'", ws.getWorkspace());

				for (WorkspaceItemDescriptor wsItem : ws.getWorkspaceItems()) {
					final Path filePath = restoreBasePath.resolve(wsItem.getFilePath());
					doRestore(filePath, ws.getWorkspace(), wsItem.getNodePath());
				}
			}

			contextDecorator.saveSessions();
		} catch (Exception e) {
			logger.error("An exception occurred while restoring from backup", e);
		} finally {
			contextDecorator.release();
			MgnlContext.setInstance(originalContext);
			originalContext.release();
		}

		logger.warn("Finished restoring workspaces in {} seconds", stopWatch.getTime(TimeUnit.SECONDS));
	}


	private void doRestore(Path restoreFile, String workspace, String nodePath) throws Exception {
		logger.warn("Restoring file {} into {}:{}", restoreFile, workspace, nodePath);

		final Path unzippedRestorePath = getImportFilePath(restoreFile);
		final InputStream inputStream = new BufferedInputStream(Files.newInputStream(unzippedRestorePath));

        DataTransporter.importXmlStream(
        		inputStream,
        		workspace,
        		nodePath,
        		unzippedRestorePath.toString(),
        		false,
        		true,
        		ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING,
        		false,
        		true);
	}

	private Path getImportFilePath(Path filePath) {
		final String filename = filePath.getFileName().toString();

		if(filename.toLowerCase().endsWith(".zip")) {
			return unzipArchive(filePath);
		} else if(filename.toLowerCase().endsWith(".xml")) {
			return filePath;
		} else {
			throw new IllegalArgumentException("Don't know how to handle file " + filePath.toString());
		}
	}

	private Path unzipArchive(Path filePath) {
		final String filename = filePath.getFileName().toString();

		if(! filename.endsWith(".zip")) {
			throw new IllegalArgumentException("Cannot unzip file with filename " + filename);
		}

		logger.info("Unzipping {}", filePath);

		try (final FileInputStream fis = new FileInputStream(filePath.toFile());
				final ZipInputStream zis = new ZipInputStream(fis)) {

			final ZipEntry entry = zis.getNextEntry();

			logger.debug("Unzipping first zip entry named '{}'", entry.getName());

			final Path destinationPath = filePath.getParent();
			final File unzipped = new File(destinationPath.toFile(), entry.getName());

			String destCanonical = destinationPath.toFile().getCanonicalPath();
			String unzippedCanonical = unzipped.getCanonicalPath();

			if(! unzippedCanonical.startsWith(destCanonical)) {
				throw new IllegalArgumentException("Destination file is outside of zip file directory");
			}

			try (final FileOutputStream fos = new FileOutputStream(unzipped)) {
	            int len;
	            byte[] buffer = new byte[1024];
	            while ((len = zis.read(buffer)) > 0) {
	                fos.write(buffer, 0, len);
	            }
			}

			zis.closeEntry();

			return unzipped.toPath();
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("Cannot find file", e);
		} catch (IOException e) {
			throw new RuntimeException("IOException...", e);
		}
	}
}
