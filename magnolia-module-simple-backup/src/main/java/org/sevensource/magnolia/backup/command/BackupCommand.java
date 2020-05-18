package org.sevensource.magnolia.backup.command;

import com.google.inject.Inject;
import com.google.inject.Provider;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.context.SystemContext;
import org.apache.commons.lang3.StringUtils;
import org.sevensource.magnolia.backup.SimpleBackupModule;
import org.sevensource.magnolia.backup.configuration.SimpleBackupJobConfiguration;
import org.sevensource.magnolia.backup.executor.backup.BackupExecutor;
import org.sevensource.magnolia.backup.support.SimpleBackupUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BackupCommand extends MgnlCommand {
	private static final Logger logger = LoggerFactory.getLogger(BackupCommand.class);

	private static final String PROP_BACKUP_CONFIGURATION = "configuration";
	private static final String PROP_BACKUP_SUBDIRECTORY = "backup-subdirectory";

	private static final DateTimeFormatter dtFormatter = new DateTimeFormatterBuilder()
			.appendPattern("yyyy")
			.appendLiteral("-")
			.appendPattern("MM")
			.appendLiteral("-")
			.appendPattern("dd")
			.appendLiteral("T")
			.appendPattern("HHmmss")
			.toFormatter();

    private final Provider<SimpleBackupModule> moduleConfigurationProvider;
    private final ServerConfiguration serverConfiguration;
    private final SystemContext systemCtx;

    @Inject
	public BackupCommand(
			Provider<SimpleBackupModule> moduleConfigurationProvider,
			ServerConfiguration serverConfiguration,
			SystemContext ctx) {

    	this.moduleConfigurationProvider = moduleConfigurationProvider;
    	this.serverConfiguration = serverConfiguration;
    	this.systemCtx = ctx;
	}

	@Override
	public boolean execute(Context context) {

		final String configAttribute = context.getAttribute(PROP_BACKUP_CONFIGURATION);
		if(StringUtils.isEmpty(configAttribute)) {
			throw new IllegalArgumentException("No attribute named " + PROP_BACKUP_CONFIGURATION + " found in requestContext");
		}

		final SimpleBackupJobConfiguration configuration = getBackupConfiguration(configAttribute);

		String backupSubdirectory = context.getAttribute(PROP_BACKUP_SUBDIRECTORY);
		if(StringUtils.isEmpty(backupSubdirectory)) {
			backupSubdirectory = dtFormatter.format(LocalDateTime.now());
		}

		final Path backupPath = buildBackupPath(configuration.getBackupPath(), backupSubdirectory);
		SimpleBackupUtils.createDirectory(backupPath);

		final BackupExecutor executor = new BackupExecutor(
				configuration.getWorkspaces(),
				backupPath,
				systemCtx);

		executor.run();
		return true;
	}


    private SimpleBackupJobConfiguration getBackupConfiguration(String requestedConfiguration) {
    	final SimpleBackupModule moduleConfiguration = moduleConfigurationProvider.get();

		final SimpleBackupJobConfiguration configuration = moduleConfiguration.getConfigurations()
				.stream()
				.filter(c -> StringUtils.equals(requestedConfiguration, c.getName()))
				.findFirst()
				.orElse(null);

		if(configuration == null) {
			throw new IllegalArgumentException("Cannot find configuration with name " + requestedConfiguration);
		}

		return configuration;
    }

    protected Path buildBackupPath(String basePath, String subdirectory) {
    	Path path = Paths.get(basePath);
    	if(! path.toFile().exists()) {
    		throw new IllegalArgumentException("Backup directory '" + basePath + "' does not exist");
    	}

		final String instanceId = serverConfiguration.isAdmin() ? "author" : "public";

		path = path.resolve(instanceId);
		if(! path.toFile().exists()) {
			try {
				Files.createDirectory(path);
			} catch(Exception e) {
				logger.error("Cannot create directory: ", e);
				throw new IllegalArgumentException(e);
			}
		}

		if(StringUtils.isBlank(subdirectory)) {
			throw new IllegalArgumentException("Subdirectory must not be empty");
		}

		final Matcher matcher = Pattern.compile(BackupExecutor.VALID_FILENAME_PATTERN).matcher(subdirectory);
		if(matcher.find()) {
			throw new IllegalArgumentException("Subdirectory contains illegal characters");
		}

		path = path.resolve(subdirectory);
		if(path.toFile().exists()) {
			logger.error("Backup job directory '{}' already exists", path);
			throw new IllegalArgumentException("Backup job subdirectory already exists");
		}
		return path;
    }
}
