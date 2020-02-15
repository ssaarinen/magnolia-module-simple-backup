package org.sevensource.magnolia.backup.executor.backup;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.contenthandler.XmlContentHandlerFactory;
import info.magnolia.importexport.filters.NamespaceFilter;
import info.magnolia.jcr.decoration.ContentDecorator;
import info.magnolia.objectfactory.Classes;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.commons.xml.Exporter;
import org.apache.jackrabbit.commons.xml.SystemViewExporter;
import org.sevensource.magnolia.backup.configuration.SimpleBackupWorkspaceConfiguration;
import org.sevensource.magnolia.backup.descriptor.SimpleBackupJobFileDescriptor;
import org.sevensource.magnolia.backup.support.SimpleBackupUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.context.SystemContext;
import info.magnolia.importexport.command.JcrExportCommand;
import info.magnolia.importexport.command.JcrExportCommand.Compression;
import info.magnolia.importexport.command.JcrExportCommand.Format;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class BackupExecutor {

	private static final Logger logger = LoggerFactory.getLogger(BackupExecutor.class);

	private static final DateTimeFormatter dtFormatter = new DateTimeFormatterBuilder()
			.appendPattern("yyyy")
			.appendLiteral("-")
			.appendPattern("MM")
			.appendLiteral("-")
			.appendPattern("dd")
			.appendLiteral("T")
			.appendPattern("HHmmss")
			.toFormatter();

	private final List<SimpleBackupWorkspaceConfiguration> configurations;
	private final JcrExportCommand.Compression compression;
	private final Path exportBasePath;
	private final SystemContext ctx;
	private final SimpleBackupJobFileDescriptor backupDescriptor = new SimpleBackupJobFileDescriptor();

	public BackupExecutor(List<SimpleBackupWorkspaceConfiguration> definitions, JcrExportCommand.Compression compression, Path basePath, SystemContext ctx) {
		this.configurations = definitions;
		this.compression = compression;
		this.exportBasePath = validateAndBuildBackupPath(basePath);
		this.ctx = ctx;
	}

	public void run() {

		final List<BackupJobDefinition> jobDefinitions = configurations
			.stream()
			.map(wsDef -> {
				Path wsBackupPath = exportBasePath.resolve(wsDef.getWorkspace().toLowerCase());
				return new BackupJobDefinition(
						wsBackupPath,
						wsDef.getWorkspace(),
						wsDef.getPath(),
						wsDef.isSplit());
			})
			.collect(Collectors.toList());

		final StopWatch totalStopWatch = StopWatch.createStarted();
		log("Starting Backup into " + exportBasePath.toString());

		for(BackupJobDefinition jobDef : jobDefinitions) {
			final StopWatch jobStopWatch = StopWatch.createStarted();

			final String workspace = jobDef.getWorkspace();

			log("Starting backup of workspace " + workspace);

			final Path workspaceBackupPath = createWorkspaceBackupDirectory(workspace);
			final List<String> nodesToBackup = getExportNodes(jobDef);

			for(String backupNode : nodesToBackup) {
				final StopWatch nodeJobStopWatch = StopWatch.createStarted();
				log("Starting backup of node " + backupNode +" in workspace " + workspace);

				final String backupFilename = createBackupFilename(workspace, backupNode);
				final String backupFilesystemFilename = createBackupFilesystemFilename(backupFilename);
				final Path backupFilepath = workspaceBackupPath.resolve(backupFilesystemFilename);

				final boolean isBackupJobRootPath = backupNode.equals(jobDef.getRepositoryRootPath());

				doBackup(workspace, backupNode, isBackupJobRootPath, backupFilename, backupFilepath);

				Path relativeBackupFilepath = exportBasePath.relativize(backupFilepath);

				backupDescriptor.addWorkspaceItem(workspace, backupNode, relativeBackupFilepath);
				log("Finished backup of node " + backupNode + " in workspace " + jobDef.getWorkspace() + " in " + getTimeAsString(nodeJobStopWatch));
			}

			backupDescriptor.addWorkspace(workspace);
			log("Finished backup of workspace " + jobDef.getWorkspace() + " in " + getTimeAsString(jobStopWatch));
		}


		log("Writing backup jobfile to " + exportBasePath);
		backupDescriptor.serialize(exportBasePath);

		log("Finished Backup in " + getTimeAsString(totalStopWatch));
	}



	private List<String> getExportNodes(BackupJobDefinition job) {

		final List<String> nodesToBackup = new ArrayList<>();
		nodesToBackup.add(job.getRepositoryRootPath());

		try {
			final Session sess = ctx.getJCRSession(job.getWorkspace());
			final Node node = sess.getNode(job.getRepositoryRootPath());
			final Iterable<Node> childNodes = JcrUtils.getChildNodes(node);
			final Iterator<Node> it = childNodes.iterator();
			while(it.hasNext()) {
				final Node childNode = it.next();
				final String primaryNodeType = childNode.getPrimaryNodeType().getName();

				if(ExcludeFolderAndSystemNodesFilter.SPLITTABLE_NODE_TYPES
						.contains(primaryNodeType)) {
					nodesToBackup.add( childNode.getPath() );
				}
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		} finally {
			ctx.release();
		}
		return nodesToBackup;
	}

	private void doBackup(String workspace, String nodePath, boolean isBackupJobRootPath, String backupFilename, Path destination) {

		if(logger.isDebugEnabled()) {
			logger.debug("Backing up node {} in workspace {} to {}", nodePath, workspace, destination);
		}

		try (
				final OutputStream out = new FileOutputStream(destination.toFile());
				final OutputStream decoratedOs = decorateOutputStream(out, backupFilename)
		) {
			final Session session = ctx.getJCRSession(workspace);
			final ContentHandler contentHandler = getContentHandler(decoratedOs);

			final ContentDecorator contentDecorator = isBackupJobRootPath ?
					new ExcludeFolderAndSystemNodesFilter() : new JcrExportCommand.DefaultFilter();

			final Node node = contentDecorator.wrapNode( session.getNode(nodePath) );

			final SystemViewExporter exporter = new SystemViewExporter(session, contentHandler, true, true);
			exporter.export(node);
		} catch (PathNotFoundException ex) {
			throw new IllegalArgumentException("Path " + nodePath + " was not found for export", ex);
		} catch (RepositoryException ex) {
			throw new IllegalStateException("A repository exception occurred", ex);
		} catch (FileNotFoundException ex) {
			throw new IllegalArgumentException("Cannot open file '" + destination.toString() + "' for writing during backup. ", ex);
		} catch (Exception ex) {
			throw new RuntimeException("An exception occurred during export", ex);
		} finally {
			ctx.release();
		}
	}

	private ContentHandler getContentHandler(OutputStream out) {
		final NamespaceFilter filter = new NamespaceFilter("sv", "xsi");
		filter.setContentHandler(XmlContentHandlerFactory.newXmlContentHandler(out));
		return filter;
	}

	private OutputStream decorateOutputStream(OutputStream os, String filename) throws IOException {
		switch (compression) {
			case ZIP:
				final ZipOutputStream zipOutputStream = new ZipOutputStream(os);
				zipOutputStream.putNextEntry(new ZipEntry(filename));
				return zipOutputStream;
			case GZ:
				return new GZIPOutputStream(os);
			default:
				return os;
		}
	}

	protected String createBackupFilename(String workspace, String nodePath) {
		final StringBuilder filename = new StringBuilder();
		filename.append( sanitizeFilename( workspace.toLowerCase() ) );

		if(! "/".equals(nodePath)) {
			String cleanedNodePath = nodePath
					.replaceAll("/{2,}", "/")
					.replace("/", ".");

			try {
				cleanedNodePath = URLEncoder.encode(cleanedNodePath, StandardCharsets.UTF_8.name());
			} catch(UnsupportedEncodingException e) {
				throw new IllegalStateException("Platform does not support encoding", e);
			}

			filename.append( sanitizeFilename( cleanedNodePath.toLowerCase() ) );
		}

		filename.append(".xml");
		return filename.toString();
	}

	protected String createBackupFilesystemFilename(String filename) {
		final String compressionExtension;
		if(this.compression == Compression.NONE) {
			compressionExtension = "";
		} else {
			compressionExtension = "." + this.compression.name().toLowerCase();
		}

		return filename + compressionExtension;
	}

	private Path createWorkspaceBackupDirectory(String workspace) {
		final Path workspacePath = exportBasePath.resolve( sanitizeFilename(workspace) );
		return SimpleBackupUtils.createDirectory(workspacePath);
	}

	private static Path validateAndBuildBackupPath(Path basePath) {
		if(!basePath.toFile().exists() ||
				!basePath.toFile().isDirectory() ||
				!Files.isWritable(basePath)) {
			logger.error("Cannot backup repository into invalid basePath {}", basePath);
			throw new IllegalArgumentException("Cannot backup repository into nonexistant or non-writable directory");
		}

		final String backupSubdirectory = dtFormatter.format(LocalDateTime.now());

		basePath = basePath.resolve(backupSubdirectory);
		return SimpleBackupUtils.createDirectory(basePath);
	}

	private String sanitizeFilename(String in) {
		return in.replaceAll("[\\\\/:*?\"<>|\\s]", "").toLowerCase();
	}

	private void log(String logMessage) {
		logger.info(logMessage);

		final String msg = String.format(
				"%s: %s", dtFormatter.format(LocalDateTime.now()), logMessage
				);

		backupDescriptor.log(msg, this.exportBasePath);
	}

	private String getTimeAsString(StopWatch w) {
		return w.getTime(TimeUnit.SECONDS) + " seconds";
	}
}
